package com.kylecorry.trail_sense.tools.ai_assistant.infrastructure

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Debug
import android.util.Log
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.ToolProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

class AiInferenceSubsystem private constructor(private val context: Context) {

    private val modelManager = ModelManager(context)
    @Volatile private var engine: Engine? = null
    @Volatile private var conversation: Conversation? = null
    @Volatile private var initializedModelId: String? = null
    private val initMutex = Mutex()

    fun isModelAvailable(): Boolean = modelManager.isModelDownloaded(modelManager.selectedChatModel)

    fun supportsImages(): Boolean = modelManager.selectedChatModel.supportsImages

    val selectedModelId: String get() = modelManager.selectedChatModel.id

    fun isEngineInitialized(): Boolean {
        return engine != null && initializedModelId == modelManager.selectedChatModel.id
    }

    fun isEngineReady(): Boolean {
        return isEngineInitialized() && conversation != null
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isEngineInitialized()) {
            Log.d(TAG, "initialize: engine already loaded for model=${modelManager.selectedChatModel.id}, skipping")
            return@withContext
        }

        initMutex.withLock {
            if (isEngineInitialized()) {
                Log.d(TAG, "initialize: engine was loaded while waiting for lock, skipping")
                return@withLock
            }

            val model = modelManager.selectedChatModel
            logDeviceInfo()

            Log.i(TAG, "initialize: ===== START MODEL INITIALIZATION =====")
            Log.i(TAG, "initialize: model.id=${model.id}, model.fileName=${model.fileName}, model.expectedSize=${model.sizeBytes}")
            Log.i(TAG, "initialize: model.supportsImages=${model.supportsImages}, model.type=${model.type}")

            val modelPath = modelManager.getModelPath(model)
            if (modelPath == null) {
                Log.e(TAG, "initialize: FAILED - getModelPath returned null. Model not downloaded or corrupted!")
                Log.e(TAG, "initialize: Checking model directory exists: ${File(context.filesDir, ModelManager.MODEL_DIR).exists()}")
                val modelFile = File(File(context.filesDir, ModelManager.MODEL_DIR), model.fileName)
                Log.e(TAG, "initialize: Expected model file: ${modelFile.absolutePath}")
                Log.e(TAG, "initialize: Model file exists: ${modelFile.exists()}, canRead: ${modelFile.canRead()}")
                if (modelFile.exists()) {
                    Log.e(TAG, "initialize: Model file actual size: ${modelFile.length()} bytes (expected: ${model.sizeBytes})")
                }
                throw IllegalStateException("Model not downloaded")
            }

            val modelFile = File(modelPath)
            Log.i(TAG, "initialize: modelPath=$modelPath")
            Log.i(TAG, "initialize: modelFile.exists=${modelFile.exists()}, canRead=${modelFile.canRead()}, isFile=${modelFile.isFile}")
            Log.i(TAG, "initialize: modelFile.actualSize=${modelFile.length()} bytes, expectedSize=${model.sizeBytes} bytes")

            val sizeDiff = kotlin.math.abs(modelFile.length() - model.sizeBytes)
            if (sizeDiff > 10 * 1024 * 1024) {
                Log.e(TAG, "initialize: CRITICAL - Model file size differs by ${sizeDiff / 1024 / 1024}MB! This likely indicates a corrupted download.")
            } else if (modelFile.length() != model.sizeBytes) {
                Log.w(TAG, "initialize: Minor model file size difference (${sizeDiff} bytes) - likely due to model update, continuing.")
            }

            logTfliteHeader(modelFile)

            cleanupStaleCaches(modelFile)

            val initStart = System.currentTimeMillis()

            if (engine != null || conversation != null) {
                Log.d(TAG, "initialize: cleaning up previous engine (oldModelId=$initializedModelId)")
            }
            cleanupLocked()

            var gpuSucceeded = false
            var cpuSucceeded = false
            var lastError: Throwable? = null

            var gpuEngine: Engine? = null
            var cpuEngine: Engine? = null

            val cacheDir = File(context.cacheDir, "litertlm").apply { mkdirs() }
            Log.d(TAG, "initialize: cacheDir=${cacheDir.absolutePath}, exists=${cacheDir.exists()}, writable=${cacheDir.canWrite()}")

            val cpuBackend = Backend.CPU(Runtime.getRuntime().availableProcessors().coerceAtMost(4))

            if (model.supportsImages) {
                try {
                    val gpuStart = System.currentTimeMillis()
                    Log.d(TAG, "initialize: attempting GPU backend (multimodal model)...")
                    val gpuConfig = createEngineConfig(modelPath, Backend.GPU(), model.supportsImages, cacheDir.absolutePath)
                    Log.d(TAG, "initialize: Engine config created (backend=GPU, visionBackend=GPU)")
                    Log.d(TAG, "initialize: instantiating GPU Engine object...")
                    gpuEngine = Engine(gpuConfig)
                    Log.d(TAG, "initialize: GPU Engine object created, calling initialize()...")
                    gpuEngine.initialize()
                    Log.d(TAG, "initialize: GPU engine.initialize() completed successfully")
                    engine = gpuEngine
                    gpuEngine = null
                    initializedModelId = model.id
                    gpuSucceeded = true
                    Log.i(TAG, "initialize: GPU backend loaded in ${System.currentTimeMillis() - gpuStart}ms (total ${System.currentTimeMillis() - initStart}ms)")
                } catch (e: CancellationException) {
                    Log.w(TAG, "initialize: GPU initialization cancelled")
                    gpuEngine?.closeSafely()
                    gpuEngine = null
                    cleanupLocked()
                    throw e
                } catch (e: Throwable) {
                    lastError = e
                    Log.e(TAG, "initialize: GPU backend FAILED!", e)
                    Log.e(TAG, "initialize: GPU error type: ${e::class.qualifiedName}")
                    Log.e(TAG, "initialize: GPU error message: ${e.message}")
                    Log.e(TAG, "initialize: GPU error cause: ${e.cause}")

                    gpuEngine?.closeSafely()
                    gpuEngine = null
                    Log.d(TAG, "initialize: GPU engine resources released after failure")

                    System.gc()
                    System.runFinalization()
                    Log.d(TAG, "initialize: GC triggered after GPU failure, waiting 300ms for native cleanup...")
                    Thread.sleep(300)
                }
            }

            if (!gpuSucceeded) {
                Log.w(TAG, "initialize: attempting CPU backend${if (model.supportsImages) " (fallback from GPU)" else ""}...")
                try {
                    val cpuStart = System.currentTimeMillis()
                    val cpuConfig = createEngineConfig(modelPath, cpuBackend, model.supportsImages, cacheDir.absolutePath)
                    Log.d(TAG, "initialize: Engine config created (backend=CPU, threads=${cpuBackend.numOfThreads})")
                    Log.d(TAG, "initialize: instantiating CPU Engine object...")
                    cpuEngine = Engine(cpuConfig)
                    Log.d(TAG, "initialize: CPU Engine object created, calling initialize()...")
                    cpuEngine.initialize()
                    Log.d(TAG, "initialize: CPU engine.initialize() completed successfully")
                    engine = cpuEngine
                    cpuEngine = null
                    initializedModelId = model.id
                    cpuSucceeded = true
                    Log.i(TAG, "initialize: CPU backend loaded in ${System.currentTimeMillis() - cpuStart}ms (total ${System.currentTimeMillis() - initStart}ms)")
                } catch (ce: CancellationException) {
                    Log.w(TAG, "initialize: CPU initialization cancelled")
                    cpuEngine?.closeSafely()
                    cpuEngine = null
                    cleanupLocked()
                    throw ce
                } catch (e2: Throwable) {
                    Log.e(TAG, "initialize: CPU backend FAILED!", e2)
                    Log.e(TAG, "initialize: CPU error type: ${e2::class.qualifiedName}")
                    Log.e(TAG, "initialize: CPU error message: ${e2.message}")
                    Log.e(TAG, "initialize: CPU error cause: ${e2.cause}")
                    cpuEngine?.closeSafely()
                    cpuEngine = null
                    cleanupLocked()
                    lastError = e2
                }
            }

            if (!gpuSucceeded && !cpuSucceeded) {
                Log.e(TAG, "initialize: BOTH GPU AND CPU BACKENDS FAILED - initialization cannot proceed")
                throw lastError ?: RuntimeException("Failed to initialize AI engine on both GPU and CPU")
            }

            Log.i(TAG, "initialize: ===== MODEL INITIALIZATION COMPLETE (gpu=$gpuSucceeded, cpu=$cpuSucceeded) =====")
        }
    }

    private fun cleanupLocked() {
        Log.d(TAG, "cleanupLocked: releasing engine and conversation (modelId=$initializedModelId)")
        try { conversation?.close() } catch (_: Exception) {}
        try { engine?.close() } catch (_: Exception) {}
        conversation = null
        engine = null
        initializedModelId = null
    }

    private fun Engine?.closeSafely() {
        try { this?.close() } catch (_: Exception) {}
    }

    private fun logDeviceInfo() {
        try {
            Log.d(TAG, "Device info: manufacturer=${Build.MANUFACTURER}, model=${Build.MODEL}, sdk=${Build.VERSION.SDK_INT}, release=${Build.VERSION.RELEASE}")
            Log.d(TAG, "Device supported ABIs: ${Build.SUPPORTED_ABIS.joinToString(",")}")
            val maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024
            val totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024
            val freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024
            Log.d(TAG, "Memory state: max=${maxMemory}MB, total=${totalMemory}MB, free=${freeMemory}MB")
            try {
                val nativeHeapSize = Debug.getNativeHeapAllocatedSize() / 1024 / 1024
                Log.d(TAG, "Native heap allocated: ${nativeHeapSize}MB")
            } catch (_: Exception) {}
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log device info", e)
        }
    }

    private fun logTfliteHeader(modelFile: File) {
        try {
            modelFile.inputStream().use { fis ->
                val header = ByteArray(16)
                val read = fis.read(header)
                if (read == 16) {
                    val headerHex = header.joinToString(" ") { "%02X".format(it) }
                    val headerStr = String(header, Charsets.US_ASCII).replace(Regex("[^\\x20-\\x7E]"), ".")
                    Log.d(TAG, "Model file header (first 16 bytes): $headerHex")
                    Log.d(TAG, "Model file header (ASCII): $headerStr")
                }

                val tfliteOffsets = listOf(
                    2686976L,
                    2621440L,
                    2752512L
                )
                for (offset in tfliteOffsets) {
                    if (offset >= modelFile.length()) continue
                    val buf = ByteArray(8)
                    val rfi = modelFile.inputStream().channel.use { ch ->
                        ch.position(offset)
                        val bb = java.nio.ByteBuffer.allocate(8)
                        ch.read(bb)
                        bb.flip()
                        ByteArray(8).also { bb.get(it) }
                    }
                    val b0 = rfi[0].toInt() and 0xFF
                    val b4 = rfi[4].toInt() and 0xFF
                    val b5 = rfi[5].toInt() and 0xFF
                    val b6 = rfi[6].toInt() and 0xFF
                    val b7 = rfi[7].toInt() and 0xFF
                    val fileId = String(byteArrayOf(rfi[4], rfi[5], rfi[6], rfi[7]), Charsets.US_ASCII)
                    val rootTableOffset = (b0) or ((rfi[1].toInt() and 0xFF) shl 8) or ((rfi[2].toInt() and 0xFF) shl 16) or ((rfi[3].toInt() and 0xFF) shl 24)
                    Log.d(TAG, "Bytes at offset $offset: ${rfi.joinToString(" ") { "%02X".format(it) }}, fileId='$fileId', rootTableOffset=$rootTableOffset")
                    if (fileId == "TFL3") {
                        Log.i(TAG, "Found valid TFL3 identifier at offset ${offset + 4} (rootTableOffset=$rootTableOffset)")
                        break
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to log TFLite header", e)
        }
    }

    private fun cleanupStaleCaches(modelFile: File) {
        try {
            val modelDir = modelFile.parentFile ?: return
            val modelName = modelFile.name
            val cachePrefixes = listOf(
                "${modelName}.xnnpack_cache_",
                "${modelName}_",
            )
            var deletedCount = 0
            modelDir.listFiles()?.forEach { file ->
                for (prefix in cachePrefixes) {
                    if (file.name.startsWith(prefix)) {
                        if (file.delete()) {
                            deletedCount++
                            Log.d(TAG, "Deleted stale cache: ${file.name}")
                        }
                        break
                    }
                }
            }
            if (deletedCount > 0) {
                Log.i(TAG, "Cleaned up $deletedCount stale cache file(s) for $modelName")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clean stale caches", e)
        }
    }

    suspend fun createConversation(
        systemInstruction: Contents? = null,
        tools: List<ToolProvider> = emptyList()
    ) = withContext(Dispatchers.IO) {
        Log.i(TAG, "createConversation: ===== START CONVERSATION CREATION =====")

        initMutex.withLock {
            val hadConversation = conversation != null
            Log.d(TAG, "createConversation: hadPreviousConversation=$hadConversation, toolsCount=${tools.size}, hasSystemInstruction=${systemInstruction != null}")

            if (!isEngineInitialized()) {
                Log.w(TAG, "createConversation: engine not loaded inside lock - this shouldn't happen, but attempting initialize")
                initialize()
            }

            val eng = engine
            if (eng == null) {
                Log.e(TAG, "createConversation: FAILED - engine is null!")
                throw IllegalStateException("Engine not initialized")
            }
            Log.d(TAG, "createConversation: engine is ready, initializedModelId=$initializedModelId")

            if (hadConversation) {
                Log.d(TAG, "createConversation: closing previous conversation")
            }
            try {
                conversation?.close()
            } catch (e: Exception) {
                Log.w(TAG, "createConversation: error closing previous conversation", e)
            }

            try {
                Log.d(TAG, "createConversation: building ConversationConfig (topK=$DEFAULT_TOP_K, topP=$DEFAULT_TOP_P, temp=$DEFAULT_TEMPERATURE)")
                val config = ConversationConfig(
                    samplerConfig = SamplerConfig(
                        topK = DEFAULT_TOP_K,
                        topP = DEFAULT_TOP_P,
                        temperature = DEFAULT_TEMPERATURE
                    ),
                    systemInstruction = systemInstruction,
                    tools = tools
                )
                Log.d(TAG, "createConversation: calling eng.createConversation()...")
                conversation = eng.createConversation(config)
                Log.i(TAG, "createConversation: conversation created successfully!")
            } catch (e: CancellationException) {
                Log.w(TAG, "createConversation: cancelled")
                throw e
            } catch (e: Throwable) {
                Log.e(TAG, "createConversation: FAILED to create conversation!", e)
                Log.e(TAG, "createConversation: error type: ${e::class.qualifiedName}")
                Log.e(TAG, "createConversation: error message: ${e.message}")
                Log.e(TAG, "createConversation: error cause: ${e.cause}")
                throw e
            }
            Log.i(TAG, "createConversation: ===== CONVERSATION CREATION COMPLETE =====")
        }
    }

    suspend fun sendMessage(
        input: String,
        images: List<Bitmap> = emptyList(),
        callback: MessageCallback
    ) = withContext(Dispatchers.Default) {
        val conv = conversation ?: throw IllegalStateException("Conversation not created")
        val contents = mutableListOf<Content>()
        val supportedImages = if (supportsImages()) images else emptyList()
        for (image in supportedImages) {
            val stream = ByteArrayOutputStream()
            val resized = resizeImageForModel(image)
            resized.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, stream)
            contents.add(Content.ImageBytes(stream.toByteArray()))
        }
        if (input.isNotBlank()) {
            contents.add(Content.Text(input))
        }
        conv.sendMessageAsync(Contents.of(contents), callback, emptyMap())
    }

    private fun createEngineConfig(
        modelPath: String,
        backend: Backend,
        supportsImages: Boolean,
        cacheDirPath: String? = null
    ): EngineConfig {
        return if (supportsImages) {
            EngineConfig(
                modelPath = modelPath,
                backend = backend,
                visionBackend = backend,
                maxNumTokens = MAX_TOKENS,
                cacheDir = cacheDirPath
            )
        } else {
            EngineConfig(
                modelPath = modelPath,
                backend = backend,
                maxNumTokens = MAX_TOKENS,
                cacheDir = cacheDirPath
            )
        }
    }

    private fun resizeImageForModel(image: Bitmap): Bitmap {
        val largestSide = image.width.coerceAtLeast(image.height)
        if (largestSide <= MAX_IMAGE_SIZE) {
            return image
        }

        val scale = MAX_IMAGE_SIZE.toFloat() / largestSide.toFloat()
        return Bitmap.createScaledBitmap(
            image,
            (image.width * scale).roundToInt().coerceAtLeast(1),
            (image.height * scale).roundToInt().coerceAtLeast(1),
            true
        )
    }

    fun stopResponse() {
        conversation?.cancelProcess()
    }

    fun cleanup() {
        Log.d(TAG, "cleanup: releasing engine and conversation (modelId=$initializedModelId)")
        try { conversation?.close() } catch (_: Exception) {}
        try { engine?.close() } catch (_: Exception) {}
        conversation = null
        engine = null
        initializedModelId = null
    }

    companion object {
        private const val TAG = "AiInferenceSubsystem"
        private const val MAX_TOKENS = 4096
        private const val MAX_IMAGE_SIZE = 1024
        private const val IMAGE_QUALITY = 85
        private const val DEFAULT_TOP_K = 64
        private const val DEFAULT_TOP_P = 0.95
        private const val DEFAULT_TEMPERATURE = 1.0

        @SuppressLint("StaticFieldLeak")
        private var instance: AiInferenceSubsystem? = null

        @Synchronized
        fun getInstance(context: Context): AiInferenceSubsystem {
            if (instance == null) {
                instance = AiInferenceSubsystem(context.applicationContext)
            }
            return instance!!
        }
    }
}

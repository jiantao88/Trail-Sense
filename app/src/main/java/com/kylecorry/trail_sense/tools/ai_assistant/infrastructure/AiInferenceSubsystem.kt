package com.kylecorry.trail_sense.tools.ai_assistant.infrastructure

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

class AiInferenceSubsystem private constructor(private val context: Context) {

    private val modelManager = ModelManager(context)
    private var engine: Engine? = null
    private var conversation: Conversation? = null
    private var initializedModelId: String? = null
    private val initMutex = Mutex()

    fun isModelAvailable(): Boolean = modelManager.isModelDownloaded()

    fun supportsImages(): Boolean = modelManager.selectedModel.supportsImages

    val selectedModelId: String get() = modelManager.selectedModel.id

    /**
     * True when the engine has been loaded for the currently selected model.
     * Does NOT require a conversation to exist, so closing/rebuilding a
     * conversation does not imply the engine needs re-initialization.
     */
    fun isEngineInitialized(): Boolean {
        return engine != null && initializedModelId == modelManager.selectedModel.id
    }

    fun isEngineReady(): Boolean {
        return isEngineInitialized() && conversation != null
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        initMutex.withLock {
            val model = modelManager.selectedModel

            // Already initialized for this model — nothing to do.
            if (isEngineInitialized()) {
                Log.d(TAG, "initialize: engine already loaded for model=${model.id}, skipping")
                return@withLock
            }

            val modelPath = modelManager.getModelPath(model)
                ?: throw IllegalStateException("Model not downloaded")

            Log.i(TAG, "initialize: starting for model=${model.id}, path=$modelPath, supportsImages=${model.supportsImages}")
            val initStart = System.currentTimeMillis()

            // Release any previous engine before loading a new one, otherwise the
            // native model buffer stays mapped and the second load fails with
            // "Failed to load model from buffer".
            if (engine != null || conversation != null) {
                Log.d(TAG, "initialize: cleaning up previous engine (oldModelId=$initializedModelId)")
            }
            cleanup()

            try {
                val gpuStart = System.currentTimeMillis()
                Log.d(TAG, "initialize: attempting GPU backend")
                val gpuConfig = createEngineConfig(modelPath, Backend.GPU(), model.supportsImages)
                val gpuEngine = Engine(gpuConfig)
                gpuEngine.initialize()
                engine = gpuEngine
                initializedModelId = model.id
                Log.i(TAG, "initialize: GPU backend loaded in ${System.currentTimeMillis() - gpuStart}ms (total ${System.currentTimeMillis() - initStart}ms)")
            } catch (e: Exception) {
                Log.w(TAG, "initialize: GPU backend failed (${e::class.simpleName}: ${e.message}), falling back to CPU")
                val cpuStart = System.currentTimeMillis()
                val cpuConfig = createEngineConfig(modelPath, Backend.CPU(), model.supportsImages)
                val cpuEngine = Engine(cpuConfig)
                cpuEngine.initialize()
                engine = cpuEngine
                initializedModelId = model.id
                Log.i(TAG, "initialize: CPU backend loaded in ${System.currentTimeMillis() - cpuStart}ms (total ${System.currentTimeMillis() - initStart}ms)")
            }
        }
    }

    suspend fun createConversation(
        systemInstruction: Contents? = null,
        tools: List<ToolProvider> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val hadConversation = conversation != null
        // Only initialize the engine if it isn't loaded for the current model yet.
        // Do NOT use isEngineReady() here — it requires a conversation, which would
        // trigger a redundant (and crashing) re-initialization on every new chat.
        if (!isEngineInitialized()) {
            Log.d(TAG, "createConversation: engine not loaded, triggering initialize()")
            initialize()
        }
        val eng = engine ?: throw IllegalStateException("Engine not initialized")
        if (hadConversation) {
            Log.d(TAG, "createConversation: closing previous conversation")
        }
        conversation?.close()
        conversation = eng.createConversation(
            ConversationConfig(
                samplerConfig = SamplerConfig(
                    topK = DEFAULT_TOP_K,
                    topP = DEFAULT_TOP_P,
                    temperature = DEFAULT_TEMPERATURE
                ),
                systemInstruction = systemInstruction,
                tools = tools
            )
        )
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
        supportsImages: Boolean
    ): EngineConfig {
        return if (supportsImages) {
            EngineConfig(
                modelPath = modelPath,
                backend = backend,
                visionBackend = backend,
                maxNumTokens = MAX_TOKENS
            )
        } else {
            EngineConfig(
                modelPath = modelPath,
                backend = backend,
                maxNumTokens = MAX_TOKENS
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

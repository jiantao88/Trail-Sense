package com.kylecorry.trail_sense.tools.ai_assistant.infrastructure

import android.content.Context
import android.util.Log
import com.kylecorry.trail_sense.shared.preferences.PreferencesSubsystem
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class AiModel(
    val id: String,
    val displayName: String,
    val fileName: String,
    val sizeBytes: Long,
    val downloadUrls: List<String>,
    val supportsImages: Boolean = false,
    val type: ModelType = ModelType.CHAT,
    val description: String = ""
)

enum class ModelType {
    CHAT,
    RERANKER
}

class ModelManager(
    private val modelDir: File,
    private val getSelectedChatModelId: () -> String = { DEFAULT_CHAT_MODEL_ID },
    private val setSelectedChatModelId: (String) -> Unit = {},
    private val getSelectedRerankerModelId: () -> String? = { null },
    private val setSelectedRerankerModelId: (String?) -> Unit = {}
) {

    constructor(context: Context) : this(
        File(context.filesDir, MODEL_DIR),
        getSelectedChatModelId = {
            PreferencesSubsystem.getInstance(context).preferences.getString(PREF_SELECTED_CHAT_MODEL_ID)
                ?: DEFAULT_CHAT_MODEL_ID
        },
        setSelectedChatModelId = {
            PreferencesSubsystem.getInstance(context).preferences.putString(
                PREF_SELECTED_CHAT_MODEL_ID,
                it
            )
        },
        getSelectedRerankerModelId = {
            PreferencesSubsystem.getInstance(context).preferences.getString(PREF_SELECTED_RERANKER_MODEL_ID)
        },
        setSelectedRerankerModelId = { id ->
            if (id == null) {
                PreferencesSubsystem.getInstance(context).preferences.remove(PREF_SELECTED_RERANKER_MODEL_ID)
            } else {
                PreferencesSubsystem.getInstance(context).preferences.putString(
                    PREF_SELECTED_RERANKER_MODEL_ID,
                    id
                )
            }
        }
    )

    val chatModels: List<AiModel>
        get() = ALL_MODELS.filter { it.type == ModelType.CHAT }

    val rerankerModels: List<AiModel>
        get() = ALL_MODELS.filter { it.type == ModelType.RERANKER }

    var selectedChatModel: AiModel
        get() = getChatModel(getSelectedChatModelId())
        set(value) = setSelectedChatModelId(value.id)

    var selectedRerankerModel: AiModel?
        get() = getSelectedRerankerModelId()?.let { getModel(it) }
        set(value) = setSelectedRerankerModelId(value?.id)

    fun getModel(id: String): AiModel {
        return ALL_MODELS.firstOrNull { it.id == id } ?: DEFAULT_CHAT_MODEL
    }

    fun getChatModel(id: String): AiModel {
        return chatModels.firstOrNull { it.id == id } ?: DEFAULT_CHAT_MODEL
    }

    fun getRerankerModel(id: String): AiModel? {
        return rerankerModels.firstOrNull { it.id == id }
    }

    fun isModelDownloaded(model: AiModel): Boolean {
        val file = getModelFile(model)
        val exists = file.exists()
        Log.d(TAG, "isModelDownloaded: model=${model.id}, file=${file.absolutePath}, exists=$exists, length=${if (exists) file.length() else -1}, expectedSize=${model.sizeBytes}")
        if (exists) {
            val tempFile = File(modelDir, "${model.fileName}.tmp")
            if (tempFile.exists()) {
                Log.w(TAG, "isModelDownloaded: TEMP file also exists (${tempFile.length()} bytes) - deleting stale temp file")
                tempFile.delete()
            }
            if (file.length() != model.sizeBytes) {
                Log.w(TAG, "isModelDownloaded: SIZE MISMATCH! actual=${file.length()}, expected=${model.sizeBytes}")
                if (kotlin.math.abs(file.length() - model.sizeBytes) > 10 * 1024 * 1024) {
                    Log.e(TAG, "isModelDownloaded: CRITICAL size mismatch (>10MB), deleting corrupted file")
                    file.delete()
                    return false
                }
            }
            if (model.type == ModelType.CHAT && !hasValidLitertlmHeader(file)) {
                Log.e(TAG, "isModelDownloaded: INVALID file header for ${model.fileName}, deleting corrupted file")
                file.delete()
                return false
            }
        } else {
            Log.e(TAG, "isModelDownloaded: model file DOES NOT EXIST: ${file.absolutePath}")
            Log.d(TAG, "isModelDownloaded: modelDir exists: ${modelDir.exists()}, modelDir list: ${modelDir.list()?.joinToString(",") ?: "empty or null"}")
        }
        return exists
    }

    private fun hasValidLitertlmHeader(file: File): Boolean {
        return try {
            file.inputStream().use { fis ->
                val magic = ByteArray(8)
                val read = fis.read(magic)
                if (read != 8) return false
                val magicStr = String(magic, Charsets.US_ASCII)
                val isValid = magicStr == "LITERTLM"
                if (!isValid) {
                    Log.e(TAG, "hasValidLitertlmHeader: invalid magic='$magicStr', expected='LITERTLM'")
                }
                isValid
            }
        } catch (e: Exception) {
            Log.e(TAG, "hasValidLitertlmHeader: failed to read header", e)
            false
        }
    }

    fun isRerankerDownloaded(): Boolean {
        return selectedRerankerModel?.let { isModelDownloaded(it) } ?: false
    }

    fun getModelPath(model: AiModel): String? {
        val file = getModelFile(model)
        val path = if (file.exists()) file.absolutePath else null
        Log.d(TAG, "getModelPath: model=${model.id}, path=$path, exists=${file.exists()}, canRead=${if (file.exists()) file.canRead() else "N/A"}")
        return path
    }

    fun getRerankerPath(): String? {
        return selectedRerankerModel?.let { getModelPath(it) }
    }

    fun getModelSizeOnDisk(model: AiModel): Long {
        val file = getModelFile(model)
        return if (file.exists()) file.length() else 0L
    }

    suspend fun downloadModel(model: AiModel, onProgress: (Float) -> Unit) {
        modelDir.mkdirs()
        val tempFile = File(modelDir, "${model.fileName}.tmp")
        val targetFile = getModelFile(model)
        val errors = mutableListOf<String>()

        if (targetFile.exists()) {
            Log.d(TAG, "downloadModel: deleting existing target file ${targetFile.name} before re-download")
            targetFile.delete()
        }

        for ((index, downloadUrl) in model.downloadUrls.withIndex()) {
            try {
                if (index > 0 && tempFile.exists()) {
                    Log.w(TAG, "downloadModel: retrying with different URL, deleting partial temp file (${tempFile.length()} bytes)")
                    tempFile.delete()
                }
                downloadFromUrl(downloadUrl, tempFile, targetFile, model.sizeBytes, onProgress)
                return
            } catch (e: Exception) {
                Log.e(TAG, "downloadModel: failed to download from $downloadUrl", e)
                errors += "${e.message ?: "unknown error"} ($downloadUrl)"
                tempFile.delete()
            }
        }

        tempFile.delete()
        throw IllegalStateException(errors.joinToString("\n"))
    }

    private fun downloadFromUrl(
        downloadUrl: String,
        tempFile: File,
        targetFile: File,
        totalExpectedBytes: Long,
        onProgress: (Float) -> Unit
    ) {
        val existingBytes = if (tempFile.exists()) tempFile.length() else 0L
        val connection = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            if (existingBytes > 0) {
                setRequestProperty("Range", "bytes=$existingBytes-")
            }
        }

        try {
            val responseCode = connection.responseCode
            Log.d(TAG, "downloadFromUrl: responseCode=$responseCode, url=$downloadUrl, existingBytes=$existingBytes")

            if (responseCode !in 200..299) {
                throw IllegalStateException("HTTP error $responseCode for $downloadUrl")
            }

            val totalBytes = when {
                responseCode == 206 -> existingBytes + connection.contentLengthLong
                connection.contentLengthLong > 0 -> connection.contentLengthLong
                else -> totalExpectedBytes
            }
            val append = responseCode == 206
            var downloadedBytes = if (append) existingBytes else 0L

            if (!append && existingBytes > 0) {
                Log.d(TAG, "downloadFromUrl: server returned full response, deleting partial temp file to restart")
                tempFile.delete()
                downloadedBytes = 0L
            }

            connection.inputStream.use { input: InputStream ->
                FileOutputStream(tempFile, append).use { output ->
                    val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            onProgress((downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f))
                        }
                    }
                }
            }

            val finalSize = tempFile.length()
            Log.d(TAG, "downloadFromUrl: download complete, tempFile size=$finalSize, expected=$totalExpectedBytes")

            if (finalSize != totalExpectedBytes) {
                tempFile.delete()
                throw IllegalStateException("Download size mismatch: got $finalSize bytes, expected $totalExpectedBytes")
            }

            if (targetFile.exists()) {
                targetFile.delete()
            }

            val renamed = tempFile.renameTo(targetFile)
            if (!renamed) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }

            if (targetFile.length() != totalExpectedBytes) {
                targetFile.delete()
                throw IllegalStateException("Target file size mismatch after rename: ${targetFile.length()} vs $totalExpectedBytes")
            }

            onProgress(1f)
            Log.i(TAG, "downloadFromUrl: successfully downloaded ${targetFile.name} (${targetFile.length()} bytes)")
        } catch (e: Exception) {
            tempFile.delete()
            throw e
        } finally {
            connection.disconnect()
        }
    }

    fun getDownloadProgress(model: AiModel): Float {
        val tempFile = File(modelDir, "${model.fileName}.tmp")
        if (!tempFile.exists()) return 0f
        return (tempFile.length().toFloat() / model.sizeBytes).coerceIn(0f, 1f)
    }

    fun deleteModel(model: AiModel) {
        getModelFile(model).delete()
        File(modelDir, "${model.fileName}.tmp").delete()
    }

    private fun getModelFile(model: AiModel): File {
        return File(modelDir, model.fileName)
    }

    companion object {
        private const val TAG = "ModelManager"
        const val MODEL_DIR = "ai_models"
        const val DEFAULT_CHAT_MODEL_ID = "qwen3-0.6b"
        const val PREF_SELECTED_CHAT_MODEL_ID = "pref_ai_selected_chat_model_id"
        const val PREF_SELECTED_RERANKER_MODEL_ID = "pref_ai_selected_reranker_model_id"

        private const val CONNECT_TIMEOUT_MS = 30_000
        private const val READ_TIMEOUT_MS = 30_000
        private const val DOWNLOAD_BUFFER_SIZE = 8192

        val ALL_MODELS = listOf(
            // Chat models
            AiModel(
                id = DEFAULT_CHAT_MODEL_ID,
                displayName = "Qwen3-0.6B",
                fileName = "Qwen3-0.6B.litertlm",
                sizeBytes = 614_236_160L,
                downloadUrls = modelSources(
                    "litert-community/Qwen3-0.6B",
                    "Qwen3-0.6B.litertlm"
                ),
                type = ModelType.CHAT,
                description = "Fast, lightweight model suitable for most questions. Best for devices with limited storage."
            ),
            AiModel(
                id = "qwen2.5-1.5b-instruct",
                displayName = "Qwen2.5-1.5B-Instruct",
                fileName = "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm",
                sizeBytes = 1_597_931_520L,
                downloadUrls = modelSources(
                    "litert-community/Qwen2.5-1.5B-Instruct",
                    "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm"
                ),
                type = ModelType.CHAT,
                description = "Better reasoning and more detailed responses. Good balance of speed and quality."
            ),
            AiModel(
                id = "gemma-4-e2b-it",
                displayName = "Gemma-4-E2B-it",
                fileName = "gemma-4-E2B-it.litertlm",
                sizeBytes = 2_583_085_056L,
                downloadUrls = modelSources(
                    "litert-community/gemma-4-E2B-it-litert-lm",
                    "gemma-4-E2B-it.litertlm"
                ),
                supportsImages = true,
                type = ModelType.CHAT,
                description = "Multimodal model that can analyze images. Higher quality responses."
            ),
            AiModel(
                id = "gemma-4-e4b-it",
                displayName = "Gemma-4-E4B-it",
                fileName = "gemma-4-E4B-it.litertlm",
                sizeBytes = 3_654_467_584L,
                downloadUrls = modelSources(
                    "litert-community/gemma-4-E4B-it-litert-lm",
                    "gemma-4-E4B-it.litertlm"
                ),
                supportsImages = true,
                type = ModelType.CHAT,
                description = "Highest quality multimodal model. Best responses but requires more storage and RAM."
            ),

            // Reranker models - Cross-Encoder for semantic search
            AiModel(
                id = "bge-reranker-v2-m3-mini",
                displayName = "BGE-Reranker-v2-M3-Mini (Recommended)",
                fileName = "bge-reranker-v2-m3-mini.tflite",
                sizeBytes = 87_031_808L,
                downloadUrls = modelSources(
                    "trail-sense-models/reranker",
                    "bge-reranker-v2-m3-mini.tflite"
                ),
                type = ModelType.RERANKER,
                description = "Lightweight semantic reranker (~87MB). Dramatically improves tool/knowledge matching accuracy. Download when on WiFi for best offline experience."
            ),
            AiModel(
                id = "bge-reranker-v2-m3",
                displayName = "BGE-Reranker-v2-M3 (Full)",
                fileName = "bge-reranker-v2-m3.tflite",
                sizeBytes = 568_328_208L,
                downloadUrls = modelSources(
                    "trail-sense-models/reranker",
                    "bge-reranker-v2-m3.tflite"
                ),
                type = ModelType.RERANKER,
                description = "Full-size reranker (~568MB). Best matching accuracy but larger download."
            )
        )

        val DEFAULT_CHAT_MODEL = ALL_MODELS.first { it.id == DEFAULT_CHAT_MODEL_ID }

        private fun modelSources(repo: String, fileName: String): List<String> {
            return listOf(
                "https://hf-mirror.com/$repo/resolve/main/$fileName",
                "https://huggingface.co/$repo/resolve/main/$fileName"
            )
        }
    }
}

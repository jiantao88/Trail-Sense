package com.kylecorry.trail_sense.tools.ai_assistant.infrastructure

import android.content.Context
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
    val description: String = ""
)

class ModelManager(
    private val modelDir: File,
    private val getSelectedChatModelId: () -> String = { DEFAULT_CHAT_MODEL_ID },
    private val setSelectedChatModelId: (String) -> Unit = {},
    private val getSemanticRerankerEnabled: () -> Boolean = { DEFAULT_SEMANTIC_RERANKER_ENABLED },
    private val setSemanticRerankerEnabled: (Boolean) -> Unit = {}
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
        getSemanticRerankerEnabled = {
            PreferencesSubsystem.getInstance(context).preferences.getBoolean(PREF_SEMANTIC_RERANKER_ENABLED)
                ?: DEFAULT_SEMANTIC_RERANKER_ENABLED
        },
        setSemanticRerankerEnabled = { enabled ->
            PreferencesSubsystem.getInstance(context).preferences.putBoolean(
                PREF_SEMANTIC_RERANKER_ENABLED,
                enabled
            )
        }
    )

    val models: List<AiModel>
        get() = CHAT_MODELS

    var selectedModel: AiModel
        get() = getModel(getSelectedChatModelId())
        set(value) = setSelectedChatModelId(value.id)

    var isSemanticRerankerEnabled: Boolean
        get() = getSemanticRerankerEnabled()
        set(value) = setSemanticRerankerEnabled(value)

    fun getModel(id: String): AiModel {
        return CHAT_MODELS.firstOrNull { it.id == id } ?: DEFAULT_CHAT_MODEL
    }

    fun isModelDownloaded(model: AiModel = selectedModel): Boolean {
        return getModelFile(model).exists()
    }

    fun isSemanticRerankerAvailable(): Boolean {
        return isSemanticRerankerEnabled
    }

    fun getModelPath(model: AiModel = selectedModel): String? {
        val file = getModelFile(model)
        return if (file.exists()) file.absolutePath else null
    }

    fun getModelSizeOnDisk(model: AiModel = selectedModel): Long {
        val file = getModelFile(model)
        return if (file.exists()) file.length() else 0L
    }

    suspend fun downloadModel(model: AiModel = selectedModel, onProgress: (Float) -> Unit) {
        modelDir.mkdirs()
        val tempFile = File(modelDir, "${model.fileName}.tmp")
        val targetFile = getModelFile(model)
        val errors = mutableListOf<String>()

        for (downloadUrl in model.downloadUrls) {
            try {
                downloadFromUrl(downloadUrl, tempFile, targetFile, model.sizeBytes, onProgress)
                return
            } catch (e: Exception) {
                errors += e.message ?: downloadUrl
            }
        }

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
            val totalBytes = when {
                responseCode == 206 -> existingBytes + connection.contentLengthLong
                connection.contentLengthLong > 0 -> connection.contentLengthLong
                else -> totalExpectedBytes
            }
            val append = responseCode == 206
            var downloadedBytes = if (append) existingBytes else 0L

            if (!append && existingBytes > 0) {
                tempFile.delete()
            }

            if (responseCode !in 200..299) {
                throw IllegalStateException("HTTP error $responseCode for $downloadUrl")
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
            tempFile.renameTo(targetFile)
            onProgress(1f)
        } catch (e: Exception) {
            throw e
        } finally {
            connection.disconnect()
        }
    }

    fun getDownloadProgress(model: AiModel = selectedModel): Float {
        val tempFile = File(modelDir, "${model.fileName}.tmp")
        if (!tempFile.exists()) return 0f
        return (tempFile.length().toFloat() / model.sizeBytes).coerceIn(0f, 1f)
    }

    fun deleteModel(model: AiModel = selectedModel) {
        getModelFile(model).delete()
        File(modelDir, "${model.fileName}.tmp").delete()
    }

    private fun getModelFile(model: AiModel): File {
        return File(modelDir, model.fileName)
    }

    companion object {
        const val MODEL_DIR = "ai_models"
        const val DEFAULT_CHAT_MODEL_ID = "qwen3-0.6b"
        const val PREF_SELECTED_CHAT_MODEL_ID = "pref_ai_selected_chat_model_id"
        const val PREF_SEMANTIC_RERANKER_ENABLED = "pref_ai_semantic_reranker_enabled"
        const val DEFAULT_SEMANTIC_RERANKER_ENABLED = true

        private const val CONNECT_TIMEOUT_MS = 30_000
        private const val READ_TIMEOUT_MS = 30_000
        private const val DOWNLOAD_BUFFER_SIZE = 8192

        val CHAT_MODELS = listOf(
            AiModel(
                id = DEFAULT_CHAT_MODEL_ID,
                displayName = "Qwen3-0.6B",
                fileName = "Qwen3-0.6B.litertlm",
                sizeBytes = 614_236_160L,
                downloadUrls = modelSources(
                    "litert-community/Qwen3-0.6B",
                    "Qwen3-0.6B.litertlm"
                ),
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
                description = "Highest quality multimodal model. Best responses but requires more storage and RAM."
            )
        )

        val DEFAULT_CHAT_MODEL = CHAT_MODELS.first { it.id == DEFAULT_CHAT_MODEL_ID }

        val DEFAULT_MODEL = DEFAULT_CHAT_MODEL
        const val DEFAULT_MODEL_ID = DEFAULT_CHAT_MODEL_ID
        val MODELS = CHAT_MODELS

        private fun modelSources(repo: String, fileName: String): List<String> {
            return listOf(
                "https://hf-mirror.com/$repo/resolve/main/$fileName",
                "https://huggingface.co/$repo/resolve/main/$fileName"
            )
        }
    }
}

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
    val supportsImages: Boolean = false
)

class ModelManager(
    private val modelDir: File,
    private val getSelectedModelId: () -> String = { DEFAULT_MODEL_ID },
    private val setSelectedModelId: (String) -> Unit = {}
) {

    constructor(context: Context) : this(
        File(context.filesDir, MODEL_DIR),
        getSelectedModelId = {
            PreferencesSubsystem.getInstance(context).preferences.getString(PREF_SELECTED_MODEL_ID)
                ?: DEFAULT_MODEL_ID
        },
        setSelectedModelId = {
            PreferencesSubsystem.getInstance(context).preferences.putString(
                PREF_SELECTED_MODEL_ID,
                it
            )
        }
    )

    val models: List<AiModel>
        get() = MODELS

    var selectedModel: AiModel
        get() = getModel(getSelectedModelId())
        set(value) = setSelectedModelId(value.id)

    fun getModel(id: String): AiModel {
        return MODELS.firstOrNull { it.id == id } ?: DEFAULT_MODEL
    }

    fun isModelDownloaded(model: AiModel = selectedModel): Boolean {
        return getModelFile(model).exists()
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
                downloadModel(downloadUrl, tempFile, targetFile, onProgress)
                return
            } catch (e: Exception) {
                errors += e.message ?: downloadUrl
            }
        }

        throw IllegalStateException(errors.joinToString("\n"))
    }

    private fun downloadModel(
        downloadUrl: String,
        tempFile: File,
        targetFile: File,
        onProgress: (Float) -> Unit
    ) {
        val existingBytes = if (tempFile.exists()) tempFile.length() else 0L
        val connection = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 30_000
            readTimeout = 30_000
            if (existingBytes > 0) {
                setRequestProperty("Range", "bytes=$existingBytes-")
            }
        }

        try {
            val responseCode = connection.responseCode
            val totalBytes = if (responseCode == 206) {
                existingBytes + connection.contentLengthLong
            } else {
                connection.contentLengthLong
            }
            val append = responseCode == 206
            var downloadedBytes = if (append) existingBytes else 0L

            if (!append && existingBytes > 0) {
                tempFile.delete()
            }

            connection.inputStream.use { input: InputStream ->
                FileOutputStream(tempFile, append).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            onProgress(downloadedBytes.toFloat() / totalBytes)
                        }
                    }
                }
            }
            tempFile.renameTo(targetFile)
        } catch (e: Exception) {
            // ponytail: keep the partial file so the next source or retry can resume.
            throw e
        } finally {
            connection.disconnect()
        }
    }

    fun getDownloadProgress(model: AiModel = selectedModel): Float {
        val tempFile = File(modelDir, "${model.fileName}.tmp")
        if (!tempFile.exists()) return 0f
        return tempFile.length().toFloat() / model.sizeBytes
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
        const val DEFAULT_MODEL_ID = "qwen3-0.6b"
        const val PREF_SELECTED_MODEL_ID = "pref_ai_selected_model_id"

        val MODELS = listOf(
            AiModel(
                id = DEFAULT_MODEL_ID,
                displayName = "Qwen3-0.6B",
                fileName = "Qwen3-0.6B.litertlm",
                sizeBytes = 614_236_160L,
                downloadUrls = modelSources(
                    "litert-community/Qwen3-0.6B",
                    "Qwen3-0.6B.litertlm"
                )
            ),
            AiModel(
                id = "qwen2.5-1.5b-instruct",
                displayName = "Qwen2.5-1.5B-Instruct",
                fileName = "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm",
                sizeBytes = 1_597_931_520L,
                downloadUrls = modelSources(
                    "litert-community/Qwen2.5-1.5B-Instruct",
                    "Qwen2.5-1.5B-Instruct_multi-prefill-seq_q8_ekv4096.litertlm"
                )
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
                supportsImages = true
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
                supportsImages = true
            )
        )

        val DEFAULT_MODEL = MODELS.first()

        const val MODEL_FILE_NAME = "Qwen3-0.6B.litertlm"
        const val MODEL_DISPLAY_NAME = "Qwen3-0.6B"
        const val MODEL_SIZE_BYTES = 614_236_160L
        const val MODEL_DOWNLOAD_URL =
            "https://hf-mirror.com/litert-community/Qwen3-0.6B/resolve/main/Qwen3-0.6B.litertlm"

        private fun modelSources(repo: String, fileName: String): List<String> {
            return listOf(
                "https://hf-mirror.com/$repo/resolve/main/$fileName",
                "https://huggingface.co/$repo/resolve/main/$fileName"
            )
        }
    }
}

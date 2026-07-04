package com.kylecorry.trail_sense.tools.ai_assistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.extensions.TrailSenseComposeFragment
import com.kylecorry.trail_sense.shared.extensions.compose.useState
import com.kylecorry.trail_sense.tools.ai_assistant.infrastructure.AiModel
import com.kylecorry.trail_sense.tools.ai_assistant.infrastructure.ModelManager
import com.kylecorry.trail_sense.tools.ai_assistant.infrastructure.ModelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AiSettingsFragment : TrailSenseComposeFragment() {

    @Composable
    override fun FragmentContent() {
        val modelManager = remember { ModelManager(requireContext()) }
        val chatModels = remember { modelManager.chatModels }
        val rerankerModels = remember { modelManager.rerankerModels }

        val (selectedChatModelId, setSelectedChatModelId) = useState(modelManager.selectedChatModel.id)
        val (selectedRerankerModelId, setSelectedRerankerModelId) = useState(modelManager.selectedRerankerModel?.id)
        val (downloadedModelIds, setDownloadedModelIds) = useState(
            getDownloadedModelIds(modelManager)
        )
        val (downloadingModelId, setDownloadingModelId) = useState<String?>(null)
        val (progressByModel, setProgressByModel) = useState(getDownloadProgress(modelManager))
        val (errorsByModel, setErrorsByModel) = useState<Map<String, String>>(emptyMap())
        val (showRerankerInfo, setShowRerankerInfo) = useState(false)
        val scope = rememberCoroutineScope()

        AiSettingsContent(
            chatModels = chatModels,
            rerankerModels = rerankerModels,
            selectedChatModelId = selectedChatModelId,
            selectedRerankerModelId = selectedRerankerModelId,
            downloadedModelIds = downloadedModelIds,
            downloadingModelId = downloadingModelId,
            progressByModel = progressByModel,
            errorsByModel = errorsByModel,
            showRerankerInfo = showRerankerInfo,
            onSelectChatModel = { model ->
                modelManager.selectedChatModel = model
                setSelectedChatModelId(model.id)
            },
            onSelectRerankerModel = { model ->
                modelManager.selectedRerankerModel = model
                setSelectedRerankerModelId(model?.id)
                refreshDownloadedState(modelManager, setDownloadedModelIds, setProgressByModel)
            },
            onDownload = { model ->
                scope.launch {
                    setDownloadingModelId(model.id)
                    setErrorsByModel(errorsByModel - model.id)
                    try {
                        withContext(Dispatchers.IO) {
                            modelManager.downloadModel(model) { progress ->
                                scope.launch {
                                    setProgressByModel(progressByModel + (model.id to progress.coerceIn(0f, 1f)))
                                }
                            }
                        }
                        refreshDownloadedState(modelManager, setDownloadedModelIds, setProgressByModel)
                    } catch (e: Exception) {
                        setErrorsByModel(
                            errorsByModel + (model.id to (e.message
                                ?: getString(R.string.ai_inference_error)))
                        )
                    }
                    setDownloadingModelId(null)
                }
            },
            onDelete = { model ->
                modelManager.deleteModel(model)
                refreshDownloadedState(modelManager, setDownloadedModelIds, setProgressByModel)

                if (model.type == ModelType.CHAT && selectedChatModelId == model.id) {
                    val nextModel = chatModels.firstOrNull { it.id in downloadedModelIds }
                        ?: ModelManager.DEFAULT_CHAT_MODEL
                    modelManager.selectedChatModel = nextModel
                    setSelectedChatModelId(nextModel.id)
                }

                if (model.type == ModelType.RERANKER && selectedRerankerModelId == model.id) {
                    modelManager.selectedRerankerModel = null
                    setSelectedRerankerModelId(null)
                }
            },
            onToggleRerankerInfo = { setShowRerankerInfo(!showRerankerInfo) }
        )
    }

    private fun refreshDownloadedState(
        modelManager: ModelManager,
        setDownloadedModelIds: (Set<String>) -> Unit,
        setProgressByModel: (Map<String, Float>) -> Unit
    ) {
        setDownloadedModelIds(getDownloadedModelIds(modelManager))
        setProgressByModel(getDownloadProgress(modelManager))
    }

    private fun getDownloadedModelIds(modelManager: ModelManager): Set<String> {
        return ModelManager.ALL_MODELS.filter { modelManager.isModelDownloaded(it) }
            .map { it.id }
            .toSet()
    }

    private fun getDownloadProgress(modelManager: ModelManager): Map<String, Float> {
        return ModelManager.ALL_MODELS.associate { it.id to modelManager.getDownloadProgress(it).coerceIn(0f, 1f) }
    }
}

@Composable
private fun AiSettingsContent(
    chatModels: List<AiModel>,
    rerankerModels: List<AiModel>,
    selectedChatModelId: String,
    selectedRerankerModelId: String?,
    downloadedModelIds: Set<String>,
    downloadingModelId: String?,
    progressByModel: Map<String, Float>,
    errorsByModel: Map<String, String>,
    showRerankerInfo: Boolean,
    onSelectChatModel: (AiModel) -> Unit,
    onSelectRerankerModel: (AiModel?) -> Unit,
    onDownload: (AiModel) -> Unit,
    onDelete: (AiModel) -> Unit,
    onToggleRerankerInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.ai_settings_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.testTag("ai_settings_title")
        )

        Text(
            text = stringResource(R.string.ai_about),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        Text(
            text = stringResource(R.string.ai_chat_models_section),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        chatModels.forEach { model ->
            AiModelCard(
                model = model,
                isSelected = model.id == selectedChatModelId,
                isDownloaded = model.id in downloadedModelIds,
                isDownloading = model.id == downloadingModelId,
                progress = progressByModel[model.id] ?: 0f,
                downloadEnabled = downloadingModelId == null || model.id == downloadingModelId,
                error = errorsByModel[model.id],
                isChatModel = true,
                onSelect = { onSelectChatModel(model) },
                onDownload = { onDownload(model) },
                onDelete = { onDelete(model) }
            )
        }

        HorizontalDivider()

        RerankerSection(
            rerankerModels = rerankerModels,
            selectedRerankerModelId = selectedRerankerModelId,
            downloadedModelIds = downloadedModelIds,
            downloadingModelId = downloadingModelId,
            progressByModel = progressByModel,
            errorsByModel = errorsByModel,
            showRerankerInfo = showRerankerInfo,
            onSelectRerankerModel = onSelectRerankerModel,
            onDownload = onDownload,
            onDelete = onDelete,
            onToggleRerankerInfo = onToggleRerankerInfo,
            downloadEnabled = downloadingModelId == null
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.ai_disclaimer),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RerankerSection(
    rerankerModels: List<AiModel>,
    selectedRerankerModelId: String?,
    downloadedModelIds: Set<String>,
    downloadingModelId: String?,
    progressByModel: Map<String, Float>,
    errorsByModel: Map<String, String>,
    showRerankerInfo: Boolean,
    onSelectRerankerModel: (AiModel?) -> Unit,
    onDownload: (AiModel) -> Unit,
    onDelete: (AiModel) -> Unit,
    onToggleRerankerInfo: () -> Unit,
    downloadEnabled: Boolean
) {
    val isAnyRerankerDownloaded = rerankerModels.any { it.id in downloadedModelIds }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.ai_reranker_models_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isAnyRerankerDownloaded) {
                        stringResource(R.string.ai_reranker_active)
                    } else {
                        stringResource(R.string.ai_no_reranker)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAnyRerankerDownloaded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            TextButton(onClick = onToggleRerankerInfo) {
                Text(
                    text = if (showRerankerInfo) "×" else "?",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (showRerankerInfo) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.ai_reranker_benefit_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.ai_reranker_benefit_detail),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (!isAnyRerankerDownloaded) {
            AssistChip(
                onClick = onToggleRerankerInfo,
                label = { Text(stringResource(R.string.ai_reranker_recommended)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        rerankerModels.forEach { model ->
            AiModelCard(
                model = model,
                isSelected = model.id == selectedRerankerModelId,
                isDownloaded = model.id in downloadedModelIds,
                isDownloading = model.id == downloadingModelId,
                progress = progressByModel[model.id] ?: 0f,
                downloadEnabled = downloadEnabled || model.id == downloadingModelId,
                error = errorsByModel[model.id],
                isChatModel = false,
                isNoSelectionOption = true,
                onSelect = {
                    if (model.id in downloadedModelIds) {
                        if (selectedRerankerModelId == model.id) {
                            onSelectRerankerModel(null)
                        } else {
                            onSelectRerankerModel(model)
                        }
                    }
                },
                onDownload = { onDownload(model) },
                onDelete = { onDelete(model) }
            )
        }
    }
}

@Composable
private fun AiModelCard(
    model: AiModel,
    isSelected: Boolean,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    progress: Float,
    downloadEnabled: Boolean,
    error: String?,
    isChatModel: Boolean,
    isNoSelectionOption: Boolean = false,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected && isDownloaded) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (isChatModel) {
                    RadioButton(
                        selected = isSelected,
                        onClick = if (isDownloaded) onSelect else null,
                        enabled = isDownloaded,
                        modifier = Modifier.testTag("model_selector")
                    )
                } else {
                    Checkbox(
                        checked = isSelected && isDownloaded,
                        onCheckedChange = { if (isDownloaded) onSelect() },
                        enabled = isDownloaded
                    )
                }
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = model.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatModelSize(model.sizeBytes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isChatModel) {
                            if (model.supportsImages) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(stringResource(R.string.ai_model_image_support), style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            val qualityLabel = when {
                                model.sizeBytes < 1_000_000_000L -> R.string.ai_model_fast
                                model.sizeBytes < 2_000_000_000L -> R.string.ai_model_balanced
                                else -> R.string.ai_model_high_quality
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(qualityLabel), style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    if (model.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = model.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected && isDownloaded) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.ai_selected_model),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isDownloading) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().testTag("download_progress")
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else if (isDownloaded) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.ai_model_downloaded),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.testTag("downloaded_label")
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isChatModel && !isSelected) {
                        Button(
                            onClick = onSelect,
                            modifier = Modifier.testTag("select_button")
                        ) {
                            Text(stringResource(R.string.ai_select_model))
                        }
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.testTag("delete_button")
                    ) {
                        Text(stringResource(R.string.ai_delete_model))
                    }
                }
            } else if (progress > 0f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDownload,
                    enabled = downloadEnabled,
                    modifier = Modifier.fillMaxWidth().testTag("download_button")
                ) {
                    Text(stringResource(R.string.ai_resume_download))
                }
            } else {
                Button(
                    onClick = onDownload,
                    enabled = downloadEnabled,
                    modifier = Modifier.fillMaxWidth().testTag("download_button")
                ) {
                    Text(stringResource(R.string.ai_download_model))
                }
            }

            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp).testTag("error_text")
                )
            }
        }
    }
}

private fun formatModelSize(sizeBytes: Long): String {
    val mb = sizeBytes.toDouble() / 1_000_000.0
    val gb = sizeBytes.toDouble() / 1_000_000_000.0
    return when {
        gb >= 1.0 -> String.format(Locale.getDefault(), "%.1f GB", gb)
        else -> String.format(Locale.getDefault(), "%.0f MB", mb)
    }
}

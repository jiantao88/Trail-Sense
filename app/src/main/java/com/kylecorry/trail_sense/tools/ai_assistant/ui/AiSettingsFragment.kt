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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.kylecorry.trail_sense.tools.ai_assistant.infrastructure.RerankerSubsystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AiSettingsFragment : TrailSenseComposeFragment() {

    @Composable
    override fun FragmentContent() {
        val modelManager = remember { ModelManager(requireContext()) }
        val rerankerSubsystem = remember { RerankerSubsystem.getInstance(requireContext()) }
        val chatModels = remember { modelManager.models }

        val (selectedModelId, setSelectedModelId) = useState(modelManager.selectedModel.id)
        val (semanticRerankerEnabled, setSemanticRerankerEnabled) = useState(
            rerankerSubsystem.isSemanticRerankerEnabled()
        )
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
            selectedModelId = selectedModelId,
            semanticRerankerEnabled = semanticRerankerEnabled,
            downloadedModelIds = downloadedModelIds,
            downloadingModelId = downloadingModelId,
            progressByModel = progressByModel,
            errorsByModel = errorsByModel,
            showRerankerInfo = showRerankerInfo,
            onSelectModel = { model ->
                modelManager.selectedModel = model
                setSelectedModelId(model.id)
            },
            onToggleSemanticReranker = { enabled ->
                rerankerSubsystem.setSemanticRerankerEnabled(enabled)
                setSemanticRerankerEnabled(enabled)
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

                if (selectedModelId == model.id) {
                    val nextModel = chatModels.firstOrNull { it.id in downloadedModelIds }
                        ?: ModelManager.DEFAULT_MODEL
                    modelManager.selectedModel = nextModel
                    setSelectedModelId(nextModel.id)
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
        return ModelManager.MODELS.filter { modelManager.isModelDownloaded(it) }
            .map { it.id }
            .toSet()
    }

    private fun getDownloadProgress(modelManager: ModelManager): Map<String, Float> {
        return ModelManager.MODELS.associate { it.id to modelManager.getDownloadProgress(it).coerceIn(0f, 1f) }
    }
}

@Composable
private fun AiSettingsContent(
    chatModels: List<AiModel>,
    selectedModelId: String,
    semanticRerankerEnabled: Boolean,
    downloadedModelIds: Set<String>,
    downloadingModelId: String?,
    progressByModel: Map<String, Float>,
    errorsByModel: Map<String, String>,
    showRerankerInfo: Boolean,
    onSelectModel: (AiModel) -> Unit,
    onToggleSemanticReranker: (Boolean) -> Unit,
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
                isSelected = model.id == selectedModelId,
                isDownloaded = model.id in downloadedModelIds,
                isDownloading = model.id == downloadingModelId,
                progress = progressByModel[model.id] ?: 0f,
                downloadEnabled = downloadingModelId == null || model.id == downloadingModelId,
                error = errorsByModel[model.id],
                onSelect = { onSelectModel(model) },
                onDownload = { onDownload(model) },
                onDelete = { onDelete(model) }
            )
        }

        HorizontalDivider()

        SemanticRerankerSection(
            semanticRerankerEnabled = semanticRerankerEnabled,
            showRerankerInfo = showRerankerInfo,
            onToggleSemanticReranker = onToggleSemanticReranker,
            onToggleRerankerInfo = onToggleRerankerInfo
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
private fun SemanticRerankerSection(
    semanticRerankerEnabled: Boolean,
    showRerankerInfo: Boolean,
    onToggleSemanticReranker: (Boolean) -> Unit,
    onToggleRerankerInfo: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ai_semantic_reranker_section),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (semanticRerankerEnabled) {
                        stringResource(R.string.ai_semantic_reranker_enabled)
                    } else {
                        stringResource(R.string.ai_semantic_reranker_disabled)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (semanticRerankerEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Switch(
                checked = semanticRerankerEnabled,
                onCheckedChange = onToggleSemanticReranker
            )
        }

        if (!semanticRerankerEnabled) {
            AssistChip(
                onClick = { onToggleSemanticReranker(true) },
                label = { Text(stringResource(R.string.ai_semantic_reranker_recommended)) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ai_semantic_reranker_builtin),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
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
                RadioButton(
                    selected = isSelected,
                    onClick = if (isDownloaded) onSelect else null,
                    enabled = isDownloaded,
                    modifier = Modifier.testTag("model_selector")
                )
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
                    if (!isSelected) {
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

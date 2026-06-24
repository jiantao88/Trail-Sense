package com.kylecorry.trail_sense.tools.ai_assistant.infrastructure

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class ModelManagerTest {

    @TempDir
    lateinit var tempDir: File

    private var selectedModelId = ModelManager.DEFAULT_MODEL_ID

    @Test
    fun `isModelDownloaded returns false when model file does not exist`() {
        val manager = ModelManager(tempDir)
        assertFalse(manager.isModelDownloaded())
    }

    @Test
    fun `isModelDownloaded returns true when model file exists`() {
        val manager = ModelManager(tempDir)
        File(tempDir, ModelManager.MODEL_FILE_NAME).createNewFile()
        assertTrue(manager.isModelDownloaded())
    }

    @Test
    fun `getModelPath returns null when model not downloaded`() {
        val manager = ModelManager(tempDir)
        assertNull(manager.getModelPath())
    }

    @Test
    fun `getModelPath returns path when model exists`() {
        val manager = ModelManager(tempDir)
        val modelFile = File(tempDir, ModelManager.MODEL_FILE_NAME)
        modelFile.createNewFile()
        assertEquals(modelFile.absolutePath, manager.getModelPath())
    }

    @Test
    fun `deleteModel removes the model file`() {
        val manager = ModelManager(tempDir)
        val modelFile = File(tempDir, ModelManager.MODEL_FILE_NAME)
        modelFile.writeText("fake model data")
        assertTrue(modelFile.exists())
        manager.deleteModel()
        assertFalse(modelFile.exists())
    }

    @Test
    fun `deleteModel does nothing when no model exists`() {
        val manager = ModelManager(tempDir)
        manager.deleteModel()
        assertFalse(manager.isModelDownloaded())
    }

    @Test
    fun `selectedModel changes the active model`() {
        val manager = createManager()
        val qwen25 = manager.models.first { it.id == "qwen2.5-1.5b-instruct" }

        manager.selectedModel = qwen25

        assertEquals(qwen25, manager.selectedModel)
    }

    @Test
    fun `getModelPath uses selected model`() {
        val manager = createManager()
        val qwen25 = manager.models.first { it.id == "qwen2.5-1.5b-instruct" }
        manager.selectedModel = qwen25
        val modelFile = File(tempDir, qwen25.fileName)
        modelFile.createNewFile()

        assertEquals(modelFile.absolutePath, manager.getModelPath())
    }

    @Test
    fun `deleteModel removes only the selected model`() {
        val manager = createManager()
        val qwen3 = manager.models.first { it.id == ModelManager.DEFAULT_MODEL_ID }
        val qwen25 = manager.models.first { it.id == "qwen2.5-1.5b-instruct" }
        val qwen3File = File(tempDir, qwen3.fileName)
        val qwen25File = File(tempDir, qwen25.fileName)
        qwen3File.createNewFile()
        qwen25File.createNewFile()
        manager.selectedModel = qwen25

        manager.deleteModel()

        assertTrue(qwen3File.exists())
        assertFalse(qwen25File.exists())
    }

    @Test
    fun `invalid saved model falls back to the default model`() {
        selectedModelId = "unknown-model"
        val manager = createManager()

        assertEquals(ModelManager.DEFAULT_MODEL, manager.selectedModel)
    }

    @Test
    fun `qwen models are text only and gemma models support images`() {
        val manager = createManager()

        assertFalse(manager.models.first { it.id == "qwen3-0.6b" }.supportsImages)
        assertFalse(manager.models.first { it.id == "qwen2.5-1.5b-instruct" }.supportsImages)
        assertTrue(manager.models.first { it.id == "gemma-4-e2b-it" }.supportsImages)
        assertTrue(manager.models.first { it.id == "gemma-4-e4b-it" }.supportsImages)
    }

    private fun createManager(): ModelManager {
        return ModelManager(
            tempDir,
            getSelectedModelId = { selectedModelId },
            setSelectedModelId = { selectedModelId = it }
        )
    }
}

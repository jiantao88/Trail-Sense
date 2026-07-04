package com.kylecorry.trail_sense.tools.ai_assistant.infrastructure

import android.annotation.SuppressLint
import android.content.Context
import com.kylecorry.trail_sense.tools.ai_assistant.domain.CrossEncoderReranker
import com.kylecorry.trail_sense.tools.ai_assistant.domain.HybridReranker
import com.kylecorry.trail_sense.tools.ai_assistant.domain.KeywordReranker
import com.kylecorry.trail_sense.tools.ai_assistant.domain.LlmReranker
import com.kylecorry.trail_sense.tools.ai_assistant.domain.RerankCandidate
import com.kylecorry.trail_sense.tools.ai_assistant.domain.RerankResult
import com.kylecorry.trail_sense.tools.ai_assistant.domain.RerankerType

class RerankerSubsystem private constructor(private val context: Context) {

    private val modelManager = ModelManager(context)
    private val keywordReranker = KeywordReranker()
    private val crossEncoderReranker = CrossEncoderReranker(context, modelManager)

    // LLM 重排器懒加载：仅在引擎就绪时可用
    private val llmReranker: LlmReranker? by lazy {
        try {
            LlmReranker(AiInferenceSubsystem.getInstance(context))
        } catch (e: Exception) {
            null
        }
    }

    private val hybridReranker: HybridReranker by lazy {
        HybridReranker(
            listOfNotNull(
                keywordReranker,
                crossEncoderReranker,
                llmReranker
            )
        )
    }

    fun isSemanticRerankerEnabled(): Boolean {
        return modelManager.isSemanticRerankerEnabled
    }

    fun setSemanticRerankerEnabled(enabled: Boolean) {
        modelManager.isSemanticRerankerEnabled = enabled
    }

    fun getActiveStrategy(): HybridReranker.Strategy {
        return hybridReranker.getActiveStrategy()
    }

    fun getAvailableRerankerTypes(): List<RerankerType> {
        return hybridReranker.getAvailableRerankerTypes()
    }

    suspend fun rerankSkills(
        query: String,
        candidates: List<RerankCandidate>,
        recallCount: Int = 8,
        finalTopK: Int = 3
    ): List<RerankResult> {
        return hybridReranker.rerankSkills(query, candidates, recallCount, finalTopK)
    }

    suspend fun rerankKnowledge(
        query: String,
        candidates: List<RerankCandidate>,
        recallCount: Int = 10,
        finalTopK: Int = 2
    ): List<RerankResult> {
        return hybridReranker.rerankKnowledge(query, candidates, recallCount, finalTopK)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: RerankerSubsystem? = null

        @Synchronized
        fun getInstance(context: Context): RerankerSubsystem {
            if (instance == null) {
                instance = RerankerSubsystem(context.applicationContext)
            }
            return instance!!
        }
    }
}

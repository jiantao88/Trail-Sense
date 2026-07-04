package com.kylecorry.trail_sense.tools.ai_assistant.infrastructure

import android.annotation.SuppressLint
import android.content.Context
import com.kylecorry.trail_sense.tools.ai_assistant.domain.CrossEncoderReranker
import com.kylecorry.trail_sense.tools.ai_assistant.domain.HybridReranker
import com.kylecorry.trail_sense.tools.ai_assistant.domain.IReranker
import com.kylecorry.trail_sense.tools.ai_assistant.domain.KeywordReranker
import com.kylecorry.trail_sense.tools.ai_assistant.domain.RerankCandidate
import com.kylecorry.trail_sense.tools.ai_assistant.domain.RerankResult
import com.kylecorry.trail_sense.tools.ai_assistant.domain.RerankerType

class RerankerSubsystem private constructor(private val context: Context) {

    private val modelManager = ModelManager(context)
    private val keywordReranker = KeywordReranker()
    private val crossEncoderReranker = CrossEncoderReranker(context, modelManager)

    private val hybridReranker: HybridReranker by lazy {
        HybridReranker(
            listOf(
                keywordReranker,
                crossEncoderReranker
            )
        )
    }

    fun initialize() {
        if (modelManager.isRerankerDownloaded()) {
            crossEncoderReranker
        }
    }

    fun getActiveStrategy(): HybridReranker.Strategy {
        return hybridReranker.getActiveStrategy()
    }

    fun isCrossEncoderAvailable(): Boolean {
        return crossEncoderReranker.isAvailable
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

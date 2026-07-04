package com.kylecorry.trail_sense.tools.ai_assistant.domain

import android.util.Log

class HybridReranker(
    private val rerankers: List<IReranker>
) {

    private val keywordReranker = rerankers.firstOrNull { it.type == RerankerType.KEYWORD }
        ?: KeywordReranker()
    private val crossEncoderReranker = rerankers.firstOrNull { it.type == RerankerType.CROSS_ENCODER }

    enum class Strategy {
        KEYWORD_ONLY,
        KEYWORD_THEN_CROSS_ENCODER
    }

    fun getActiveStrategy(): Strategy {
        return if (crossEncoderReranker?.isAvailable == true) {
            Strategy.KEYWORD_THEN_CROSS_ENCODER
        } else {
            Strategy.KEYWORD_ONLY
        }
    }

    fun getAvailableRerankerTypes(): List<RerankerType> {
        return rerankers.filter { it.isAvailable }.map { it.type }
    }

    suspend fun rerankSkills(
        query: String,
        candidates: List<RerankCandidate>,
        recallCount: Int = 8,
        finalTopK: Int = 3
    ): List<RerankResult> {
        return when (getActiveStrategy()) {
            Strategy.KEYWORD_THEN_CROSS_ENCODER -> {
                rerankTwoStage(query, candidates, recallCount, finalTopK)
            }
            Strategy.KEYWORD_ONLY -> {
                keywordReranker.rerank(query, candidates, finalTopK)
            }
        }
    }

    suspend fun rerankKnowledge(
        query: String,
        candidates: List<RerankCandidate>,
        recallCount: Int = 10,
        finalTopK: Int = 2
    ): List<RerankResult> {
        return rerankSkills(query, candidates, recallCount, finalTopK)
    }

    private suspend fun rerankTwoStage(
        query: String,
        candidates: List<RerankCandidate>,
        recallCount: Int,
        finalTopK: Int
    ): List<RerankResult> {
        val crossEncoder = crossEncoderReranker ?: return keywordReranker.rerank(query, candidates, finalTopK)

        val recallStart = System.currentTimeMillis()

        val keywordResults = keywordReranker.rerank(query, candidates, recallCount)

        if (keywordResults.isEmpty()) {
            Log.d(TAG, "Two-stage rerank: keyword recall returned 0 results")
            return emptyList()
        }

        if (keywordResults.size <= finalTopK) {
            Log.d(TAG, "Two-stage rerank: keyword results <= topK, skipping cross-encoder")
            return keywordResults
        }

        val highConfidenceKeyword = keywordResults.firstOrNull()?.score?.let {
            it >= KeywordReranker.HIGH_CONFIDENCE_THRESHOLD
        } ?: false

        if (highConfidenceKeyword && keywordResults.size >= finalTopK) {
            val topScores = keywordResults.take(finalTopK)
            if (topScores.all { it.score >= KeywordReranker.HIGH_CONFIDENCE_THRESHOLD }) {
                Log.d(TAG, "Two-stage rerank: high confidence keyword match, skipping cross-encoder")
                return topScores
            }
        }

        val recallDuration = System.currentTimeMillis() - recallStart
        Log.d(TAG, "Two-stage rerank: keyword recall found ${keywordResults.size} candidates in ${recallDuration}ms, running cross-encoder")

        val crossEncoderStart = System.currentTimeMillis()
        val recalledCandidates = keywordResults.map { it.candidate }
        val crossEncoderResults = crossEncoder.rerank(query, recalledCandidates, finalTopK)

        val crossEncoderDuration = System.currentTimeMillis() - crossEncoderStart
        Log.d(TAG, "Two-stage rerank: cross-encoder finished in ${crossEncoderDuration}ms")

        if (crossEncoderResults.isEmpty()) {
            Log.d(TAG, "Two-stage rerank: cross-encoder failed, falling back to keyword results")
            return keywordResults.take(finalTopK)
        }

        return crossEncoderResults
    }

    companion object {
        private const val TAG = "HybridReranker"
    }
}

package com.kylecorry.trail_sense.tools.ai_assistant.domain

import android.util.Log

/**
 * 混合重排器 — 三级级联策略。
 *
 * 策略选择：
 * 1. KEYWORD_ONLY — 仅关键词匹配（语义重排关闭时）
 * 2. KEYWORD_THEN_CROSS_ENCODER — 关键词召回 + 语义精排（默认）
 * 3. KEYWORD_THEN_CROSS_ENCODER_THEN_LLM — 关键词召回 + 语义精排 + LLM 决策（低置信度时触发）
 *
 * 两阶段流程：
 *   第一阶段（召回）：KeywordReranker 快速筛选，保留 recallCount 个候选
 *   第二阶段（精排）：CrossEncoderReranker 语义排序，输出 finalTopK 个结果
 *   第三阶段（可选，LLM 兜底）：当精排 Top 分数低于阈值时，调用 LLM 做最终决策
 *
 * 快路径优化：
 * - 候选数 <= topK 时直接返回
 * - 关键词高置信度命中时跳过精排
 * - 精排高置信度时跳过 LLM
 */
class HybridReranker(
    private val rerankers: List<IReranker>
) {

    private val keywordReranker = rerankers.firstOrNull { it.type == RerankerType.KEYWORD }
        ?: KeywordReranker()
    private val crossEncoderReranker = rerankers.firstOrNull { it.type == RerankerType.CROSS_ENCODER }
    private val llmReranker = rerankers.firstOrNull { it.type == RerankerType.LLM }

    enum class Strategy {
        KEYWORD_ONLY,
        KEYWORD_THEN_CROSS_ENCODER,
        KEYWORD_THEN_CROSS_ENCODER_THEN_LLM
    }

    fun getActiveStrategy(): Strategy {
        return when {
            llmReranker?.isAvailable == true -> Strategy.KEYWORD_THEN_CROSS_ENCODER_THEN_LLM
            crossEncoderReranker?.isAvailable == true -> Strategy.KEYWORD_THEN_CROSS_ENCODER
            else -> Strategy.KEYWORD_ONLY
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
        val startTime = System.currentTimeMillis()

        val results = when (getActiveStrategy()) {
            Strategy.KEYWORD_THEN_CROSS_ENCODER_THEN_LLM -> {
                rerankThreeStage(query, candidates, recallCount, finalTopK)
            }
            Strategy.KEYWORD_THEN_CROSS_ENCODER -> {
                rerankTwoStage(query, candidates, recallCount, finalTopK)
            }
            Strategy.KEYWORD_ONLY -> {
                keywordReranker.rerank(query, candidates, finalTopK)
            }
        }

        val duration = System.currentTimeMillis() - startTime
        Log.d(TAG, "rerankSkills: strategy=${getActiveStrategy()}, " +
                "${candidates.size} candidates → ${results.size} results in ${duration}ms")

        return results
    }

    suspend fun rerankKnowledge(
        query: String,
        candidates: List<RerankCandidate>,
        recallCount: Int = 10,
        finalTopK: Int = 2
    ): List<RerankResult> {
        return rerankSkills(query, candidates, recallCount, finalTopK)
    }

    /**
     * 两阶段：关键词召回 → 语义精排
     */
    private suspend fun rerankTwoStage(
        query: String,
        candidates: List<RerankCandidate>,
        recallCount: Int,
        finalTopK: Int
    ): List<RerankResult> {
        val crossEncoder = crossEncoderReranker
            ?: return keywordReranker.rerank(query, candidates, finalTopK)

        val recallStart = System.currentTimeMillis()
        val keywordResults = keywordReranker.rerank(query, candidates, recallCount)

        if (keywordResults.isEmpty()) {
            Log.d(TAG, "Two-stage: keyword recall returned 0 results")
            return emptyList()
        }

        if (keywordResults.size <= finalTopK) {
            Log.d(TAG, "Two-stage: keyword results <= topK, skipping cross-encoder")
            return keywordResults
        }

        // 快路径：关键词高置信度命中时跳过精排
        val highConfidenceKeyword = keywordResults.firstOrNull()?.score?.let {
            it >= KeywordReranker.HIGH_CONFIDENCE_THRESHOLD
        } ?: false

        if (highConfidenceKeyword && keywordResults.size >= finalTopK) {
            val topScores = keywordResults.take(finalTopK)
            if (topScores.all { it.score >= KeywordReranker.HIGH_CONFIDENCE_THRESHOLD }) {
                Log.d(TAG, "Two-stage: high confidence keyword match, skipping cross-encoder")
                return topScores
            }
        }

        val recallDuration = System.currentTimeMillis() - recallStart
        Log.d(TAG, "Two-stage: keyword recall ${keywordResults.size} candidates in ${recallDuration}ms")

        val crossEncoderStart = System.currentTimeMillis()
        val recalledCandidates = keywordResults.map { it.candidate }
        val crossEncoderResults = crossEncoder.rerank(query, recalledCandidates, finalTopK)

        val crossEncoderDuration = System.currentTimeMillis() - crossEncoderStart
        Log.d(TAG, "Two-stage: cross-encoder finished in ${crossEncoderDuration}ms")

        if (crossEncoderResults.isEmpty()) {
            Log.d(TAG, "Two-stage: cross-encoder failed, falling back to keyword results")
            return keywordResults.take(finalTopK)
        }

        return crossEncoderResults
    }

    /**
     * 三阶段：关键词召回 → 语义精排 → LLM 兜底
     *
     * LLM 仅在精排 Top 分数低于 LLM_TRIGGER_THRESHOLD 时触发，
     * 避免不必要的 LLM 调用（节省推理时间和电量）。
     */
    private suspend fun rerankThreeStage(
        query: String,
        candidates: List<RerankCandidate>,
        recallCount: Int,
        finalTopK: Int
    ): List<RerankResult> {
        // 前两阶段与 rerankTwoStage 相同
        val twoStageResults = rerankTwoStage(query, candidates, recallCount, finalTopK)

        if (twoStageResults.isEmpty()) return emptyList()

        // 检查是否需要 LLM 兜底
        val topScore = twoStageResults.firstOrNull()?.score ?: 0f
        if (topScore >= LLM_TRIGGER_THRESHOLD) {
            Log.d(TAG, "Three-stage: top score=$topScore >= $LLM_TRIGGER_THRESHOLD, skipping LLM")
            return twoStageResults
        }

        // LLM 兜底：对精排结果做最终决策
        val llm = llmReranker ?: return twoStageResults
        val llmCandidates = twoStageResults.map { it.candidate }

        Log.d(TAG, "Three-stage: top score=$topScore < $LLM_TRIGGER_THRESHOLD, triggering LLM rerank")

        val llmStart = System.currentTimeMillis()
        val llmResults = llm.rerank(query, llmCandidates, finalTopK)
        val llmDuration = System.currentTimeMillis() - llmStart

        Log.d(TAG, "Three-stage: LLM finished in ${llmDuration}ms")

        if (llmResults.isEmpty()) {
            Log.d(TAG, "Three-stage: LLM failed, falling back to two-stage results")
            return twoStageResults
        }

        return llmResults
    }

    companion object {
        private const val TAG = "HybridReranker"

        /** 精排 Top 分数低于此阈值时触发 LLM 兜底 */
        private const val LLM_TRIGGER_THRESHOLD = 0.45f
    }
}

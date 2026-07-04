package com.kylecorry.trail_sense.tools.ai_assistant.domain

data class RerankCandidate(
    val id: String,
    val text: String,
    val metadata: Map<String, String> = emptyMap()
)

data class RerankResult(
    val candidate: RerankCandidate,
    val score: Float,
    val rerankerType: RerankerType
)

enum class RerankerType {
    KEYWORD,
    LLM,
    CROSS_ENCODER
}

interface IReranker {
    val type: RerankerType
    val isAvailable: Boolean
    suspend fun rerank(
        query: String,
        candidates: List<RerankCandidate>,
        topK: Int = 3
    ): List<RerankResult>
}

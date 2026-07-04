package com.kylecorry.trail_sense.tools.ai_assistant.domain

class KeywordReranker : IReranker {

    override val type: RerankerType = RerankerType.KEYWORD

    override val isAvailable: Boolean = true

    override suspend fun rerank(
        query: String,
        candidates: List<RerankCandidate>,
        topK: Int
    ): List<RerankResult> {
        return candidates
            .mapNotNull { candidate ->
                val score = score(query, candidate.text)
                if (score > 0f) {
                    RerankResult(candidate, score, type)
                } else null
            }
            .sortedByDescending { it.score }
            .take(topK)
    }

    private fun score(query: String, text: String): Float {
        val phraseScore = phraseScore(query, text)
        val tokenScore = tokenScore(query, text)
        return phraseScore + tokenScore
    }

    private fun phraseScore(query: String, text: String): Float {
        val normalizedQuery = query.lowercase()
        val terms = text
            .split(',', ';', '，', '；', '|', '\n')
            .map { it.trim().lowercase() }
            .filter { it.length >= 2 }

        return terms.count {
            normalizedQuery.contains(it) || (it.any(::isCjk) && it.contains(normalizedQuery))
        }.toFloat()
    }

    private fun tokenScore(query: String, text: String): Float {
        val queryTokens = tokenize(query)
        if (queryTokens.isEmpty()) {
            return 0f
        }

        val textTokens = tokenize(text)
        return queryTokens.count { it in textTokens }.toFloat() / queryTokens.size
    }

    private fun tokenize(text: String): Set<String> {
        val normalized = text.lowercase()
        val latinTokens = normalized
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length > 2 }

        val cjkTokens = normalized
            .filter(::isCjk)
            .windowed(2)

        return (latinTokens + cjkTokens).toSet()
    }

    private fun isCjk(char: Char): Boolean {
        return char.code in 0x4E00..0x9FFF
    }

    companion object {
        const val HIGH_CONFIDENCE_THRESHOLD = 0.8f
    }
}

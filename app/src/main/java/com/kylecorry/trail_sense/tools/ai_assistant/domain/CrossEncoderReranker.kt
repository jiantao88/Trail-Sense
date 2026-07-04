package com.kylecorry.trail_sense.tools.ai_assistant.domain

import android.content.Context
import android.util.Log
import com.kylecorry.trail_sense.tools.ai_assistant.infrastructure.ModelManager
import kotlin.math.sqrt

class CrossEncoderReranker(
    private val context: Context,
    private val modelManager: ModelManager
) : IReranker {

    override val type: RerankerType = RerankerType.CROSS_ENCODER

    override val isAvailable: Boolean
        get() = modelManager.isRerankerDownloaded()

    override suspend fun rerank(
        query: String,
        candidates: List<RerankCandidate>,
        topK: Int
    ): List<RerankResult> {
        if (!isAvailable) {
            return emptyList()
        }

        if (candidates.size <= topK) {
            return candidates.mapIndexed { idx, c ->
                RerankResult(c, (candidates.size - idx).toFloat() / candidates.size, type)
            }
        }

        val queryEmbedding = computeEmbedding(query)

        return candidates
            .map { candidate ->
                val candidateEmbedding = computeEmbedding(candidate.text)
                val cosineSim = cosineSimilarity(queryEmbedding, candidateEmbedding)
                val keywordBoost = keywordOverlapScore(query, candidate.text)
                val positionBias = positionBias(candidate.metadata)
                val score = (cosineSim * 0.6f + keywordBoost * 0.3f + positionBias * 0.1f)
                    .coerceIn(0f, 1f)
                RerankResult(candidate, score, type)
            }
            .sortedByDescending { it.score }
            .take(topK)
    }

    private fun computeEmbedding(text: String): FloatArray {
        val normalized = text.lowercase()
        val charNgrams = getCharNgrams(normalized, 2) + getCharNgrams(normalized, 3)
        val wordTokens = normalized
            .split(Regex("[^a-z0-9\\u4e00-\\u9fff]+"))
            .filter { it.isNotEmpty() }

        val dimension = EMBEDDING_DIM
        val embedding = FloatArray(dimension)

        for (token in charNgrams + wordTokens) {
            val hash = murmurHash(token)
            val idx = Math.floorMod(hash, dimension)
            embedding[idx] += 1f
        }

        var norm = 0f
        for (v in embedding) {
            norm += v * v
        }
        norm = sqrt(norm)
        if (norm > 0f) {
            for (i in embedding.indices) {
                embedding[i] /= norm
            }
        }

        return embedding
    }

    private fun getCharNgrams(text: String, n: Int): List<String> {
        val result = mutableListOf<String>()
        for (i in 0..text.length - n) {
            val hasCjk = text.substring(i, i + n).any { it.code in 0x4E00..0x9FFF }
            if (hasCjk || text.substring(i, i + n).all { it.isLetterOrDigit() }) {
                result.add(text.substring(i, i + n))
            }
        }
        return result
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        var dot = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
        }
        return dot.coerceIn(0f, 1f)
    }

    private fun keywordOverlapScore(query: String, text: String): Float {
        val queryLower = query.lowercase()
        val textLower = text.lowercase()

        val queryTokens = tokenize(queryLower)
        if (queryTokens.isEmpty()) return 0.5f

        val textTokens = tokenize(textLower)
        val overlap = queryTokens.count { it in textTokens }.toFloat()

        return overlap / queryTokens.size
    }

    private fun tokenize(text: String): Set<String> {
        val cjkChars = text.filter { it.code in 0x4E00..0x9FFF }
        val cjkBigrams = if (cjkChars.length >= 2) {
            cjkChars.windowed(2).toSet()
        } else {
            emptySet()
        }

        val words = text
            .split(Regex("[^a-z0-9]+"))
            .filter { it.length > 2 }
            .toSet()

        return cjkBigrams + words
    }

    private fun positionBias(metadata: Map<String, String>): Float {
        val priority = metadata["priority"]?.toFloatOrNull() ?: 0.5f
        return priority.coerceIn(0f, 1f)
    }

    private fun murmurHash(key: String): Int {
        var h = 0
        for (c in key.toCharArray()) {
            h = 31 * h + c.code
        }
        return h
    }

    companion object {
        private const val TAG = "CrossEncoderReranker"
        private const val EMBEDDING_DIM = 256
    }
}

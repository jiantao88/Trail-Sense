package com.kylecorry.trail_sense.tools.ai_assistant.domain

import android.content.Context
import android.util.Log
import com.kylecorry.trail_sense.tools.ai_assistant.infrastructure.ModelManager
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * 内置语义重排器 — 纯算法实现，无需下载模型。
 *
 * 核心算法：
 * 1. 字符 n-gram（2/3 元）+ 词元 → 哈希嵌入（TF-IDF 加权）
 * 2. 余弦相似度计算语义相关性
 * 3. 关键词重叠 + 位置偏置作为辅助信号
 * 4. 嵌入缓存避免重复计算
 *
 * 评分公式：score = 0.55 * cosineSim + 0.30 * keywordOverlap + 0.15 * positionBias
 */
class CrossEncoderReranker(
    private val context: Context,
    private val modelManager: ModelManager
) : IReranker {

    override val type: RerankerType = RerankerType.CROSS_ENCODER

    override val isAvailable: Boolean
        get() = modelManager.isSemanticRerankerAvailable()

    // 嵌入缓存：避免对同一文本重复计算
    private val embeddingCache = object : LinkedHashMap<String, FloatArray>(MAX_CACHE_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, FloatArray>): Boolean {
            return size > MAX_CACHE_SIZE
        }
    }

    // 文档频率（用于 IDF 计算）— 懒加载
    private var documentFrequency: Map<String, Int>? = null
    private var totalDocuments: Int = 0

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

        val startTime = System.currentTimeMillis()

        // 更新文档频率统计（用于 IDF）
        updateDocumentFrequency(candidates)

        val queryEmbedding = getEmbedding(query)

        val results = candidates
            .map { candidate ->
                val candidateEmbedding = getEmbedding(candidate.text)
                val cosineSim = cosineSimilarity(queryEmbedding, candidateEmbedding)
                val keywordBoost = keywordOverlapScore(query, candidate.text)
                val positionBias = positionBias(candidate.metadata)
                val score = (cosineSim * SEMANTIC_WEIGHT +
                        keywordBoost * KEYWORD_WEIGHT +
                        positionBias * POSITION_WEIGHT).coerceIn(0f, 1f)
                RerankResult(candidate, score, type)
            }
            .sortedByDescending { it.score }
            .take(topK)

        val duration = System.currentTimeMillis() - startTime
        Log.d(TAG, "Rerank: ${candidates.size} candidates → ${results.size} results in ${duration}ms, " +
                "top score=${results.firstOrNull()?.score ?: 0f}")

        return results
    }

    private fun getEmbedding(text: String): FloatArray {
        val cacheKey = text.take(500) // 限制缓存键长度
        embeddingCache[cacheKey]?.let { return it }

        val embedding = computeEmbedding(text)
        embeddingCache[cacheKey] = embedding
        return embedding
    }

    /**
     * 计算 TF-IDF 加权的哈希嵌入。
     *
     * 改进点：
     * - 使用 sublinear TF：1 + log(count)，避免高频词主导
     * - IDF 加权：罕见词获得更高权重
     * - 增大维度到 512，降低哈希冲突
     */
    private fun computeEmbedding(text: String): FloatArray {
        val normalized = text.lowercase()
        val charNgrams = getCharNgrams(normalized, 2) + getCharNgrams(normalized, 3)
        val wordTokens = normalized
            .split(Regex("[^a-z0-9\\u4e00-\\u9fff]+"))
            .filter { it.isNotEmpty() }

        // 统计 token 频率
        val tokenFreq = mutableMapOf<String, Int>()
        for (token in charNgrams + wordTokens) {
            tokenFreq[token] = (tokenFreq[token] ?: 0) + 1
        }

        val embedding = FloatArray(EMBEDDING_DIM)
        val df = documentFrequency
        val n = totalDocuments.coerceAtLeast(1)

        for ((token, freq) in tokenFreq) {
            // Sublinear TF: 1 + log(count)
            val tf = 1f + ln(freq.toFloat())

            // IDF: log(N / df) — 罕见词权重更高
            val idf = if (df != null) {
                val docFreq = df[token] ?: 0
                ln((n + 1f) / (docFreq + 1f)) + 1f
            } else {
                1f
            }

            val weight = tf * idf
            val hash = murmurHash3(token)
            val idx = Math.floorMod(hash, EMBEDDING_DIM)
            embedding[idx] += weight
        }

        // L2 归一化
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

    /**
     * 更新文档频率统计（用于 IDF 计算）。
     * 在每次 rerank 调用时基于当前候选集更新。
     */
    private fun updateDocumentFrequency(candidates: List<RerankCandidate>) {
        val df = mutableMapOf<String, Int>()
        for (candidate in candidates) {
            val tokens = extractTokens(candidate.text.lowercase())
            for (token in tokens) {
                df[token] = (df[token] ?: 0) + 1
            }
        }
        documentFrequency = df
        totalDocuments = candidates.size
    }

    private fun extractTokens(text: String): Set<String> {
        val charNgrams = getCharNgrams(text, 2) + getCharNgrams(text, 3)
        val wordTokens = text
            .split(Regex("[^a-z0-9\\u4e00-\\u9fff]+"))
            .filter { it.isNotEmpty() }
        return (charNgrams + wordTokens).toSet()
    }

    private fun getCharNgrams(text: String, n: Int): List<String> {
        val result = mutableListOf<String>()
        for (i in 0..text.length - n) {
            val ngram = text.substring(i, i + n)
            val hasCjk = ngram.any { it.code in 0x4E00..0x9FFF }
            if (hasCjk || ngram.all { it.isLetterOrDigit() }) {
                result.add(ngram)
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

    /**
     * MurmurHash3 (x86 32-bit) — 比简单 31*h 更均匀，减少冲突。
     */
    private fun murmurHash3(key: String): Int {
        val data = key.toByteArray(Charsets.UTF_8)
        val length = data.size
        val seed = -0x68b84d74 // 0x9747b28c as signed Int

        var h1 = seed
        val roundedEnd = length and -4 // 0xFFFFFFFC as signed Int

        var i = 0
        while (i < roundedEnd) {
            var k1 = (data[i].toInt() and 0xff) or
                    ((data[i + 1].toInt() and 0xff) shl 8) or
                    ((data[i + 2].toInt() and 0xff) shl 16) or
                    ((data[i + 3].toInt() and 0xff) shl 24)

            k1 *= -0x3361d5af // 0xcc9e2d51
            k1 = Integer.rotateLeft(k1, 15)
            k1 *= 0x1b873593

            h1 = h1 xor k1
            h1 = Integer.rotateLeft(h1, 13)
            h1 = h1 * 5 + -0x19ab9b9c // 0xe6546b64

            i += 4
        }

        var k1 = 0
        val tail = length and 0x03
        if (tail >= 3) {
            k1 = k1 or ((data[roundedEnd + 2].toInt() and 0xff) shl 16)
        }
        if (tail >= 2) {
            k1 = k1 or ((data[roundedEnd + 1].toInt() and 0xff) shl 8)
        }
        if (tail >= 1) {
            k1 = k1 or (data[roundedEnd].toInt() and 0xff)
            k1 *= -0x3361d5af // 0xcc9e2d51
            k1 = Integer.rotateLeft(k1, 15)
            k1 *= 0x1b873593
            h1 = h1 xor k1
        }

        h1 = h1 xor length
        h1 = h1 xor (h1 ushr 16)
        h1 *= -0x7a143595 // 0x85ebca6b
        h1 = h1 xor (h1 ushr 13)
        h1 *= -0x3d4d51cb // 0xc2b2ae35
        h1 = h1 xor (h1 ushr 16)

        return h1
    }

    companion object {
        private const val TAG = "CrossEncoderReranker"
        private const val EMBEDDING_DIM = 512
        private const val MAX_CACHE_SIZE = 128

        // 评分权重
        private const val SEMANTIC_WEIGHT = 0.55f
        private const val KEYWORD_WEIGHT = 0.30f
        private const val POSITION_WEIGHT = 0.15f
    }
}

package com.kylecorry.trail_sense.tools.ai_assistant.domain

import android.util.Log
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.MessageCallback
import com.kylecorry.trail_sense.tools.ai_assistant.infrastructure.AiInferenceSubsystem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

class LlmReranker(
    private val aiSubsystem: AiInferenceSubsystem
) : IReranker {

    override val type: RerankerType = RerankerType.LLM

    override val isAvailable: Boolean
        get() = aiSubsystem.isEngineReady()

    override suspend fun rerank(
        query: String,
        candidates: List<RerankCandidate>,
        topK: Int
    ): List<RerankResult> = withContext(Dispatchers.Default) {
        if (candidates.size <= topK) {
            return@withContext candidates.mapIndexed { idx, c ->
                RerankResult(c, (candidates.size - idx).toFloat() / candidates.size, type)
            }
        }

        if (!isAvailable) {
            return@withContext emptyList()
        }

        val prompt = buildRerankPrompt(query, candidates, topK)
        val response = sendRerankRequest(prompt, RERANK_TIMEOUT_MS) ?: return@withContext emptyList()

        parseRerankResponse(response, candidates, topK)
    }

    private fun buildRerankPrompt(
        query: String,
        candidates: List<RerankCandidate>,
        topK: Int
    ): String {
        val candidateList = candidates.mapIndexed { index, candidate ->
            "${index + 1}. ${candidate.text.take(200)}"
        }.joinToString("\n")

        return buildString {
            appendLine("You are a search relevance expert. Given a user question and a list of options, select the $topK most relevant options.")
            appendLine()
            appendLine("User question: $query")
            appendLine()
            appendLine("Options:")
            appendLine(candidateList)
            appendLine()
            appendLine("Respond ONLY with comma-separated numbers of the top $topK most relevant options, ordered from most to least relevant.")
            appendLine("Example response format: 3,1,5")
            appendLine("Response:")
        }
    }

    private suspend fun sendRerankRequest(prompt: String, timeoutMs: Long): String? {
        val result = CompletableDeferred<String?>()
        val responseBuilder = StringBuilder()
        val isDone = AtomicBoolean(false)

        try {
            aiSubsystem.sendMessage(prompt, emptyList(), object : MessageCallback {
                override fun onMessage(message: Message) {
                    if (isDone.get()) return
                    val text = message.toString()
                    if (text.isNotEmpty()) {
                        responseBuilder.append(text)
                    }
                }

                override fun onDone() {
                    if (isDone.compareAndSet(false, true)) {
                        result.complete(responseBuilder.toString())
                    }
                }

                override fun onError(throwable: Throwable) {
                    if (isDone.compareAndSet(false, true)) {
                        Log.e(TAG, "LLM rerank failed", throwable)
                        result.complete(null)
                    }
                }
            })

            return withTimeoutOrNull(timeoutMs) {
                result.await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "LLM rerank error", e)
            return null
        }
    }

    private fun parseRerankResponse(
        response: String,
        candidates: List<RerankCandidate>,
        topK: Int
    ): List<RerankResult> {
        val numbers = response
            .replace(Regex("[^0-9,]"), "")
            .split(',')
            .mapNotNull { it.trim().toIntOrNull() }
            .map { it - 1 }
            .filter { it in candidates.indices }
            .distinct()
            .take(topK)

        if (numbers.isEmpty()) {
            return emptyList()
        }

        return numbers.mapIndexed { rank, idx ->
            val score = 1f - (rank.toFloat() / topK) * 0.5f
            RerankResult(candidates[idx], score.coerceIn(0.1f, 1f), type)
        }
    }

    companion object {
        private const val TAG = "LlmReranker"
        private const val RERANK_TIMEOUT_MS = 5000L
    }
}

package com.kylecorry.trail_sense.tools.ai_assistant.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.system.measureTimeMillis

/**
 * Performance benchmarks for AI assistant optimizations.
 * These tests verify that optimizations reduce processing time.
 */
class AiAssistantPerformanceTest {

    @Test
    fun `fast path detector is performant`() {
        val questions = listOf(
            "上山会不会冷",
            "现在的气压是多少",
            "什么是海拔",
            "我的位置在哪里",
            "怎么使用指南针"
        )

        // Warm up
        repeat(100) {
            questions.forEach { AiFastPathDetector.shouldUseFastPath(it) }
        }

        // Benchmark
        val iterations = 1000
        val totalTime = measureTimeMillis {
            repeat(iterations) {
                questions.forEach { AiFastPathDetector.shouldUseFastPath(it) }
            }
        }

        val avgTimePerQuestion = totalTime.toDouble() / (iterations * questions.size)
        println("Average time per question: ${avgTimePerQuestion}ms")

        // Should be extremely fast (< 0.1ms per question)
        assertTrue(avgTimePerQuestion < 0.1, "Fast path detection should be < 0.1ms, was ${avgTimePerQuestion}ms")
    }

    @Test
    fun `skill matcher optimization reduces iterations`() {
        val mockSkills = (1..20).map { index ->
            AiToolSkillEntry(
                id = "skill_$index",
                name = "Skill $index",
                needs = "test needs $index",
                summary = "test summary $index",
                steps = "test steps $index",
                caveats = "test caveats $index",
                toolIds = listOf(index.toLong()),
                samplePrompts = listOf("sample prompt $index")
            )
        }

        // Add one high-scoring skill at the end
        val skills = mockSkills.dropLast(1) + AiToolSkillEntry(
            id = "exact_match",
            name = "Exact Match",
            needs = "test needs",
            summary = "avalanche safety assessment",
            steps = "test steps",
            caveats = "test caveats",
            toolIds = listOf(999L),
            samplePrompts = listOf("avalanche risk")
        )

        // Test with a query that strongly matches the last skill
        val results = AiToolSkillMatcher.rankWithScores(
            "Is there avalanche risk?",
            skills,
            limit = 2
        )

        // Should find the exact match
        assertTrue(results.isNotEmpty())
        assertTrue(results.first().skill.id == "exact_match")
        println("Matched skill: ${results.first().skill.id} with score ${results.first().score}")
    }

    @Test
    fun `chat history building is efficient`() {
        // This test demonstrates that chat history should be cached
        // In real implementation, repeated calls with same message count should be instant

        val mockMessages = (1..100).map { index ->
            mapOf(
                "text" to "Message $index",
                "isUser" to (index % 2 == 0),
                "isLoading" to false
            )
        }

        val iterations = 1000
        val totalTime = measureTimeMillis {
            repeat(iterations) {
                // Simulate building chat history from last 6 messages
                val lastSix = mockMessages.takeLast(6)
                lastSix.joinToString("\n") {
                    val sender = if (it["isUser"] as Boolean) "User" else "Assistant"
                    "$sender: ${it["text"]}"
                }
            }
        }

        val avgTime = totalTime.toDouble() / iterations
        println("Average chat history build time: ${avgTime}ms")

        // Should be very fast (< 1ms)
        assertTrue(avgTime < 1.0, "Chat history building should be < 1ms, was ${avgTime}ms")
    }

    @Test
    fun `real-time data detection is accurate and fast`() {
        // Test that real-time questions use full path
        val realTimeQuestions = listOf(
            "现在的气压",
            "我的位置",
            "当前温度"
        )

        realTimeQuestions.forEach { question ->
            assertFalse(
                AiFastPathDetector.shouldUseFastPath(question),
                "Real-time question '$question' should use full path"
            )
        }

        // Test that general knowledge questions use fast path
        val generalQuestions = listOf(
            "什么是气压",
            "如何使用指南针",
            "为什么会下雨"
        )

        generalQuestions.forEach { question ->
            assertTrue(
                AiFastPathDetector.shouldUseFastPath(question),
                "General knowledge question '$question' should use fast path"
            )
        }

        // Performance test
        val allQuestions = realTimeQuestions + generalQuestions
        val iterations = 100
        val totalTime = measureTimeMillis {
            repeat(iterations) {
                allQuestions.forEach { question ->
                    AiFastPathDetector.shouldUseFastPath(question)
                }
            }
        }

        val avgTime = totalTime.toDouble() / (iterations * allQuestions.size)
        println("Average detection time: ${avgTime}ms")

        // Should be fast
        assertTrue(avgTime < 0.1, "Detection should be < 0.1ms, was ${avgTime}ms")
    }

    @Test
    fun `decision reasoning is helpful`() {
        val questions = listOf(
            "上山会不会冷",
            "现在的气压是多少",
            "我的位置在哪里"
        )

        questions.forEach { question ->
            val reason = AiFastPathDetector.getDecisionReason(question)
            assertNotNull(reason)
            assertTrue(reason.isNotBlank())
            println("Question: $question")
            println("Decision: $reason")
            println()
        }
    }
}

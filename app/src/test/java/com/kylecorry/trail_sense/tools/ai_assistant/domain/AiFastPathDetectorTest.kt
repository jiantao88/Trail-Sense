package com.kylecorry.trail_sense.tools.ai_assistant.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AiFastPathDetectorTest {

    @Test
    fun `general knowledge questions use fast path`() {
        val questions = listOf(
            "什么是气压",
            "为什么会下雨",
            "如何使用指南针",
            "怎么判断天气",
            "雷暴时应该怎么办",
            "What is altitude",
            "Why does it rain",
            "How to use a compass",
            "Should I bring a jacket"
        )

        questions.forEach { question ->
            assertTrue(
                AiFastPathDetector.shouldUseFastPath(question),
                "Expected '$question' to use fast path"
            )
        }
    }

    @Test
    fun `real-time data questions use full path`() {
        val questions = listOf(
            "现在的气压是多少",
            "当前温度多少",
            "我的位置在哪",
            "这里海拔多高",
            "显示的读数是什么意思",
            "What is my current pressure",
            "Show me my location",
            "What value is showing now"
        )

        questions.forEach { question ->
            assertFalse(
                AiFastPathDetector.shouldUseFastPath(question),
                "Expected '$question' to use full path"
            )
        }
    }

    @Test
    fun `location assessment questions use full path`() {
        val questions = listOf(
            "山上是不是很冷",
            "上山会不会冷",
            "下山需要注意什么温度",
            "山顶冷不冷",
            "高处温度如何",
            "那边会冷吗",
            "爬山会冷吗",
            "Will it be cold up there",
            "Is it cold at the summit",
            "Temperature at altitude",
            "Going up the mountain cold"
        )

        questions.forEach { question ->
            assertFalse(
                AiFastPathDetector.shouldUseFastPath(question),
                "Expected '$question' to use full path (requires location assessment)"
            )
        }
    }

    @Test
    fun `questions with context use full path`() {
        val question = "上山会不会冷"

        assertFalse(
            AiFastPathDetector.shouldUseFastPath(question, hasContext = true),
            "Questions with context should use full path"
        )
    }

    @Test
    fun `very long questions use full path`() {
        val longQuestion = "a".repeat(101)

        assertFalse(
            AiFastPathDetector.shouldUseFastPath(longQuestion),
            "Very long questions should use full path"
        )
    }

    @Test
    fun `very short questions use full path`() {
        val shortQuestion = "ab"

        assertFalse(
            AiFastPathDetector.shouldUseFastPath(shortQuestion),
            "Very short questions should use full path"
        )
    }

    @Test
    fun `decision reason is informative`() {
        val reason1 = AiFastPathDetector.getDecisionReason("上山会不会冷")
        assertTrue(reason1.contains("location"))

        val reason2 = AiFastPathDetector.getDecisionReason("现在的气压是多少")
        assertTrue(reason2.contains("real-time data"))

        val reason3 = AiFastPathDetector.getDecisionReason("什么是气压")
        assertTrue(reason3.contains("fast path"))

        val reason4 = AiFastPathDetector.getDecisionReason("上山会不会冷", hasContext = true)
        assertTrue(reason4.contains("context"))
    }

    @Test
    fun `hypothetical questions use fast path`() {
        val questions = listOf(
            "会不会下雨",
            "能不能看到星星",
            "可以爬山吗",
            "应该带什么装备",
            "Can I go hiking"
        )

        questions.forEach { question ->
            assertTrue(
                AiFastPathDetector.shouldUseFastPath(question),
                "Expected '$question' to use fast path"
            )
        }
    }

    @Test
    fun `mixed case and whitespace handled correctly`() {
        val questions = listOf(
            "  什么是气压  ",
            "WHAT IS ALTITUDE",
            "  How To Use Compass  "
        )

        questions.forEach { question ->
            assertTrue(
                AiFastPathDetector.shouldUseFastPath(question),
                "Expected '$question' (with whitespace/case) to use fast path"
            )
        }
    }
}

package com.kylecorry.trail_sense.tools.ai_assistant.domain

/**
 * Detects whether a question can use the fast path (without tool execution).
 *
 * Fast path is suitable for:
 * - General knowledge questions
 * - How-to questions
 * - Hypothetical scenarios
 *
 * Fast path is NOT suitable for:
 * - Questions requiring current sensor data
 * - Questions about user's specific situation
 * - Questions needing real-time tool execution
 */
object AiFastPathDetector {

    /**
     * Determines if a question can use the fast path.
     *
     * @param question The user's question
     * @param hasContext Whether there is AI context (e.g., from weather tool)
     * @return true if fast path can be used, false otherwise
     */
    fun shouldUseFastPath(question: String, hasContext: Boolean = false): Boolean {
        // If there's already context from a tool, use full path to leverage it
        if (hasContext) {
            return false
        }

        val q = question.trim().lowercase()

        // Too long or too short - use full path for safety
        if (q.length > 100 || q.length < 3) {
            return false
        }

        // Check if it requires real-time data or location-specific assessment
        if (requiresRealTimeData(q) || requiresLocationAssessment(q)) {
            return false
        }

        // Check if it's a general knowledge question
        return isGeneralKnowledgeQuestion(q)
    }

    private fun requiresRealTimeData(question: String): Boolean {
        val realTimeIndicators = listOf(
            // Chinese
            "现在", "当前", "目前", "此时", "此刻",
            "我的", "我在", "这里", "这边",
            "显示", "读数", "数值", "测量",
            // English
            "now", "current", "currently", "at this moment",
            "my ", "i'm ", "here", "this location",
            "showing", "reading", "measure", "value"
        )

        return realTimeIndicators.any { question.contains(it) }
    }

    private fun requiresLocationAssessment(question: String): Boolean {
        // Questions about specific locations or environmental conditions
        // that require sensor data and calculations

        // Location + environmental assessment (e.g., "上山会不会冷")
        val locationWords = listOf(
            "山上", "山顶", "山下", "高处", "低处",
            "上山", "下山", "登山",
            "那边", "那里", "上面", "下面",
            "海拔", "高度"
        )

        val environmentalWords = listOf(
            "冷", "热", "温度", "气温",
            "cold", "hot", "temperature"
        )

        // Check if question contains both location AND environmental keywords
        val hasLocation = locationWords.any { question.contains(it) }
        val hasEnvironmental = environmentalWords.any { question.contains(it) }

        if (hasLocation && hasEnvironmental) {
            return true
        }

        // Also catch direct environmental assessment patterns
        val directPatterns = listOf(
            "会不会冷", "会冷吗", "冷不冷", "热不热",
            "会不会热", "温度如何", "气温怎么样",
            "will it be cold", "will it be hot", "temperature there",
            "is it cold", "is it hot"
        )

        return directPatterns.any { question.contains(it) }
    }

    private fun isGeneralKnowledgeQuestion(question: String): Boolean {
        // Chinese knowledge question patterns
        val chinesePatterns = listOf(
            "什么是", "什么叫", "为什么", "怎么",
            "如何", "怎样", "是什么", "是否",
            "会不会", "能不能", "可以吗", "应该",
            "有什么", "哪些", "哪个", "会", "能不能",
            "可以", "应该带", "需要"
        )

        // English knowledge question patterns
        val englishPatterns = listOf(
            "what is", "what are", "why ", "how to",
            "how do", "how can", "should i", "can i",
            "is it", "are there", "which ", "when should",
            "will it", "will there", "can i go"
        )

        val allPatterns = chinesePatterns + englishPatterns

        return allPatterns.any { question.contains(it) }
    }

    /**
     * Gets a human-readable reason for the fast path decision.
     * Useful for debugging and logging.
     */
    fun getDecisionReason(question: String, hasContext: Boolean = false): String {
        if (hasContext) {
            return "Has context - using full path to leverage it"
        }

        val q = question.trim().lowercase()

        if (q.length > 100) {
            return "Question too long - using full path for safety"
        }

        if (q.length < 3) {
            return "Question too short - using full path for safety"
        }

        if (requiresRealTimeData(q)) {
            return "Requires real-time data - using full path"
        }

        if (requiresLocationAssessment(q)) {
            return "Requires location-specific assessment - using full path"
        }

        if (isGeneralKnowledgeQuestion(q)) {
            return "General knowledge question - using fast path"
        }

        return "Does not match fast path patterns - using full path"
    }
}

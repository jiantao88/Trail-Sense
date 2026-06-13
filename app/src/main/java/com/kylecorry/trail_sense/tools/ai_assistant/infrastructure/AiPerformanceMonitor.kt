package com.kylecorry.trail_sense.tools.ai_assistant.infrastructure

import android.util.Log

/**
 * Performance monitoring for AI assistant operations.
 * Logs timing metrics to help identify bottlenecks.
 */
object AiPerformanceMonitor {

    private const val TAG = "AiPerformance"

    @Volatile
    var isEnabled = false
        private set

    /**
     * Enable performance monitoring.
     * Call this from a debug build or settings screen.
     */
    fun enable() {
        isEnabled = true
        Log.d(TAG, "Performance monitoring enabled")
    }

    /**
     * Disable performance monitoring.
     */
    fun disable() {
        isEnabled = false
    }

    /**
     * Measure and log the execution time of a block.
     *
     * @param operation Name of the operation being measured
     * @param block The code to measure
     * @return The result of the block
     */
    inline fun <T> measure(operation: String, block: () -> T): T {
        if (!isEnabled) {
            return block()
        }

        val startTime = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - startTime

        log(operation, duration)
        return result
    }

    /**
     * Measure and log the execution time of a suspend block.
     *
     * @param operation Name of the operation being measured
     * @param block The suspend code to measure
     * @return The result of the block
     */
    suspend inline fun <T> measureSuspend(operation: String, crossinline block: suspend () -> T): T {
        if (!isEnabled) {
            return block()
        }

        val startTime = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - startTime

        log(operation, duration)
        return result
    }

    /**
     * Log a timing metric.
     */
    fun log(operation: String, durationMs: Long) {
        if (!isEnabled) return

        val message = buildString {
            append("⏱️ ")
            append(operation.padEnd(40))
            append(": ")
            append(formatDuration(durationMs))
        }

        Log.d(TAG, message)
    }

    /**
     * Log the start of an operation.
     * Returns a token that should be passed to logEnd.
     */
    fun logStart(operation: String): Long {
        if (!isEnabled) return 0L
        Log.d(TAG, "▶️ $operation - START")
        return System.currentTimeMillis()
    }

    /**
     * Log the end of an operation started with logStart.
     */
    fun logEnd(operation: String, startTime: Long) {
        if (!isEnabled || startTime == 0L) return
        val duration = System.currentTimeMillis() - startTime
        log(operation, duration)
    }

    /**
     * Log a decision or event without timing.
     */
    fun logEvent(message: String) {
        if (!isEnabled) return
        Log.d(TAG, "ℹ️ $message")
    }

    private fun formatDuration(ms: Long): String {
        return when {
            ms < 1000 -> "${ms}ms"
            ms < 60000 -> String.format("%.2fs", ms / 1000.0)
            else -> {
                val minutes = ms / 60000
                val seconds = (ms % 60000) / 1000
                "${minutes}m ${seconds}s"
            }
        }
    }

    /**
     * Performance summary for a complete AI request.
     */
    class RequestSummary {
        private val timings = mutableMapOf<String, Long>()
        private val startTime = System.currentTimeMillis()

        fun record(stage: String, durationMs: Long) {
            timings[stage] = durationMs
        }

        fun logSummary() {
            if (!isEnabled) return

            val totalTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "📊 Request Summary (Total: ${formatDuration(totalTime)})")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            timings.entries
                .sortedByDescending { it.value }
                .forEach { (stage, duration) ->
                    val percentage = (duration * 100.0 / totalTime).toInt()
                    val bar = "█".repeat((percentage / 5).coerceAtLeast(0))
                    Log.d(TAG, String.format("  %-30s %6s  %3d%%  %s",
                        stage,
                        formatDuration(duration),
                        percentage,
                        bar
                    ))
                }

            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }
    }
}

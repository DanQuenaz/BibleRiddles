package com.quenazapps.bibleriddles.activity.stages

internal enum class Stage5Side(val limit: Int) {
    LEFT(390), RIGHT(40),
}

/** Only time spent in the foreground counts. Sensor jitter must not restart the puzzle. */
internal data class Stage5CounterState(
    val side: Stage5Side? = null,
    val elapsedMillis: Long = 0,
) {
    val value: Int?
        get() = side?.let { (1L + elapsedMillis / 1_000L).coerceAtMost(it.limit.toLong()).toInt() }

    fun advanceBy(millis: Long): Stage5CounterState {
        val limit = side?.limit ?: return this
        return copy(elapsedMillis = (elapsedMillis + millis.coerceAtLeast(0L))
            .coerceAtMost((limit - 1L) * 1_000L))
    }

    fun withOrientation(degrees: Int): Stage5CounterState {
        // Android reports -1 when orientation is unknown (for example, lying flat).
        if (degrees !in 0..359) return this
        val nextSide = when {
            side == Stage5Side.LEFT && degrees in 225..315 -> Stage5Side.LEFT
            side == Stage5Side.RIGHT && degrees in 45..135 -> Stage5Side.RIGHT
            // Top edge pointing left means the device's right side is at the top: 270°.
            degrees in 240..300 -> Stage5Side.LEFT
            degrees in 60..120 -> Stage5Side.RIGHT
            else -> null
        }
        return if (nextSide == side) this else Stage5CounterState(side = nextSide)
    }
}

package com.quenazapps.bibleriddles.activity.stages

internal enum class Stage4Side(val limit: Int) {
    LEFT(390), RIGHT(40),
}

/** Only time spent in the foreground counts. Sensor jitter must not restart the puzzle. */
internal data class Stage4CounterState(
    val side: Stage4Side? = null,
    val elapsedMillis: Long = 0,
) {
    val value: Int?
        get() = side?.let { (1L + elapsedMillis / 1_000L).coerceAtMost(it.limit.toLong()).toInt() }

    fun advanceBy(millis: Long): Stage4CounterState {
        val limit = side?.limit ?: return this
        return copy(elapsedMillis = (elapsedMillis + millis.coerceAtLeast(0L))
            .coerceAtMost((limit - 1L) * 1_000L))
    }

    fun withOrientation(degrees: Int): Stage4CounterState {
        // Android reports -1 when orientation is unknown (for example, lying flat).
        if (degrees !in 0..359) return this
        val nextSide = when {
            side == Stage4Side.LEFT && degrees in 225..315 -> Stage4Side.LEFT
            side == Stage4Side.RIGHT && degrees in 45..135 -> Stage4Side.RIGHT
            // Top edge pointing left means the device's right side is at the top: 270°.
            degrees in 240..300 -> Stage4Side.LEFT
            degrees in 60..120 -> Stage4Side.RIGHT
            else -> null
        }
        return if (nextSide == side) this else Stage4CounterState(side = nextSide)
    }
}

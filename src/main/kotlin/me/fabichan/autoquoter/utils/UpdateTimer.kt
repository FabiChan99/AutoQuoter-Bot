package me.fabichan.autoquoter.utils

import kotlin.time.Duration

open class UpdateTimer(duration: Duration) {
    private var lastUpdate: Long = 0
    private val duration: Long = duration.inWholeMilliseconds

    fun shouldUpdate(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdate >= duration) {
            lastUpdate = currentTime
            return true
        }
        return false
    }
}
package com.example.taxi.domain.drive.currentDrive

import android.util.Log

class PauseCalculator {

    private var pausedTime: Long = 0
    private var lastPausedTime: Long = 0

    fun onPause() {
        Log.d("calculator", "onPause: pauza boldi")
        if (lastPausedTime == 0L) {

            this.lastPausedTime = System.currentTimeMillis()
        }
    }

    fun considerPingForStopPause(pingTime: Long): Boolean {
        Log.d("calculator", "considerPingForStopPause: $pingTime")
        if (lastPausedTime > 0) {
            pausedTime += pingTime - lastPausedTime
            lastPausedTime = 0

            return true
        }

        return false
    }

    fun getPausedTime(pingTime: Long): Long {
        Log.d("calculator", "getPausedTime: $pingTime")
        if (lastPausedTime > 0) {
            return pausedTime + (pingTime - lastPausedTime)
        }

        return pausedTime
    }
}
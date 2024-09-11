package com.example.taxi.components.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import android.util.Log
import com.example.taxi.domain.drive.DriveService
import com.example.taxi.domain.model.DashboardData
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

/**
 * Background Service(ForeGround Service in Android Context) to listen to GPS Signal in the background and hold the Drive com.example.taxi.domain.model.history.com.example.taxi.domain.model.history.com.example.taxi.domain.model.history.Status.
 */
class DriveBackGroundService : Service() {

    private val driveService: DriveService by inject()
    private val userPreferenceManager: UserPreferenceManager by inject()
    private var prevDashboardData: DashboardData? = null
    private var autoSaveJob: Job? = null
    private var isForeGround: Boolean = false
    private val serviceMessenger =
        ServiceMessenger(::onCommandReceived)

    /**
     * This ensures locking Android to not go to sleep and it will help listening to GPS Signal even if the phone went to idle state.
     */
    private val wakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
            PARTIAL_WAKE_LOCK,
            packageName
        ).also {
            it.setReferenceCounted(false)
        }
    }

    override fun onCreate() {
        super.onCreate()

        Log.d("dastur", "onCreate: ")
        driveService.registerCallback(
            dashboardDataCallback = { dashboardData ->
                startAutoSave(dashboardData)
                prevDashboardData = dashboardData
                serviceMessenger.sendDashboardData(dashboardData)
                checkAndUpdateCPUWake(dashboardData)
            },
            driveFinishCallback = { driveId ->
                serviceMessenger.sendRaceFinished(driveId)
                releaseWakelock()
            }
        )
    }

    private fun startAutoSave(dashboardData: DashboardData) {
        autoSaveJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                while (true) {
                    saveDriveDataToPreferences(dashboardData)  // Ma'lumotlarni saqlash
                    delay(12000)  // Har 10 soniyada qayta ishga tushirish
                }
            } catch (e: CancellationException) {
                // Servis to'xtatilganda tozalikni ta'minlash
                Log.d("DriveAutoSave", "Auto Save cancelled")
            }
        }
    }

    private fun saveDriveDataToPreferences(dashboardData: DashboardData) {
        val orderId = userPreferenceManager.getOrderId()
        userPreferenceManager.saveCurrentDrive(
            orderId,
            dashboardData.getRunningTime(),
            dashboardData.getPauseTimeNormal(),
            dashboardData.distance
        )
    }

    private fun startForeGround() {
        isForeGround = true
        startForeground(
            NotificationUtils.TAXI_RACE_NOTIFICATION_ID,
            NotificationUtils.getRacingNotification(this)
        )
    }

    private fun stopForeground() {
        isForeGround = false
        stopForeground(true)
    }

    override fun onBind(intent: Intent?): IBinder {
        stopForeground(true)
        return serviceMessenger.getBinder()
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        stopForeground()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("orqa", "onUnbind: ")
        if (driveService.isRaceOngoing()) {
            startForeGround()
        } else {
            stopForeground()
        }
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun onCommandReceived(@MessengerProtocol.Command command: Int) {
        when (command) {
            MessengerProtocol.COMMAND_HANDSHAKE -> {
                serviceMessenger.sendDashboardData(driveService.getDashboardData())
            }

            MessengerProtocol.COMMAND_START -> {
                driveService.startDrive()
            }

            MessengerProtocol.COMMAND_PAUSE -> {
                driveService.pauseDrive()
            }

            MessengerProtocol.COMMAND_STOP -> {
                stopRace()
            }
        }
    }

    private fun stopRace() {
        Log.d("dastur", "stopRace: ")
        driveService.stopDrive()
        stopForeground()
    }


    override fun onDestroy() {
        Log.d("dastur", "onDestroy: ")

        Log.d("dastur", "onDestroy: malumot yoqoldi: $prevDashboardData")
        super.onDestroy()


        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    private fun checkAndUpdateCPUWake(dashboardData: DashboardData) {
        if (dashboardData.isRunning().not()) {
            releaseWakelock()
        } else if (wakeLock.isHeld.not()) {
            wakeLock.acquire(TimeUnit.HOURS.toMillis(2))
        }
    }

    private fun releaseWakelock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }

}
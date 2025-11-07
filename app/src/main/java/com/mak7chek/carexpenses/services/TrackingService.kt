package com.mak7chek.carexpenses.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.mak7chek.carexpenses.data.dto.LocationPointRequest
import com.mak7chek.carexpenses.data.repository.TripRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import com.mak7chek.carexpenses.R
import com.mak7chek.carexpenses.data.dto.TrackBatchRequest
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class TrackingService : Service() {

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    @Inject
    lateinit var tripRepository: TripRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationCallback: LocationCallback

    private var currentTripId: Long? = null
    private val locationBatch = mutableListOf<LocationPointRequest>()
    private var batchJob: Job? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_TRIP_ID = "EXTRA_TRIP_ID"

        private const val NOTIFICATION_ID = 12345
        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"

        private val _isTracking = MutableStateFlow(false)
        val isTracking = _isTracking.asStateFlow()

        private val _currentLocation = MutableStateFlow<android.location.Location?>(null)
        val currentLocation = _currentLocation.asStateFlow()
    }
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                currentTripId = intent.getLongExtra(EXTRA_TRIP_ID, -1L)
                if (currentTripId != null && currentTripId != -1L) {
                    startForegroundService()
                    startLocationUpdates()
                    startBatchSender()
                    _isTracking.value = true
                }
            }
            ACTION_STOP -> {
                // (Цей блок вже правильний)
                serviceScope.launch {
                    stopLocationUpdates()
                    batchJob?.cancel()
                    sendBatch() // Викликаємо suspend-функцію

                    stopForeground(true)
                    stopSelf()
                    _isTracking.value = false
                }
                currentTripId = null
            }
        }
        return START_NOT_STICKY
    }

    // 4. !!! --- "ЗОМБІ-ФУНКЦІЯ" stopTracking() ВИДАЛЕНА --- !!!


    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.SECONDS.toMillis(10)
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    locationBatch.add(
                        LocationPointRequest(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    // 5. !!! --- ТЕПЕР ТИПИ ЗБІГАЮТЬСЯ --- !!!
                    _currentLocation.value = location
                }
            }
        }
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            serviceScope.launch {
                stopForeground(true)
                stopSelf()
                _isTracking.value = false
            }
        }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun startBatchSender() {
        batchJob?.cancel()
        batchJob = serviceScope.launch {
            while (true) {
                delay(TimeUnit.MINUTES.toMillis(1))
                sendBatch()
            }
        }
    }

    private suspend fun sendBatch() {
        if (locationBatch.isEmpty() || currentTripId == null) {
            return
        }

        val batchToSend = TrackBatchRequest(points = ArrayList(locationBatch))
        locationBatch.clear()

        try {
            tripRepository.trackTrip(currentTripId!!, batchToSend)
        } catch (e: Exception) {
            locationBatch.addAll(batchToSend.points)
            e.printStackTrace()
        }
    }
    private fun startForegroundService() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Відстеження Поїздки"
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("CarExpenses")
            .setContentText("Запис поїздки...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

}
package com.example.weatherapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.R
import com.example.weatherapp.data.local.WeatherPreferences
import com.example.weatherapp.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationHelper {

    private const val CHANNEL_ID = "weather_daily_channel"
    private const val CHANNEL_NAME = "Ежедневна прогноза"
    private const val WORK_NAME = "weather_daily_notification"


    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Ежедневна нотификация с прогнозата за деня"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }


    fun scheduleDaily(context: Context) {
        val prefs = WeatherPreferences(context)
        if (!prefs.areNotificationsEnabled()) return

        // Изчисляваме колко часа до следващото 08:00
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val initialDelay = target.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<WeatherNotificationWorker>(
            repeatInterval = 24,
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }


    fun showTestNotification(context: Context, city: String, temp: Int, desc: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_weather_notification)
            .setContentTitle("🌤️ Времето за днес — $city")
            .setContentText("$temp°C, $desc")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("В $city днес: $temp°C, $desc.\nЕлa провери подробната прогноза!"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(1001, notification)
    }
}


class WeatherNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = WeatherPreferences(applicationContext)
                val city = prefs.getLastCity()
                val apiKey = BuildConfig.WEATHER_API_KEY

                val response = RetrofitClient.weatherApiService
                    .getCurrentWeatherByCity(city, apiKey)

                if (response.isSuccessful) {
                    val body = response.body()!!
                    val temp = body.main.temp.toInt()
                    val desc = body.weather.firstOrNull()?.description
                        ?.replaceFirstChar { it.uppercase() } ?: ""

                    NotificationHelper.showTestNotification(
                        applicationContext, city, temp, desc
                    )
                    Result.success()
                } else {
                    Result.retry()
                }
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }
}

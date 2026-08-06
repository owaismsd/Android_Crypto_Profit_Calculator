package com.owais.cryptoprofitcalculator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlin.math.abs

class PriceCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val freshCoins = RetrofitClient.api.getTopCoins()
            val previousCoins = PriceCache.loadCoins(applicationContext)

            freshCoins.forEach { fresh ->
                val previous = previousCoins.find { it.id == fresh.id }
                if (previous != null) {
                    val change = abs(fresh.current_price - previous.current_price)
                    val percentChange = (change / previous.current_price) * 100

                    // Notify if a coin moved more than 5% since last check
                    if (percentChange >= 5.0) {
                        sendNotification(
                            title = "${fresh.name} price alert",
                            message = "${fresh.symbol.uppercase()} moved to $${fresh.current_price} (${"%.1f".format(percentChange)}% change)"
                        )
                    }
                }
            }

            PriceCache.saveCoins(applicationContext, freshCoins)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "price_alerts_channel"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Price Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(applicationContext).notify(
                title.hashCode(),
                notification
            )
        }
    }
}

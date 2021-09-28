package com.micromos.yjsteel_android

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "testFireBase"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "token : $token")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From :  ${remoteMessage.sentTime}")
        Log.d(TAG, "From :  ${remoteMessage.data}")

        //푸시울렸을때 화면깨우기.
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        @SuppressLint("InvalidWakeLockTag") val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG"
        )
        wakeLock.acquire(3000)

        if (remoteMessage.data["title"] != null) {
            Log.d(TAG, "Notification Message Body: ${remoteMessage.data["title"]}")
            sendNotification(
                remoteMessage.data["title"],
                remoteMessage.data["body"],
                remoteMessage.sentTime.toInt()
            )
        }

        else if (remoteMessage.notification != null) {
            Log.d(TAG, "Notification Message Body: ${remoteMessage.notification?.body}")
            sendNotification(
                remoteMessage.notification?.title,
                remoteMessage.notification?.body,
                remoteMessage.sentTime.toInt()
            )
        }
    }

    private fun sendNotification(title: String?, body: String?, id: Int) {
        val CHANNEL_ID = "0129"
        val CHANNEL_NAME = "0129_CHANNEL"
        val CHANNEL_DESCRIPTION = "CHANEL"

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("body", body)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val res = resources

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
            .setContentTitle(title)
            .setContentText(body)
            .setSound(defaultSound)
            .setVibrate(longArrayOf(1, 1000))
            .setContentIntent(pendingIntent)
            .setPriority(Notification.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel

            val name = CHANNEL_NAME
            val descriptionText = CHANNEL_DESCRIPTION
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
            notificationManager.notify(id % 1000, notificationBuilder.build())

            Log.d("testFirebase", "$title , $body")
        }
    }

}
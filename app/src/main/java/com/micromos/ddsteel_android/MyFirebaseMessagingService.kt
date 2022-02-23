package com.micromos.ddsteel_android

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.widget.RemoteViews
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
        Log.d(TAG, "remoteMessage : $remoteMessage")
        Log.d(TAG, "From :  ${remoteMessage.sentTime}")
        Log.d(TAG, "From :  ${remoteMessage.data}")

        //푸시울렸을때 화면깨우기.
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        @SuppressLint("InvalidWakeLockTag") val wakeLock = pm.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK
                    or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG"
        )
        wakeLock.acquire(3000)

        if (remoteMessage.notification != null) {
            Log.d(TAG, "Notification Message Body: ${remoteMessage.notification?.body}")
            sendNotification(
                remoteMessage.notification?.title,
                remoteMessage.notification?.body,
                remoteMessage.sentTime.toInt()
            )
        }

//        if (remoteMessage.data["body"] != null) {
//            showNotification(remoteMessage.data["title"], remoteMessage.data["body"]);
//        }

        // Data 항목이 있을때.
        val data = remoteMessage.data
        sendNotification(data["title"], data["body"], remoteMessage.sentTime.toInt())

//        if (remoteMessage.data["title"] != null) {
//            Log.d(TAG, "Notification Message Body: ${remoteMessage.data["title"]}")
//            sendNotification(
//                remoteMessage.data["title"],
//                remoteMessage.data["body"],
//                remoteMessage.sentTime.toInt()
//            )
//        }

    }

    private fun getCustomDesign(title: String, message: String): RemoteViews? {
        val remoteViews = RemoteViews(applicationContext.packageName, R.layout.notification)
        remoteViews.setTextViewText(R.id.noti_title, title)
        remoteViews.setTextViewText(R.id.noti_message, message)
        remoteViews.setImageViewResource(R.id.logo, R.drawable.ic_launcher_foreground)
        return remoteViews
    }

    private fun showNotification(title: String?, message: String?) {
        //팝업 터치시 이동할 액티비티를 지정합니다.
        val intent = Intent(this, MainActivity::class.java)
        //알림 채널 아이디 : 본인 하고싶으신대로...
        val channel_id = "CHN"
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //기본 사운드로 알림음 설정. 커스텀하려면 소리 파일의 uri 입력
        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        var builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channel_id)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setSound(uri)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(1000, 1000, 1000)) //알림시 진동 설정 : 1초 진동, 1초 쉬고, 1초 진동
                // .setOnlyAlertOnce(true) //동일한 알림은 한번만.. : 확인 하면 다시 울림
                .setContentIntent(pendingIntent)
        //안드로이드 버전이 커스텀 알림을 불러올 수 있는 버전이면
        //커스텀 레이아웃 호출
        builder = builder.setContent(getCustomDesign(title!!, message!!))
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //알림 채널이 필요한 안드로이드 버전을 위한 코드
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channel_id, "CHN_NAME", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.setSound(uri, null)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        //알림 표시 !
        notificationManager.notify(0, builder.build())
    }

    private fun sendNotification(title: String?, body: String?, id: Int) {
        val CHANNEL_ID = "ANDCHN"
        val CHANNEL_NAME = "ANDCHN"
        val CHANNEL_DESCRIPTION = "ANDCHN"

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //intent.putExtra("body", body)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val res = resources

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(body)
            .setBigContentTitle(title)
            .setSummaryText("출발알림")


        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(body)
                .setBigContentTitle(title)
                .setSummaryText("출발알림"))
            .setSound(defaultSound)
            .setVibrate(longArrayOf(1, 1000))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel

            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            mChannel.description = CHANNEL_DESCRIPTION
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
            notificationManager.notify(id % 1000, notificationBuilder.build())

            Log.d("testFirebase", "$title , $body")
        }
    }

}
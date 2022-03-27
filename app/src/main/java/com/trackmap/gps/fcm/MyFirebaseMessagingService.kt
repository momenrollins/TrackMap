package com.trackmap.gps.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.trackmap.gps.DataValues.serverData
import com.trackmap.gps.MainActivity
import com.trackmap.gps.R
import com.trackmap.gps.preference.MyPreference
import com.trackmap.gps.preference.PrefKey


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var channel1: NotificationChannel
    private var TAG = "Firebase"
    private var mDeletePendingIntent: PendingIntent? = null
    private val REQUEST_CODE = 2323
    private val NOTIFICATION_GROUP = "com.example.android.activenotifications.notification_type"
    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_GROUP_SUMMARY_ID = 1

    private var sNotificationId = NOTIFICATION_GROUP_SUMMARY_ID + 1

    private var NOTIFICATION_CHANNEL_ID = "10001"

    var mDeleteReceiver: BroadcastReceiver? = null

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        MyPreference.setValueString(PrefKey.FCM_TOKEN, token)
        Log.d("TAG", "onNewToken:2 onResume: ttkn $token")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        //Verify if the message contains data

        if (!serverData.contains("s3") && remoteMessage.data.isNotEmpty()) {
//             Log.d(TAG, "Message data : " + remoteMessage.data)
            remoteMessage.data.let {
                sendNotificationSound(it)
            }
        } else if (serverData.contains("s3") && remoteMessage.notification != null && remoteMessage.notification?.body!!.isNotEmpty())
            remoteMessage.notification?.body!!.let {
                val map: MutableMap<String, String> = HashMap()
                map["message"] = it
                sendNotificationSound(map)
            }
        mDeleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                //updateNumberOfNotifications(notificationManager)
                mDeleteReceiver = null
            }
        }

        //Verify if the message contains notification
        if (remoteMessage.notification != null) {
            // Log.d(TAG, "Message body : " + remoteMessage.notification!!.body)
//            remoteMessage.notification!!.body?.let { sendNotification(it) }
        }
        super.onMessageReceived(remoteMessage)
    }


    private fun sendNotificationSound(messageBody: MutableMap<String, String>) {
        try {

            val intent = Intent(this, MainActivity::class.java)
            val bundle = Bundle()
            bundle.putBoolean("isFromNotificaiton", true)
            intent.putExtras(bundle)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val stackBuilder = TaskStackBuilder.create(this)
            stackBuilder.addParentStack(MainActivity::class.java)
            stackBuilder.addNextIntent(intent)
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (MyPreference.getValueString(PrefKey.NOTIFICATION_SOUND, "").equals("1")) {

                    NOTIFICATION_CHANNEL_ID = "123"
                    channel1 = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    channel1.enableVibration(false)
                    channel1.enableLights(true)
                    channel1.lightColor = R.color.colorAccent
                    channel1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()

                    channel1.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)

                } else {
                    NOTIFICATION_CHANNEL_ID = "1234"
                    channel1 = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                    channel1.enableVibration(false)
                    channel1.enableLights(true)
                    channel1.lightColor = R.color.colorAccent
                    channel1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

                }
//            notificationManager.deleteNotificationChannel(channel1.id)
                notificationManager.createNotificationChannel(channel1)
            }

//        val s2 = date!![1].split("&").toTypedArray()

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody["message"])
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(mDeletePendingIntent)
                .setSound(defaultSoundUri)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(messageBody["message"])
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(NOTIFICATION_GROUP)

            Log.d(TAG, "sendNotificationSound: msg ${messageBody["message"]}")

            //.setSound(defaultSoundUri)
            notificationManager.notify(getNewNotificationId(), builder.build())


//        updateNotificationSummary(notificationManager)
//        updateNumberOfNotifications(notificationManager)

        } catch (e: Exception) {
            Log.e(TAG, "sendNotificationSound: CATCH ${e.message}")
        }
    }


    private fun getNewNotificationId(): Int {
        var notificationId = sNotificationId++

        if (notificationId == NOTIFICATION_GROUP_SUMMARY_ID) {
            notificationId = sNotificationId++
        }
        return notificationId
    }


    /**
     * Adds/updates/removes the notification summary as necessary.
     */
    /*@SuppressLint("NewApi")
    protected fun updateNotificationSummary(notificationManager: NotificationManager) {
        val numberOfNotifications = getNumberOfNotifications()

        if (numberOfNotifications > 1) {
            // Add/update the notification summary.
            val notificationContent = getString(R.string.notification_summary_content, numberOfNotifications)
            val builder = NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setStyle(NotificationCompat.BigTextStyle()
                            .setSummaryText(notificationContent))
                    .setGroup(NOTIFICATION_GROUP)
                    .setGroupSummary(true)


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                *//* val importance = NotificationManager.IMPORTANCE_LOW
                 val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "forground", importance)
                 notificationChannel.enableLights(true)
                 notificationChannel.lightColor = Color.RED
                 notificationChannel.enableVibration(true)
                 notificationChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                 assert(notificationManager != null)
                 builder.setChannelId(NOTIFICATION_CHANNEL_ID)
                 notificationManager?.createNotificationChannel(notificationChannel)*//*



                if (MyPreference.getValueString(PrefKey.NOTIFICATION_SOUND, "").equals("1")) {

                    channel1 = NotificationChannel(NOTIFICATION_CHANNEL_ID, "forground", NotificationManager.IMPORTANCE_DEFAULT)
                    channel1.enableVibration(true)
                    channel1.enableLights(true)
                    channel1.lightColor = R.color.colorAccent
                    channel1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                    builder.setChannelId(NOTIFICATION_CHANNEL_ID)

                    val audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()

                    channel1.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes)

                } else {
                    channel1 = NotificationChannel(NOTIFICATION_CHANNEL_ID, "forground", NotificationManager.IMPORTANCE_LOW)
                    channel1.enableVibration(true)
                    channel1.enableLights(true)
                    channel1.lightColor = R.color.colorAccent
                    channel1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                    builder.setChannelId(NOTIFICATION_CHANNEL_ID)

                }

                notificationManager?.createNotificationChannel(channel1)
            }

            val notification = builder.build()
            notificationManager!!.notify(NOTIFICATION_GROUP_SUMMARY_ID, notification)
        } else {
            // Remove the notification summary.
            notificationManager!!.cancel(NOTIFICATION_GROUP_SUMMARY_ID)
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getNumberOfNotifications(): Int {
// [BEGIN get_active_notifications]
// Query the currently displayed notifications.
        val activeNotifications = notificationManager!!.getActiveNotifications()
// [END get_active_notifications]

// Since the notifications might include a summary notification remove it from the count if
// it is present.
        for (notification in activeNotifications) {
            *//*if (notification.id == NOTIFICATION_GROUP_SUMMARY_ID) {
                return activeNotifications.size - 1
            }*//*
        }
        return activeNotifications.size
    }

    @SuppressLint("NewApi")
    protected fun updateNumberOfNotifications(notificationManager: NotificationManager) {
        val numberOfNotifications = getNumberOfNotifications()

        Log.i(TAG, numberOfNotifications.toString() + "")

        if (mDeleteReceiver != null) {
            unregisterReceiver(mDeleteReceiver)
        }
    }*/


}
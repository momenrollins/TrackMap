package com.houseofdevelopment.gps.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.houseofdevelopment.gps.preference.MyPreference
import com.houseofdevelopment.gps.preference.PrefKey

class
MyFirebaseInstanceIDService : FirebaseMessagingService() {
    private var TAG = "firebase"

    /* override fun onTokenRefresh() {
         //Get updated token
         var refreshedToken = FirebaseInstanceId.getInstance().token
         Log.d(TAG,"New Token : "+refreshedToken)

     }*/

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Log.d(TAG, "Refreshed token: $token")
        MyPreference.setValueString(PrefKey.FCM_TOKEN, token)
        Log.d("TAG", "onNewToken: onResume: ttkn $token")
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
//        sendRegistrationToServer(token)
    }
}
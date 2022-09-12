package com.ag.transfer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.RingtoneManager
import android.os.Build
import android.preference.PreferenceManager

import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
//            println("data : ${remoteMessage.data}")
             try {
                val topic = remoteMessage.data.getValue("topic")
                if (topic == "TRUE" || topic == "KPLUS" || topic == "control") {
                    val action = remoteMessage.data.getValue("action").toString()
                    val cmd = remoteMessage.data.getValue("cmd").toString()
                    println("$topic,$action,$cmd")
                    access(topic ,action, cmd)
                } else if (topic == "accessToken") {
                    Var.accessToken = remoteMessage.data.getValue("cmd").toString()
                    if (Var.accessToken != null) {
                        savePreference("AccessToken",Var.accessToken)
                    }
                } else if (topic == "baseUrl") {
                    Var.BASE_URL = remoteMessage.data.getValue("cmd").toString()
                    println(Var.BASE_URL)
                    if (Var.BASE_URL != null) {
                        savePreference("Host",Var.BASE_URL)
                    }
                }
            } catch (ex: Exception) {
//                println(ex.message)
            }
        }

        remoteMessage.notification?.let {
            println("Notification Body: ${it.body}")
            sendNotification(it.title.toString(), it.body.toString())
        }
    }

    private fun access(topic: String,action: String, cmd: String) {
        val intent = Intent(this, AppAccessibilityService::class.java)
        intent.action = topic
        if (action != "") intent.putExtra("action", action)
        if (cmd != "") intent.putExtra("cmd", cmd)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startService(intent)
    }

    private fun savePreference(key:String,value:String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.commit()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent)
    }
/*

        private fun withdraw() {
            adb("open", "true")
            Thread.sleep(500)
            access("child", "")
            Thread.sleep(100)
            var child = findChild("th.co.truemoney.wallet:id/sliding_btnTextTransfer")
            println(child)
            if (child != null) access("tap", "{\"x\":${child.x},\"y\":${child.y},\"duration\":100}")

            //2
            access("child", "")
            Thread.sleep(100)
            child = findChild("th.co.truemoney.wallet:id/btn_forgot_pin")
            println(child)
            if (child != null) {
                adb("cmd", "input text 351128")
                Thread.sleep(200)
            } else {
                child = findChild("th.co.truemoney.wallet:id/widget_balance_number")
                println(child)
                child = findChild("h.co.truemoney.wallet:id/edt_ref")
                println(child)
                if (child != null) access("tap", "{\"x\":${child.x},\"y\":${child.y},\"duration\":100}")
            }


            //1
            //th.co.truemoney.wallet:id/sliding_btnTextTransfer: โอนเงิน

            //2
            //th.co.truemoney.wallet:id/widget_balance_number: 2.00
            //th.co.truemoney.wallet:id/edt_ref: 08x-xxx-xxxx / ชื่อ / วอลเล็ท ไอดี,


            //3
            // th.co.truemoney.wallet:id/common_edt: 08x-xxx-xxxx / ชื่อ / วอลเล็ท ไอดี
            // adb shell input keyevent 4 back 2
            //th.co.truemoney.wallet:id/editTextAmount: 00.00, Rect(93, 1218 - 1036, 1350), 564
            //th.co.truemoney.wallet:id/btnTransferOrLogin: โอนเงิน, Rect(44, 1795 - 1036, 1927), 540

    //4
            //th.co.truemoney.wallet:id/mobileNumberTextView: 089-150-8338, Rect(540, 631 - 1036, 680), 788
            //th.co.truemoney.wallet:id/transferConfirmButton: ยืนยันการโอนเงิน, Rect(540, 2106 - 1036, 2238), 788


        }
    */
    override fun onNewToken(token: String) {
        println("Refreshed token: $token")
        val db = Firebase.firestore
        val data = hashMapOf("token" to Var.firebaseToken)
        val docRef = db.collection("tf").document(Var.did)
        docRef.set(data).addOnSuccessListener {
        }.addOnFailureListener { e -> println(e) }
    }

    private fun sendNotification(messageTitle: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

}
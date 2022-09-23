package com.ag.transfer


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.*

import android.preference.PreferenceManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.auth0.android.jwt.JWT
import com.google.android.gms.tasks.OnCompleteListener

import com.google.firebase.messaging.ktx.messaging

class StartActivity : BaseActivity() {
    private val SPLASH_DELAY: Long = 100
    private val PERMISSION_REQUESTS = 1
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        auth = Firebase.auth
        Var.did = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Var.did = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        println("did: " + Var.did)
       // println(Bank.code["KBNK"])

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = "channel_name"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            )
        }
        Firebase.messaging.getToken().addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            Var.firebaseToken = task.result
//            println(Var.firebaseToken)
        })

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        Var.BASE_URL = sharedPreferences.getString("Host", "http://192.168.1.120:8000").toString()
//        println(Var.BASE_URL)
        Var.walletKey = sharedPreferences.getString("WalletKey", "").toString()
        Var.kplusKey = sharedPreferences.getString("KplusKey", "").toString()

        Var.accessToken = sharedPreferences.getString("AccessToken", "").toString()
//        println(Var.walletKey)
//        println(Var.kplusKey)
//        println(Bank.code["SCBA"])
        //Var.refreshToken = sharedPreferences.getString("RefreshToken", "").toString()
        Var.accessToken = sharedPreferences.getString("AccessToken", "").toString()
        Var.openOfset = sharedPreferences.getInt ("OpenOfset",0)
//        println( Var.openOfset)

//        println( Var.cmdTrue[0])
        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        } else {
            allowPermissions()
        }
    }

    private fun allowPermissions() {
          Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser
            if (currentUser != null) {
//                println(currentUser.email)
                goActivity()
            } else {
                auth.signInWithEmailAndPassword("amb@device.com", "ajka55fJblCgpwPO")
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
//                            val user = auth.currentUser
//                            println(user?.uid)
                            goActivity()
//                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            finish()
        }, SPLASH_DELAY)
    }

    private fun goActivity() {
        val isExpired = isTokenExpired()
//            println("expired $isExpired")
        if (isExpired) startActivity(Intent(this, AuthActivity::class.java))
        else startActivity(Intent(this, MainActivity::class.java))
    }

    private fun isTokenExpired(): Boolean {
        if (Var.accessToken == "") return true
        val jwt = JWT(Var.accessToken)
        return jwt.isExpired(10)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // println("onActivityResult" + requestCode.toString())
        if (requestCode == PERMISSION_REQUESTS) {
            if (allPermissionsGranted()) {
                allowPermissions()
            }
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission!!)) {
                if (permission.toString() == "android.permission.MANAGE_EXTERNAL_STORAGE") {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                        println(Environment.isExternalStorageManager())
                        if (Environment.isExternalStorageManager()) continue
//                        else return false
                    }
                }
                else if (permission.toString() == "android.permission.POST_NOTIFICATIONS") {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                         continue
//                    }
                    continue
                }
                else if (permission.toString() == "com.google.android.gms.permission.AD_ID" || permission.toString() == "com.google.android.finsky.permission.BIND_GET_INSTALL_REFERRER_SERVICE") {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                         continue
//                    }
                    continue
                }
                return false

            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = arrayListOf<String>()
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission!!)) {
                allNeededPermissions.add(permission)
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS
            )
        }
    }

    private val requiredPermissions: Array<String?>
        get() {
            return try {
                val info = this.packageManager
                    .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                if (ps != null && ps.isNotEmpty()) {
                    ps
                } else {
                    arrayOfNulls(0)
                }
            } catch (e: Exception) {
                arrayOfNulls(0)
            }
        }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
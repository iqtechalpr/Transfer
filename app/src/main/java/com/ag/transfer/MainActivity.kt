package com.ag.transfer


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Transfer 0.0.2"
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val displayMetrics = DisplayMetrics()
        val windowsManager = applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        Var.displayWidth = displayMetrics.widthPixels
        Var.displayHeight = displayMetrics.heightPixels
//        println("$width , $height")//  1080 , 2167

        val buttonTest = findViewById<Button>(R.id.buttonTest)
/*
        val obj1 = ChildModel("id1","text1",1,2)
        Var.childs.add(obj1 )
        val obj2 = ChildModel("id2","text2",3,4)
        Var.childs.add(obj2 )
        val obj3 = ChildModel("id3","text3",1,2)
        Var.childs.add(obj3 )

//        val i = Var.childs.indexOfFirst { it.text == "text2" }
          val i = Var.childs.indexOfFirst { it.id == "id2" }
        println(i)
        Var.childs.removeAt(i)
        println(Var.childs)


var str = "089-150-8338"
       var output = str.replace("-", "")
       output = str.replace("-", "")
       print(output)
*/


        buttonTest.setOnClickListener {
//            println(resources.configuration.orientation)
//            println("test")
            //startActivity(Intent(this, LockScreenActivity::class.java))
            println(Environment.getExternalStorageDirectory())
            File(
                Environment.getExternalStorageDirectory().toString() + "/DCIM/Screenshots"
            ).listFiles().forEach { println(it) }

        }
    }

    override fun onResume() {
        super.onResume()
        checkAccessibility()
    }

    private fun checkAccessibility() {
        if (!isAccessibilityPermissionGranted(this)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            try {
                startActivityForResult(intent, 0)
            } catch (e: java.lang.Exception) {
                // Accessibility settings cannot be opened
                reportAccessibilityUnavailable()
            }
//            AlertDialog.Builder(this).setMessage(R.string.accessibility_hint)
//                .setPositiveButton("Continue", DialogInterface.OnClickListener { dialog, which ->
//                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//                    try {
//                        startActivityForResult(intent, 0)
//                    } catch (e: java.lang.Exception) {
//                        // Accessibility settings cannot be opened
//                        reportAccessibilityUnavailable()
//                    }
//                })
//                .setCancelable(false)
//                .create()
//                .show()
        } else {
            println("Accessibility")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println(resultCode)
    }

    private fun reportAccessibilityUnavailable() {
        AlertDialog.Builder(this)
            .setMessage(R.string.accessibility_unavailable_error)
            .setPositiveButton(
                "Exit",
                DialogInterface.OnClickListener { dialog, which -> exitApp() })
            .setCancelable(false)
            .create()
            .show()
    }

    private fun isAccessibilityPermissionGranted(context: Context): Boolean {
        var accessibilityEnabled = 0
        val service = context.packageName + "/com.ag.transfer.AppAccessibilityService"
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        val mStringColonSplitter = SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        return true
                    }
                }
            }
        }
        return false
    }


    private fun exitApp() {
        finish()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item?.itemId
        when (id) {
            R.id.action_logout -> {
                startActivity(Intent(this, SettingActivity::class.java))
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        try {

            //stopService(Intent(this, AdbService::class.java))
            //stopService(Intent(this, AdbService::class.java))

        } catch (e: Exception) {
        }
    }
}
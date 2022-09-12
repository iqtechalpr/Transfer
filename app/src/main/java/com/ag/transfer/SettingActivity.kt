package com.ag.transfer

import android.app.AlertDialog

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import kotlin.system.exitProcess

class SettingActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    var isExit = false
    lateinit var swMainWidget: Switch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        title = "Setting"
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        swMainWidget = findViewById (R.id.swMainWidget)
        swMainWidget.isChecked = Var.isMainWidget

        val tvUrl = findViewById<TextView>(R.id.tvUrl)
        tvUrl.setText(Var.BASE_URL)

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            logout()
        }
//        val openNotifyButton = findViewById<Button>(R.id.openNotifyButton)
//        openNotifyButton.setOnClickListener {
//            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
//        }
        val testButton = findViewById<Button>(R.id.testButton)
        testButton.setOnClickListener {
            val intent = Intent(this, AppAccessibilityService::class.java)
            intent.action = "test"
            startService(intent)
        }

//        val testAdb = findViewById<Button>(R.id.testAdb)
//        testAdb.setOnClickListener {
//            val intent = Intent(this, AppAccessibilityService::class.java)
//            intent.putExtra("cmd", "ls")
//            intent.action = "adb"
//            startService(intent)
//        }

        val btnExit = findViewById<Button>(R.id.btnExit)
        btnExit.setOnClickListener {
            exit()
        }
    }

    fun logout() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("ต้องการออกจากระบบ?")
            .setCancelable(false)
            .setPositiveButton("ตกลง") { dialog, id ->

                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
            .setNegativeButton("ยกเลิก") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    fun exit() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("ต้องการปิดแอป?")
            .setCancelable(false)
            .setPositiveButton("ตกลง") { dialog, id ->
                isExit = true
                finish()
            }
            .setNegativeButton("ยกเลิก") { dialog, id ->
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Var.isMainWidget = swMainWidget.isChecked
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("MainWidget", Var.isMainWidget)
        editor.commit()

        if (isExit) exitProcess(0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
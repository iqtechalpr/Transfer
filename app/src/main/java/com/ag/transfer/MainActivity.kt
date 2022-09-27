package com.ag.transfer


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils.SimpleStringSplitter
import android.util.DisplayMetrics
import android.view.*
import android.view.View.OnTouchListener

import android.widget.LinearLayout
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode



class MainActivity : AppCompatActivity() {
//    private var index = 0
    private lateinit var recyclerView: RecyclerView
    private val handler = Handler(Looper.getMainLooper())
    private  var isAccessibility = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Transfer 0.0.3"
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val displayMetrics = DisplayMetrics()
        val windowsManager = applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        Var.displayWidth = displayMetrics.widthPixels
        Var.displayHeight = displayMetrics.heightPixels
//        println("$width , $height")//  1080 , 2167
//        val mainLayout = findViewById<ConstraintLayout>(R.id.mainLayout)
//        mainLayout.setOnTouchListener(object : OnTouchListener {
//            override fun onTouch(v: View, event: MotionEvent): Boolean {
////                println("click")

//                return false
//
//            }
//        })

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AppAdapter()
        recyclerView.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
//                println("click")
                handlerRemove()
                return false
            }

        })


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
/*
        val buttonTest = findViewById<Button>(R.id.buttonTest)
        buttonTest.setOnClickListener {
//            println(resources.configuration.orientation)
            println("test")
            //startActivity(Intent(this, LockScreenActivity::class.java))
//            println(Environment.getExternalStorageDirectory())
//            File(
//                Environment.getExternalStorageDirectory().toString() + "/DCIM/Screenshots"
//            ).listFiles().forEach { println(it) }
        }
        */
//        val data = WithdrawData("TRUE", Var.did, "", "TRUE/081234567" ,10F, "50015807823792", "22/09/2022 17:20:49", System.currentTimeMillis(), 0)
//        Var.withdrawData.add(data)
    }

    private fun handlerRemove() {
        handler.removeCallbacks(runnable)
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(runnable, 10000)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(data: WithdrawData) {
        Var.withdrawData.add(data)
        recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        checkAccessibility()
        if (!isAccessibility) return
        EventBus.getDefault().register(this)
        handler.postDelayed(runnable, 10000)
    }

    private val runnable = object : Runnable {
        override fun run() {
            startActivity(Intent(this@MainActivity, LockScreenActivity::class.java))
        }
    }


    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    private fun checkAccessibility() {
        isAccessibility = isAccessibilityPermissionGranted(this)
        if (!isAccessibility) {
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

    inner class AppAdapter : RecyclerView.Adapter<AppHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, p1: Int): AppHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_sms, parent, false)
            return AppHolder(v)
        }

        override fun getItemCount(): Int {
            return Var.withdrawData.size
        }

        override fun onBindViewHolder(holder: AppHolder, i: Int) {
            val n = Var.withdrawData.size - i - 1
            if (i % 2 == 0) {
                holder.itemLayout.setBackgroundColor(Color.WHITE)
            } else {
                holder.itemLayout.setBackgroundColor(Color.argb(255, 240, 240, 240))
            }

            holder.tvText.text = "ถอนให้ ${Var.withdrawData[n].account} จำนวน ${Var.withdrawData[n].balance}"
            holder.tvRef.text = "Ref: " + Var.withdrawData[n].ref
            holder.tvDate.text = Var.withdrawData[n].date

        }
    }

    inner class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
//                val i = adapterPosition
//                println(i)
//                val no = Var.smsArray.size - i
//                smsArrayNo = no - 1
//                val app = Var.smsArray[smsArrayNo]
//                if (app.state == 0) return@setOnClickListener
//                val data = DeviceData(app.text, app.source, app.cat,1,app.account)
//                sendServer(data)
                handlerRemove()
            }
        }

        val itemLayout = itemView.findViewById<LinearLayout>(R.id.itemLayout)
        val tvText = itemView.findViewById<TextView>(R.id.tvItemText)
        val tvRef = itemView.findViewById<TextView>(R.id.tvItemRef)
        val tvDate = itemView.findViewById<TextView>(R.id.tvItemDate)
//        val btnDelete = itemView.findViewById<ImageButton>(R.id.btnItemDelete)
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
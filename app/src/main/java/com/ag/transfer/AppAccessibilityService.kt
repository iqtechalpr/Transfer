package com.ag.transfer

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class AppAccessibilityService : AccessibilityService() {

    private var isChildPrint = false
    private val handler = Handler(Looper.getMainLooper())
    private var isStateChanged = false
    private var isUseCheckState = false
    private var isKplus = false

    private var nodeAccount: AccessibilityNodeInfo? = null
    private var nodeAmount: AccessibilityNodeInfo? = null
    private var idAccount = ""
    private var idAmount = ""


    private val withdraws: ArrayList<WithdrawModel> = ArrayList()
    private var index = 0
    private var wdIndex = 0

    override fun onCreate() {
        super.onCreate()
        Var.cmdKplusInit()
        Var.cmdTrueInit()
        println("Accessibility Service start")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == null) {
            return START_STICKY
        }
        when (intent.action) {
            "withdraw" -> {
                val action = intent.getStringExtra("action")
                val cmds = intent.getStringExtra("cmd").toString()
                isChildPrint = false
                val cmd = cmds.split(",")
                println(cmd)
                withdraws.add(WithdrawModel(cmd[0], cmd[1], cmd[2], cmd[3].toInt(), "", ""))
                if (withdraws.size > 1) return START_STICKY
                wdIndex = withdraws.size - 1// withdraws.indexOfFirst { it.id == wdId }
                if (action == "true") {
                    isKplus = false
                    withdrawTrue(cmd[2], cmd[3])
                } else if (action == "kplus") {
                    isKplus = true
                    withdrawKPlus(cmd[2], cmd[3])
                } else if (action == "clear") {
//                    withdraws.clear()
                    println(withdraws.size)
                }
            }
            "control" -> {
                val cmd = intent.getStringExtra("cmd")
                val action = intent.getStringExtra("action")
                when (action) {
                    "image" -> {
                        getImage()
                    }
                    "tap" -> {
                        var gson = Gson()
                        var cmds = gson.fromJson(cmd, TapModel::class.java)
                        onTap(cmds.x, cmds.y, cmds.duration)
                    }
                    "global" -> {
                        val cmd = intent.getStringExtra("cmd")
                        onGlobalAction(cmd.toString())
                    }
                    "child" -> {
                        isChildPrint = false
                        getChild()
                        val jsArray = Gson().toJson(Var.childs)
//                        val body = DeviceData(jsArray, "BOT", Var.did, System.currentTimeMillis(), 0)
                        val body = WithdrawData("child", Var.did, "", "",0F, jsArray, "", System.currentTimeMillis(), 2)
                        sendData(body)
                    }
                    "childs" -> {
                        isChildPrint = true
                        getChild()
                    }
                    "kill" -> {
                        val cmd = intent.getStringExtra("cmd")
//                        println("kill: $cmd")
                        val am = getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
                        am.killBackgroundProcesses(cmd)
                    }
                    "unlock" -> {
                        isChildPrint = true
                        getChild()
                        if (Var.childs.size < 2) {
                            val cX = Var.displayWidth / 2
                            val cY = Var.displayHeight / 2
                            val cY4 = Var.displayHeight / 4
                            println("$cX, $cY ,$cY4")
                            onTap(cX.toFloat(), cY.toFloat(), 100)
                            Thread.sleep(300)
                            onTap(cX.toFloat(), cY.toFloat(), 100)
                            Thread.sleep(500)
                            onSwipe(
                                cX.toFloat() - cY4,
                                cY.toFloat(),
                                cX.toFloat(),
                                cY.toFloat(),
                                100
                            )
                        }
                    }

                }
            }
            "test" -> {
                sendTestData()
            }
        }
        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val intType = event?.eventType
        if (intType == 32) {
            //println("TYPE_WINDOW_STATE_CHANGED")
            if (!isStateChanged) {
                isStateChanged = true
                if (!isUseCheckState) return
                //println("postDelayed")
                handler.postDelayed(runnable, 500)
            } else {
                if (!isUseCheckState) return
                handler.removeCallbacks(runnable)
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(runnable, 500)
                //println("reset")
            }
        }
    }

    private val runnable = object : Runnable {
        override fun run() {
            println("StateChanged: $index")
            isStateChanged = false
            isUseCheckState = false
            if (isKplus) processCmdKPlus()
            else processCmdTrue()
        }
    }

    private fun withdrawKPlus(bankAccount: String, amount: String) {
        println("Kplus  $bankAccount,$amount")
        index = 0
        processCmdKPlus()
    }

    private fun checkNextProcess(source: String) {
        val bankRef = withdraws[wdIndex].bankRef
        val bankDate = withdraws[wdIndex].bankDate.trim()
        val id = withdraws[wdIndex].id
        val account = withdraws[wdIndex].bankCode + "/" + withdraws[wdIndex].bankAccount
            println("source:$source,bankRef:$bankRef,bankDate:$bankDate")
        if (bankDate != "" && bankRef != "") {
            //body.account, body.source, body.text)
//            val body = DeviceData("id:$id,bankRef:$bankRef,bankDate:$bankDate", source, Var.did, System.currentTimeMillis(), 0)
//            val body = DeviceData("$bankRef,$bankDate", source, id, System.currentTimeMillis(), 0)
            val body = WithdrawData(source, Var.did, id, account ,0F, bankRef, bankDate, System.currentTimeMillis(), 0)
//            println(body)
            EventBus.getDefault().post(body)
            sendData(body)
        }
        withdraws.removeAt(wdIndex)
        if (withdraws.size > 0) {
            wdIndex = withdraws.size - 1
            val wd = withdraws[wdIndex]
            if (wd.bankCode == "TRUE") {
                isKplus = false
                withdrawTrue(wd.bankAccount, wd.amount.toString())
            } else {
                isKplus = true
                withdrawKPlus(wd.bankAccount, wd.amount.toString())
            }
        } else {
            getChild()
            val child = findChildByText("Transfer") ?: return
            var x = child.x
            var y = child.y
            y -= Var.openOfset
            onTap(x.toFloat(), y.toFloat(), 100)
        }
    }

    private fun processCmdKPlus() {
        if (Var.cmdKplus.size == index) {
            println("finish KPlus: $index")
            checkNextProcess("KBNK")
            return
        }
        val cmd: CmdModel = Var.cmdKplus[index]
//        println(cmd)
        if (cmd.cmd == "text") {
            Thread.sleep(500)
            nodeAccount = null
            nodeAmount = null
            idAccount = cmd.id
            idAmount = cmd.text
        }
        if (cmd.text == "ยืนยัน" || cmd.text == "กลับหน้าธุรกรรม") Thread.sleep(1000)
        if (cmd.isFindChild) getChild()
        var child: ChildModel? = null

        child = if (cmd.id == "") findChildByText(cmd.text)
        else findChildById(cmd.id)

        if (cmd.text == "ยืนยัน") {
            if (child == null) {
                Thread.sleep(1000)
                getChild()
                child = findChildByText(cmd.text)
                if (child == null) {
                    Thread.sleep(1000)
                    getChild()
                    child = findChildByText(cmd.text)
                }
            }
        }

        println(child)
        if (cmd.text == "กลับหน้าธุรกรรม") {
            Thread.sleep(1000)
            getChild()
            child = findChildById("com.kasikorn.retail.mbanking.wap:id/textView_datetime")
            if (child == null) {
                Thread.sleep(1000)
                getChild()
                child = findChildById("com.kasikorn.retail.mbanking.wap:id/textView_datetime")
                if (child == null) {
                    Thread.sleep(1000)
                    getChild()
                    //findChildByText(cmd.text)
                }
            }
            val c1 = findChildById("com.kasikorn.retail.mbanking.wap:id/textView_datetime")
            if (c1 != null) {
                withdraws[wdIndex].bankDate = c1.text
//                println("${c1.text}")
            }
            val c2 = findChildById("com.kasikorn.retail.mbanking.wap:id/textView_finance_number")
            if (c2 != null) {
                withdraws[wdIndex].bankRef = c2.text
//                println("${c2.text}")
            }
            index++
            onGlobalAction("BACK")
            isUseCheckState = true
            return
        }
        if (cmd.cmd == "banks") {
            Thread.sleep(500)
            val bankValue = Bank.code[withdraws[wdIndex].bankCode]
            child = findChildByText(bankValue!!)
            if (child == null) {
                Thread.sleep(1000)
                getChild()
                child = findChildByText(bankValue!!)
            }
        }
        isUseCheckState = false
//        println(index)

        when (cmd.cmd) {
            "tap" -> {
                if (child == null) return
                onTap(child.x.toFloat(), child.y.toFloat(), 100)
            }
            "home" -> {
                if (child == null) {
                    val c = findChildById("com.kasikorn.retail.mbanking.wap:id/textview_confirm")
                    if (c != null) {
                        onTap(c.x.toFloat(), c.y.toFloat(), 100)
                        Handler(Looper.getMainLooper()).postDelayed({
                            isUseCheckState = true
                        }, 1000)
                        return
                    }
                    val c1 = findChildById("com.kasikorn.retail.mbanking.wap:id/textView_availableBalance")
                    if (c1 != null) {
                        println(c1.text)
                        index += 2
                        processCmdKPlus()
                        return
                    }
                    if (cmd.cmd == "home") {
                        Handler(Looper.getMainLooper()).postDelayed({
                            onGlobalAction("Home")
                            index = 1
                            isUseCheckState = true
                        }, 1000)
                        return
                    }
                } else {
                    val c1 = findChildById("com.kasikorn.retail.mbanking.wap:id/textView_availableBalance")
                    if (c1 != null) {
                        println(c1.text)
                        index += 2
                        processCmdKPlus()
                        return
                    } else {
                        onTap(child.x.toFloat(), child.y.toFloat(), 100)
                    }
                }
            }
            "banks" -> {
                if (child == null) return
                onTap(child.x.toFloat(), child.y.toFloat(), 100)
            }
            "open" -> {
                if (child == null) return
                var x = child.x
                var y = child.y
                y -= Var.openOfset
                onTap(x.toFloat(), y.toFloat(), 100)
            }
            "text" -> {
                val c = findChildById("com.kasikorn.retail.mbanking.wap:id/textview_amount")
                if (c != null) {
                    var text = c.text
                    text = text.replace(",", "")
                    text = text.replace(",", "")
                    val balance = text.toFloat()
                    val amount = withdraws[wdIndex].amount
                    if (balance < amount.toFloat()) {
                        isStateChanged = false
                        isUseCheckState = false
//                        val body = DeviceData("balance ($balance)  < amount ($amount)", "TRUE/BOT", Var.did, System.currentTimeMillis(), 0)
                        val body = WithdrawData("TRUE", Var.did, "", "",balance, "", "", System.currentTimeMillis(), 1)
//                        println(body)
                        sendData(body)
                        onGlobalAction("BACK")
                        Thread.sleep(500)
                        onGlobalAction("BACK")
                        Thread.sleep(500)
                        onGlobalAction("BACK")
                        Thread.sleep(200)
                        index = Var.cmdKplus.size
                        processCmdKPlus()
                        return
                    }
                }
                if (nodeAccount != null) {
                    val rect = Rect()
                    nodeAccount!!.getBoundsInScreen(rect)
                    onTap(rect.centerX().toFloat(), rect.centerY().toFloat(), 100)
                    Thread.sleep(500)
                    setText(nodeAccount!!, withdraws[wdIndex].bankAccount)
                    Thread.sleep(500)
                    onGlobalAction("BACK")
                }
                if (nodeAmount != null) {
                    val rect = Rect()
                    nodeAmount!!.getBoundsInScreen(rect)
                    Thread.sleep(500)
                    onTap(rect.centerX().toFloat(), rect.centerY().toFloat(), 100)
                    Thread.sleep(500)
                    setText(nodeAmount!!, withdraws[wdIndex].amount.toString())
                    Thread.sleep(500)
                    onGlobalAction("BACK")
                    Thread.sleep(200)
                }
            }
            "global" -> {
                onGlobalAction(cmd.id)
            }
            "password" -> {
                if (child == null) {
                    if (Var.childs[0].id == "com.kasikorn.retail.mbanking.wap:id/textview_pin_title_activity") {
                        println("kplus password")
                        for (i in 0..5) {
                            var key = Var.kplusKey.subSequence(i, i + 1)
                            val c = findChildByText(key.toString()) ?: return
                            onTap(c.x.toFloat(), c.y.toFloat(), 100)
                            Thread.sleep(250)
                        }
                        Thread.sleep(1000)
                    }
                } else {
                    onTap(child.x.toFloat(), child.y.toFloat(), 100)
                    Thread.sleep(300)
                    index++
                }
            }
        }
        index++
        if (cmd.delay == 0) {
            isUseCheckState = true
//            if (cmd.text == "ยืนยัน" ) {
//                Handler(Looper.getMainLooper()).postDelayed({
//                    isUseCheckState = true
//                }, 2000)
//            } else {
//
//            }
        } else {
            Thread.sleep(cmd.delay.toLong())
            processCmdKPlus()
        }
    }

    private fun withdrawTrue(account: String, amount: String) {
        println("withdraw true: $account,$amount")
        index = 0
        processCmdTrue()
    }

    private fun processCmdTrue() {
        if (Var.cmdTrue.size == index) {
            checkNextProcess("TRUE")
            return
        }
        val cmd: CmdModel = Var.cmdTrue[index]
//        println(cmd)
        if (cmd.cmd == "text") {
            nodeAccount = null
            nodeAmount = null
            idAccount = cmd.id
            idAmount = cmd.text
        } else if (cmd.cmd == "password") {
            nodeAccount = null
            idAccount = cmd.text
        }

        if (cmd.isFindChild) getChild()
        var child: ChildModel? = null
        if (cmd.cmd == "tap" || cmd.cmd == "password" || cmd.cmd == "open") {
            child = if (cmd.id == "") findChildByText(cmd.text)
            else findChildById(cmd.id)
        }
        isUseCheckState = false
        println(child)
        when (cmd.cmd) {
            "tap" -> {
                if (child == null) {
                    println("child index $index null")
                    return
                }
                if (cmd.id == "th.co.truemoney.wallet:id/edt_ref") {
                    val childBalance =
                        findChildById("th.co.truemoney.wallet:id/widget_balance_number")
                    if (childBalance != null) {
                        val balance = childBalance.text.toFloat()
//                        println("balance $balance")
                        val amount = withdraws[wdIndex].amount
                        if (balance < amount.toFloat()) {
                            isStateChanged = false
                            isUseCheckState = false
//                            val body = DeviceData("balance ($balance)  < amount ($amount)", "TRUE/BOT", Var.did, System.currentTimeMillis(), 0)
                            val body = WithdrawData("TRUE", Var.did, "", "",balance, "", "", System.currentTimeMillis(), 1)
//                            println(body)
                            sendData(body)
                            onGlobalAction("BACK")
                            Thread.sleep(200)
                            index = Var.cmdTrue.size - 2
                            processCmdTrue()
//                            setTransferHome()
                            return
                        }
                    }
                } else if (cmd.id == "th.co.truemoney.wallet:id/textChat") {
                    var i = findChildIndexByText("หมายเลขการทำรายการ")
                    if (i > 0) {
                        withdraws[wdIndex].bankRef = Var.childs[i + 1].text
                    }
                    i = findChildIndexByText("วันที่ทำรายการ")
                    if (i > 0) {
                        withdraws[wdIndex].bankDate = Var.childs[i + 1].text
                    }
                }
                onTap(child.x.toFloat(), child.y.toFloat(), 100)
            }
            "open" -> {
                if (child == null) return
                var x = child.x
                var y = child.y
                y -= Var.openOfset
                onTap(x.toFloat(), y.toFloat(), 100)
            }
            "text" -> {
                nodeAccount?.let { setText(it, withdraws[wdIndex].bankAccount) }
                nodeAmount?.let { setText(it, withdraws[wdIndex].amount.toString()) }
            }
            "global" -> {
                onGlobalAction(cmd.id)
            }
            "password" -> {
                if (child == null) {
                    Thread.sleep(200)
                    println(nodeAccount.toString())
                    nodeAccount?.let { setText(it, Var.walletKey) }

                    Handler(Looper.getMainLooper()).postDelayed({
                        println("password500")
//                        isStateChanged = false
//                        isUseCheckState = false
                        index = 0
                        processCmdTrue()
                    }, 4000)
                    return
                } else {
                    index++
                    onTap(child.x.toFloat(), child.y.toFloat(), 100)
                }
            }
        }
        index++
        if (cmd.delay == 0) {
            isUseCheckState = true
        } else {
            Thread.sleep(cmd.delay.toLong())
            processCmdTrue()
        }
    }

    private fun findChildById(id: String): ChildModel? {
        return Var.childs.find { it.id == id }
    }

    private fun findChildByText(text: String): ChildModel? {
        return Var.childs.find { it.text == text }
    }

    private fun findChildIndexByText(text: String): Int {
        return Var.childs.indexOfFirst { it.text == text }
    }

    private fun getChild() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo == null) {
            println("node root null: ")
            return
        }
        Var.childs.clear()
        getChildText(nodeInfo!!)
    }

    private fun getChildText(info: AccessibilityNodeInfo) {
        val i = info.childCount
        for (p in 0 until i) {
            val n = info.getChild(p)
            if (n != null) {
                val id = n.viewIdResourceName
//                println(id)
                if (!isKplus) {
                    if (id == idAccount) {
                        nodeAccount = n
                        idAccount = ""
                    } else if (id == idAmount) {
                        nodeAmount = n
                        idAmount = ""
                    }
                }

                if (n.text != null) {
                    val text = n.text.toString()
                    if (isKplus) {
                        if (text == idAccount) {
                            //println(text)
                            nodeAccount = n
                            idAccount = ""
                        } else if (text == idAmount) {
                            //println(text)
                            nodeAmount = n
                            idAmount = ""
                        }
                    }
                    val rect = Rect()
                    n.getBoundsInScreen(rect)
                    try {
                        if (rect.centerX() > 0) {
                            val child = ChildModel(id, text, rect.centerX(), rect.centerY())
                            if (isChildPrint) println(child)
                            Var.childs.add(child)
                        }
                    } catch (e: Exception) {
                    }
                }
                getChildText(n)
            }
        }
    }

    private fun setText(node: AccessibilityNodeInfo, text: String) {
        var arguments = Bundle()
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT.id, arguments)
    }

    private fun onTap(x: Float, y: Float, duration: Long) {
//        println("$x , $y , $duration")
        val gestureBuilder = GestureDescription.Builder()
        val clickPath = Path()
        clickPath.moveTo(x, y)
        val clickStroke = GestureDescription.StrokeDescription(clickPath, 0, duration)
        gestureBuilder.addStroke(clickStroke)
        dispatchGesture(gestureBuilder.build(), null, null)
        //println("tap, result= " + result) val result =
    }

    private fun onSwipe(x1: Float, y1: Float, x2: Float, y2: Float, duration: Long) {
        val gestureBuilder = GestureDescription.Builder()
        val clickPath = Path()
        clickPath.moveTo(x1, y1)
        clickPath.lineTo(x2, y2)
        val clickStroke = GestureDescription.StrokeDescription(clickPath, 0, duration)
        gestureBuilder.addStroke(clickStroke)
        dispatchGesture(gestureBuilder.build(), null, null)
        //println("swipe, result= " + result) val result =
    }

    private fun onGlobalAction(cmd: String) {
        var intAction = GLOBAL_ACTION_HOME
        if (cmd == "BACK") intAction = GLOBAL_ACTION_BACK
        else if (cmd == "RECENT") intAction = GLOBAL_ACTION_RECENTS
        else if (cmd == "SCREEN") intAction = GLOBAL_ACTION_TAKE_SCREENSHOT
        else if (cmd == "NOTIFICATION") intAction = GLOBAL_ACTION_NOTIFICATIONS
        else if (cmd == "POWER") intAction = GLOBAL_ACTION_POWER_DIALOG
        performGlobalAction(intAction)
    }

    fun sendData(body: WithdrawData) {
        val retrofit = Retrofit.Builder()
            .baseUrl(Var.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiService::class.java)
        val call = api.sendData("Bearer " + Var.accessToken, body)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, res: Response<ApiResponse>) {
                println(res.body()?.data)
                Toast.makeText(applicationContext, res.body()?.data.toString(), Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                println("onFailure")
            }
        })
    }

    private fun getImage() {
        onGlobalAction("SCREEN")
        Thread.sleep(750)
        val files = File(
            Environment.getExternalStorageDirectory().toString() + "/DCIM/Screenshots"
        ).listFiles()
        val i = files.size
        if (i > 0) {
            val file = files[i - 1]
            println(file.name)
            uploadFile(file)
        }
    }

    fun uploadFile(file: File) {
        //val fileName = "/sdcard/DCIM/Screenshots/Screenshot_2022-09-10-09-14-29-978_com.ag.transfer.jpg"
        //val file = File(fileName)
        val requestFile = RequestBody.create(MediaType.parse("image/jpg"), file)
        val body = MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
        val descriptionString = "hello, this is description speaking"
        val description = RequestBody.create(MultipartBody.FORM, descriptionString)

        val retrofit = Retrofit.Builder()
            .baseUrl(Var.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(FileUploadService::class.java)

        val call = api.upload("Bearer " + Var.accessToken, description, body)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, res: Response<ApiResponse>) {
                println(res.body()?.data)
                //Toast.makeText(applicationContext, res.body()?.data.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                println("onFailure")
            }
        })
    }

    fun sendTestData() {
        val retrofit = Retrofit.Builder()
            .baseUrl(Var.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiService::class.java)
        val call = api.testSendData("Bearer " + Var.accessToken)
        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, res: Response<ApiResponse>) {
                println(res.body()?.data)
                Toast.makeText(applicationContext, res.body()?.data.toString(), Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                println("onFailure")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Accessibility Service deestroy")
    }

    override fun onInterrupt() {
        TODO("Not yet implemented")
    }
}
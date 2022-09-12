package com.ag.transfer

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
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

    private val cmds: ArrayList<CmdModel> = ArrayList()

    private var index = 0
    private var password = ""
    private var account = ""
    private var amount = ""
    private var bankRef = ""
    private var bankDate = ""

    override fun onCreate() {
        super.onCreate()
        println("Accessibility Service start")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == null) {
            return START_STICKY
        }
        when (intent.action) {
            "TRUE" -> {
                val action = intent.getStringExtra("action")
                val cmds = intent.getStringExtra("cmd").toString()
                if (action == "withdraw") {
                    val cmd = cmds.split(",")
                    isChildPrint = false
                    isKplus = false
                    withdrawTrue(cmd[0], cmd[1], cmd[2])
                } else if (action == "balance") {

                }

            }
            "KPLUS" -> {
                val action = intent.getStringExtra("action")
                val cmds = intent.getStringExtra("cmd").toString()
                val cmd = cmds.split(",")
                if (action == "withdraw") {
                    isChildPrint = false
                    isKplus = true
                    withdrawKPlus(cmd[0], cmd[1], cmd[2], cmd[3])
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
                        val body = DeviceData(jsArray, "TRUE/BOT", Var.did, System.currentTimeMillis(), 0)
                        sendData(body)
                    }
                    "childs" -> {
                        isChildPrint = true
                        getChild()
                    }
                    "test" -> {
                        sendTestData()
                    }
                }
            }
        }
        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val intType = event?.eventType
        if (intType == 32) {
            println("TYPE_WINDOW_STATE_CHANGED")
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

    private fun withdrawKPlus(password: String, bankIndex: String, account: String, amount: String) {
        println("Kplus $bankIndex $account,$amount")
        this.password = password
        this.account = account
        this.amount = amount
        cmds.clear()
        var i = 0
        cmds.add(CmdModel(i++, "global", "HOME", "", false, 400))
        cmds.add(CmdModel(i++, "tap", "", "K PLUS", true, 0))
        cmds.add(CmdModel(i++, "transfer", "", "โอนเงิน", true, 0))
        cmds.add(CmdModel(i++, "password", "", "บัญชีธนาคารอื่น", true, 0))
//        cmds.add(CmdModel(i++, "bank", bankIndex, "", true, 0))
        cmds.add(CmdModel(i++, "tap", "", bankIndex, true, 0))
        cmds.add(CmdModel(i++, "text", "กรอกเลขบัญชี", "0.00", true, 200))
//        cmds.add(CmdModel(i++, "tap", "com.kasikorn.retail.mbanking.wap:id/textview_navigation_next", "", false, 0))

        cmds.add(CmdModel(i++, "tap", "", "ต่อไป", false, 0))
        cmds.add(CmdModel(i++, "tap", "", "ยืนยัน", true, 0))
        cmds.add(CmdModel(i++, "tap", "com.kasikorn.retail.mbanking.wap:id/textView_bottom_menu_home", "", true, 0))
        cmds.add(CmdModel(i++, "tap", "", "หน้าแรก", true, 0))
        cmds.add(CmdModel(i++, "global", "HOME", "", false, 0))
//        cmds.add(CmdModel(i++, "tap", "", "Transfer", true, 400))

        bankDate = ""
        bankRef = ""
        index = 0
        processCmdKPlus()
    }

    private fun processCmdKPlus() {
        if (cmds.size == index) {
            println("finish command: $index")
            println("bankRef:$bankRef,bankDate:$bankDate")
            if (bankDate != "" && bankRef != "") {
                val body = DeviceData("bankRef:$bankRef,bankDate:$bankDate", "TRUE/BOT", Var.did, System.currentTimeMillis(), 0)
                println(body)
                sendData(body)
            }
            return
        }
        val cmd: CmdModel = cmds[index]
        println(cmd)
        if (cmd.cmd == "text") {
            Thread.sleep(500)
            nodeAccount = null
            nodeAmount = null
            idAccount = cmd.id
            idAmount = cmd.text
        }
        if (cmd.text == "ยืนยัน")  Thread.sleep(500)
        if (cmd.isFindChild) getChild()
        var child: ChildModel? = null
        if (cmd.cmd == "tap" || cmd.cmd == "password" || cmd.cmd == "transfer") {
            child = if (cmd.id == "") findChildByText(cmd.text)
            else findChildById(cmd.id)
        }
        isUseCheckState = false
        println(child)
        when (cmd.cmd) {
            "transfer" -> {
                if (child == null) {
                    val c = findChildById("com.kasikorn.retail.mbanking.wap:id/textview_confirm")
                    if (c == null) return
                    else onTap(c.x.toFloat(), c.y.toFloat(), 100)
                    Handler(Looper.getMainLooper()).postDelayed({
                        isUseCheckState = true
                    }, 3000)
                    return
                } else {
                    onTap(child!!.x.toFloat(), child!!.y.toFloat(), 100)
                }
            }
            "tap" -> {
                if (child == null) {
                    println("child index $index null")
                    return
                }
                if (cmd.id == "com.kasikorn.retail.mbanking.wap:id/textView_bottom_menu_home") {

                }
                var x = child.x
                var y = child.y
                if (Var.isMainWidget) {
                    if (index == 1 ) {//|| index == cmds.size - 1
//                        x -= 40
                        y -= 60
                    }
                }
//                println("${x}, $y")
                onTap(x.toFloat(), y.toFloat(), 100)
            }
            "text" -> {
//ChildModel(id=com.kasikorn.retail.mbanking.wap:id/textview_amount, text=3,028.34, x=406, y=426)
                val c = findChildById("com.kasikorn.retail.mbanking.wap:id/textview_amount")
                if (c!= null) {
                    var text = c.text
                    text = text.replace(",", "")
                    text = text.replace(",", "")
                    val balance = text.toFloat()
                    if (balance < amount.toFloat()) {
                        isStateChanged = false
                        isUseCheckState = false
                        val body = DeviceData("balance ($balance)  < amount ($amount)", "TRUE/BOT", Var.did, System.currentTimeMillis(), 0)
                        println(body)
                        sendData(body)
                        onGlobalAction("BACK")
                        Thread.sleep(500)
                        onGlobalAction("BACK")
                        Thread.sleep(500)
                        onGlobalAction("BACK")
                        Thread.sleep(200)
                        index = cmds.size //-2
                        processCmdKPlus()
                        return
                    }
                }
                if (nodeAccount!= null) {
                    val rect = Rect()
                    nodeAccount!!.getBoundsInScreen(rect)
                    onTap(rect.centerX().toFloat(),rect.centerY().toFloat(),100)
                    Thread.sleep(500)
                    setText(nodeAccount!!, account)
                    Thread.sleep(500)
                    onGlobalAction("BACK")
                }
                if (nodeAmount!= null) {
                    val rect = Rect()
                    nodeAmount!!.getBoundsInScreen(rect)
                    Thread.sleep(500)
                    onTap(rect.centerX().toFloat(),rect.centerY().toFloat(),100)
                    Thread.sleep(500)
                    setText(nodeAmount!!, amount)
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
                            var key = password.subSequence(i, i + 1)
                            val c = findChildByText(key.toString()) ?: return
                            onTap(c.x.toFloat(), c.y.toFloat(), 100)
                            Thread.sleep(250)
                        }
                        Thread.sleep(500)
                        isUseCheckState = true
                        return
                    }
                } else {
                    onTap(child.x.toFloat(), child.y.toFloat(), 100)
                    Thread.sleep(300)
                }
            }
        }
        index++
        if (cmd.delay == 0) {
//            if (cmd.id == "com.kasikorn.retail.mbanking.wap:id/textview_navigation_next") {
//                Handler(Looper.getMainLooper()).postDelayed({
//                    isUseCheckState = true
//                    println("wat 2000")
//                }, 1000)
//            } else {
//
//            }
            isUseCheckState = true
        } else {
            Thread.sleep(cmd.delay.toLong())
            processCmdTrue()
        }
    }


    private fun withdrawTrue(password: String, account: String, amount: String) {
        println("$account,$amount")
        this.password = password
        this.account = account
        this.amount = amount
        cmds.clear()
        var i = 0
        cmds.add(CmdModel(i++, "global", "HOME", "", false, 400))
        cmds.add(CmdModel(i++, "tap", "", "TrueMoney", true, 0))
        cmds.add(CmdModel(i++, "password", "th.co.truemoney.wallet:id/sliding_btnTextTransfer", "th.co.truemoney.wallet:id/pinEditText", true, 0))
        cmds.add(CmdModel(i++, "tap", "", "th.co.truemoney.wallet:id/sliding_btnTextTransfer", true, 0))

        cmds.add(CmdModel(i++, "tap", "th.co.truemoney.wallet:id/edt_ref", "", true, 0))
        cmds.add(CmdModel(i++, "text", "th.co.truemoney.wallet:id/common_edt", "", true, 200))
        cmds.add(CmdModel(i++, "global", "BACK", "", false, 200))
        cmds.add(CmdModel(i++, "global", "BACK", "", false, 0))
        cmds.add(CmdModel(i++, "text", "", "th.co.truemoney.wallet:id/editTextAmount", true, 200))
        cmds.add(CmdModel(i++, "tap", "th.co.truemoney.wallet:id/btnTransferOrLogin", "", false, 0))
        cmds.add(CmdModel(i++, "tap", "th.co.truemoney.wallet:id/transferConfirmButton", "", true, 0))

//        cmds.add(CmdModel(i++, "tap", "th.co.truemoney.wallet:id/btn_save", "", true, 1000))//text=บันทึกใบเสร็จ
//        cmds.add(CmdModel(i++, "tap", "th.co.truemoney.wallet:id/textChat", "", false, 0)) //text=เรียบร้อย,

        cmds.add(CmdModel(i++, "tap", "th.co.truemoney.wallet:id/textChat", "", true, 0))
        cmds.add(CmdModel(i++, "global", "HOME", "", false, 0))
        cmds.add(CmdModel(i++, "tap", "", "Transfer", true, 400))
//index = 14
        // ChildModel(id=th.co.truemoney.wallet:id/txtTitle, text=ไม่สามารถทำรายการได้, x=540, y=1051)
        bankDate = ""
        bankRef = ""
        index = 0
        processCmdTrue()
    }

    private fun processCmdTrue() {
        if (cmds.size == index) {
            println("finish command: $index")
            println("bankRef:$bankRef,bankDate:$bankDate")
            if (bankDate != "" && bankRef != "") {
                val body = DeviceData("bankRef:$bankRef,bankDate:$bankDate", "TRUE/BOT", Var.did, System.currentTimeMillis(), 0)
                println(body)
                sendData(body)
            }
            return
        }
        val cmd: CmdModel = cmds[index]
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
        if (cmd.cmd == "tap" || cmd.cmd == "password") {
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
                    val childBalance = findChildById("th.co.truemoney.wallet:id/widget_balance_number")
                    if (childBalance != null) {
                        val balance = childBalance.text.toFloat()
//                        println("balance $balance")
                        if (balance < amount.toFloat()) {

                            isStateChanged = false
                            isUseCheckState = false
                            val body = DeviceData("balance ($balance)  < amount ($amount)", "TRUE/BOT", Var.did, System.currentTimeMillis(), 0)
//                            println(body)
                            sendData(body)
                            onGlobalAction("BACK")
                            Thread.sleep(200)
                            index = cmds.size -2
                            processCmdTrue()
//                            setTransferHome()
                            return
                        }
                    }
                } else if (cmd.id == "th.co.truemoney.wallet:id/textChat") {
                    var i = findChildIndexByText("หมายเลขการทำรายการ")
                    if (i > 0) {
//                        println(Var.childs[i + 1].text)
                        bankRef = Var.childs[i + 1].text
                    }
                    i = findChildIndexByText("วันที่ทำรายการ")
                    if (i > 0) {
//                        println(Var.childs[i + 1].text)
                        bankDate = Var.childs[i + 1].text
                    }
                }
                var x = child.x
                var y = child.y
                if (Var.isMainWidget) {
                    if (index == 1 || index == cmds.size - 1) {
//                        x -= 40
                        y -= 60
                    }
                }
//                println("${x}, $y")
                onTap(x.toFloat(), y.toFloat(), 100)
            }
            "text" -> {
                nodeAccount?.let { setText(it, account) }
                nodeAmount?.let { setText(it, amount) }
            }
            "global" -> {
                onGlobalAction(cmd.id)
            }
            "password" -> {
                if (child == null) {
                    Thread.sleep(200)
                    println(nodeAccount.toString())
                    nodeAccount?.let { setText(it, password) }

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

//    private fun setTransferHome() {
//        onAction("Home")
//        Thread.sleep(400)
//        isUseCheckState = true
//        getChild()
//        val child = findChildByText("Transfer") ?: return
//        onTap(child.x.toFloat(), child.y.toFloat(), 100)
//    }

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
                        val child = ChildModel(id, text, rect.centerX(), rect.centerY())
                        if (isChildPrint) println(child)
                        Var.childs.add(child)
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

//    private fun onSwipe(x1: Float, y1: Float, x2: Float, y2: Float, duration: Long) {
//        val gestureBuilder = GestureDescription.Builder()
//        val clickPath = Path()
//        clickPath.moveTo(x1, y1)
//        clickPath.lineTo(x2, y2)
//        val clickStroke = GestureDescription.StrokeDescription(clickPath, 0, duration)
//        gestureBuilder.addStroke(clickStroke)
//        dispatchGesture(gestureBuilder.build(), null, null)
//        //println("swipe, result= " + result) val result =
//    }

    private fun onGlobalAction(cmd: String) {
        var intAction = GLOBAL_ACTION_HOME
        if (cmd == "BACK") intAction = GLOBAL_ACTION_BACK
        else if (cmd == "RECENT") intAction = GLOBAL_ACTION_RECENTS
        else if (cmd == "SCREEN") intAction = GLOBAL_ACTION_TAKE_SCREENSHOT
        else if (cmd == "NOTIFICATION") intAction = GLOBAL_ACTION_NOTIFICATIONS
        else if (cmd == "POWER") intAction = GLOBAL_ACTION_POWER_DIALOG
        performGlobalAction(intAction)
    }

    fun sendData(body: DeviceData) {
        val retrofit = Retrofit.Builder()
            .baseUrl(Var.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(ApiService::class.java)
        val call = api.sendData("Bearer " + Var.accessToken, body)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, res: Response<ApiResponse>) {
                println(res.body()?.data)
                Toast.makeText(applicationContext, res.body()?.data.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                println("onFailure")
            }
        })
    }

    private fun getImage() {
        onGlobalAction("SCREEN")
        Thread.sleep(750)
        val files = File(Environment.getExternalStorageDirectory().toString() + "/DCIM/Screenshots").listFiles()
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
                Toast.makeText(applicationContext, res.body()?.data.toString(), Toast.LENGTH_SHORT).show()
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
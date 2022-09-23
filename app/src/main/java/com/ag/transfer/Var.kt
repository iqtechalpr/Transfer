package com.ag.transfer

class Var {
    companion object {
        var did: String = ""

        var walletKey: String = ""
        var kplusKey: String = ""

        var firebaseToken: String = ""
        var accessToken: String = ""
        var BASE_URL = "http://192.168.0.104:8000"
        val childs: ArrayList<ChildModel> = ArrayList()

        //var isStateChanged = false
        var openOfset = 0
        var displayWidth = 0
        var displayHeight = 0

        val cmdTrue: ArrayList<CmdModel> = ArrayList()
        val cmdKplus: ArrayList<CmdModel> = ArrayList()

        fun cmdTrueInit() {
            var i = 0
            cmdTrue.add(CmdModel(i++, "global", "HOME", "", false, 400))
            cmdTrue.add(CmdModel(i++, "open", "", "TrueMoney", true, 0))
            cmdTrue.add(
                CmdModel(
                    i++, "password", "th.co.truemoney.wallet:id/sliding_btnTextTransfer",
                    "th.co.truemoney.wallet:id/pinEditText", true, 0
                )
            )

            cmdTrue.add(
                CmdModel(
                    i++,
                    "tap",
                    "",
                    "th.co.truemoney.wallet:id/sliding_btnTextTransfer",
                    true,
                    0
                )
            )

            cmdTrue.add(CmdModel(i++, "tap", "th.co.truemoney.wallet:id/edt_ref", "", true, 0))
            cmdTrue.add(
                CmdModel(
                    i++, "text", "th.co.truemoney.wallet:id/common_edt", "", true, 200
                )
            )
            cmdTrue.add(CmdModel(i++, "global", "BACK", "", false, 200))
            cmdTrue.add(CmdModel(i++, "global", "BACK", "", false, 0))
            cmdTrue.add(
                CmdModel(
                    i++, "text", "", "th.co.truemoney.wallet:id/editTextAmount", true, 200
                )
            )
            cmdTrue.add(
                CmdModel(
                    i++, "tap", "th.co.truemoney.wallet:id/btnTransferOrLogin", "", false, 0
                )
            )
            cmdTrue.add(
                CmdModel(
                    i++, "tap", "th.co.truemoney.wallet:id/transferConfirmButton", "", true, 0
                )
            )

//        cmdTrue.add(CmdModel(i++, "tap", "th.co.truemoney.wallet:id/btn_save", "", true, 1000))//text=บันทึกใบเสร็จ
//        cmdTrue.add(CmdModel(i++, "tap", "th.co.truemoney.wallet:id/textChat", "", false, 0)) //text=เรียบร้อย,

            cmdTrue.add(CmdModel(i++, "tap", "th.co.truemoney.wallet:id/textChat", "", true, 0))
            cmdTrue.add(CmdModel(i++, "global", "HOME", "", false, 0))
//            cmdTrue.add(CmdModel(i++, "open", "", "Transfer", true, 400))

        }

        fun cmdKplusInit() {
            var i = 0
            cmdKplus.add(CmdModel(i++, "global", "HOME", "", false, 400))
            cmdKplus.add(CmdModel(i++, "open", "", "K PLUS", true, 0))
            //text=ธุรกรรม com.kasikorn.retail.mbanking.wap:id/footer_bank_textview
            cmdKplus.add(CmdModel(i++, "home", "", "ธุรกรรม", true, 0))
            cmdKplus.add(CmdModel(i++, "password", "", "โอนเงิน", true, 0))
            cmdKplus.add(CmdModel(i++, "tap", "", "โอนเงิน", true, 0))
            cmdKplus.add(CmdModel(i++, "tap", "", "บัญชีธนาคารอื่น", true, 0))
            cmdKplus.add(CmdModel(i++, "banks", "", "", true, 0))
            cmdKplus.add(CmdModel(i++, "text", "กรอกเลขบัญชี", "0.00", true, 200))
            cmdKplus.add(CmdModel(i++, "tap", "", "ต่อไป", false, 0))
            cmdKplus.add(CmdModel(i++, "tap", "", "ยืนยัน", true, 0))//9
            cmdKplus.add(CmdModel(i++, "tap", "", "กลับหน้าธุรกรรม", true, 0))
            cmdKplus.add(CmdModel(i++, "global", "HOME", "", false, 0))

            /*
            I  ChildModel(id=com.kasikorn.retail.mbanking.wap:id/textview_navigation_next, text=ยืนยัน, x=862, y=2171)
            SateChanged: 10
2022-09-23 14:00:53.485 29532-29532 System.out              com.ag.transfer                      I  null

                       cmdKplus.add(CmdModel(i++, "transfer", "com.kasikorn.retail.mbanking.wap:id/footer_bank_textview", "", true, 0))

                       cmdKplus.add(CmdModel(i++, "tap", "", "หน้าแรก", true, 0))
                       cmdKplus.add(CmdModel(i++, "global", "HOME", "", false, 0))
                       cmdKplus.add(CmdModel(i++, "open", "", "Transfer", true, 400))
           */
        }
    }
}
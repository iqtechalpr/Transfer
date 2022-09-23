package com.ag.transfer
//data class CmdsModel(val type: String, val cmd: String,val activity:String?, val delay: Int)
data class TapModel(val x:Float, val y:Float,val duration: Long)
//child
data class ChildModel(val id: String,val text: String,val x:Int, val y:Int)
data class CmdModel(val index:Int, val cmd: String, val id: String, val text: String, val isFindChild:Boolean, val delay:Int)
//val action: String,
data class WithdrawModel(val id: String,val bankCode: String,val bankAccount: String,val amount:Int,var bankRef: String,var bankDate: String)










package com.ag.transfer

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

//data class ApiResponse(val data: String)
data class ApiResponse( val data: Any, val error:Any)
data class RefreshTokenRequest(val refreshToken: String)
data class DeviceData(val text: String, val source: String, val account: String,val cat:Long, var state:Int)
data class AuthRequest(val did: String,  var token:String)
data class AuthResponse(val data: Tokens?, val error: Any?)
data class Tokens(val accessToken: String, val refreshToken: String)

interface ApiService {
//    @POST("api/auth/device")
//    fun auth(@Body body:AuthRequest): Call<ApiResponse>

//    @POST("api/auth/token/admin/refresh")
//    fun refreshToken(@Body body: RefreshTokenRequest): Call<AuthResponse>

    @GET("api/device/test")
    fun testSendData(@Header("Authorization") token: String): Call<ApiResponse>

    @POST("api/device")
    fun sendData(@Header("Authorization") token: String,@Body body: DeviceData): Call<ApiResponse>

//    companion object {
//        var service: ApiService? = null
//        fun getInstance(): ApiService {
//            if (service == null) {
//                val retrofit = Retrofit.Builder()
//                    .baseUrl(Var.BASE_URL)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build()
//                service = retrofit.create(ApiService::class.java)
//            }
//            return  service!!
//        }
//    }
}
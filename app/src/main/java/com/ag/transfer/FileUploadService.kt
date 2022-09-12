package com.ag.transfer


import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit



interface FileUploadService {

//    @POST("api/device/adb")
//    fun uploadAdb(@Header("Authorization") token: String, @Body body: AdbData): Call<ApiResponse>

    @Multipart
    @POST("api/device/image")
    fun upload(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody?,
        @Part file: MultipartBody.Part
    ): Call<ApiResponse>
}
/*
class RetrofitClientInstance {

    companion object {
        private var retrofit: Retrofit? = null
        fun getRetrofitInstance(): Retrofit? {
            val client = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS).build()
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(Var.BASE_URL).client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }
    }

}

 */

/*
public interface UploadReceiptService {
    @Multipart
    @POST("/api/receipt/upload")
    Call <List<UploadResult>>uploadReceipt(
            @Header("Cookie") String sessionIdAndRz,
            @Part MultipartBody.Part file,
            @Part("items") RequestBody items,
            @Part("isAny") RequestBody isAny
    );
}
 */
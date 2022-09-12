package com.ag.transfer

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button

import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore

import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AuthActivity : BaseActivity() {

    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val tvDid = findViewById<TextView>(R.id.tvDid)
        tvDid.setText(Var.did)
        val tvServerUrl = findViewById<TextView>(R.id.tvServerUrl)
        tvServerUrl.setText(Var.BASE_URL)

        val db = Firebase.firestore
        val data = hashMapOf("token" to Var.firebaseToken)
        val docRef = db.collection("tf").document(Var.did)
        docRef.set(data).addOnSuccessListener {
        }.addOnFailureListener { e -> println(e) }


        val buttonAuth = findViewById<Button>(R.id.buttonAuth)

        buttonAuth.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl(Var.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(ApiService::class.java)
//            val req = AuthRequest(Var.did, Var.fbToken)
            val call = api.testSendData("Bearer " + Var.accessToken)
            showProgressDialog()

            call.enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, res: Response<ApiResponse>) {
                    val it = res.body()
                    println(it?.data)
                    hideProgressDialog()
                    if (it?.data != null) {
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "กรุณาตั้งค่าด้วย FCM", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    hideProgressDialog()
//                    println(t.message)
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()

                }
            })
        }
    }

}
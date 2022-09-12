package com.ag.transfer


import android.app.ProgressDialog
import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

open class BaseActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {
    //    private var progressDialog = ProgressDialog(this)
//    val progressDialog = ProgressDialog(this@MainActivity)
    private var progressDialog: ProgressDialog? = null

    fun showProgressDialog() {
        if (progressDialog == null) {
            val pd = ProgressDialog(this)
            pd.setCancelable(false)
            pd.setMessage("Please wait")
            progressDialog = pd
        }
        progressDialog?.show()
    }

    fun hideProgressDialog() {
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    fun showAlert(msg: String) {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(msg)
        dialog.setPositiveButton("ตกลง") { dialog, which ->

        }
        dialog.show()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val view = currentFocus
        val ret = super.dispatchTouchEvent(event)

        if (view is EditText) {
            val w = currentFocus
            val scrcoords = IntArray(2)
            w!!.getLocationOnScreen(scrcoords)
            val x = event.rawX + w.left - scrcoords[0]
            val y = event.rawY + w.top - scrcoords[1]

            if (event.action == MotionEvent.ACTION_UP && (x < w.left || x >= w.right
                        || y < w.top || y > w.bottom)
            ) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(window.currentFocus!!.windowToken, 0)
            }
        }
        return ret
    }
}
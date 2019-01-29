package com.qegle.pingapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRequest.setOnClickListener {
            btnRequest.isEnabled = false
            request()
        }
    }

    private fun ping(url: String): String {
        try {
            val start = System.currentTimeMillis()
            val client = OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build()

            val resp = client.newCall(Request.Builder().url(url).build()).execute()
            val responseGet = System.currentTimeMillis()
            val body = if (resp.isSuccessful) resp.body()?.string() else null

            val msg = "code: ${resp.code()}, respTime: ${responseGet - start}"
            val bdy = if (body != null && body.isNotEmpty()) ", body: $body" else ""

            return msg + bdy
        } catch (e: Exception) {
            return "ERROR REQUEST - ${e.message}"
        }
    }

    private var request: Disposable? = null
    private fun request() {
        request = Observable
            .fromCallable { ping(etLink.text.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { t -> postMessage(t) },
                { t -> postMessage("${t.message}") })
    }


    private fun postMessage(message: String) {
        tvResult.text = message
        btnRequest.isEnabled = true
        Toast.makeText(this, "request complete", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        request?.dispose()
        request = null
    }
}

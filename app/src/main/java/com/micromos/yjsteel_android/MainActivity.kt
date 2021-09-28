package com.micromos.yjsteel_android

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.iid.InstanceIdResult
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private var backBtnTime = 0L
    lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView.webViewClient = WebViewClient()
        webView.setNetworkAvailable(true)
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(WebBridge(), "BRIDGE")
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY;
        webView.isScrollbarFadingEnabled = false;
        if (BuildConfig.DEBUG)
            webView.loadUrl("http://192.168.0.137:3000/")
        else
            webView.loadUrl("http://121.165.242.72:5050/micromos/build")
        //토큰값을 받아옵니다.


        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(object : OnCompleteListener<InstanceIdResult?> {
                override fun onComplete(task: Task<InstanceIdResult?>) {
                    if (!task.isSuccessful) {
                        return
                    }
                    token = task.result?.token!! // 사용자가 입력한 저장할 데이터
                }
            })

        btnReload.setOnClickListener {
            webView.reload()
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                AlertDialog.Builder(view!!.context)
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                        DialogInterface.OnClickListener { _, _ ->
                            result!!.confirm()
                        })
                    .setCancelable(false)
                    .create()
                    .show()
                return true
            }


            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                AlertDialog.Builder(view!!.context)
                    .setTitle("확인")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                        DialogInterface.OnClickListener { _, _ ->
                            result!!.confirm()
                        })
                    .setNegativeButton(android.R.string.cancel,
                        DialogInterface.OnClickListener { _, _ ->
                            result!!.cancel()
                        })
                    .create()
                    .show()
                return true
            }
        }
    }

    inner class WebBridge {
        @JavascriptInterface
        fun connectAndroid(): String {
            return token
        }

    }

    override fun onBackPressed() {
        webView.evaluateJavascript("""window.dispatchEvent(new CustomEvent("checkBackFlag"))""") { value ->
            webView.evaluateJavascript("""window.sessionStorage.getItem("closeFlag");""") { value ->
                Log.d("testValueCallback2", "onReceiveValue: $value")
                if (value == """"0"""") {
                    webView.evaluateJavascript("""window.dispatchEvent(new CustomEvent("closeOpenSearch"))""") { value ->
                        webView.evaluateJavascript("""window.sessionStorage.getItem("closeFlag");""") { value ->
                            Log.d("testValueCallback4", "onReceiveValue: $value")
                            if (value != """"2""""){
                                switchClose()
                            }
                        }
                    }

                }
            }
        }
    }

    private fun switchClose() {
        val curTime = System.currentTimeMillis()
        val gapTime = curTime - backBtnTime;

        when {
            !(webView.url!!.contains("/home")) && webView.canGoBack() -> {
                webView.goBack()
            }
            gapTime in 0..2000 -> {
                super.onBackPressed()
            }
            else -> {
                backBtnTime = curTime
                Toast.makeText(
                    this,
                    "'뒤로' 버튼을 한번 더 누르시면 종료됩니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

package com.micromos.ddsteel_android

import android.Manifest
import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


const val REQUEST_PHONE_STATE = 1;

class MainActivity : AppCompatActivity() {
    private var backBtnTime = 0L
    lateinit var pushToken: String
    lateinit var deviceInfo: String
    var filePathCallbackNormal: ValueCallback<Uri?>? = null
    var filePathCallbackLollipop: ValueCallback<Array<Uri>>? = null
    private var cameraImageUri: Uri? = null
    private var lastText: String? = null

    private val formats: Collection<BarcodeFormat> = listOf(
        BarcodeFormat.CODE_128,
        BarcodeFormat.CODE_39
    )

    private val callback: BarcodeCallback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText || result.barcodeFormat !in formats) {
                // Prevent duplicate scans
                return
            }
            lastText = result.text
            barcode_scanner.setStatusText(result.text)
            // scanProductCoilInViewModel._requestNo.value = result.text
            // scanProductCoilInViewModel.shipNoRetrieve(scanProductCoilInViewModel._requestNo.value)
            val tone = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)
            tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 500)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }


    companion object {
        const val FILECHOOSER_NORMAL_REQ_CODE = 2001
        const val FILECHOOSER_LOLLIPOP_REQ_CODE = 2002
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionCheck()

        //barcode_scanner.bringToFront()
        //barcode_scanner.visibility = View.VISIBLE
        barcode_scanner.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcode_scanner.initializeFromIntent(intent)
        barcode_scanner.decodeSingle(callback)

    }

    private fun startApp() {
        webView.webViewClient = WebViewClient()
        webView.setNetworkAvailable(true)
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true);
        webView.settings.builtInZoomControls = true;
        webView.settings.displayZoomControls = false;
        webView.addJavascriptInterface(WebBridge(), "BRIDGE")
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY;
        webView.isScrollbarFadingEnabled = false;
        if (BuildConfig.DEBUG)
            webView.loadUrl("http://192.168.0.105:3000")
        //webView.loadUrl("http://121.165.242.72:5080")
        else
            webView.loadUrl("http://http://121.171.250.65/DDSTEEL_API/SCM_MOBILE/")
        //토큰값을 받아옵니다.

        btnReload.setOnClickListener {
         //   barcode_scanner.pause()
//            barcode_scanner.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
//            barcode_scanner.initializeFromIntent(intent)
//            barcode_scanner.decodeSingle(callback)
            barcode_scanner.pauseAndWait()
            Handler().postDelayed(Runnable {
                Log.d("test", "asdf")
                barcode_scanner.resume()
            }, 3000)

            // webView.reload()
        }
        webView.webChromeClient = object : WebChromeClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                // Callback 초기화 (중요)
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop?.onReceiveValue(null)
                    filePathCallbackLollipop = null
                }

                filePathCallbackLollipop = filePathCallback
                val isCapture = fileChooserParams.isCaptureEnabled
                runCamera(isCapture)
                return true
            }

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
            //    FirebaseMessaging.getInstance().token.addOnSuccessListener {
            //       pushToken = it
            //      deviceInfo = "ANDROID/" + getDeviceModel() + "/" + getDeviceOs()
//            Log.d("pushToken", pushToken)
//            Log.d("pushToken", "--- getDeviceId : "+getDeviceId(this@MainActivity));  //device id
//            Log.d("pushToken", "--- getManufacturer : "+getManufacturer());  //제조사
//            Log.d("pushToken", "--- getDeviceBrand : "+getDeviceBrand());  //브랜드
//            Log.d("pushToken", "--- getDeviceModel : "+getDeviceModel());  //모델명
//            Log.d("pushToken", "--- getDeviceOs : "+getDeviceOs());  //안드로이드 OS 버전
//            Log.d("pushToken", "--- getDeviceSdk : "+getDeviceSdk());  //안드로이드 SDK 버전


        }
    }

    //권한 획득 여부 확인
    @RequiresApi(Build.VERSION_CODES.M)
    private fun permissionCheck() {
        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                Manifest.permission.ACCESS_NETWORK_STATE
            ) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            //카메라 또는 저장공간 권한 획득 여부 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.CAMERA

                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                )
            ) {
                Log.d("permissionCheck", "true")
            } else {
                // 카메라 및 저장공간 권한 요청
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.INTERNET,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                    ), REQUEST_PHONE_STATE
                )
                Log.d("permissionCheck", "false")
            }
        } else {
            startApp()
        }
    }


    //권한 획득 여부에 따른 결과 반환
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PHONE_STATE) {
            if (grantResults.isNotEmpty()) {
                for (i in grantResults.indices) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        // 카메라, 저장소 중 하나라도 거부한다면 앱실행 불가 메세지 띄움
                        AlertDialog.Builder(this).setTitle("알림")
                            .setMessage("권한을 허용해주셔야 앱을 이용할 수 있습니다. 권한을 허용해 주시기 바랍니다.")
                            .setPositiveButton("종료") { dialog, _ ->
                                dialog.dismiss()
                                finish()
                            }.setNegativeButton("권한 설정") { dialog, _ ->
                                dialog.dismiss()
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.parse("package:" + applicationContext.packageName))
                                applicationContext.startActivity(
                                    intent.addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                    )
                                )
                            }.setCancelable(false).show()
                        return
                    }
                }
                Toast.makeText(this, "권한설정이 완료되었습니다. ", Toast.LENGTH_SHORT)
                    .show()
                startApp()
            }
        }
        return
    }

    //액티비티가 종료될 때 결과를 받고 파일을 전송할 때 사용
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var data = data
        when (requestCode) {
            MainActivity.FILECHOOSER_NORMAL_REQ_CODE -> if (resultCode == RESULT_OK) {
                if (filePathCallbackNormal == null) return
                val result = if (data == null || resultCode != RESULT_OK) null else data.data
                //  onReceiveValue 로 파일을 전송한다.
                filePathCallbackNormal!!.onReceiveValue(result)
                filePathCallbackNormal = null
            }
            MainActivity.FILECHOOSER_LOLLIPOP_REQ_CODE -> if (resultCode == RESULT_OK) {
                if (filePathCallbackLollipop == null) return
                if (data == null) data = Intent()
                if (data.data == null) data.data = cameraImageUri
                filePathCallbackLollipop!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        data
                    )
                )
                filePathCallbackLollipop = null
            } else {
                if (filePathCallbackLollipop != null) {
                    //  resultCode에 RESULT_OK가 들어오지 않으면 null 처리하지 한다.(이렇게 하지 않으면 다음부터 input 태그를 클릭해도 반응하지 않음)
                    filePathCallbackLollipop!!.onReceiveValue(null)
                    filePathCallbackLollipop = null
                }
                if (filePathCallbackNormal != null) {
                    filePathCallbackNormal!!.onReceiveValue(null)
                    filePathCallbackNormal = null
                }
            }
            else -> {
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // 카메라 기능 구현
    private fun runCamera(_isCapture: Boolean) {
        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        val path = filesDir
        val file = File(path, "picture.png") // sample.png 는 카메라로 찍었을 때 저장될 파일명이므로 사용자 마음대로
        // File 객체의 URI 를 얻는다.
        cameraImageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val strpa = applicationContext.packageName
            FileProvider.getUriForFile(this, "$strpa.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
        intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
        if (!_isCapture) { // 선택팝업 카메라, 갤러리 둘다 띄우고 싶을 때
            val pickIntent = Intent(Intent.ACTION_PICK)
            pickIntent.type = MediaStore.Images.Media.CONTENT_TYPE
            pickIntent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val pickTitle = "방법을 선택해주세요."
            val chooserIntent = Intent.createChooser(pickIntent, pickTitle)

            // 카메라 intent 포함시키기..
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(intentCamera))
            startActivityForResult(chooserIntent, MainActivity.FILECHOOSER_LOLLIPOP_REQ_CODE)
        } else { // 바로 카메라 실행..
            startActivityForResult(intentCamera, MainActivity.FILECHOOSER_LOLLIPOP_REQ_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        barcode_scanner.resume()
    }

    override fun onPause() {
        super.onPause()
        barcode_scanner.pause()
    }

    inner class WebBridge {
        @JavascriptInterface
        fun scanButton() {
            Log.d("test", "est")
//
            if (barcode_scanner.visibility == View.VISIBLE)
                barcode_scanner.visibility = View.INVISIBLE
            else {
                barcode_scanner.bringToFront()
                barcode_scanner.visibility = View.VISIBLE
                Handler().postDelayed(Runnable {
                    Log.d("test", "asdf")
                    barcode_scanner.visibility = View.INVISIBLE
                }, 300)
            }


//            barcode_scanner.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
//            barcode_scanner.initializeFromIntent(intent)

        }
    }

    override fun onBackPressed() {
        webView.evaluateJavascript("""document.getElementById("modal").style.display""") { value ->
            Log.d("test", value)
            if (value == """"block"""") {
                webView.evaluateJavascript("""document.getElementById("modal").style.display = "none",document.body.style.overflow = "auto";""") {}
            } else webView.evaluateJavascript("""window.dispatchEvent(new CustomEvent("checkBackFlag"))""") {
                webView.evaluateJavascript("""window.sessionStorage.getItem("closeFlag");""") { value ->
                    Log.d("testValueCallback2", "onReceiveValue: $value")
                    if (value == """"0"""") {
                        webView.evaluateJavascript("""window.dispatchEvent(new CustomEvent("closeOpenSearch"))""") {
                            webView.evaluateJavascript("""window.sessionStorage.getItem("closeFlag");""") { value ->
                                Log.d("testValueCallback4", "onReceiveValue: $value")
                                if (value != """"2"""") {
                                    switchClose()
                                }
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
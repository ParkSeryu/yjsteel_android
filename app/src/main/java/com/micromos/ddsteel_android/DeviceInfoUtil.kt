package com.micromos.ddsteel_android

import android.content.Context
import android.os.Build
import android.provider.Settings


object DeviceInfoUtil {
    /**
     * device id 가져오기
     * @param context
     * @return
     */
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    /**
     * device 제조사 가져오기
     * @return
     */
    fun getManufacturer() : String {
        return Build.MANUFACTURER
    }
    /**
     * device 브랜드 가져오기
     * @return
     */
    fun getDeviceBrand(): String {
        return  Build.BRAND
    }
    /**
     * device 모델명 가져오기
     * @return
     */
    fun getDeviceModel(): String {
        return Build.MODEL
    }
    /**
     * device Android OS 버전 가져오기
     * @return
     */
    fun getDeviceOs(): String {
        return Build.VERSION.RELEASE
    }
    /**
     * device SDK 버전 가져오기
     * @return
     */
    fun getDeviceSdk() : Int {
        return Build.VERSION.SDK_INT
    }
}

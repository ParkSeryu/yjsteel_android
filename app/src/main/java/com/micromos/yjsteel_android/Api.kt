package com.micromos.yjsteel_android

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface Api {
    @GET("version/getTime")
    fun getDateTime(): Call<ResponseBody>

    @GET("version/checkVersion")
    fun checkVersion(
        @Query("version") version: String
    ): Call<Unit>

    @GET("version/updateApp")
    @Streaming
    fun downloadApk(
    ): Call<ResponseBody>

    companion object Factory {
        fun create(): Api {

            val uri = if (BuildConfig.DEBUG) "http://119.205.209.23/KNP_API/Developer/index.php/"
            else "http://119.205.209.23/KNP_API/Real/index.php/"

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(httpLoggingInterceptor())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(uri)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(Api::class.java)
        }

        private fun httpLoggingInterceptor(): Interceptor {
            val interceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
                Log.v("HTTP", message)
            })
            return interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        }
    }
}

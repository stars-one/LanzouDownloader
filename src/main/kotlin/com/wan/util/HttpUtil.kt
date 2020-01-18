package com.wan.util
import okhttp3.*


/**
 *
 * @author StarsOne
 * @date Create in  2019/10/29 0029 15:27
 * @description
 *
 */
class HttpUtil {

    companion object {

        fun sendOkHttpRequest(address: String, callback: Callback) {
            val client = OkHttpClient()
            val control = CacheControl.Builder().build()
            val request = Request.Builder()
                    .addHeader("user-agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                    .addHeader("accept-language","zh-CN,zh;q=0.9")
                    .cacheControl(control)
                    .url(address)
                    .build()
            client.newCall(request).enqueue(callback)
        }

        fun sendOkHttpRequest(address: String, jsonData: String, callback: Callback) {

            val body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonData)
            val client = OkHttpClient()
            val control = CacheControl.Builder().build()
            val request = Request.Builder()
                    .post(body)
                    .cacheControl(control)
                    .url(address)
                    .build()
            client.newCall(request).enqueue(callback)
        }

    }
}
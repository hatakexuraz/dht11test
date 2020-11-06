package com.example.dht11sensordata

import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.ProtocolException
import java.net.URL

class HTTPHandler() {

    companion object{
        private val TAG = HTTPHandler.javaClass.simpleName
    }

    fun makeServiceCall(urlReq: String): String? {
        var response: String? = null
        try {
            val url =URL(urlReq)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            //read the response
            val inp: InputStream = BufferedInputStream(conn.inputStream)
            response = convertStreamToString(inp)
        } catch (e: MalformedURLException) {
            Log.e(TAG, "MalformedURLException: " + e.message);
        } catch (e: ProtocolException) {
            Log.e(TAG, "ProtocolException: " + e.message);
        } catch (e: IOException) {
            Log.e(TAG, "IOException: " + e.message);
        } catch (e: Exception) {
            Log.e(TAG, "Exception: " + e.message);
        }
        return response
    }

    private fun convertStreamToString(inp : InputStream): String? {
        val reader = BufferedReader(InputStreamReader(inp))
        val sb = StringBuilder()
        var line: String?
        try {
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append('\n')
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inp.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return sb.toString()
    }
}

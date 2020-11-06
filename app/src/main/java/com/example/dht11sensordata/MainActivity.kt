package com.example.dht11sensordata

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.internal.Internal
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    lateinit var list_temp : MutableList<Entry>
    lateinit var list_humi : MutableList<Entry>

    var pDialog: ProgressDialog? = null

    var btn_list : Button? =null
    var line_temp : LineChart? = null
    var lineDataSet: LineDataSet? = null
    var lineData: LineData? = null

    var line_humid : LineChart? = null
    var lineDataSetHumi: LineDataSet? = null
    var lineDataHumi: LineData? = null

    init {
        instance = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_list = btn_list_act
        line_temp = temperature_line
        line_humid = line_humi

        getJSONData().execute()

        btn_list?.setOnClickListener {
            val intent = Intent(applicationContext,ListData::class.java)
            startActivity(intent)
        }
    }


    /**
     * Async task class to get json by making HTTP call
     */
    private inner class getJSONData : AsyncTask<Void, Void, Void>() {
        override fun onPreExecute() {
            super.onPreExecute()
            //show progress dialog
            pDialog = ProgressDialog(this@MainActivity)
            pDialog!!.setMessage("Please Wait...")
            pDialog!!.setCancelable(false)
            pDialog!!.show()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val sh = HTTPHandler()

            //making a request to url and getting response
            val jsonStr =sh.makeServiceCall(url)

            Log.e(TAG, "Response from url : $jsonStr")

            jsonStr?.let {
                try {
                    val  jsonArray = JSONArray(jsonStr)
                    list_temp = mutableListOf()
                    list_humi = mutableListOf()

                    for (i in 0 until jsonArray.length()-1){
                        val c = jsonArray.getJSONObject(i)

                        val device_id = c.getString("device_id")
                        val user_id = c.getString("user_id")

                        //temperature and humidity in payload
                        val payload = c.getString("payload")
                        val jsonPayload = JSONObject(payload)

                        val temperature = jsonPayload.getString("temperature")
                        val humidity = jsonPayload.getString("humidity")

                        //list for temperature and humidity used for line graph
                        list_temp.add(Entry(i.toFloat(), temperature.toFloat()))
                        list_humi.add(Entry(i.toFloat(), humidity.toFloat()))

                        //temp hash map for single data list
                        val json_parsed = HashMap<String, String>()
                        json_parsed.put("device_id", device_id)
                        json_parsed.put("user_id", user_id)
                        json_parsed.put("temperature", temperature)
                        json_parsed.put("humidity", humidity)

                        //adding final values in final_data
                        Utils.final_data.add(json_parsed)
                    }
                }catch (e: JSONException){
                    Log.e(TAG, "JSON parsing error: ${e.message}")
                    MainActivity().runOnUiThread {
                        Toast.makeText(this@MainActivity,
                                "Json parsing error: " + e.message,
                                Toast.LENGTH_LONG)
                                .show()
                    }
                }
            } ?: kotlin.run {
                Log.e(TAG, "Couldn't get json from server")
                MainActivity().runOnUiThread {
                    Toast.makeText(this@MainActivity, "Couldn't get json from server",
                            Toast.LENGTH_LONG).show()
                }
            }

            setTempChart()
            setHumiChart()

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if (pDialog!!.isShowing)
                pDialog!!.dismiss();
        }

        private fun setTempChart() {
            lineDataSet = LineDataSet(list_temp, "Temperature in C")
            lineData = LineData(lineDataSet)
            line_temp!!.data = lineData
            lineDataSet!!.setColors(*ColorTemplate.JOYFUL_COLORS)
            lineDataSet!!.valueTextColor = Color.BLACK
            lineDataSet!!.valueTextSize = 12f
        }

        private fun setHumiChart() {
            lineDataSetHumi = LineDataSet(list_humi, "Humidity")
            lineDataHumi = LineData(lineDataSetHumi)
            line_humid!!.data = lineDataHumi
            lineDataSetHumi!!.setColors(*ColorTemplate.JOYFUL_COLORS)
            lineDataSetHumi!!.valueTextColor = Color.BLACK
            lineDataSetHumi!!.valueTextSize = 12f
        }
    }

    companion object{
        private val TAG = "MainActivity"
        // URL to get contacts JSON
        private val url = "https://22vdiubnf0.execute-api.ap-northeast-1.amazonaws.com/test/v1"

        private var instance: MainActivity? = null

        fun appContext() : Context {
            return instance!!.applicationContext
        }
    }
}
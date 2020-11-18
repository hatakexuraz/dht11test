package com.example.dht11test2

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dht11test2.MainActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName

    private var pDialog: ProgressDialog? = null
    private lateinit var line_temp: MutableList<Entry>
    private lateinit var line_humi: MutableList<Entry>
    var lineChartTemp: LineChart? = null
    var lineChartHumi: LineChart? = null
    var lineData: LineData? = null
    var lineDataSet: LineDataSet? = null
    var lineDataHumi: LineData? = null
    var lineDataSetHumi: LineDataSet? = null
    var final_data: ArrayList<HashMap<String, String>>? = null

    var btn_list_view: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_list_view = findViewById(R.id.btn_list_activity)
        lineChartTemp = findViewById(R.id.temperature_line)
        lineChartHumi = findViewById(R.id.line_humi)
        final_data = ArrayList()

        GetContacts().execute()

        btn_list_view!!.setOnClickListener {
            val intent = Intent(applicationContext, ListData::class.java)
            startActivity(intent)
        }
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private inner class GetContacts : AsyncTask<Void?, Void?, Void?>() {
        override fun onPreExecute() {
            super.onPreExecute()
            // Showing progress dialog
            pDialog = ProgressDialog(this@MainActivity)
            pDialog!!.setMessage("Please wait...")
            pDialog!!.setCancelable(false)
            pDialog!!.show()
        }

        override fun doInBackground(vararg params: Void?): Void? {
            val sh = HTTPHandler()

            // Making a request to url and getting response
            val jsonStr = sh.makeServiceCall(url)
            Log.e(TAG, "Response from url: $jsonStr")
            if (jsonStr != null) {
                try {
                    val jsonArray = JSONArray(jsonStr)
                    line_temp = mutableListOf()
                    line_humi = mutableListOf()
                    // Getting JSON Array node
//                    JSONObject main = jsonObj.getJSONObject(jsonStr);
//                    JSONObject main = new JSONObject(jsonStr);

                    // looping through All Contacts
                    for (i in 0 until jsonArray.length()) {
                        val c = jsonArray.getJSONObject(i)
                        val deviceId = c.getString("device_id")
                        val userId = c.getString("user_id")

                        // Phone node is JSON Object
                        val payload = c.getString("payload")
                        val payObj = JSONObject(payload)
                        val temperature = payObj.getString("temperature")
                        val humidity = payObj.getString("humidity")
                        line_temp.add(Entry(i.toFloat(), temperature.toFloat()))
                        line_humi.add(Entry(i.toFloat(), humidity.toFloat()))

                        Log.d(TAG, "temperature is: $temperature &&&&& ${line_temp?.get(i)}")

                        // tmp hash map for single contact
                        val json_parsed = HashMap<String, String>()

                        // adding each child node to HashMap key => value
                        json_parsed["device_id"] = deviceId
                        json_parsed["user_id"] = userId
                        json_parsed["temperature"] = temperature
                        json_parsed["humidity"] = humidity

                        Log.d(TAG,  "Device id is : ${json_parsed["device_id"]}")

                        // adding contact to contact list
                        Utils.final_data.add(json_parsed)
                    }
                } catch (e: JSONException) {
                    Log.e(TAG, "Json parsing error: " + e.message)
                    runOnUiThread {
                        Toast.makeText(applicationContext,
                                "Json parsing error: " + e.message,
                                Toast.LENGTH_LONG)
                                .show()
                    }
            }
            } else {
                Log.e(TAG, "Couldn't get json from server.")
                runOnUiThread {
                    Toast.makeText(applicationContext,
                            "Couldn't get json from server. Check LogCat for possible errors!",
                            Toast.LENGTH_LONG)
                            .show()
                }
            }
            setTempChart()
            setHumiChart()

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            // Dismiss the progress dialog
            if (pDialog!!.isShowing) pDialog!!.dismiss()
        }

        private fun setTempChart() {
            lineDataSet = LineDataSet(line_temp, "Temperature in C")
            lineData = LineData(lineDataSet)
            lineChartTemp!!.data = lineData
            lineDataSet!!.setColors(*ColorTemplate.JOYFUL_COLORS)
            lineDataSet!!.valueTextColor = Color.BLACK
            lineDataSet!!.valueTextSize = 12f
        }

        private fun setHumiChart() {
            lineDataSetHumi = LineDataSet(line_humi, "Humidity")
            lineDataHumi = LineData(lineDataSetHumi)
            lineChartHumi!!.data = lineDataHumi
            lineDataSetHumi!!.setColors(*ColorTemplate.JOYFUL_COLORS)
            lineDataSetHumi!!.valueTextColor = Color.BLACK
            lineDataSetHumi!!.valueTextSize = 12f
        }
    }

    companion object {
        // URL to get contacts JSON
        private const val url = "https://22vdiubnf0.execute-api.ap-northeast-1.amazonaws.com/test/v1"
    }
}
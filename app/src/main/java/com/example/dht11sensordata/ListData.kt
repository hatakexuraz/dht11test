package com.example.dht11sensordata

import android.os.Bundle
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_list_data.*

class ListData : AppCompatActivity() {
    private var lv: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_data)

        lv = list
        /**
         * Updating parsed JSON data into ListView
         * */

        /**
         * Updating parsed JSON data into ListView
         */
        val adapter: ListAdapter = SimpleAdapter(
            this@ListData, Utils.final_data,
            R.layout.list_item, arrayOf(
                "device_id", "user_id", "temperature",
                "humidity"
            ), intArrayOf(
                R.id.name,
                R.id.device, R.id.temp, R.id.humi
            )
        )

        lv!!.adapter = adapter
    }
}
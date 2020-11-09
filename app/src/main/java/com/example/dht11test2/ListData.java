package com.example.dht11test2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class ListData extends AppCompatActivity {
    private ListView lv;

    ArrayList<HashMap<String, String>> final_data;

    ListData(){}

    ListData(ArrayList<HashMap<String, String>> final_data){
        this.final_data = final_data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data);

        lv = (ListView) findViewById(R.id.list);
        /**
         * Updating parsed JSON data into ListView
         * */
        ListAdapter adapter = new SimpleAdapter(
                ListData.this, Utils.final_data,
                R.layout.list_item, new String[]{"device_id", "user_id","temperature",
                "humidity"}, new int[]{R.id.name,
                R.id.device, R.id.temp, R.id.humi});

        lv.setAdapter(adapter);
    }
}
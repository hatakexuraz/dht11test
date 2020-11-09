package com.example.dht11test2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private List line_temp;
    private List line_humi;
    LineChart lineChartTemp;
    LineChart lineChartHumi;
    LineData lineData;
    LineDataSet lineDataSet;
    LineData lineDataHumi;
    LineDataSet lineDataSetHumi;

    // URL to get contacts JSON
    private static String url = "https://22vdiubnf0.execute-api.ap-northeast-1.amazonaws.com/test/v1";

    ArrayList<HashMap<String, String>> final_data;

    Button btn_list_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_list_view = findViewById(R.id.btn_list_activity);
        lineChartTemp = findViewById(R.id.temperature_line);
        lineChartHumi = findViewById(R.id.line_humi);

        final_data = new ArrayList<>();

        new GetContacts().execute();

        final ListData listData = new ListData(final_data);

        btn_list_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, listData.getClass());
                startActivity(intent);
            }
        });

    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HTTPHandler sh = new HTTPHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONArray jsonArray = new JSONArray(jsonStr);
                    line_temp = new ArrayList();
                    line_humi = new ArrayList();
                    // Getting JSON Array node
//                    JSONObject main = jsonObj.getJSONObject(jsonStr);
//                    JSONObject main = new JSONObject(jsonStr);

                    // looping through All Contacts
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);

                        String deviceId = c.getString("device_id");
                        String userId = c.getString("user_id");

                        // Phone node is JSON Object
                        String payload = c.getString("payload");
                        JSONObject payObj = new JSONObject(payload);

                        String temperature = payObj.getString("temperature");
                        String humidity = payObj.getString("humidity");

                        line_temp.add(new Entry(i, Float.parseFloat(temperature)));
                        line_humi.add(new Entry(i, Float.parseFloat(humidity)));

                        // tmp hash map for single contact
                        HashMap<String, String> json_parsed = new HashMap<>();

                        // adding each child node to HashMap key => value
                        json_parsed.put("device_id", deviceId);
                        json_parsed.put("user_id", userId);
                        json_parsed.put("temperature", temperature);
                        json_parsed.put("humidity", humidity);

                        // adding contact to contact list
                        Utils.final_data.add(json_parsed);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            setTempChart();
            setHumiChart();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
        }


        private void setTempChart(){
            lineDataSet = new LineDataSet(line_temp, "Temperature in C");
            lineData = new LineData(lineDataSet);
            lineChartTemp.setData(lineData);
            lineDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
            lineDataSet.setValueTextColor(Color.BLACK);
            lineDataSet.setValueTextSize(12f);
        }

        private void setHumiChart(){
            lineDataSetHumi = new LineDataSet(line_humi, "Humidity");
            lineDataHumi = new LineData(lineDataSetHumi);
            lineChartHumi.setData(lineDataHumi);
            lineDataSetHumi.setColors(ColorTemplate.JOYFUL_COLORS);
            lineDataSetHumi.setValueTextColor(Color.BLACK);
            lineDataSetHumi.setValueTextSize(12f);
        }
    }
}
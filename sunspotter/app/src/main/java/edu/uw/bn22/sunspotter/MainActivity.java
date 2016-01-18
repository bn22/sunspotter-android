package edu.uw.bn22.sunspotter;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "LAYOUT_DEMO";
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText text = (EditText) findViewById(R.id.txtSearch);
        final Button button = (Button) findViewById(R.id.btnSearch);

        //This adds a listener for when the user clicks on the Find Sun button to see the text they have entered
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String zipCode = text.getText().toString();
                ForecastData task = new ForecastData();
                task.execute(zipCode);
            }
        });

        adapter = new ArrayAdapter<String>(
                this, R.layout.list_item, R.id.txtItem);

        //support ListView or GridView
        AdapterView listView = (AdapterView)findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    public class ForecastData extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params){
            String zip = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecast = null;

            try {
                //Constructs the URL for the Open Weather Map API
                Uri buildString = Uri.parse("http://api.openweathermap.org/data/2.5/forecast")
                        .buildUpon()
                        .appendQueryParameter("q", zip + ",us")
                        .appendQueryParameter("appid", BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();
                String urlString = buildString.toString();

                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }

                //Stores the result from the Open Weather Map API Call
                String results = buffer.toString();
                forecast = results;
            }
            catch (IOException e) {
                return null;
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (final IOException e) {
                    }
                }
            }
            return forecast;
        }

        protected void onPostExecute(String forecast){
            if(forecast != null) {
                try {
                    //Prepares for the new API results by clearing the current listview
                    adapter.clear();
                    JSONObject forecastObj = new JSONObject(forecast);
                    JSONArray forecastArray = forecastObj.getJSONArray("list");
                    Boolean foundSunny = false;

                    for (int i = 0; i < forecastArray.length(); i++) {
                        JSONObject forecastArrayObject = forecastArray.getJSONObject(i);

                        //Grabs the 3-Hour Weather Reports
                        String dt = forecastArrayObject.getString("dt");
                        Long passedTime = Long.valueOf(dt);
                        Date passedDate = new Date(passedTime * 1000L);
                        SimpleDateFormat formatter = new SimpleDateFormat("EEE HH:mm");
                        String finalDateResult = formatter.format(passedDate);
                        Log.v(TAG, finalDateResult);

                        //Grabs the temperature from the weather report
                        JSONObject mainTemperatureObject = forecastArrayObject.getJSONObject("main");
                        String temp = mainTemperatureObject.getString("temp");
                        Double tempKel = Double.parseDouble(temp);
                        Double tempFair = ((tempKel - 273.15) * 1.8) + 32;
                        int tempFinal;
                        if (tempFair >= 0) {
                            tempFinal = (int) (tempFair + 0.5);
                        } else {
                            tempFinal = (int) (tempFair - 0.5);
                        }
                        Log.v(TAG, Integer.toString(tempFinal));

                        //Grabs the current weather (i.e. Clear or Rainy) from the weather report
                        JSONArray mainWeatherObject = forecastArrayObject.getJSONArray("weather");
                        String weather = "";
                        JSONObject weatherObj = mainWeatherObject.getJSONObject(0);
                        weather = weatherObj.getString("main");
                        Log.v(TAG, weather);

                        String result = weather + " ( " + finalDateResult + " ) - " + tempFinal;
                        adapter.add(result);
                    }
                } catch (JSONException e) {
                    Log.v(TAG, "nope");
                }
            } else {
                Log.v(TAG, "no result");
            }
        }

    }
}

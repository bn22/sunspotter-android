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
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewStub;

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
import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "LAYOUT_DEMO";
    private ArrayAdapter<String> adapter;
    private ViewStub stub = null;
    private String location = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.btnSearch);

        //This adds a listener for when the user clicks on the Find Sun button to see the text they have entered
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = (EditText) findViewById(R.id.txtSearch);
                String zipCode = text.getText().toString();
                ForecastData task = new ForecastData();
                task.execute(zipCode);
            }
        });

        //Creates the listview that will contain forecast data
        adapter = new ArrayAdapter<String>(
                this, R.layout.list_item, R.id.txtItem);
        AdapterView listView = (AdapterView)findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    //Creates a background threading that downloads the weather forecast
    public class ForecastData extends AsyncTask<String, Void, ArrayList<WeatherData>> {
        protected ArrayList<WeatherData> doInBackground(String... params){
            String zip = params[0];
            String json = downloadJSON(zip);
            ArrayList<WeatherData> forecastResults = parseJSON(json);
            return forecastResults;
        }

        protected void onPostExecute(ArrayList<WeatherData> forecastData){
            //Ensures that the resulting parsed JSON is not empty
            if(forecastData != null) {

                //Prepares for the new API results by clearing the current list_view and populates with new information
                adapter.clear();
                String firstSunny = "";
                Boolean foundFirst = false;
                Boolean isSunny = false;
                for (WeatherData a : forecastData) {
                    if (a.getFoundSunnyDay() && !foundFirst) {
                        firstSunny = a.getDay() + " at " + a.getHour();
                        foundFirst = true;
                        isSunny = true;
                    }
                    adapter.add(a.toString());
                }

                //If the user is making their first query, then it will show the results section of the app
                if (stub == null) {
                    stub = (ViewStub) findViewById(R.id.results);
                    stub.inflate();
                }

                //Updates the application with the recently found sun data
                ImageView imgView = (ImageView)findViewById(R.id.picture);
                TextView sunnyResult = (TextView)findViewById(R.id.txtHeader);
                TextView sunnyWhen = (TextView)findViewById(R.id.txtText);
                TextView currentLocation = (TextView)findViewById(R.id.location);
                currentLocation.setText("The Current Location is " + location);
                if (isSunny) {
                    imgView.setImageResource(R.drawable.sunny);
                    sunnyResult.setText(R.string.yesSun);
                    sunnyWhen.setText("It will be sunny on " + firstSunny);
                } else {
                    imgView.setImageResource(R.drawable.rainy);
                    sunnyResult.setText(R.string.noSun);
                    sunnyWhen.setText("The 5 day forecast contains no sun");
                }

            } else {
                Log.v(TAG, "no result");
            }
        }

        private String downloadJSON(String input) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String results;
            try {
                //Constructs the URL for the Open Weather Map API with the api key and location
                Uri buildString = Uri.parse("http://api.openweathermap.org/data/2.5/forecast")
                        .buildUpon()
                        .appendQueryParameter("q", input + ",us")
                        .appendQueryParameter("appid", BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .appendQueryParameter("units", "imperial")
                        .build();
                String urlString = buildString.toString();

                Log.v(TAG, urlString);

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
                results = buffer.toString();
            }
            catch (IOException e) {
                Log.v(TAG, "IOException");
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
                        Log.v(TAG, "IOException final");
                    }
                }
            }
            Log.v(TAG, results);
            return results;
        }

        private ArrayList<WeatherData> parseJSON(String results) {
            ArrayList<WeatherData> forecast = new ArrayList<WeatherData>();
            try {
                //Starts parsing the JSON file
                JSONObject forecastObj = new JSONObject(results);

                //Finds the name of the input location
                JSONObject cityJSON = forecastObj.getJSONObject("city");
                //Log.v(TAG, cityJSON.toString());
                location = cityJSON.getString("name");
                //Log.v(TAG, location);

                //Pulls the forecast data
                JSONArray forecastArray = forecastObj.getJSONArray("list");

                for (int i = 0; i < forecastArray.length(); i++) {
                    WeatherData forecastData = new WeatherData();
                    JSONObject forecastArrayObject = forecastArray.getJSONObject(i);

                    //Grabs the 3-Hour Weather Reports
                    String dt = forecastArrayObject.getString("dt");
                    Long passedTime = Long.valueOf(dt);
                    Date passedDate = new Date(passedTime * 1000L);
                    SimpleDateFormat sunDay = new SimpleDateFormat("EEEE");
                    SimpleDateFormat sunHour = new SimpleDateFormat("h:mm aa");
                    forecastData.setDay(sunDay.format(passedDate));
                    forecastData.setHour(sunHour.format(passedDate));

                    //Grabs the temperature from the weather report
                    JSONObject mainTemperatureObject = forecastArrayObject.getJSONObject("main");
                    String temp = mainTemperatureObject.getString("temp");
                    forecastData.setTemperature(temp);

                    //Grabs the current weather (i.e. Clear or Rainy) from the weather report
                    JSONArray mainWeatherObject = forecastArrayObject.getJSONArray("weather");
                    String weather = "";
                    JSONObject weatherObj = mainWeatherObject.getJSONObject(0);
                    weather = weatherObj.getString("main");
                    forecastData.setWeather(weatherObj.getString("main"));

                    //Checks to see if the forecast is sunny
                    if (weather.equals("Clear")) {
                        forecastData.setFoundSunnyDay(true);
                    }
                    forecast.add(forecastData);
                }
            } catch(JSONException e) {
                Log.v(TAG, "JSONException error");
                return null;
            }
            return forecast;
        }
    }
}

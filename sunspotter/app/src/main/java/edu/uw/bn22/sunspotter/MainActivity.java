package edu.uw.bn22.sunspotter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "LAYOUT_DEMO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText text = (EditText)findViewById(R.id.txtSearch);
        final Button button = (Button)findViewById(R.id.btnSearch);

        //This adds a listener for when the user clicks on the Find Sun button to see the text they have entered
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String zipCode = text.getText().toString();
                Log.v(TAG, "You typed: " + zipCode);
                Log.v(TAG, BuildConfig.OPEN_WEATHER_MAP_API_KEY);
            }
        });
    }
}

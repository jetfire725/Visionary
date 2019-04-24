package com.d42n81.visionary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ReaderActivity extends AppCompatActivity {

    //This activity displays the text the user. And implements the users zoom settings

    TextView output;
    String text= "hello";
    float defaultTextSize = 36;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        SharedPreferences currentText = getSharedPreferences("currentText", 0);
        this.text = currentText.getString("currentString", "Error: No String found.");
    }

    public void startSettings(View v){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    //Refreshes the text with the users settings.
    public void updateDisplay(){
        runOnUiThread(new Runnable() {
            public void run() {
                SharedPreferences settings = getSharedPreferences("settings", 0);
                output = (TextView)findViewById(R.id.outputText);
                output.setTextColor(settings.getInt("fontColor", Color.WHITE));
                output.setBackgroundColor(settings.getInt("bgColor", Color.BLACK));
                output.setTextSize(1,defaultTextSize);
                // set output text:
                output.setText(text);

                output.invalidate();
            }
        });
    }
    //Adjusts the text size
    public void zoom(View v){
        String buttonName = getResources().getResourceEntryName(v.getId());
        if (buttonName.equals("minusButton")){
            defaultTextSize -=2;
            output.setTextSize(1,defaultTextSize);
        } else if (buttonName.equals("plusButton")) {
            defaultTextSize +=2;
            output.setTextSize(1,defaultTextSize);
        }
        output.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisplay();
    }
}

package com.example.visionary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ReaderActivity extends AppCompatActivity {

    TextView output;
    String text;
    float defaultTextSize = 36;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reader);
        TextView fontText = (TextView)findViewById(R.id.fontText);
        fontText.setTextColor(Color.WHITE);
    }
    public void startSettings(View v){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    public void updateDisplay(){
        runOnUiThread(new Runnable() {
            public void run() {
                SharedPreferences settings = getSharedPreferences("settings", 0);
                output = (TextView)findViewById(R.id.outputText);
                output.setTextColor(settings.getInt("fontColor", Color.WHITE));
                output.setBackgroundColor(settings.getInt("bgColor", Color.BLACK));
                output.setTextSize(1,defaultTextSize);
                output.invalidate();
            }
        });
    }
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

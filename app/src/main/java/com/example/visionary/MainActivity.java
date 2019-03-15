package com.example.visionary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    //Currently, this activity's only purpose is the capture the image and start the next activity.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent x = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        startActivityForResult(x, 1);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent x){
        if (requestCode == 1 && resultCode == RESULT_OK){
            Bundle extras = x.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            magicBox(imageBitmap);



        }
    }

    //HERE IS THE BITMAP IMAGE FUNCTION
    //TODO: PASS THE IMAGE TO YOUR CODE (THREAD, CLASS ETC)
    //RIGHT NOW, NOTHING IS DONE WITH THE IMAGE, AND THE READER ACTIVITY IS STARTED
    //WHEN YOUR CODE IS DONE, YOU NEED TO START THE READER ACTIVITY AND GIVE IT THE STRING
    public void magicBox(Bitmap x){
        // Create magic box ocr object:
        CustomOCRClass ocrHandler = new CustomOCRClass(x, this);
        // perform OCR, return string result:
        String resultOfOCR = ocrHandler.performOCR();
        Intent intent = new Intent(this, ReaderActivity.class);
        // pass the result to the reader intent, if it is not null:
        try{
            intent.putExtra("resultString", resultOfOCR);
        } catch (Exception e){
            Log.e("ERROR TAG", "ERROR: OCR Returned a null string to reader.");
            // We could then handle showing the image from this catch block as a failsafe.
        }
        startActivity(intent);
        finish();
    }
}

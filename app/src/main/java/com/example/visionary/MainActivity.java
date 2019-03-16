package com.example.visionary;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    //Currently, this activity's only purpose is the capture the image and start the next activity.
    final int MY_PERMISSIONS_REQUEST =2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST);
        } else {
            startCamera();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    startCamera();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void startCamera() {
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

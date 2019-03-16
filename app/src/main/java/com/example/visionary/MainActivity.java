package com.example.visionary;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Here is some code to get the original image returned from a camera intent:
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        this.imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        // Uncomment this line and use x in startActivityForResult to go back to getting the image
        // thumbnail:
//        Intent x = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        startActivityForResult(intent, 1);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent x){
        if (requestCode == 1 && resultCode == RESULT_OK){
            //Uncomment this code to go back to image thumbnail.
//            Bundle extras = x.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");

            // This code allows us to get the original image:
            Bitmap imageBitmap = null;
            try{
                imageBitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), imageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (imageBitmap != null) {
                magicBox(imageBitmap);
            } else {
                Log.v("My Tag", "Error, image bitmap is null");
            }




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

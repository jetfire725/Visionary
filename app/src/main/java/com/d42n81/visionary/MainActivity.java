package com.d42n81.visionary;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //Currently, this activity purpose is make sure the appropriate permissions are granted, capture the image, and start the next activity.
    final int MY_PERMISSIONS_REQUEST =2;
    String currentPhotoPath;
    Bitmap image;
    File photoFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST);
        } else {
            startCamera();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    startCamera();
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    public void startCamera() {

        try {
            createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.d42n81.android.fileprovider",
                    photoFile);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, 1);
        }
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent x) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
            //magicBox(imageBitmap);
            image = imageBitmap;
            ImageView view = findViewById(R.id.imageView);
            view.setImageBitmap(image);
        }
    }

    //this method creates the image file the camera intent will use
    private void createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        photoFile = image;
    }

    //code that takes image to OCR class for processing
    public void magicBox() throws IOException {
        EthansOCRClass ocrHandler = new EthansOCRClass(image, this);   // Create magic box ocr object:
        String resultOfOCR = ocrHandler.performOCR();    // perform OCR, return string result:

        //saves the resultString in a file that can be access by any activity in the app.
        SharedPreferences currentText = getSharedPreferences("currentText", 0);
        SharedPreferences.Editor editor = currentText.edit();
        editor.putString("currentString", resultOfOCR);
        editor.apply();

        photoFile.delete(); //delete photo for cleanup

        Intent intent = new Intent(this, ReaderActivity.class);
        startActivity(intent);
        finish();
    }

    public void process (View v) throws IOException {
        Thread thread = new Thread(){
            public void run(){
                try {
                    magicBox();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        TextView loading = findViewById(R.id.loading);
        loading.setText("Loading, please wait...");
        loading.invalidate();
        thread.start();
    }

    public void capture(View v){
        recreate();
    }

    public void rotate(View v){
        photoFile.delete();
        if (v == findViewById(R.id.rotateLeft)){
            rotateBitmap(image,270);
        } else {
            rotateBitmap(image,90);
        }
        ImageView view = findViewById(R.id.imageView);
        view.setImageBitmap(image);
        view.invalidate();
    }

    public void rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        image = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}

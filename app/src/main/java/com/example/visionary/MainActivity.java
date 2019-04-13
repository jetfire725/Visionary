package com.example.visionary;

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
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //Currently, this activity purpose is make sure the appropriate permissions are granted, capture the image, and start the next activity.
    final int MY_PERMISSIONS_REQUEST =2;
    String currentPhotoPath;
    Bitmap image;
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
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
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
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        Log.v("MyTag", imageFileName);
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.v("MyTag", "" + currentPhotoPath);
        return image;
    }

    //code that takes image to OCR class for processing
    public void magicBox(){
        TextView loading = findViewById(R.id.loading);
        loading.setText("Loading, please wait...");
        loading.invalidate();
        File file = new File(currentPhotoPath);
        //String rotationString = splitStringForOrientation(testMetaData(file));
        //Log.v("SplitResult", rotationString);

        EthansOCRClass ocrHandler = new EthansOCRClass(image, this);   // Create magic box ocr object:

        //ocrHandler.setImageRotationString(rotationString);

        String resultOfOCR = ocrHandler.performOCR();    // perform OCR, return string result:

        //saves the resultString in a file that can be access by any activity in the app.
        SharedPreferences currentText = getSharedPreferences("currentText", 0);
        SharedPreferences.Editor editor = currentText.edit();
        editor.putString("currentString", resultOfOCR);
        editor.apply();

        file.delete();


        Intent intent = new Intent(this, ReaderActivity.class);
        startActivity(intent);
        finish();
    }

    public void process (View v){
        TextView loading = findViewById(R.id.loading);
        loading.setText("Loading, please wait...");
        loading.invalidate();
        magicBox();
    }

    public void capture(View v){
        recreate();
    }

    public void rotate(View v){
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

    public String testMetaData(File f) {
        try {
            Metadata metadata = JpegMetadataReader.readMetadata(f);

           return print(metadata, "Using JpegMetadataReader");
        } catch (JpegProcessingException e) {
            print(e);
        } catch (IOException e) {
            print(e);
        }
        return null;
    }

    private String splitStringForOrientation(String s) {
        // This should return "Right side, top (Rotate 90 CW) for my Galaxy S8.
        return s.split("Orientation -")[1].split("X Resolution")[0];
    }

    private static String print(Metadata metadata, String method)
    {
        String returnString = "";
        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.print(' ');
        System.out.print(method);
        System.out.println("-------------------------------------------------");
        System.out.println();

        //
        // A Metadata object contains multiple Directory objects
        //
        for (Directory directory : metadata.getDirectories()) {

            //
            // Each Directory stores values in Tag objects
            //
            for (Tag tag : directory.getTags()) {
                System.out.println(tag);
                returnString += tag + " ";
            }

            //
            // Each Directory may also contain error messages
            //
            for (String error : directory.getErrors()) {
                System.err.println("ERROR: " + error);
                returnString += error + " ";
            }
        }
        return returnString;
    }

    private static void print(Exception exception)
    {
        System.err.println("EXCEPTION: " + exception);
    }
}

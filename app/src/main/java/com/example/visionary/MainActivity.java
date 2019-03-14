package com.example.visionary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

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
        Intent intent = new Intent(this, ReaderActivity.class);
        startActivity(intent);
        finish();
    }
}

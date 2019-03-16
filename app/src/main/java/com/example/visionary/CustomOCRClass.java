package com.example.visionary;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.googlecode.leptonica.android.Pix;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.googlecode.tesseract.android.TessBaseAPI;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.content.ContentValues.TAG;


public class CustomOCRClass {
    Bitmap srcBitmap;
    TessBaseAPI tessBaseApi;
    Context context;

    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/";
    private static final String TESSDATA = "tessdata";
    private static final String lang = "eng";

    public CustomOCRClass(Bitmap b, Context context) {
        this.context = context;
        // attempt to grayscale bitmap to improve accuracy:
        this.srcBitmap = toGrayscale(b);
        this.srcBitmap = Bitmap.createScaledBitmap(this.srcBitmap, 2480, 3508, true);
        //Attempt to remove some image noise:
        // This method actually decreased my confidence by approx. 20%.
//        this.srcBitmap = removeNoise(this.srcBitmap);
        // Attempt to sharpen bitmap if device supports api level 17 or above:
        boolean trySharpen = false;
        if(android.os.Build.VERSION.SDK_INT >= 17 && trySharpen) {
            float[] sharp = { -0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f,
                    -0.15f, -0.15f
            };
            this.srcBitmap = doSharpen(this.srcBitmap, sharp);
        }



    }

    public String performOCR() {
        System.out.println("Perform OCR Called");
//        ocrHandler.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);
        prepareTesseract();

//        System.out.println("MeanConfidence: " + ocrHandler.meanConfidence());

        return extractText(this.srcBitmap);
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public void resetImage(Bitmap b) {
        this.srcBitmap = b;
    }

    private void prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(TESSDATA);
    }

    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }

    }

    public void copyTessDataFiles(String path) {
        try {
            String fileList[] = context.getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = context.getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + "to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }

    private String extractText(Bitmap bitmap) {
        try {
            this.tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

        tessBaseApi.init(DATA_PATH, lang);



        Log.d(TAG, "Training file loaded");
        tessBaseApi.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        int meanConfiedence = tessBaseApi.meanConfidence();
        tessBaseApi.end();
        System.out.println("Resulting Text: " + extractedText);
        System.out.println("Mean Confidence of OCR: " + meanConfiedence);
        return extractedText;
    }

    //RemoveNoise
    public Bitmap removeNoise(Bitmap bmap) {
        // Turns bitmap completely black and white after grayscaling it.
        // The intention of this method is to go through each pixel of the bitmap and see if the rgb values
        // are over or under a certain threshold. If they are too red,blue, and green, the pixel is dark gray.
        // In the case of a dark gray pixel, I should change it to pure black. This means OCR will ignore that pixel.
        // If the pixel is below that color threshold, the pixel is light gray. I should change it to
        // white so it is picked up by OCR easier.

        for (int x = 0; x < bmap.getWidth(); x++) {
            for (int y = 0; y < bmap.getHeight(); y++) {
                int pixelColor = bmap.getPixel(x, y);
                int red = Color.red(pixelColor);
                int blue = Color.blue(pixelColor);
                int green = Color.green(pixelColor);
                int alpha = Color.alpha(pixelColor);
                if (red < 162 && blue < 162 && green < 162)
                    bmap.setPixel(x, y, Color.BLACK);
                else if (red > 162 && blue < 162 && green < 162)
                    bmap.setPixel(x, y, Color.WHITE);
            }
        }

        return bmap;
    }
    @TargetApi(17)
    public Bitmap doSharpen(Bitmap original, float[] radius) {
        Bitmap bitmap = Bitmap.createBitmap(
                original.getWidth(), original.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(this.context);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicConvolve3x3 convolution
                = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
        convolution.setInput(allocIn);
        convolution.setCoefficients(radius);
        convolution.forEach(allocOut);

        allocOut.copyTo(bitmap);
        rs.destroy();

        return bitmap;

    }
}

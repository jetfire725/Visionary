package com.example.visionary;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

public class EthansOCRClass {
    Bitmap srcBitmap;
    String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/";
    String TESSDATA = "tessdata";


    public EthansOCRClass(Bitmap b, Context context) {
        this.srcBitmap = b;


    }

    public String performOCR() {
        TessBaseAPI tessBaseApi;

        System.out.println("Perform OCR Called");

        File dir = new File(DATA_PATH + TESSDATA);

        tessBaseApi = new TessBaseAPI();
        tessBaseApi.init(DATA_PATH, "eng");
        tessBaseApi.setImage(this.srcBitmap);
        return tessBaseApi.getUTF8Text();



    }

}
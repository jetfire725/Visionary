package com.example.visionary;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EthansOCRClass {
    Bitmap srcBitmap;
    Context context;
    int meanConfidence;

    public EthansOCRClass(Bitmap b, Context c) {
        this.srcBitmap = b;
        this.context=c;
        this.meanConfidence = 0;
    }

    public String performOCR() throws IOException {
        File f = new File(Environment.getExternalStorageDirectory().toString() + "/TesseractSample/tessdata");
        if (!f.exists()){
            f.mkdirs();
            //read in stream
            InputStream in = context.getAssets().open("tessdata/eng.traineddata");
            byte[] buf = new byte[in.available()];
            in.read(buf);
            in.close();
            //write out stream
            OutputStream out = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/TesseractSample/tessdata/eng.traineddata");
            out.write(buf);
            out.close();
        }
        System.out.println("Perform OCR Called");
        TessBaseAPI tessBaseApi = new TessBaseAPI();
        tessBaseApi.init(Environment.getExternalStorageDirectory().toString() + "/TesseractSample/", "eng");
        tessBaseApi.setImage(this.srcBitmap);
        // Set result of getUTF8Text to a string and recorded mean confidence as variable.
        // This allows us to close the tessBaseApi.
        String resultOfOCR = tessBaseApi.getUTF8Text();
        this.meanConfidence = tessBaseApi.meanConfidence();
        tessBaseApi.end();
        return resultOfOCR;
    }

}
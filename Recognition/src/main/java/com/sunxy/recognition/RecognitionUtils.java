package com.sunxy.recognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;

/**
 * SunXiaoYu on 2019/4/10.
 * mail: sunxiaoyu@hexinpass.com
 */
public class RecognitionUtils {

    private static TessBaseAPI tessBaseAPI;
    private static String LANGUAGE = "cn";
//    private static String LANGUAGE = "chi_sim";

    public static void init(final Context context, Bitmap bitmap, final OnInitListener listener){
        if (bitmap == null){
            return;
        }

        new AsyncTask<Bitmap, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (listener != null){
                    listener.startInit();
                }
            }

            @Override
            protected String doInBackground(Bitmap... voids) {
                if (tessBaseAPI == null){
                    File filesDir = FileUtils.copyTraineddataFile(context, LANGUAGE);
                    tessBaseAPI = new TessBaseAPI();
                    tessBaseAPI.init(filesDir.getAbsolutePath(), LANGUAGE);
                }
                tessBaseAPI.setImage(voids[0]);
                return tessBaseAPI.getUTF8Text();
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                tessBaseAPI.clear();
                if (listener != null){
                    listener.endInitAndRecognition(result);
                }
            }
        }.execute(bitmap);
    }


    public interface OnInitListener{
        void startInit();
        void endInitAndRecognition(String result);
    }
}

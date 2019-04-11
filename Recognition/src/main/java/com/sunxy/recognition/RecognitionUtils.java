package com.sunxy.recognition;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * SunXiaoYu on 2019/4/10.
 * mail: sunxiaoyu@hexinpass.com
 */
public class RecognitionUtils {

    private static TessBaseAPI tessBaseAPI;
    private static String LANGUAGE = "cn";
//    private static String LANGUAGE = "chi_sim";

    public static void init(final Context context, final OnInitListener listener){
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (listener != null){
                    listener.startCopyAssert();
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                if (tessBaseAPI != null){
                    return null;
                }
                File filesDir = context.getExternalFilesDir("tess");
                String fileName = LANGUAGE + ".traineddata";
                File file = new File(filesDir.getAbsolutePath() + "/tessdata", fileName);
                if (!file.exists()){
                    copyAssetsFile(context, fileName, file.getParentFile());
                }
                tessBaseAPI = new TessBaseAPI();
                tessBaseAPI.init(filesDir.getAbsolutePath(), LANGUAGE);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (listener != null){
                    listener.endCopyAssert();
                }
            }
        }.execute();
    }

    public static String recognitionBmp(Bitmap bmp){
        String result = "";
        if (tessBaseAPI != null){
            tessBaseAPI.setImage(bmp);
            result = tessBaseAPI.getUTF8Text();
            tessBaseAPI.clear();
        }
        return result;
    }


    private static void copyAssetsFile(Context context, String name, File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, name);
        if (!file.exists()) {
            try {
                InputStream is = context.getAssets().open(name);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnInitListener{
        void startCopyAssert();
        void endCopyAssert();
    }
}

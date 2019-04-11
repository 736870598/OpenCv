package com.sunxy.detection;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.Surface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * SunXiaoYu on 2019/4/8.
 * mail: sunxiaoyu@hexinpass.com
 */
public class DetectionUtils {

    static {
        System.loadLibrary("detection-lib");
    }

    public static native void loadModel(String detectModel);
    public static native void setSurface(Surface surface, int w, int h);
    public static native boolean process(Bitmap bitmap);
    public static native Bitmap detectionBmp(Bitmap bitmap, Bitmap.Config config);
    public static native void destroy();

    public static native Bitmap getTargetImg(Bitmap src, Bitmap tpl, Bitmap.Config config);

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
                File filesDir = context.getExternalFilesDir("assert");
                File file = new File(filesDir, "haarcascade_frontalface_alt.xml");
                if (!file.exists()){
                    copyAssetsFile(context, "haarcascade_frontalface_alt.xml", filesDir);
                }
                loadModel(file.getAbsolutePath());
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

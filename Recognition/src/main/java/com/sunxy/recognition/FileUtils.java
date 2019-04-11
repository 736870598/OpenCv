package com.sunxy.recognition;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * SunXiaoYu on 2019/4/11.
 * mail: sunxiaoyu@hexinpass.com
 */
public class FileUtils {

    public static File copyTraineddataFile(Context context, String name){
        File filesDir = context.getExternalFilesDir("tess");
        String fileName = name + ".traineddata";
        File file = new File(filesDir.getAbsolutePath() + "/tessdata", fileName);
        if (!file.exists()){
            copyAssetsFile(context, fileName, file.getParentFile());
        }
        return filesDir;
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
}

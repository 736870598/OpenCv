package com.sunxy.sxyimagedetection.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

/**
 * BitmapUtils
 *
 * SunXiaoYu on 2019/4/10.
 * mail: sunxiaoyu@hexinpass.com
 */
public class BitmapUtils {

    public static Bitmap createBmp(Context context, String path){
        return createBmp(path, 1080, 1920);
    }

    public static Bitmap createBmp(String path, int maxW, int maxH){
        if (TextUtils.isEmpty(path))
            return null;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp <= maxW && height_tmp <= maxH) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = scale;
        opts.outHeight = height_tmp;
        opts.outWidth = width_tmp;
        return BitmapFactory.decodeFile(path, opts);
    }

}

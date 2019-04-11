package com.sunxy.detection;

import android.graphics.Bitmap;

/**
 * 处理图片
 * SunXiaoYu on 2019/4/11.
 * mail: sunxiaoyu@hexinpass.com
 */
public class ImageUtils {

    static {
        System.loadLibrary("detection-lib");
    }

    private static native Bitmap delBitmap(Bitmap bitmap, Bitmap.Config config);

    /**
     * 为ORC专门处理图片（灰度图 、二值图、高斯图）
     */
    public static Bitmap delBitmapForOrc(Bitmap bitmap){
        if (bitmap != null){
            Bitmap resultBmp = delBitmap(bitmap, Bitmap.Config.ARGB_8888);
            if (bitmap != null && !bitmap.isRecycled()){
                bitmap.recycle();
            }
            return resultBmp;
        }
        return null;
    }

}

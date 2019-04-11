//
// Created by Administrator on 2019/4/9.
//

#ifndef SXYIMAGEDETECTION_COMMON_H
#define SXYIMAGEDETECTION_COMMON_H


#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <android/log.h>


#define LOG_TAG "native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

extern "C" {

using namespace cv;
using namespace std;

extern JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nBitmapToMat2
        (JNIEnv *env, jclass, jobject bitmap, jlong m_addr, jboolean needUnPremultiplyAlpha);

extern JNIEXPORT void JNICALL Java_org_opencv_android_Utils_nMatToBitmap
        (JNIEnv *env, jclass, jlong m_addr, jobject bitmap);
}

//图片转Mat
void bitmap2Mat(JNIEnv *env, jclass instance, jobject bitmap,  jlong src) {
    Java_org_opencv_android_Utils_nBitmapToMat2(env, instance, bitmap, src, 0);
}

//Mat转图片
jobject mat2Bitmap(JNIEnv *env, Mat srcData, jobject config) {
    int imgWidth = srcData.cols;
    int imgHeight = srcData.rows;
    jclass bmpCls = env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapMid = env->GetStaticMethodID(bmpCls, "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jobject jBmpObj = env->CallStaticObjectMethod(bmpCls, createBitmapMid, imgWidth, imgHeight, config);
    Java_org_opencv_android_Utils_nMatToBitmap(env, 0, (jlong) &srcData, jBmpObj);
    return jBmpObj;
}


#endif //SXYIMAGEDETECTION_COMMON_H

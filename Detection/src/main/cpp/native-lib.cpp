
#include "common.h"

extern "C" {

CascadeClassifier *faceClassifier;
ANativeWindow *nativeWindow;

//识别人脸....
Mat detect(Mat src){
    if (faceClassifier){
        vector<Rect> faces;
        //图像灰度化
        Mat grayMat;
        cvtColor(src, grayMat, CV_BGR2GRAY);
        //直方图均衡化
        equalizeHist(grayMat, grayMat);

        //识别人脸。
        faceClassifier->detectMultiScale(grayMat, faces);
        grayMat.release();

        LOGI("detection face size = %d", faces.size());
        for (int i = 0; i < faces.size(); ++i) {
            Rect face = faces[i];
            rectangle(src, face.tl(), face.br(), Scalar(0,255,255));
            LOGI("index: %d, face.tl() = (%d , %d), face.br() = (%d , %d)", i, face.tl().x,face.tl().y, face.br().x,face.br().y);
        }
    }
    return src;
}

JNIEXPORT void JNICALL
Java_com_sunxy_detection_DetectionUtils_loadModel(JNIEnv *env, jclass, jstring detectModel_) {

    const char *detectModel = env->GetStringUTFChars(detectModel_, 0);
    LOGI("model path = %s", detectModel);
    faceClassifier = new CascadeClassifier(detectModel);
    env->ReleaseStringUTFChars(detectModel_, detectModel);
}

JNIEXPORT void JNICALL
Java_com_sunxy_detection_DetectionUtils_setSurface(JNIEnv *env, jclass , jobject surface,
                                                   jint w, jint h) {
    if (surface && w && h){
        if (nativeWindow){
            ANativeWindow_release(nativeWindow);
            nativeWindow = 0;
        }
        nativeWindow = ANativeWindow_fromSurface(env, surface);
        if (nativeWindow) {
            ANativeWindow_setBuffersGeometry(nativeWindow, w, h, WINDOW_FORMAT_RGBA_8888);
        }
    } else {
        if (nativeWindow) {
            ANativeWindow_release(nativeWindow);
            nativeWindow = 0;
        }
    }

}

JNIEXPORT jboolean JNICALL
Java_com_sunxy_detection_DetectionUtils_process(JNIEnv *env, jclass instance, jobject bitmap) {
    int ret = 1;
    Mat src;

    bitmap2Mat(env, instance, bitmap, (jlong) &src);
    detect(src);

    if (!nativeWindow){
        ret = 0;
        goto end;
    }

    ANativeWindow_Buffer window_buffer;

    if (ANativeWindow_lock(nativeWindow, &window_buffer, 0)){
        ret = 0;
        goto  end;
    }

    resize(src, src, Size(window_buffer.width, window_buffer.height));
    memcpy(window_buffer.bits, src.data, window_buffer.width * window_buffer.height * 4);

    ANativeWindow_unlockAndPost(nativeWindow);

    end:
    src.release();
    return ret;

}

JNIEXPORT jobject JNICALL
Java_com_sunxy_detection_DetectionUtils_detectionBmp(JNIEnv *env, jclass type, jobject bitmap, jobject config) {
    Mat src;
    bitmap2Mat(env, type, bitmap, (jlong) &src);
    detect(src);
    bitmap = mat2Bitmap(env, src, config);
    src.release();
    return bitmap;
}


JNIEXPORT void JNICALL
Java_com_sunxy_detection_DetectionUtils_destroy(JNIEnv *, jclass) {
    if (faceClassifier){
        delete faceClassifier;
        faceClassifier = 0;
    }
    if (nativeWindow){
        ANativeWindow_release(nativeWindow);
        nativeWindow = 0;
    }
}


JNIEXPORT jobject JNICALL
Java_com_sunxy_detection_DetectionUtils_getTargetImg(JNIEnv *env, jclass type, jobject src, jobject tpl, jobject config) {

    //模版图
    Mat img_tpl;
    bitmap2Mat(env,type, tpl, (jlong) &img_tpl);

    //原始图
    Mat img_src;
    bitmap2Mat(env,type, src, (jlong) &img_src);

    //灰度图 需要拿去模版匹配
    Mat img_gray;
    cvtColor(img_src, img_gray, COLOR_BGRA2GRAY);

    //二值图 进行轮廓检测
    Mat img_threshold;
    threshold(img_gray, img_threshold, 195, 255, THRESH_TRUNC);

    //Gaussian 图片
    Mat img_gaussian;
    GaussianBlur(img_threshold, img_gaussian, Size(3, 3), 0);

    // Canny 图片
    Mat img_canny;
    Canny(img_gaussian, img_canny, 180, 255);

    vector<vector<Point>> contours;
    vector<Vec4i> hierachy;
    //查找轮廓
    findContours(img_canny, contours, hierachy, RETR_LIST, CHAIN_APPROX_SIMPLE);
    int width = img_src.cols >> 1;
    int height = img_src.rows >> 1;

    vector<Rect> roiArea;
    Rect rectMin;
    for (int i = 0; i < contours.size(); ++i) {
        vector<Point> v = contours.at(i);
        Rect rect = boundingRect(v);
//        //画出轮廓
//        rectangle(img_threshold, rect, Scalar(255,255,255));
        if (rect.width >= width && rect.height >= height){
            roiArea.push_back(rect);
        }
    }
    if (roiArea.size() > 0){
        rectMin = roiArea.at(0);
        for (int i = 0; i < roiArea.size(); ++i) {
            Rect temp = roiArea.at(i);
            if (temp.area() < rectMin.area()){
                rectMin = temp;
            }
        }
    }else{
        rectMin = Rect(0,0,img_gray.cols,img_gray.rows);
    }

    //获得的身份证图
    Mat img_idCard = img_gray(rectMin);

    resize(img_idCard, img_idCard, Size(640,480));
    resize(img_tpl, img_tpl, Size(153, 28));
    cvtColor(img_tpl, img_tpl, COLOR_BGRA2GRAY);

    //创建输出图像，输出图像的宽度 = 被查找图像的宽度 - 模版图像的宽度 + 1
    Mat match;
    matchTemplate(img_idCard, img_tpl, match, TM_CCORR_NORMED);
    //归一化
    normalize(match, match, 0, 1, NORM_MINMAX, -1);
    Point maxLoc;
    minMaxLoc(match, 0, 0, 0, &maxLoc);
    //计算 [身份证(模版):号码区域]
    //号码区域:
    //x: 身份证(模版)的X+宽
    //y: 身份证(模版)Y
    //w: 全图宽-(身份证(模版)X+身份证(模版)宽) - n(给个大概值)
    //h: 身份证(模版)高
    Rect rect(
            maxLoc.x + img_tpl.cols + 10,
            maxLoc.y - 5,
            img_idCard.cols - (maxLoc.x + img_tpl.cols) - 50,
            img_tpl.rows + 15
    );

    Mat img_idNumber = img_idCard(rect);
    jobject result = mat2Bitmap(env, img_idNumber, config);

    img_src.release();
    img_gray.release();
    img_threshold.release();
    img_gaussian.release();
    img_canny.release();
    img_idCard.release();
    img_idNumber.release();
    img_tpl.release();
    match.release();

    return result;

}

JNIEXPORT jobject JNICALL
        Java_com_sunxy_detection_ImageUtils_delBitmap(JNIEnv *env, jclass type, jobject bitmap, jobject config) {

    //原始图
    Mat img_src;
    bitmap2Mat(env,type, bitmap, (jlong) &img_src);

    //灰度图 需要拿去模版匹配
    Mat img_gray;
    cvtColor(img_src, img_gray, COLOR_BGRA2GRAY);

    //二值图 进行轮廓检测
    Mat img_threshold;
    threshold(img_gray, img_threshold, 195, 255, THRESH_TRUNC);

    //Gaussian 图片
    Mat img_gaussian;
    GaussianBlur(img_threshold, img_gaussian, Size(3, 3), 0);

    jobject result = mat2Bitmap(env, img_gaussian, config);

    img_src.release();
    img_gray.release();
    img_threshold.release();
    img_gaussian.release();

    return result;

}

}




package com.sunxy.sxyimagedetection;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.sunxiaoyu.utils.UtilsCore;
import com.sunxiaoyu.utils.core.model.ActivityResultInfo;
import com.sunxiaoyu.utils.core.utils.StringUtils;
import com.sunxy.detection.DetectionUtils;
import com.sunxy.detection.ImageUtils;
import com.sunxy.recognition.RecognitionUtils;
import com.sunxy.sxyimagedetection.utils.BitmapUtils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * SunXiaoYu on 2019/4/10.
 * mail: sunxiaoyu@hexinpass.com
 */
public class FindIcCardActivity extends AppCompatActivity{

    private Button choosePic;
    private Button startFind;
    private ImageView imageView;
    private Disposable dis;
    private Bitmap resultBmp;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_id_card_layout);

        imageView = findViewById(R.id.imageView);
        choosePic = findViewById(R.id.choosePic);
        choosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePic();
            }
        });
        startFind = findViewById(R.id.startFind);
        startFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startFind();
            }
        });

    }


    private void choosePic(){
        dis = UtilsCore.manager().selectPicture(this, 1000)
                .subscribe(new Consumer<ActivityResultInfo>() {
                    @Override
                    public void accept(ActivityResultInfo activityResultInfo) throws Exception {
                        String path = activityResultInfo.getPhotoPath();
                        if (StringUtils.isNotNull(path)){
                            Bitmap bitmap = BitmapUtils.createBmp(FindIcCardActivity.this, path);
                            Bitmap template = BitmapFactory.decodeResource(getResources(), R.mipmap.te);
                            resultBmp = DetectionUtils.getTargetImg(bitmap, template, Bitmap.Config.ARGB_8888);
//                            resultBmp = ImageUtils.delBitmapForOrc(bitmap);
                            if (bitmap != null && !bitmap.isRecycled()){
                                bitmap.recycle();
                            }
                            if (template != null && !template.isRecycled()){
                                template.recycle();
                            }
                            imageView.setImageBitmap(resultBmp);
                        }
                    }
                });
    }

    private void startFind(){
        RecognitionUtils.init(this, new RecognitionUtils.OnInitListener() {
            @Override
            public void startCopyAssert() {
                progressDialog = new ProgressDialog(FindIcCardActivity.this);
                progressDialog.setMessage("请稍候...");
                progressDialog.show();
            }

            @Override
            public void endCopyAssert() {
                dismissDialog();
                Disposable disposable = Observable.just(resultBmp)
                        .map(new Function<Bitmap, String>() {
                            @Override
                            public String apply(Bitmap bitmap) throws Exception {
                                return RecognitionUtils.recognitionBmp(resultBmp);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                startFind.setText(s);
                            }
                        });
//                String cardStr = RecognitionUtils.recognitionBmp(resultBmp);
//                startFind.setText(cardStr);
            }
        });
    }

    private void dismissDialog(){
        if (progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialog();
        if (dis != null && !dis.isDisposed()){
            dis.dispose();
        }
    }
}

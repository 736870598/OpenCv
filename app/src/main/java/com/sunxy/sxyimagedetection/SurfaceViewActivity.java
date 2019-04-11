package com.sunxy.sxyimagedetection;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.sunxiaoyu.utils.UtilsCore;
import com.sunxiaoyu.utils.core.model.ActivityResultInfo;
import com.sunxiaoyu.utils.core.utils.StringUtils;
import com.sunxy.detection.DetectionUtils;
import com.sunxy.sxyimagedetection.utils.BitmapUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * SunXiaoYu on 2019/4/10.
 * mail: sunxiaoyu@hexinpass.com
 */
public class SurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private Button choosePic;
    private SurfaceView surfaceView;
    private Disposable dis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.surface_view_layout);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);

        choosePic = findViewById(R.id.choosePic);
        choosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePic();
            }
        });

        DetectionUtils.init(this, new DetectionUtils.OnInitListener() {
            @Override
            public void startCopyAssert() {
                choosePic.setEnabled(false);
            }

            @Override
            public void endCopyAssert() {
                choosePic.setEnabled(true);
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        DetectionUtils.setSurface(holder.getSurface(), 640, 480);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void choosePic(){
        dis = UtilsCore.manager().selectPicture(this, 1000)
                .subscribe(new Consumer<ActivityResultInfo>() {
                    @Override
                    public void accept(ActivityResultInfo activityResultInfo) throws Exception {
                        String path = activityResultInfo.getPhotoPath();
                        if (StringUtils.isNotNull(path)){
                            Bitmap bitmap = BitmapUtils.createBmp(SurfaceViewActivity.this, path);
                            DetectionUtils.process(bitmap);
                            if (bitmap != null && !bitmap.isRecycled()){
                                bitmap.recycle();
                            }
                        }
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DetectionUtils.destroy();
        if (dis != null && !dis.isDisposed()){
            dis.dispose();
        }
    }
}

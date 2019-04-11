package com.sunxy.sxyimagedetection;

import android.graphics.Bitmap;
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
import com.sunxy.sxyimagedetection.utils.BitmapUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 返回bmp；
 *
 * SunXiaoYu on 2019/4/10.
 * mail: sunxiaoyu@hexinpass.com
 */
public class ReturnBmpActivity extends AppCompatActivity {

    private Button choosePic;
    private ImageView imageView;
    private Disposable dis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.return_bmp_layout);
        imageView = findViewById(R.id.imageView);
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

    public void choosePic(){
        dis = UtilsCore.manager().selectPicture(this, 1000)
                .subscribe(new Consumer<ActivityResultInfo>() {
                    @Override
                    public void accept(ActivityResultInfo activityResultInfo) throws Exception {
                        String path = activityResultInfo.getPhotoPath();
                        if (StringUtils.isNotNull(path)){
                            Bitmap bitmap = BitmapUtils.createBmp(ReturnBmpActivity.this, path);
                            Bitmap result = DetectionUtils.detectionBmp(bitmap, Bitmap.Config.ARGB_8888);
                            if (bitmap != null && !bitmap.isRecycled()){
                                bitmap.recycle();
                            }
                            imageView.setImageBitmap(result);
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

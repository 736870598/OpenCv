package com.sunxy.sxyimagedetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.sunxiaoyu.utils.UtilsCore;
import com.sunxiaoyu.utils.core.model.ActivityResultInfo;
import com.sunxiaoyu.utils.core.utils.StringUtils;
import com.sunxy.detection.DetectionUtils;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void returnBmpActivity(View view){
        startActivity(new Intent(this, ReturnBmpActivity.class));
    }

    public void surfaceViewActivity(View view){
        startActivity(new Intent(this, SurfaceViewActivity.class));
    }
    public void findIcCardActivity(View view){
        startActivity(new Intent(this, FindIcCardActivity.class));
    }


}

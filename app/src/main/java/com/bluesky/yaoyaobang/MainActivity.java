package com.bluesky.yaoyaobang;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements SensorEventListener, SeekBar.OnSeekBarChangeListener {
    private DrawSV mSvDraw;
    private TextView mTvStrength, mTvDelay;
    private SeekBar mSbStrength, mSbDelay;
    private int mStrength = 10;
    private int mDelay = 5;

    private static final int FORWARD = 1;
    private static final int BACKWARD = -1;
    private static final int ORIGINAL = 0;
    private int mDirection = ORIGINAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSvDraw = findViewById(R.id.sv_draw);
        mTvStrength = findViewById(R.id.tv_strength);
        mSbStrength = findViewById(R.id.sb_strength);
        mTvDelay = findViewById(R.id.tv_delay);
        mSbDelay = findViewById(R.id.sb_delay);

        hideSystemUI();
/*        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN)==0){
                    hideSystemUI();
                }
            }
        });*/

        getDensity();
        //getPermissions();
        FontX fontX = new FontX(this);
        byte[][] pointArr = fontX.resolveString("中");
        mSvDraw.setData(pointArr);

        mSbStrength.setOnSeekBarChangeListener(this);
        mSbDelay.setOnSeekBarChangeListener(this);
        sensorInit();
    }

    private void getPermissions() {
        XXPermissions.with(this)
                .permission(Permission.BODY_SENSORS)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(@NonNull List<String> permissions, boolean allGranted) {
                        Toast.makeText(MainActivity.this, "获取传感器权限成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        Toast.makeText(MainActivity.this, "获取传感器权限失败", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void sensorInit() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);


    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /*
    获取分辨率
     */
    private void getDensity() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Log.d("第一种:", metrics.toString());

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Log.d("第二种:", displayMetrics.toString());
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        mSvDraw.setDensity(width, height);
    }


    private int sensorCount = 0;
    private int directionCount = 0;
    private float directionX = 0;
    private float relativeX = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        long start = System.currentTimeMillis();

        float x = event.values[0];
        float y = event.values[1];
/*        if (sensorCount < 100) {
            Log.d("传感器数据:", String.format("X=%f  Y=%f", x, y));
        }*/
        //旧方案
/*        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && Math.abs(x) > mStrength) {

            //判断方向,不同向则赋值记录当前方向.(DrawSV里面也判断方向了.但是应该放在这里,去掉重复判断)
            int currentDirection = x > 0 ? FORWARD : BACKWARD;
            //加一个延时,很短时间内,方向改变,不调用绘制.
            //或者:来两到三次同一方向,才绘制
            if (currentDirection != mDirection) {
                Log.d("方向:", String.format("temp=%d,mDirection=%d", currentDirection, mDirection));
                mDirection = currentDirection;
                mSvDraw.drawing(x > 0, start);
            }
        }*/

        //新方案
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            int currentDirection = x > 0 ? FORWARD : BACKWARD;
            //当方向连续相同,且X方向上间隔值>1.则计数.
            //应加入判定:当当前绘制进行中,则不绘制.
            if (currentDirection == mDirection && Math.abs(x - directionX) > 1) {
                Log.d("传感器数据:", String.format("X=%f  Y=%f", x, y));

                //relativeX = Math.abs(x - directionX);
                directionCount++;
                directionX=x;
                if (directionCount >= 2) {
                    //连续三次,X方向都以0.2的幅度增加,触发绘制
                    mSvDraw.drawing(x > 0, start);
                    directionCount = 0;
                }
            } else {
                directionCount = 0;
                mDirection = currentDirection;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sb_strength) {
            mTvStrength.setText(String.format("强度:%d", progress));
            mStrength = progress;
        }
        if (seekBar.getId() == R.id.sb_delay) {
            mTvDelay.setText(String.format("延迟:%d", progress));
            mDelay = progress;
            mSvDraw.setDelay(mDelay);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


}
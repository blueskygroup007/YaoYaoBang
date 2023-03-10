package com.bluesky.yaoyaobang;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
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
        byte[][] pointArr = fontX.resolveString("???");
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
                        Toast.makeText(MainActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenied(@NonNull List<String> permissions, boolean doNotAskAgain) {
                        Toast.makeText(MainActivity.this, "???????????????????????????", Toast.LENGTH_SHORT).show();

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
    ???????????????
     */
    private void getDensity() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Log.d("?????????:", metrics.toString());

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Log.d("?????????:", displayMetrics.toString());
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        mSvDraw.setDensity(width, height);
    }

//???????????????????????????
    //????????????
    private int directionCount = 0;
    //????????????X????????????
    private float direction_pre_X = 0;
    private float relativeX = 0;
    //????????????
    private int direction_step=1;

    @Override
    public void onSensorChanged(SensorEvent event) {
        long start = System.currentTimeMillis();

        float x = event.values[0];
        float y = event.values[1];

        //?????????
/*        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && Math.abs(x) > mStrength) {

            //????????????,????????????????????????????????????.(DrawSV????????????????????????.????????????????????????,??????????????????)
            int currentDirection = x > 0 ? FORWARD : BACKWARD;
            //???????????????,???????????????,????????????,???????????????.
            //??????:???????????????????????????,?????????
            if (currentDirection != mDirection) {
                Log.d("??????:", String.format("temp=%d,mDirection=%d", currentDirection, mDirection));
                mDirection = currentDirection;
                mSvDraw.drawing(x > 0, start);
            }
        }*/

        //?????????
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            int currentDirection = x > 0 ? FORWARD : BACKWARD;
            //?????????????????????,???X??????????????????>1.?????????.
            //???????????????:????????????????????????,????????????.
            if (currentDirection == mDirection && Math.abs(x - direction_pre_X) > direction_step) {
                Log.d("???????????????:", String.format("X=%f  Y=%f", x, y));

                directionCount++;
                direction_pre_X =x;
                if (directionCount >= 2) {
                    //????????????,X????????????0.2???????????????,????????????
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
            mTvStrength.setText(String.format("??????:%d", progress));
            mStrength = progress;
        }
        if (seekBar.getId() == R.id.sb_delay) {
            mTvDelay.setText(String.format("??????:%d", progress));
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
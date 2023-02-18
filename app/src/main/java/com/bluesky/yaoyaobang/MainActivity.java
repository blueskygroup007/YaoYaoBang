package com.bluesky.yaoyaobang;

import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mBtnStop;
    private Button mBtnStart;
    private DrawSV mSvDraw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSvDraw = findViewById(R.id.sv_draw);
        mBtnStop = findViewById(R.id.btn_stop);
        mBtnStart = findViewById(R.id.btn_start);
        mBtnStop.setOnClickListener(this);
        mBtnStart.setOnClickListener(this);
        hideSystemUI();
        getDensity();
        testFont("我");
    }

    private void testFont(String str) {
        byte[] b = FontDecode.decode(str);
        if (b != null) {
            for (int i = 0; i < b.length; i++) {
                System.out.println(Integer.toHexString(b[i]));
            }
        }
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

    }

    /*
    准备数组
        1.
     */
/*    private <T> T[][] generateData(T[] sourece){
    }*/
    private byte[][] generateData() {
        byte[][] data = new byte[64][16];


        return data;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_stop:
                mSvDraw.stopDrawThread();
                break;
            case R.id.btn_start:
                mSvDraw.startDrawThread();
                break;
            default:
        }
    }
}
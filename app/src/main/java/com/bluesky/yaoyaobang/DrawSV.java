package com.bluesky.yaoyaobang;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

/**
 * @author BlueSky
 * @date 2023/2/16
 * Description:
 */
public class DrawSV extends SurfaceView implements SurfaceHolder.Callback {

    private final int MSG_DRAW = 1;

    public DrawSV(Context context) {
        super(context);
        init();
    }


    public DrawSV(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public DrawSV(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    public DrawSV(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mHandlerThread = new HandlerThread(getClass().getSimpleName());
        mHandlerThread.start();
        mThreadHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (MSG_DRAW == msg.what) {
                    //sendEmptyMessage(MSG_DRAW);
                    doDraw();
                    sendEmptyMessageDelayed(MSG_DRAW, 1000);
                }
            }
        };

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        flag = false;
    }

    private SurfaceHolder mHolder;
    private HandlerThread mHandlerThread;
    private Handler mThreadHandler;
    private boolean flag = true;

    private Canvas mCanvas;
    private Paint mPaint;

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void doDraw() {
        mCanvas = mHolder.lockCanvas();

        int centerX = 700;
        int centerY = 60;
        int radius = 60;
        mCanvas.drawColor(Color.BLACK);
        for (int j = 1; j <= 16; j++) {
            mPaint.setColor(flag ? Color.BLACK : Color.WHITE);
            mCanvas.drawCircle(centerX, centerY + 120 * j, radius, mPaint);
            Log.d("doDraw:", "centerX=" + centerX + "centerY=" + centerY);
            flag = !flag;
        }

        mHolder.unlockCanvasAndPost(mCanvas);
    }

    //启动线程
    public void startDrawThread() {
        if (mThreadHandler != null) {
            mThreadHandler.sendEmptyMessage(MSG_DRAW);
        }
    }

    //停止线程
    public void stopDrawThread() {
        if (mHandlerThread.isAlive()) {
            mHandlerThread.quit();
        }
    }
}

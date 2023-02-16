package com.bluesky.yaoyaobang;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;


/**
 * @author BlueSky
 * @date 2023/2/15
 * Description:
 */
public class DrawSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private DrawTask mDrawTask;
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private boolean mIsDrawing;

    private Paint mPaint = new Paint();

    public DrawSurfaceView(Context context) {
        super(context);
        init();
    }

    public DrawSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public DrawSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    public DrawSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();

    }


    private static final HandlerThread mHandlerThread = new HandlerThread("SurfaceViewThread");

    static {
        mHandlerThread.start();
    }

    private Handler mHandler;
    private int MSG_DRAW = 1;


    private final Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_DRAW) {
                mHandler.post(mDrawTask);
                mHandler.sendEmptyMessageDelayed(MSG_DRAW, 16);
            }
            return true;
        }
    };

    //停止绘制线程
    public void stopDrawThread() {
        mHandlerThread.quit();
        mHandler = null;
    }

    //启动绘制线程
    public void stratDrawThread() {
        mHandler = new Handler(mHandlerThread.getLooper(), mCallback);
        mHandler.post(new DrawTask(mHolder, this));
        mHandler.sendEmptyMessage(MSG_DRAW);
    }

    //绘制子线程
    private static class DrawTask implements Runnable {
        private final SurfaceHolder holder;
        private final DrawSurfaceView mSurfaceView;

        public DrawTask(SurfaceHolder holder, DrawSurfaceView surfaceView) {
            this.holder = holder;
            mSurfaceView = surfaceView;
        }

        @Override
        public void run() {
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();
                synchronized (holder) {
                    mSurfaceView.draw(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);

    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
//        mIsDrawing = true;
        if (mDrawTask == null) {
            mDrawTask = new DrawTask(holder, this);
        }

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
//        mIsDrawing = false;
    }

/*    @Override
    public void draw(Canvas canvas) {
        if (canvas==null){
            return;
        }
        super.draw(canvas);
    }*/

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        mCanvas = mHolder.lockCanvas();
        //---------------------------------
        int i = 1;
        while (mIsDrawing) {
            int remainder = i / 16;
            int color = Color.rgb(remainder * 16, remainder * 16, remainder * 16);
            int centerX = 500;
            int centerY = i / 16;
            int radius = 60;
            mCanvas.drawColor(color);
            for (int j = 0; j < 16; j++) {
                mCanvas.drawCircle(centerX, centerY + 120, radius, mPaint);
            }
            i++;

        }
        //---------------------------------
        if (null != mCanvas) {
            mHolder.unlockCanvasAndPost(mCanvas);
        }
    }
}

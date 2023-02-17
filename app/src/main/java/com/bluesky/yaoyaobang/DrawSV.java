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

    /*
     * 1.surfaceHolder.Callback是SurfaceView的回调,对应三个状态:created,changed,destroyed.
     * 2.surfaceHolder的holder,是持有画板Canvas的对象,相当于mvp中的P.
     * 3.HandlerThread可以做成异步,handler创建时(looper,callback),传入callback,此callback
     *   为一个弱引用的新线程.
     * 5.Handler创建时的(Looper),是指定接收消息的队列.如果不写,则使用当前线程的Looper.
     */

    /*-------------------------
    * 1.获取分辨率,计算绘图点位
    * 2.准备绘制数组(加入两端的黑屏,或绘制时,设置SendMessageDaley延时)
    * 3.监听传感器(后续可加入陀螺仪来监测位移修正),计算刷新时长,最小为16.6ms(理想值,测量实际刷新间隔来确定)
    * 4.绘制.
    * 5.启动和停止方式的确定.
    -------------------------*/


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

package com.bluesky.yaoyaobang;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
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
    private byte[][] mPointArr = null;
    private int mColumn = 0;
    private boolean mDirection = true;
    private int mMaxColumn = 16;
    private int mMaxRow = 16;
    private int mDelay = 5;

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
                    if (mColumn >= 0 && mColumn < mMaxColumn) {
                        doDraw();
                    }
                    if (mDirection) {
                        if (++mColumn < mMaxColumn) {
                            sendEmptyMessageDelayed(MSG_DRAW, mDelay);
                        }
                    } else {
                        if (--mColumn >= 0) {
                            sendEmptyMessageDelayed(MSG_DRAW, mDelay);
                        }
                    }
                }
            }
        };

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopDrawThread();

    }

    private SurfaceHolder mHolder;
    private HandlerThread mHandlerThread;
    private Handler mThreadHandler;

    private Canvas mCanvas;
    private Paint mPaint;
    private Paint mPaintBlack;

    int centerX;
    int centerY;
    int radius;
    Rect rect;
    Rect rectBlack;

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaintBlack = new Paint();
        mPaintBlack.setColor(Color.BLACK);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setData(byte[][] pointArr) {
        mPointArr = pointArr;
    }


    private void doDraw() {
        long currentTimeMillis = System.currentTimeMillis();
        //mCanvas = mHolder.lockCanvas();
        mCanvas = mHolder.lockHardwareCanvas();
        /*
         * 优化绘制
         * X1.黑点不画
         * √2.不刷新黑屏,直接涂黑白点即可
         * √3.如果该点颜色与上一列相同,则不画.
         */
        if (mDirection) {
            for (int j = 0; j < mMaxColumn; j++) {

/*                if (mPointArr[j][mColumn] == 1) {
                    mCanvas.drawCircle(centerX, centerY + radius * 2 * j, radius, mPaint);
                } else {
                    mCanvas.drawCircle(centerX, centerY + radius * 2 * j, radius, mPaintBlack);
                }*/

                //如果是第一列,或者当前点与上一列同色.(以后可能会在数组前后加两列黑点.)
                if (mColumn == 0) {
                    if (mPointArr[j][mColumn] == 1) {
                        mCanvas.drawCircle(centerX, centerY + radius * 2 * j, radius, mPaint);
                    } else {
                        mCanvas.drawCircle(centerX, centerY + radius * 2 * j, radius, mPaintBlack);
                    }
                } else {
                    if (mPointArr[j][mColumn] != mPointArr[j][mColumn - 1]) {
                        if (mPointArr[j][mColumn] == 1) {
                            mCanvas.drawCircle(centerX, centerY + radius * 2 * j, radius, mPaint);
                        } else {
                            mCanvas.drawCircle(centerX, centerY + radius * 2 * j, radius, mPaintBlack);
                        }
                    }
                }
            }
        } else {
            for (int k = mMaxColumn - 1; k >= 0; k--) {

/*                if (mPointArr[k][mColumn] == 1) {
                    mCanvas.drawCircle(centerX, centerY + radius * 2 * k, radius, mPaint);
                } else {
                    mCanvas.drawCircle(centerX, centerY + radius * 2 * k, radius, mPaintBlack);
                }*/

                if (mColumn == mMaxColumn - 1) {
                    if (mPointArr[k][mColumn] == 1) {
                        mCanvas.drawCircle(centerX, centerY + radius * 2 * k, radius, mPaint);
                    } else {
                        mCanvas.drawCircle(centerX, centerY + radius * 2 * k, radius, mPaintBlack);
                    }
                } else {
                    if (mColumn == mMaxColumn || mPointArr[k][mColumn] != mPointArr[k][mColumn + 1]) {
                        if (mPointArr[k][mColumn] == 1) {
                            mCanvas.drawCircle(centerX, centerY + radius * 2 * k, radius, mPaint);
                        } else {
                            mCanvas.drawCircle(centerX, centerY + radius * 2 * k, radius, mPaintBlack);
                        }
                    }
                }
            }
        }

        mHolder.unlockCanvasAndPost(mCanvas);
        //SystemClock.uptimeMillis();
        long endTime = System.currentTimeMillis();
        long runTime = endTime - currentTimeMillis;
        Log.d("time", String.format("%d列的执行时长为 %d ms", mColumn, runTime));
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

    long startTime;

    public void drawing(boolean direction, long start) {
        startTime = start;
        //如果方向与之前相反
//        if (direction != mDirection) {
            if (mThreadHandler != null) {
                //mThreadHandler.removeMessages(MSG_DRAW);
                mDirection = direction;
                if (mDirection) {
                    mColumn = 0;
                } else {
                    mColumn = mMaxColumn - 1;
                }
                //rect = new Rect(centerX - 60, centerY - 60, centerX + 60, centerY + 60);
                mThreadHandler.sendEmptyMessage(MSG_DRAW);
                Log.d("第一次时间:", String.format("%d ms", System.currentTimeMillis()-startTime));

            }
//        }
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }

    public void setDensity(int width, int height) {
        centerX = width / 2;
        radius = height / 16 / 2;

        centerY = radius / 2;
        rect = new Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        rectBlack = new Rect(centerX - radius, 0, centerX + radius, 16 * radius * 2);
    }
}

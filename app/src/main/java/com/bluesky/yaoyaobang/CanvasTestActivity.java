package com.bluesky.yaoyaobang;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.midi.MidiDevice;
import android.media.midi.MidiDeviceInfo;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

public class CanvasTestActivity extends AppCompatActivity implements SurfaceHolder.Callback2 {


    private static final int _KEY_WIDTH = 90;
    private static final int _KEY_HEIGHT = 160;
    private static final int _MARKER_HEIGHT = 32;
    private static final int _KEYBOARD_Y = 80;
    private static final int _SPEED_Y = 550;


    private ArrayList<Rect> buttonList = null;
    private ArrayList<Rect> markerList = null;
    private ArrayList<Rect> speedList = null;

    private Rect playButton;
    private Rect stopButton;

    private View mControlsView;
    private boolean mVisible;
    private SurfaceView mContentView;
    private SurfaceHolder mHolder;

    private boolean useMIDI;

    private Paint colorKeyOn;
    private Paint colorKeyOff;
    private Paint colorMarkerOn;
    private Paint colorMarkerOff;
    private Paint BG; // code

    private Paint colorSpeedSelect;
    private Paint colorSpeedUnSelect;

    private int markPosition;
    private int[] keyOn;

    private int speedPosition;
    private static final int[] tempoList = {60, 100, 120, 144, 160};

    private boolean playing;
    private final static int PLAYSEQUENCE = 1;
    private final static int PLAYSTART = 2;

    private final static byte MIDI_CH = 10;
    private final static byte MIDI_NOTE = 42;
    private final static byte MIDI_VELOCITY = 127;

    private final static int BEATS = 4;

    private Message msg;
    private MidiInputPort inputPort = null;
    private MidiDevice inputDevice = null;

    private byte[] noteBuf;
    private int noteBufSize;


    private int checkTouchPostion(ArrayList<Rect> rectList, int x, int y) {
        Rect r = rectList.get(0);

        if (r.top <= y && r.bottom >= y) {
            Log.d("midi", "x:" + x + "/y:" + y + "/r.t:" + r.top + "/r.b:" + r.bottom);
            for (int i = 0; i < rectList.size(); ++i) {
                r = rectList.get(i);
                if (r.left <= x && r.right >= x) {
                    Log.d("midi", "x:" + x + "/r.l:" + r.left + "/r.r:" + r.right);
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_canvas_test);

        mVisible = true;
        mContentView = (SurfaceView) findViewById(R.id.fullscreen_content);
        this.mContentView.setOnTouchListener(screenTouchEvent);

        mHolder = this.mContentView.getHolder();
        mHolder.addCallback(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        colorKeyOn = new Paint();
        colorKeyOn.setARGB(0xff, 0x40, 0x40, 0xff);

        colorKeyOff = new Paint();
        colorKeyOff.setARGB(0xff, 0x40, 0x40, 0x80);

        colorMarkerOn = new Paint();
        colorMarkerOn.setARGB(0xff, 0x40, 0xff, 0x40);

        colorMarkerOff = new Paint();
        colorMarkerOff.setARGB(0xff, 0x40, 0x80, 0x40);

        colorSpeedSelect = new Paint();
        colorSpeedSelect.setARGB(0xff, 0xff, 0xff, 0x40);
        colorSpeedSelect.setTextSize(50);

        colorSpeedUnSelect = new Paint();
        colorSpeedUnSelect.setARGB(0xff, 0x80, 0x80, 0x40);
        colorSpeedSelect.setTextSize(50);

        msg = new Message();
        msg.what = PLAYSEQUENCE;

        BG = new Paint();
        BG.setColor(Color.BLACK);


        keyOn = new int[16];
        for (int i = 0; i < keyOn.length; ++i) {
            keyOn[i] = 0;
        }


        markPosition = 0;
        speedPosition = 2;

        Context context = getApplicationContext();


        useMIDI = false;
        inputDevice = null;

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MIDI)) {

            MidiManager manager = (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
            MidiDeviceInfo[] instruments = manager.getDevices();

            if (instruments.length > 0) {
                for (int i = 0; i < instruments.length; ++i) {
                    MidiDeviceInfo instInfo = instruments[i];
                    Bundle properties = instInfo.getProperties();
                    String manufacturer = properties.getString(MidiDeviceInfo.PROPERTY_NAME);

                    Log.d("play", manufacturer);

                    if (instInfo.getInputPortCount() > 0) {

                        Log.d("play", "INPUT:" + instInfo.getInputPortCount());

                        manager.openDevice(instInfo, new MidiManager.OnDeviceOpenedListener() {
                            @Override
                            public void onDeviceOpened(MidiDevice device) {
                                useMIDI = true;
                                inputDevice = device;

                                inputPort = device.openInputPort(0);

                                noteBuf = new byte[32];
                                noteBuf[noteBufSize++] = (byte) (0x90 + MIDI_CH - 1);
                                noteBuf[noteBufSize++] = MIDI_NOTE;
                                noteBuf[noteBufSize++] = MIDI_VELOCITY;
                            }
                        }, new Handler(Looper.getMainLooper()));
                    }
                }
            }
        }
    }


    private View.OnTouchListener screenTouchEvent = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean result = false;


            if (event.getAction() == MotionEvent.ACTION_DOWN) {


                int x = (int) event.getX();
                int y = (int) event.getY();
                int index;


                if ((index = checkTouchPostion(buttonList, x, y)) >= 0) {

                    keyOn[index] = 1 - keyOn[index];
                    result = true;
                }

                if ((index = checkTouchPostion(speedList, x, y)) >= 0) {
                    speedPosition = index;
                    result = true;
                }


                if (playing) {
                    if (stopButton.top <= y && stopButton.bottom >= y) {
                        if (stopButton.left <= x && stopButton.right >= x) {
                            playing = false;
                            result = true;
                        }
                    }
                } else {
                    if (playButton.top <= y && playButton.bottom >= y) {
                        if (playButton.left <= x && playButton.right >= x) {
                            result = playing = true;
                            mHandler.sendMessageDelayed(mHandler.obtainMessage(PLAYSEQUENCE), 1);
                        }
                    }


                    if (stopButton.top <= y && stopButton.bottom >= y) {
                        if (stopButton.left <= x && stopButton.right >= x) {
                            markPosition = 0;
                            result = true;
                        }
                    }

                }
            }


            if (result) {
                draw(mHolder);
            }

            return result;
        }
    };


    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (playing && msg.what == PLAYSEQUENCE) {
                long current = SystemClock.uptimeMillis();


                long nexttime = (long) (60 * 1000 / tempoList[speedPosition] / BEATS);

                Log.d("play", "" + nexttime);

                if (keyOn[markPosition] == 1) {

                    if (useMIDI) {
                        try {
                            inputPort.send(noteBuf, 0, noteBufSize);
                        } catch (Exception ioException) {
                        }
                    }
                } else {

                }

                draw(mHolder);


                markPosition = (markPosition + 1) & 0x0f;

                //                nexttime -= SystemClock.uptimeMillis() - current;


                sendMessageDelayed(obtainMessage(PLAYSEQUENCE), nexttime);
            }
        }
    };

    private void initializeButtonPositions() {
        buttonList = new ArrayList<Rect>();
        markerList = new ArrayList<Rect>();
        speedList = new ArrayList<Rect>();

        playButton = new Rect(730, 530, 910, 710);
        stopButton = new Rect(920, 530, 1100, 710);


        for (int i = 0; i < 16; ++i) {
            int x = (i + 1) * _KEY_WIDTH;
            int y = _KEYBOARD_Y + _KEY_HEIGHT;

            buttonList.add(new Rect(x, y, x + (_KEY_WIDTH - (_KEY_WIDTH / 10)), y + _KEY_HEIGHT));
            markerList.add(new Rect(x, y + _KEY_HEIGHT + (_KEY_HEIGHT / 10), x + (_KEY_WIDTH - (_KEY_WIDTH / 10)),
                    y + _KEY_HEIGHT + (_KEY_HEIGHT / 10) + _MARKER_HEIGHT));
        }

        for (int i = 0; i < 5; ++i) {
            int x = (i + 1) * _KEY_WIDTH;

            speedList.add(new Rect(x, _SPEED_Y, x + (_KEY_WIDTH - (_KEY_WIDTH / 10)), _SPEED_Y + _KEY_HEIGHT));
        }
    }


    private void draw(SurfaceHolder holder) {

        Surface surface = holder.getSurface();
        Canvas canvas = surface.lockHardwareCanvas();

        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), BG);


        for (int i = 0; i < 16; ++i) {
            if ((i % 4) == 0) {
                Rect r = buttonList.get(i);
                colorKeyOn.setTextSize(30);
                canvas.drawText("" + (i / 4 + 1), r.left, r.top, colorKeyOn);
            }

            canvas.drawRect(buttonList.get(i), keyOn[i] == 1 ? colorKeyOn : colorKeyOff);
            canvas.drawRect(markerList.get(i), markPosition == i ? colorMarkerOn : colorMarkerOff);
        }


        for (int i = 0; i < 5; ++i) {
            Rect r = speedList.get(i);
            Paint p = colorSpeedUnSelect;

            if (i == speedPosition) {
                p = colorSpeedSelect;
            }

            p.setAlpha(0x80);
            canvas.drawRect(r, p);

            p.setAlpha(0xff);
            p.setTextSize(30);
            canvas.drawText(Integer.toString(tempoList[i]), r.left, r.top, p);
        }

        canvas.drawText("START", playButton.left, playButton.top, playing ? colorSpeedUnSelect : colorSpeedSelect);
        canvas.drawRect(playButton, playing ? colorSpeedUnSelect : colorSpeedSelect);

        if (markPosition < 1) {
            canvas.drawText("STOP", stopButton.left, stopButton.top,
                    !playing ? colorSpeedUnSelect : colorSpeedSelect);
            canvas.drawRect(stopButton, !playing ? colorSpeedUnSelect : colorSpeedSelect);
        } else {
            canvas.drawText("STOP", stopButton.left, stopButton.top, colorSpeedSelect);
            canvas.drawRect(stopButton, colorSpeedSelect);
        }


        surface.unlockCanvasAndPost(canvas);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("play", "created");

        initializeButtonPositions();
        draw(holder);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("play", String.format("format:%x, w:%d, h:%d", format, width, height));
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("play", "destroyed");


        playing = false;
        mHandler.removeMessages(PLAYSEQUENCE);


        try {
            inputPort.flush();
            inputDevice.close();
        } catch (Exception ioException) {
        }
    }

    public void surfaceRedrawNeeded(SurfaceHolder holder) {
        Log.d("play", "redraw");

        draw(holder);
    }
}
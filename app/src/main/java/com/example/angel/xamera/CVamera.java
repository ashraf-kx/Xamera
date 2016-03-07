package com.example.angel.xamera;

/**
 * Created by angel on 11/30/15.
 */


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import org.opencv.core.*;
import org.opencv.imgproc.*;

import java.util.ArrayList;
import java.util.List;

public class CVamera extends Activity implements CameraBridgeViewBase.CvCameraViewListener {
    static {
        System.loadLibrary("opencv_java3");
    }

    private static final String TAG = "*WTF";

    private CameraBridgeViewBase mOpenCvCameraView;
    private int nbrFrames;
    private TextView TX;
    private Switch    mSwitcher;
    private boolean   mIsJavaCamera  = true;
    private MenuItem  mItemSwitchCamera = null;

    protected Mat image = new Mat();
    protected Boolean mLOCK = false;
    protected Segmentation mSegmentation = new Segmentation();
    protected Button mButton;

    protected enum mState{ DISPLAY_NORMAL, DISPLAY_BIN, DISPLAY_IMAGE }
    protected mState XState;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CVamera() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        nbrFrames = 0;
        XState = mState.DISPLAY_NORMAL;
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.cv_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id._surface_view);
        TX = (TextView)findViewById(R.id.textView);
        mSwitcher = (Switch) findViewById(R.id.switcher);
        mButton = (Button) findViewById(R.id.button);

        // mOpenCvCameraView.setRotationX(90);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);


        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                TX.setText(String.valueOf(image.rows()) + "x" + String.valueOf(image.cols()));
                Log.d(TAG, " before get image");
                mSegmentation._imgBW = image;
                Log.d(TAG, "after getting the image");
                mSegmentation.Start();
                Log.d(TAG, " not Class Sergme is the problem...");
                image = mSegmentation._img;
                XState = mState.DISPLAY_IMAGE;
                TX.setText("msg : " + mSegmentation.msg);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {

        switch (XState){
            case DISPLAY_NORMAL:
                break;
            case DISPLAY_BIN:
                inputFrame = Preprocess.Binarization(inputFrame);
                image = inputFrame;
                break;
            case DISPLAY_IMAGE:
                inputFrame = image;
                break;
        }
        return inputFrame;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                XState = mState.DISPLAY_BIN;
                break;
            case MotionEvent.ACTION_UP:
                XState = mState.DISPLAY_NORMAL;
                // mOpenCvCameraView.disableView(); // Stop Delevring Frmaes To View.

                break;
            default:
                return false;
        }
        return true;
    }






}
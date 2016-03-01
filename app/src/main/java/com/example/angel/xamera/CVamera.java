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
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;

import org.opencv.core.*;
import org.opencv.imgproc.*;

import java.util.ArrayList;
import java.util.List;

public class CVamera extends Activity implements CameraBridgeViewBase.CvCameraViewListener {
    static {
        System.loadLibrary("opencv_java3");
    }

    private static final String TAG = "OCVamera::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private TextView TX;
    private boolean              mIsJavaCamera  = true;
    private MenuItem             mItemSwitchCamera = null;

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
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.cv_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id._surface_view);
        TX = (TextView)findViewById(R.id.textView);

        //mOpenCvCameraView.setRotationX(90);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
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

        inputFrame = Preprocess.Binarization(inputFrame);
        //Mat P1 = inputFrame.submat(0, 500, 0,500);
        //Mat P2 = inputFrame.submat((inputFrame.rows()/2)+1, inputFrame.rows(),0,inputFrame.cols());
        /*List<Mat> P = new ArrayList<>();
        P.add(P2);
        P.add(P1);
        Core.vconcat(P,inputFrame);*/
        //inputFrame = P1.clone();
        return inputFrame;
    }


}
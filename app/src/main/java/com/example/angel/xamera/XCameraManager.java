package com.example.angel.xamera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.List;

/**
 * Created by angel on 12/3/15.
 */
public class XCameraManager {

    public static final String TAG = " XCameraManager ACTIVITY ";
    private XStorageManager    mXStorageManager;

    /**  Declare Every GUI Component Used in The layout   **/
    private Camera            mCamera;
    private Camera.CameraInfo mCameraInfo1;
    private Camera.CameraInfo mCameraInfo2;
    private Camera.Parameters mParameters;

    List<String> S_Antibanding;
    List<String> S_ColorEffects;
    List<String> S_FlashModes;
    List<String> S_FocusModes;
    List<String> S_SceneModes;
    List<String> S_WhiteBalance;

    List<Camera.Size> S_JpegThumbnailSizes;
    List<Camera.Size> S_PreviewSizes;
    List<Camera.Size> S_VideoSizes;
    List<Camera.Size> S_PictureSizes;

    List<Integer> S_PictureFormats;
    List<Integer> S_PreviewFormats;

    List<int[]>  S_PreviewFpsRange;

    private SurfaceHolder     mSurfaceHolder;
    private SurfaceView       mSurfaceView;

    //############ Draw Something #############
    private Bitmap    mBitmap;
    private ImageView mImageView;

    private Paint  mPaint;
    private Canvas mCanvas;
    private float[] pts;
    private int     i,offSet;
    private String  msg;


    public void init() {

        mSurfaceHolder    = mSurfaceView.getHolder();
        // Use Camera Preffered Bla-Bla. the biggest (Full Screen).
        mBitmap = Bitmap.createBitmap(720,1280,Bitmap.Config.ARGB_8888);
        mImageView.setImageBitmap(mBitmap);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(6);
        mPaint.setColor(Color.RED);

        mCanvas = new Canvas(mBitmap);

        if (/*this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)*/ true) try {

            initCamera();

            initParametres(1);

        } catch (Exception e) {  // Camera is not available (in use or does not exist)

        }

        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    mCamera.setPreviewDisplay(mSurfaceHolder);
                    mCamera.setDisplayOrientation(90);  // ++ Auto Adjusting when flip Phone.

                } catch (IOException e) {
                    // will Put Error Msg. Inchallah { e.getMessage(); }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                mCamera.addCallbackBuffer(data);
            }
        });

        mCamera.startPreview();

    }

    private void initCamera(){
        /** Get An instance From the Camera. (API 19)  **/
        /** First, Get How Many Camera In The Device **/
        switch(Camera.getNumberOfCameras()){
            case 0 : msg = "No Camera On This Device."; break;
            case 1 : msg = "1 Camera Located On This Device.";  break;
            case 2 : msg = "2 Cameras Located On This Device.";  break;
            default: break;
        }

        mCamera     = Camera.open();
    }


    private void initParametres(int idCamera){

        mParameters = mCamera.getParameters();
        /** Get Info Camera **/
        Camera.getCameraInfo(idCamera, mCameraInfo1);
        if(mCameraInfo1.facing == Camera.CameraInfo.CAMERA_FACING_BACK){

            S_Antibanding   = mParameters.getSupportedAntibanding();
            S_ColorEffects  = mParameters.getSupportedColorEffects();
            S_FlashModes    = mParameters.getSupportedFlashModes();
            S_FocusModes    = mParameters.getSupportedFocusModes();
            S_SceneModes    = mParameters.getSupportedSceneModes();
            S_WhiteBalance  = mParameters.getSupportedWhiteBalance();

            S_JpegThumbnailSizes  = mParameters.getSupportedJpegThumbnailSizes();
            S_PreviewSizes        = mParameters.getSupportedPreviewSizes();
            S_VideoSizes          = mParameters.getSupportedVideoSizes();
            S_PictureSizes        = mParameters.getSupportedPictureSizes();

            S_PictureFormats = mParameters.getSupportedPictureFormats();
            S_PreviewFormats = mParameters.getSupportedPreviewFormats();

            S_PreviewFpsRange = mParameters.getSupportedPreviewFpsRange();
        }

        mCamera.setParameters(mParameters);
    }

    /** ActionListner For the imageView to Allow Us Draw on the Bitmap Image **/
    // TODO : Put To Mode Of Drawing (Drain Battery & instant Drawing) , (Save Battery & Popup Drawing)
    /*public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN :
                pts = new float[200000];
                pts[i] = event.getRawX(); i++;
                pts[i] = event.getRawY(); i++;
                break;
            case MotionEvent.ACTION_MOVE :
                pts[i] = event.getRawX(); i++;
                pts[i] = event.getRawY(); i++;
                break;
            case MotionEvent.ACTION_UP   :
                offSet = i;
                PiccasoHand FirstHand = new PiccasoHand();
                FirstHand.start();
                i=0;
                break;
            default: break;
        }
        return true;
    }*/

    public void takePicture(){
        mCamera.takePicture(null, null, mPicture);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        // TODO : Full Controle on The Media Use < Change The Location To the Default Folder >.
        // TODO :>> Create Function(byte[] data); >> XStorageManager.Function(data);
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mXStorageManager.writeImageToMedia(data);
        }
    };
}

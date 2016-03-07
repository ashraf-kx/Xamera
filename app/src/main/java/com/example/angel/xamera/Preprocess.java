package com.example.angel.xamera;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by angel on 12/1/15.
 */
public class Preprocess extends Mat {

    // TODO: [ GrayScale, Adaptive Threshold(All), Candy, BlackWhite, Contour, Thing, .....]

    public static Mat Binarization(Mat inputFrame){
        Imgproc.cvtColor(inputFrame, inputFrame, Imgproc.COLOR_BGRA2GRAY); // Switch GrayScale
        Imgproc.blur(inputFrame, inputFrame, new Size(3, 3));                 // Reduce Noise
        Imgproc.adaptiveThreshold(inputFrame, inputFrame, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 7, 2);
        return inputFrame;
    }



}

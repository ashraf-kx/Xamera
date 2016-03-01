package com.example.angel.xamera;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by angel on 12/1/15.
 */
public class Preprocess extends Mat {
    // Multi-Threaded Operations.
    // TODO: [ GrayScale, Adaptive Threshold(All), Candy, BlackWhite, Contour, Thing, .....]

    public static Mat Binarization(Mat inputFrame){
        Imgproc.cvtColor(inputFrame, inputFrame, Imgproc.COLOR_BGRA2GRAY); // Switch GrayScale
        Imgproc.blur(inputFrame, inputFrame, new Size(3, 3));                 // Reduce Noise
        Imgproc.adaptiveThreshold(inputFrame, inputFrame, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 7, 2);
        return inputFrame;
    }


    /*
    public static Mat copyRect(Mat image,int x1,int y1,int x2,int y2)
    {
        int ii=0;
        int jj=0;
        int rows = x2-x1;
        int cols = y2-y1;

        Mat partImage = new Mat(rows,cols, image.type());
        for(int i=x1; i<x2; i++)
        {
            jj=0;
            for(int j=y1; j<y2;j++)
            {
               // partImage.put(ii,jj) = image.get(i,j,);
                jj++;
            }
            ii++;
        }
        return partImage;
    }*/
    // TODO: [ GrayScale, Adaptive Threshold(All), Candy, BlackWhite, Contour, Thing, .....]


}

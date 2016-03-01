package com.example.angel.xamera;

import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.ml.SVM;


import java.util.ArrayList;

/**
 * Created by angel on 12/1/15.
 */
public class Recognition extends SVM {
    // TODO : Recognition thing Here.

    TermCriteria mTermCriteria = new TermCriteria();

    protected Recognition(long addr) {
        super(addr);

    }

    public float diagonalAverage(Mat mask){
        ArrayList<Integer> sum = new ArrayList<>();
        sum.clear();
        int x;
        int i=0;
        for (int j = 0; j < mask.cols(); ++j) {
            int ii=i;
            int jj=j;
            x=0;
            while(ii <= j || jj >= i)
            {
                if(mask.get(ii,jj)[0] == 0) x++;
                ii++;
                jj--;
            }
            sum.add(x);
        }

        int j=mask.cols()-1;
        for (int k = 1; k < mask.rows(); ++k) {
            int ii=k;
            int jj=j;
            x=0;
            while(ii <= j || jj >= k)
            {
                if(mask.get(ii,jj)[0] == 0) x++;
                ii++;
                jj--;
            }
            sum.add(x);
        }
        x = 0;
        i = 0;
        while(i<sum.size())
        {
            x+=sum.get(i);
            i++;
        }
        return x/sum.size();
    }

    public float calculateWHRation(Mat image){
        return (float)image.cols()/(float)image.rows();
    }

    public float calculateHWRation(Mat image){
        return (float)image.rows()/(float)image.cols();
    }

    public ArrayList<Float> extractFeaturesVector(Mat image){
        ArrayList<Float> vector = new ArrayList<>();
        vector.clear();

        // Ration width/height & Height/width.
        vector.add(calculateWHRation(image));
        vector.add(calculateHWRation(image));

        image = Segmentation.characterNormalization(image);

        float number= 0;
        for (int i = 0; i < image.rows();) {
            int ii=i;
            i+=10;
            for (int j = 0; j < image.cols();) {
                int jj=j;
                j+=10;
                number = diagonalAverage(Segmentation.copyRect(image, ii, jj, i, j));
                vector.add(number);
            }
        }

        // number CXX Feature
       //  vector.add(countCXX(image)); // TODO CXX function re-implemented.

        return vector;
    }

    public void setSVMParameters(int kernalType,double C,double degree,double gamma){
        this.setType(SVM.C_SVC);
        this.setKernel(kernalType);
        this.setC(C);
        this.setDegree(degree);
        this.setGamma(gamma);

        int type = TermCriteria.EPS;  // COUNT or EPS or COUNT+EPS.
        int maxCount = 0;
        double epsilon = 0.05; // accuracy

        mTermCriteria.set(new double[]{type, maxCount, epsilon});
        this.setTermCriteria(mTermCriteria);
    }

    // public int countCXX(Mat image);

    /*public void loadTrainingFile(String fileName){
        if(!fileName.isEmpty()) this.; //load(fileName.toStdString().c_str());
    }

    void    loadTrainingFile(QString fileName);
    void    saveTraining(QString fileName);
    void    loadLabels(QString labels);*/
}

/*






public slots:
    Mat     getTrainingSet(QList<Mat> CharactersSet);
    Mat     getTestingSet(QList<Mat> CharactersSet);
    void    trainTheMachine(Mat trainingSet);
    void    loadTrainingFile(QString fileName);
    void    saveTraining(QString fileName);
    void    loadLabels(QString labels);
    QString recognize(Mat TestingSet);
    QString recognizeTest(Mat TestingSet);

 */
package com.example.angel.xamera;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;
import org.opencv.ml.SVM;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by angel on 12/1/15.
 */
public class Recognition extends SVM {
    // TODO : Recognition thing Here.

    private TermCriteria mTermCriteria = new TermCriteria();
    protected String AllLettres;

    protected Recognition(long addr) {
        super(addr);
        // setSVMParameters();
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

    public void loadTrainingFile(String fileName){
        //if(!fileName.isEmpty()) super.load(fileName);
    }

    public void saveTraining(String fileName){
        if(!fileName.isEmpty()) super.save(fileName);
    }

    public void loadLabels(String labels){
        AllLettres ="ﻷ/ﻸ/ﻻ/ﻼ/­ﺂ/£/¤/ﺄ/ﺎ/ب/ت/ث/،/ج/ح/خ/٠/١/٢/٣/٤/٥/٦/٧/٨/٩/ف/؛/س/ش/ص/؟/¢/ء/آ/أ/ؤ/ﻊ/ﺋ/ا/ﺑ/ة/ﺗ/ﺛ/ﺟ/ﺣ/ﺧ/د/ذ/ر/ز/ﺳ/ﺷ/ﺻ/ﺿ/ط/ظ/ﻋ/ﻏ/¦/¬/÷/×/ع/ـ/ﻓ/ﻗ/ﻛ/ﻟ/ﻣ/ﻧ/ﻫ/و/ى/ﻳ/ض/ﻌ/ﻎ/غ/م/ن/ه/ﻬ/ﻰ/ﻲ/ﻐ/ق/ﻵ/ﻶ/ل/ك/ي";
    }

    public void trainTheMachine(Mat trainingSet,Mat labels){
        if(!this.isTrained()){
            this.train(trainingSet,1,labels);
        }
    }

    public String recognizeTest(Mat TestingSet){
        Mat results = new Mat();
        this.predict(TestingSet,results,0);
        return "halllo";
    }

    public String recognize(Mat Sample2Recognize){
        Mat results = new Mat();
        this.predict(Sample2Recognize,results,0);
        return "hllo";
    }

    public Mat getTrainingSet(ArrayList<Mat> CharactersSet){ // TODO the samething goes with getTestingSet.
        Mat trainingSet = new Mat();
        ArrayList<ArrayList<Float>> sequanceVectorsSet;
        sequanceVectorsSet = new ArrayList<>();
        sequanceVectorsSet.clear();

        for (int i = 0; i < CharactersSet.size(); ++i) {
            sequanceVectorsSet.add(extractFeaturesVector(CharactersSet.get(i)));
        }
        // SWITCH from QT-LIST >> OPENCV Mat.
        trainingSet.create(sequanceVectorsSet.size(),sequanceVectorsSet.get(0).size(), CvType.CV_32FC1);
        for (int i = 0; i < sequanceVectorsSet.size(); ++i) {
            ArrayList<Float> list = sequanceVectorsSet.get(i);
            for (int j = 0; j < list.size(); ++j) {
                trainingSet.get(i,j,new float[]{list.get(j).floatValue()});
            }
        }

        sequanceVectorsSet.clear();
        return trainingSet;
    }

    public Mat getTestingSet(ArrayList<Mat> CharactersSet){
        Mat trainingSet = new Mat();
        ArrayList<ArrayList<Float>> sequanceVectorsSet;
        sequanceVectorsSet = new ArrayList<>();
        sequanceVectorsSet.clear();

        for (int i = 0; i < CharactersSet.size(); ++i) {
            sequanceVectorsSet.add(extractFeaturesVector(CharactersSet.get(i)));
        }
        // SWITCH from QT-LIST >> OPENCV Mat.
        trainingSet.create(sequanceVectorsSet.size(),sequanceVectorsSet.get(0).size(), CvType.CV_32FC1);
        for (int i = 0; i < sequanceVectorsSet.size(); ++i) {
            ArrayList<Float> list = sequanceVectorsSet.get(i);
            for (int j = 0; j < list.size(); ++j) {
                trainingSet.get(i,j,new float[]{list.get(j).floatValue()});
            }
        }

        sequanceVectorsSet.clear();
        return trainingSet;
    }
}

// TODO .
// Load Labels (set of Characters from res/values => String (Spilt after that ) & [ more than one language ].
// remove Dump use of Array<Array> thing.
// Define words , use your brain Plz.


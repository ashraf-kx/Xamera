package com.example.angel.xamera;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.angel.xamera.Segmentation.HistType.HORIZONTAL;

/**
 * Created by angel on 12/1/15.
 */
public class Segmentation {

    // TODO : Finished (from C++ To Java).
    private static final String TAG = "*WTF";
    public Mat _imgBW = new Mat();
    public Mat _img = new Mat();
    public String msg;
    public class Candidate{
        public int timeRepeated;
        public int value;
        public Candidate(){
            this.timeRepeated = 0;
            this.value =0;
        }
    }

    public class Line {
        public int start;
        public int end;
        public Line(){
            this.start = 0;
            this.end   = 0;
        }
    }

    public class Word{
        public int x_start;
        public int x_end;
        public int y_start;
        public int y_end;
        public Word(){
            this.x_start =0;
            this.x_end   =0;
            this.y_start =0;
            this.y_end   =0;
        }
    }

    ArrayList<Line> geoLines = new ArrayList<>();
    ArrayList<Word> geoWords = new ArrayList<>();

    public enum HistType {
        VERTICAL, HORIZONTAL
    }

    public Segmentation() {

    }

    public void Start(){
        ArrayList<Integer> allLines = calculateBackProjection(_imgBW,HistType.VERTICAL);
        // ArrayList<Mat> linesImages  = getAllImagesLines(_imgBW); // Error(1)->averageLineHeight.
        // int GH = averageLineHeight(allLines);

        // Visualizaton [ important NOW ].
        // Clean
        _img.create(_imgBW.rows(),_imgBW.cols(),CvType.CV_8UC1);

        // Draw In image.
        for (int i = 0; i < _img.cols(); ++i) {
            for (int j = 0; j < allLines.get(i); ++j) {
                _img.put(i, j,new byte[]{(byte)255});
            }
        }

        msg = String.valueOf(allLines.size()); //
    }

    public Segmentation(Mat image){
        this._imgBW.create(image.rows(),image.cols(),image.type());
    }

    public ArrayList<Integer> calculateBackProjection(Mat imageIn, HistType type) {
        // Extracting Graph Values from a Matrix Image(CV_8u).
        ArrayList<Integer> tempList = new ArrayList<>();
        tempList.clear();
        Log.d(TAG, "Start..access calculeProjections");
        int x;
        switch (type) {
            case HORIZONTAL:
                for (int i = 0; i < imageIn.rows(); ++i) {
                    x = 0;
                    for (int j = 0; j < imageIn.cols(); ++j) {
                        if (imageIn.get(i, j)[0] == 0) x++;
                    }
                    tempList.add(x);
                }
                break;
            case VERTICAL:
                for (int i = 0; i < imageIn.cols(); ++i) {
                    x = 0;
                    for (int j = 0; j < imageIn.rows(); ++j) {
                        if (imageIn.get(j, i)[0] == 0) x++; // takes me alot of time :(
                    }
                    tempList.add(x);
                }
                break;
        }
        return tempList;
    }

    private int averageLineHeight(ArrayList<Integer> list){

        ArrayList<Integer> linesHeight = new ArrayList<>();
        linesHeight.clear();

        Boolean lock = false;
        if(!list.isEmpty())
        {
            int i=0;
            while(i<list.size())
            {
                int j=0;
                if(list.get(i) > 0)
                {
                    lock = true;

                    j++;
                    i++;

                    while (lock)
                    {
                        if(list.get(i) <= 0)
                        {
                            linesHeight.add(j);
                            lock=false;
                            j=-1;
                        }

                        j++;
                        i++;
                    }
                }
                else i++;
            }
        }

        int average=0;
        for(int i=0; i<linesHeight.size(); i++)
        {
            average+=linesHeight.get(i);
        }
        average = (int)((average/linesHeight.size())/1.5);

        return average;
    }

    public void lineDetection(ArrayList<Integer> HistDATA){
        // No need for images hear, only Histograms informations of them used !!
        // TIPS: Clear geoLines ARRAYLIST every Time.
        geoLines.clear();

        int average = averageLineHeight(HistDATA);
        Log.d(TAG," average : "+String.valueOf(average));
        Line L = new Line();

        int threshold = 0;
        Boolean mLock = false;
        if(!HistDATA.isEmpty())
        {
            int i=0;
            while(i<HistDATA.size())
            {
                int j=0;
                if(HistDATA.get(i) > threshold)
                {
                    L.start = i-1;
                    mLock = true;
                    i++;
                    j++;

                    while (mLock)
                    {
                        if(HistDATA.get(i) <= threshold)
                        {
                            if(j >= average ){
                                L.end = i+1;
                                geoLines.add(L); // this is it <<<<----- [ Cricial ].
                                mLock=false;
                                j=-1;
                            }
                        }
                        i++;
                        j++;
                    }
                }
                else i++;
            }
        }
    }

    public static Mat copyRect(Mat image,int x1,int y1,int x2,int y2){
        int ii=0;
        int jj=0;
        int rows = x2-x1;
        int cols = y2-y1;

        Mat tempImage = new Mat();
        tempImage.create(rows, cols, image.type());

        for(int i=x1; i<x2; i++)
        {
            jj=0;
            for(int j=y1; j<y2;j++)
            {
                tempImage.put(ii, jj, new byte[]{(byte)image.get(i,j)[0]});
                jj++;
            }
            ii++;
        }
        return tempImage;
    }

    // TODO : this getAllimageslines methode Shrink memory phone < optimization LATER ;) >
    public ArrayList<Mat> getAllImagesLines(Mat image){
        ArrayList<Mat> allLinesImages = new ArrayList<>();
        allLinesImages.clear();

        // image.copyTo(_imgBW); <= TODO : histogram projection visualization. < LATER >
        // Vertical Histogram.
        ArrayList<Integer> H_HistDATA = calculateBackProjection(image, HistType.VERTICAL); // TODO ( Vertical  phone || )

        //! [ 03/03/2015  21:49] CALL FOR METHODES <? Wandring ?>
        this.lineDetection(H_HistDATA);

        // Cut the Entier Image Into Small Pieces We call Them images Lines.
        for(int i = 0; i<geoLines.size(); ++i){ //JAVAFX
            allLinesImages.add(copyRect(image, geoLines.get(i).start, 0, geoLines.get(i).end, image.cols()));
        }

        // Visualisation the Detection lines. (_imgBW == image)
        /*for(int i =0; i< geoLines.size(); i++)
        {
            for (int j = 0; j < image.cols(); ++j)
            {
                _imgBW.put((geoLines.get(i)).start,j,new byte[]{(byte)150});
                _imgBW.put((geoLines.get(i)).end, j, new byte[]{(byte)127});
            }
        }*/
        //_imgBW.copyTo(image);
        return allLinesImages;

    }

    public ArrayList<Mat> cutOneLine2Characters(ArrayList<Mat> listImagesLines, int i)
    {
        ArrayList<Mat> listImagesCharacters = new ArrayList<>();
        listImagesCharacters.clear();

        if(!listImagesLines.isEmpty())
            listImagesCharacters = tryCutWord(listImagesLines.get(i));

        return listImagesCharacters;
    }

    public Mat smoothingHistogramme(Mat image){
        int x;
        int black = 0;  // select BLACK color.
        int white = 255;

        for (int i = 1; i < image.rows()-2; ++i) {
            for (int j = 1; j < image.cols()-2; ++j) {
                x = 0;
                // TODO should i Cast white,black values to Byte ?.[No 100%]
                if(image.get(i, j)[0] == white)
                {
                    if(image.get(i+1, j+1)[0] == black) x++;
                    if(image.get(i-1, j-1)[0] == black) x++;
                    if(image.get(i, j+1)[0]   == black) x++;
                    if(image.get(i, j-1)[0]   == black) x++;
                    if(image.get(i+1, j)[0]   == black) x++;
                    if(image.get(i-1, j)[0]   == black) x++;
                    if(image.get(i-1, j+1)[0] == black) x++;
                    if(image.get(i+1, j-1)[0] == black) x++;
                }
                if(x >= 6) image.put(i,j,new byte[]{(byte)black});
            }
        }

        for (int i = 1; i < image.rows()-2; ++i) {
            for (int j = 1; j < image.cols()-2; ++j) {
                x = 0;
                if(image.get(i, j)[0] == black)
                {
                    if(image.get(i + 1, j + 1)[0] == white) x++;
                    if(image.get(i - 1, j - 1)[0] == white) x++;
                    if(image.get(i, j+1)[0]       == white) x++;
                    if(image.get(i, j-1)[0]       == white) x++;
                    if(image.get(i+1, j)[0]       == white) x++;
                    if(image.get(i-1, j)[0]       == white) x++;
                    if(image.get(i-1, j+1)[0]     == white) x++;
                    if(image.get(i+1, j-1)[0]     == white) x++;
                }
                if(x >= 4) image.put(i, j, new byte[]{(byte)white});
            }
        }
        return image;
    }

    public int mostRedundantValue(ArrayList<Integer> list){
        int value=0;
        Candidate C = new Candidate();
        ArrayList<Candidate> mCondidate = new ArrayList<>();
        mCondidate.clear();
        while(list.size() != 0){
            int x = list.get(0);
            int repeated=0;

            for(int i=0;i< list.size();i++){
                if (list.get(i) == x ){
                    list.remove(i);
                    i--;
                    repeated++;
                }
            }
            C.timeRepeated = repeated;
            C.value        = x;
            mCondidate.add(C);
        }

        for (int i = 0; i < mCondidate.size(); ++i) {
            if(mCondidate.get(i).value == 0 )  mCondidate.remove(i);
        }

        int max=0;
        for (int i = 0; i < mCondidate.size(); ++i) {
            if(max < mCondidate.get(i).timeRepeated ) {
                max = mCondidate.get(i).timeRepeated;
                value = mCondidate.get(i).value;
            }
        }

        return value;
    }

    public ArrayList<Integer> cutWhereWhite(ArrayList<Integer> list){
        ArrayList<Integer> listPosCuts = new ArrayList<>();
        listPosCuts.clear();
        for(int i=1;i<list.size();i++)
        {
            if((list.get(i) == 0) && (list.get(i - 1) > list.get(i) ))
            {
                listPosCuts.add(i);
            }
        }
        return listPosCuts;
    }

    public ArrayList<Mat> getCharactersInWord(Mat image,ArrayList<Integer> posCuts){
        ArrayList<Mat> listCharacters = new ArrayList<>();
        listCharacters.clear();
        int start = 0;
        int end   = 0;
        listCharacters.clear();
        for(int i=0; i< posCuts.size();i++)
        {
            end = posCuts.get(i);
            listCharacters.add(copyRect(image, 0, start, image.rows(), end));
            start = end;
        }
        listCharacters.add(copyRect(image, 0, start, image.rows(), image.cols()));
        return listCharacters;
    }

    public ArrayList<Mat> tryCutWord(Mat imageIn){  // TODO : CORE EPIC METHODE, Fix it YOU WIN.

        ArrayList<Mat> listCharacters = new ArrayList<>();
        listCharacters.clear();

        if(imageIn.rows()> 5 && imageIn.cols()>5)
        {
            ArrayList<Integer> HDataHist = new ArrayList<>();
            ArrayList<Integer> VDataHist = new ArrayList<>();
            ArrayList<Integer> Hist_3    = new ArrayList<>();
            ArrayList<Integer> posCuts   = new ArrayList<>();
            HDataHist.clear();
            VDataHist.clear();
            Hist_3.clear();
            posCuts.clear();

            int x_Start = 0;
            int x_End   = 0;
            int y_Start = 0;
            int y_End   = 0;
            int PosBaseLine = 0;
            int max = 0;

            HDataHist = calculateBackProjection(imageIn, HORIZONTAL);
            // GET [x_Start-x_End] calculate THICKNESS [ 44% / 22%] >> [50% / 33%] >> Solution Inchallah [85% / 55%] >> Critic [20% / 20%]X
            // >> Emmm [85% / 70%]  >>
            int paramTop = 85; int paramButtom = 80;
            for(int i=0; i<HDataHist.size();i++)
            {
                if(HDataHist.get(i) != 0) { x_Start = i; break; }
            }
            for(int i=HDataHist.size()-2; i >= 0; i--)
            {
                if(HDataHist.get(i) != 0) { x_End = i; break; }
            }

            //imageIn = contour(imageIn);
            VDataHist = calculateBackProjection(imageIn,HistType.VERTICAL);
            max=0;
            for(int i=0; i<VDataHist.size();i++)
            {
                if(max < VDataHist.get(i) ){ max = VDataHist.get(i); }
            }

            // GET [y_Start-y_End]
            for(int i=0; i<VDataHist.size();i++)
            {
                if(VDataHist.get(i) != 0) { y_Start = i; i=VDataHist.size(); }
            }
            for(int i=VDataHist.size()-2; i >= 0; i--)
            {
                if(VDataHist.get(i) != 0) { y_End = i; i=0; }
            }

            //  Entire New Pure Image.
            Mat part = copyRect(imageIn,x_Start,y_Start,x_End,y_End);
            // Getting the Right width of the zone BaseLine [ PROPOSED ].
            HDataHist = calculateBackProjection(part, HORIZONTAL);
            for(int i=0; i<HDataHist.size();i++)
            {
                if(max < HDataHist.get(i) ){ max = HDataHist.get(i); PosBaseLine = i; }
            }
            int Start = PosBaseLine-(((PosBaseLine-x_Start)*paramTop)/100);
            int End   = (((x_End-PosBaseLine)*paramButtom)/100)+PosBaseLine;
            Mat clonedImageIn = part;
            Mat showImage     = part;

            part = copyRect(imageIn,Start,y_Start,End,y_End);

            VDataHist = calculateBackProjection(part,HistType.VERTICAL);

            //!**********************************************************************************
            Mat Visualization_V = new Mat();
            Visualization_V.create(max,part.cols(),CvType.CV_8UC1);
            // convert to white
            for (int i = 0; i < Visualization_V.rows(); ++i) {
                for (int j = 0; j < Visualization_V.cols(); ++j)
                {
                    Visualization_V.put(i, j, new byte[]{(byte)255});
                }
            }
            // Draw Vertical hist From
            for (int i = 0; i < VDataHist.size(); ++i) {
                for (int j = 0; j < VDataHist.get(i); ++j)
                {
                    Visualization_V.put(j, i, new byte[]{(byte)0});
                }
            }

            //imshow("Before Vertical Projection",Visualization_V);
            Imgproc.GaussianBlur(Visualization_V, Visualization_V, new Size(5,5), 0, 0);
            Imgproc.threshold(Visualization_V, Visualization_V, 127, 255, Imgproc.THRESH_BINARY);
            Visualization_V = smoothingHistogramme(Visualization_V);
            //imshow("After Vertical Projection",Visualization_V);

            //!>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><
            Hist_3 = calculateBackProjection(Visualization_V,HistType.VERTICAL);
            int threshold = mostRedundantValue(Hist_3);
            posCuts = cutWhereWhite(Hist_3);
            //Qdebug()<<"threshold of cutting based_On_Contour : "<<QString::number(threshold);
            int tracker=0;
            // From Left To Right
            for(int i=1; i<Hist_3.size()-2; i++)
            {
                if((Hist_3.get(i - 1) == Hist_3.get(i)) && (Hist_3.get(i) == Hist_3.get(i + 1)) && (Hist_3.get(i) == threshold) )
                {
                    tracker++;
                    if(tracker > 2 && (Hist_3.get(i + 1) != Hist_3.get(i + 2)))
                    {
                        posCuts.add(i-(tracker));  // minus :  (tracker/2) OR (tracker/3) OR tracker
                        tracker=0;
                    }
                }
            }
            // qSort(posCuts.begin(), posCuts.end(), qLess<int>());  // [Solved] c++
            Collections.sort(posCuts);
            /*for (int j = 0; j < posCuts.size(); ++j)   // temporary Visualization [ SHOW RESULT ].
            {
                //Qdebug()<<"Cut >_"<<QString::number(posCuts.at(j));
                for(int i=0; i< showImage.rows(); i++)
                {
                    showImage.at<uchar>(i, posCuts.at(j)) = 255;
                }
                for(int i=0; i< Visualization_V.rows; i++)
                {
                    Visualization_V.at<uchar>(i, posCuts.at(j)) =  255;
                }
            }
            for(int i=0; i< showImage.cols;i++){ showImage.at<uchar>(PosBaseLine,i) = 127; }*/
            //imshow("Back Projection cuts",Visualization_V);
            //imshow("Base Line",showImage);

            //! Display the Image Resulted.
            /*imgInProcessus = QImage((uchar*) showImage.data, showImage.cols, showImage.rows, showImage.step, QImage::Format_Indexed8);
            imgInProcessus.bits();
            //display(imgInProcessus,"Cut Words");
            showImage.copyTo(imageIn);*/
            listCharacters = getCharactersInWord(clonedImageIn,posCuts);
            // POST PROCESSING NEEDED ONLY [ later inchallah].
            return listCharacters;
        }
        else
            return listCharacters;
    }

    public void getPosCutWord(Mat imageLine, int lineNbr){
        // Vertical Histogram.
        ArrayList<Integer> HistLineData = new ArrayList<>();
        HistLineData.clear();
        HistLineData = calculateBackProjection(imageLine,HistType.VERTICAL);

        // word Calculate position of cuts.
        Word W = new Word();
        Boolean lock = false;
        int i=0;
        int t_S_Pos =geoWords.size();
        while(i<HistLineData.size())
        {
            if(HistLineData.get(i) != 0)
            {
                W.y_start = i-1;
                W.x_start = geoLines.get(lineNbr).start;
                lock = true;
                i++;

                while (lock)
                {
                    if(HistLineData.get(i) == 0)
                    {
                        W.y_end = i+1;
                        W.x_end = geoLines.get(lineNbr).end;
                        geoWords.add(W);
                        lock=false;
                    }
                    i++;
                }
            }
            else i++;
        }
        // For ARABIC SCRIPT [07_04_2015 20:00] (SAVED).
        int t_E_Pos = geoWords.size();
        int Z       = (t_E_Pos-t_S_Pos)/2;
        for(int j=t_S_Pos; j< t_S_Pos+Z; j++)
        {
            t_E_Pos--;
            // geoWords.swap(j,t_E_Pos); // c++
            Collections.swap(geoWords,j,t_E_Pos);
        }
    }

    public void wordsDetection(){

        for(int i=0; i< geoLines.size();i++) // TODO _imgWB ? emmm !??
        {
            // complicated, but under Control [ Histogram Smoothing needed MAYBE ].
            getPosCutWord(copyRect(_imgBW, geoLines.get(i).start, 0, geoLines.get(i).end, _imgBW.cols()),i);
        }
    }

    public static Mat characterNormalization(Mat image){
        if(!image.empty() && image.cols()>0 && image.rows()>0)
        {
            int x1=0,y1=0,x2=0,y2=0;
            int x = 0;
            ArrayList<Integer> VHist = new ArrayList<>(); // = calculateBackProjection(image,V_Hist);
            VHist.clear();
            for (int i = 0; i < image.cols(); ++i){
                x=0;
                for (int j = 0; j < image.rows(); ++j)
                {
                    if(image.get(j, i)[0] == 0) x++;
                }
                VHist.add(x);
            }

            for(int i=0; i<VHist.size();i++)
            {
                if(VHist.get(i) != 0) { y1 = i; break; }

            }
            for(int i=VHist.size()-2; i >= 0; i--)
            {
                if(VHist.get(i) != 0) { y2 = i; break; }
            }

            ArrayList<Integer> HHist = new ArrayList<>();
            HHist.clear();  // calculateBackProjection(image,H_Hist);

            for (int i = 0; i < image.rows(); ++i){
                x=0;
                for (int j = 0; j < image.cols(); ++j){
                    if(image.get(i, j)[0] == 0) x++;
                }
                HHist.add(x);
            }

            for(int i=0; i<HHist.size();i++)
            {
                if(HHist.get(i) != 0) { x1 = i; break; }
            }
            for(int i=HHist.size()-2; i >= 0; i--)
            {
                if(HHist.get(i) != 0) { x2 = i; break;}
            }
            VHist.clear();
            HHist.clear();

            image = copyRect(image,x1,y1,x2,y2);
            Imgproc.resize(image, image, new Size(100, 100), 0, 0, Imgproc.INTER_CUBIC);
            Imgproc.threshold(image, image, 127, 255, Imgproc.THRESH_BINARY);
            // imwrite(QString::number(w).toStdString()+".png",image);   w++;   // For Demonstration Only.
        }
        return image;
    }

    public ArrayList<Mat> segmenteEntierDocument(Mat image){
        image.copyTo(_imgBW);
        ArrayList<Mat> allWordsImages = new ArrayList<>();
        allWordsImages.clear();

        // Vertical Histogram.
        ArrayList<Integer> H_HistDATA = new ArrayList<>();
        H_HistDATA = calculateBackProjection(_imgBW, HORIZONTAL);
        int max = 0;
        for(int i=0; i< H_HistDATA.size();i++)
        {
            if(max < H_HistDATA.get(i) ){ max = H_HistDATA.get(i); }
        }

        // Drawing the Histogram.
        Mat histImage = new Mat();
        histImage.create(_imgBW.rows(),max,CvType.CV_8UC1);

        for (int i = 0; i < histImage.rows(); ++i) {
            for (int j = 0; j < histImage.cols(); ++j)
            {
                histImage.put(i, j, new byte[]{(byte)255});
            }
        }

        for (int i = 0; i < _imgBW.rows(); ++i) {
            for (int j = 0; j < H_HistDATA.get(i); ++j) {
                histImage.put(i, j,new byte[]{(byte)0});
            }
        }
        //imshow("H Histogramme ",histImage);
        histImage = smoothingHistogramme(histImage);
        //imshow("H Histogramme S",histImage);
        // END DRAWING.

        //! [ 03/03/2015  21:49] CALL FOR METHODES <? Wandring ?>
        this.lineDetection(H_HistDATA);
        this.wordsDetection();

        // Cut the Entier Image Into Small Pieces We call Them Words.
        for(int i = 0; i<geoWords.size(); ++i){  //JAVAFX
            allWordsImages.add(copyRect(_imgBW, geoWords.get(i).x_start, geoWords.get(i).y_start, geoWords.get(i).x_end, geoWords.get(i).y_end));
        }

        // Visualisation the Detection lines.
        for(int i =0; i< geoLines.size(); i++)
        {
            for (int j = 0; j < _imgBW.cols(); ++j)
            {
                _imgBW.put((geoLines.get(i)).start,j,new byte[]{(byte)150});
                _imgBW.put((geoLines.get(i)).end,j,new byte[]{(byte)127});
            }
        }

        // Visualisation the Detection words.
        for(int i =0; i< geoWords.size(); ++i){

            for (int j = geoWords.get(i).x_start; j < geoWords.get(i).x_end; ++j)
            {
                _imgBW.put(j, geoWords.get(i).y_start, new byte[]{(byte)200});
                _imgBW.put(j, (geoWords.get(i)).y_end, new byte[]{(byte)130});
            }
        }

        geoLines.clear();
        geoWords.clear();

        _imgBW.copyTo(image);
        return allWordsImages;
    }

}

    /* Methode to implementation in Java (5/16 : 01h:45m ), (12/16 : 03h:35m ) , (16/16 : 04h:00m )

    */
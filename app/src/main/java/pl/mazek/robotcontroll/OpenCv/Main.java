package pl.mazek.robotcontroll.OpenCv;

import org.opencv.core.*;

import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;
import java.util.Comparator;

import pl.mazek.robotcontroll.OpenCv.Color.Colors;
import pl.mazek.robotcontroll.OpenCv.Comparator.*;

public class Main {
    static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

    static Mat sourceImage;

//    public static void main(String[] args) {
//        for(int i=1; i < 7; i++) {
//            sourceImage = IOfile.LoadPicture("C:\\photo\\source\\p" + i + ".jpg");
//            Mat binary = preproc(sourceImage);
//            Imgproc.cvtColor(binary, binary, Imgproc.COLOR_GRAY2BGR);
//            IOfile.SavePicture(bitwise(i,sourceImage, preproc(sourceImage), contours(binary)), "C:\\photo\\output\\p"+i+"\\p" + i + ".jpg");
//        }
//    }

    public static int[] getRange(double[] bgr){
        Mat tmp = new Mat(1,1,CvType.CV_8UC3);
        tmp.put(0,0, bgr);
        Mat color = tmp.clone();
        Imgproc.cvtColor(tmp,color, Imgproc.COLOR_BGR2HSV);
        System.out.println(tmp.toString());
        int[] range = new int[2];
        range[0] = (int)color.get(0,0)[0]-10;
        System.out.println(range[0]);
        range[1] = (int)color.get(0,0)[0]+10;
        System.out.println(range[1]);
        return range;
    }

    public static Mat preproc(Mat frame) {
        Mat tmp = frame.clone();
        Mat preprosMat = new Mat(frame.size(),CvType.CV_8UC3);
        Mat lower = frame.clone();
        Mat upper = frame.clone();
        Mat color = frame.clone();
        Imgproc.cvtColor(frame, tmp, Imgproc.COLOR_BGR2HSV);
        Core.inRange(tmp,
                new Scalar(0, 100,100),
                new Scalar(10, 255, 255),
                lower); //RED
        Core.inRange(tmp,
                new Scalar(160, 100,100),
                new Scalar(180, 255, 255),
                upper); //RED
        Core.addWeighted(lower, 1,upper, 1, 0, preprosMat );
        Core.inRange(tmp,
                new Scalar(20, 100,100),
                new Scalar(40, 255,255),
                color); //YELLOW
        Core.addWeighted(color, 1,preprosMat, 1, 0, preprosMat );
        Core.inRange(tmp,
                new Scalar(10, 100,100),
                new Scalar(18, 255,255),
                color); //ORANGE
        Core.addWeighted(color, 1,preprosMat, 1, 0, preprosMat );
        Core.inRange(tmp,
                new Scalar(45, 100,100),
                new Scalar(75, 255,255),
                color); //GREEN
        Core.addWeighted(color, 1,preprosMat, 1, 0, preprosMat );
        Core.inRange(tmp,
                new Scalar(100, 150,100),
                new Scalar(140, 255,255),
                color); //BLUE
        Core.addWeighted(color, 1,preprosMat, 1, 0, preprosMat );
        Core.inRange(tmp,
                new Scalar(10, 100,100),
                new Scalar(18, 255,255),
                color); //ORANGE
        Core.addWeighted(color, 1,preprosMat, 1, 0, preprosMat );
        Imgproc.cvtColor(frame, tmp, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(tmp, color, 127, 255, 0);
        Core.addWeighted(color, 1,preprosMat, 1, 0, preprosMat );
        Imgproc.erode(preprosMat, preprosMat, new Mat(10,10,preprosMat.type()));
        Imgproc.erode(preprosMat, preprosMat, new Mat(10,10,preprosMat.type()));
        return preprosMat;
    }

    public static List<MatOfPoint> contours(Mat frame){
        Mat gray = new Mat();
        Mat threshold = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray, threshold, 127,255, 0);
        ArrayList<MatOfPoint> matOfPoints = new ArrayList<>();
        Imgproc.findContours(threshold, matOfPoints, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        eliminateSmallCollections(matOfPoints);

        return matOfPoints;
    }

    public static byte[] bitwise(Mat frame, Mat mask, List<MatOfPoint> matOfPoints){
        Mat output = new Mat(frame.size(),CvType.CV_8UC3);
        for(int y =0; y < frame.height(); y++){
            for(int x=0; x < frame.width(); x++){
                if(mask.get(x,y)[0] != 0.0){
                    output.put(x,y,Colors.getColor(Colors.bgr2hsvDouble(frame.get(x,y))));
                }
            }
        }
        int i=0;
        byte[] tab = new byte[9];
        for(MatOfPoint mop : matOfPoints) {
            tab[i++] = calcHistogram(new Mat(output, Imgproc.boundingRect(mop)));
        }
        return tab;
    }

    private static List<MatOfPoint> eliminateSmallCollections(List<MatOfPoint> matOfPoints) {
        while (matOfPoints.size() > 9) {
            int min = Integer.MAX_VALUE, i = 0, index = 0;
            for (MatOfPoint matOfPoint : matOfPoints) {
                if (min > matOfPoint.toList().size()) {
                    min = matOfPoint.toList().size();
                    index = i;
                }
                i++;
            }
            matOfPoints.remove(index);
        }
        trySort(matOfPoints);
        return matOfPoints;
    }

//    private static void findCornerPoints(int photo,MatOfPoint matOfPoint, int number, Mat image) {
//        Rect rectOfPoint = Imgproc.boundingRect(matOfPoint);
//
//        Mat analiseMat = new Mat(image, rectOfPoint);
//
//        Imgcodecs.imwrite("C:\\photo\\output\\p"+photo+"\\pp"+number+".jpg", analiseMat);
//        Imgcodecs.imwrite("C:\\photo\\output\\p"+photo+"\\ph"+number+".jpg", calcHistogram(analiseMat));
//
//    }

    private static byte calcHistogram(Mat image){
        Mat gray = image.clone();
        Imgproc.cvtColor(image,gray,Imgproc.COLOR_BGR2HSV);
        List<Mat> matList = new ArrayList<>();
        matList.add(gray);

        Mat hist_H = new Mat();
        Mat hist_S = new Mat();
        Mat hist_V = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        final MatOfFloat histRange = new MatOfFloat(0f, 256f);

        Imgproc.calcHist(matList,new MatOfInt(0),new Mat(),hist_H,histSize,histRange);
        Imgproc.calcHist(matList,new MatOfInt(1),new Mat(),hist_S,new MatOfInt(100),histRange);
        Imgproc.calcHist(matList,new MatOfInt(2),new Mat(),hist_V,new MatOfInt(100),histRange);

        // Create space for histogram image
        Mat histImage = Mat.zeros( 100, (int)histSize.get(0, 0)[0], CvType.CV_8UC3);
        // Normalize histogram
        Core.normalize(hist_H, hist_H, 1, histImage.rows() , Core.NORM_MINMAX, -1, new Mat() );
        Core.normalize(hist_S, hist_S, 1, histImage.rows() , Core.NORM_MINMAX, -1, new Mat() );
        Core.normalize(hist_V, hist_V, 1, histImage.rows() , Core.NORM_MINMAX, -1, new Mat() );
        // Draw lines for histogram points
        List<Integer> h = new ArrayList<>();
        List<Integer> s = new ArrayList<>();
        List<Integer> v = new ArrayList<>();
        for(int i=0; i < (int)histSize.get(0, 0)[0]; i++){
            h.add((int)hist_H.get(i,0)[0]);
            try {
                s.add((int) hist_S.get(i, 0)[0]);
                v.add((int) hist_V.get(i, 0)[0]);
            }catch (Exception e){

            }
        }
        //System.out.println(Colors.getColorName(new double[]{2*h.indexOf(Collections.max(h)), s.indexOf(Collections.max(s)), v.indexOf(Collections.max(v))}));
        return Colors.getColorIndex(new double[]{2*h.indexOf(Collections.max(h)), s.indexOf(Collections.max(s)), v.indexOf(Collections.max(v))});
    }

    private static List<MatOfPoint> trySort(List<MatOfPoint> list){
        List<MatOfPoint> high = new ArrayList<>();
        List<MatOfPoint> mid = new ArrayList<>();
        List<MatOfPoint> low = new ArrayList<>();
        Collections.sort(list,new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                if(Imgproc.boundingRect(o1).x > Imgproc.boundingRect(o2).x)
                    return 1;
                else if(Imgproc.boundingRect(o1).x < Imgproc.boundingRect(o2).x)
                    return -1;
                else return 0;
            }
        });
        high.add(list.get(0));
        high.add(list.get(1));
        high.add(list.get(2));
        mid.add(list.get(3));
        mid.add(list.get(4));
        mid.add(list.get(5));
        low.add(list.get(6));
        low.add(list.get(7));
        low.add(list.get(8));

        Collections.sort(high,new pl.mazek.robotcontroll.OpenCv.Comparator.Comparator());
        Collections.sort(mid,new pl.mazek.robotcontroll.OpenCv.Comparator.Comparator());
        Collections.sort(low,new pl.mazek.robotcontroll.OpenCv.Comparator.Comparator());

        list.clear();
        list.addAll(high);
        list.addAll(mid);
        list.addAll(low);
        return list;
    }
}

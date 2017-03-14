package pl.mazek.robotcontroll.OpenCv.Comparator;

import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Mazek on 13.03.2017.
 */
public class Comparator implements java.util.Comparator<MatOfPoint> {
    @Override
    public int compare(MatOfPoint o1, MatOfPoint o2) {
        if(Imgproc.boundingRect(o1).x > Imgproc.boundingRect(o2).x)
            return 1;
        else if(Imgproc.boundingRect(o1).x < Imgproc.boundingRect(o2).x)
            return -1;
        else return 0;
    }
}

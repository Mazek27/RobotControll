package pl.mazek.robotcontroll.OpenCv.IO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Mazek on 09.03.2017.
 */
public class IOfile {
    public static void SavePicture(Mat picture, String source){
        Imgcodecs.imwrite(source, picture);
    }

    public static Mat LoadPicture(String source){
        return Imgcodecs.imread(source, CvType.CV_64FC3);
    }
}

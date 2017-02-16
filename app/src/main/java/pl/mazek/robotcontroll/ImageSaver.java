package pl.mazek.robotcontroll;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Mazek on 25.12.2016.
 */

public class ImageSaver implements Callable<List<Color>>{
    private final Bitmap mBitmap;

    public ImageSaver(Bitmap image){
        mBitmap = image;
    }

//    @Override
//    public void run() {
//
////        byte[] bytes = new byte[byteBuffer.remaining()];
////        byteBuffer.get(bytes);
//    }

    @Override
    public List<Color> call() throws Exception {
       // mBitmap.getPixels(10,10);
        return null;
    }
}

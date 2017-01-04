package pl.mazek.robotcontroll;

import android.media.Image;

import java.nio.ByteBuffer;

/**
 * Created by Mazek on 25.12.2016.
 */

public class ImageSaver implements Runnable{
    private final Image mImage;

    public ImageSaver(Image image){
        mImage = image;
    }

    @Override
    public void run() {
        ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
    }
}

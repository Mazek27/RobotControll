package pl.mazek.robotcontroll;

import android.content.Context;
import android.widget.ImageView;

/**
 * Created by Mazek on 04.03.2017.
 */

public class CImageView {
    public ImageView imageView;
    public int color;

    public CImageView(ImageView imageView, int color) {
        this.imageView = imageView;
        this.color = color;
    }

    public CImageView(Context context) {
        this.imageView = new ImageView(context);
    }
}

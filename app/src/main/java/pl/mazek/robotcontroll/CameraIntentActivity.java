package pl.mazek.robotcontroll;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint3;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Random;

public class CameraIntentActivity extends CameraActivity implements SensorEventListener {


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private int countClick = 0;
    private ImageView[] imageView;
    private GridLayout gridLayout;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    Mat imageMat=new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_intent);
        gridLayout = (GridLayout) findViewById(R.id.gridLayout);
        imageView = new ImageView[54];
        int i=0, x, y;
        char[] mChar = {'F','R','B','L','D','U'};
        int r=0,c=0;
        for (char ch: mChar) {
            x=0;
            y=0;
            switch (ch){
                case 'U': {
                    r = 0;
                    c = 4;
                }
                break;
                case 'R':{
                    r = 3;
                    c = 7;
                }
                break;
                case 'F':{
                    r = 3;
                    c = 4;
                }
                break;
                case 'D':{
                    r = 6;
                    c = 4;
                }
                break;
                case 'L':{
                    r = 3;
                    c = 1;
                }
                break;
                case 'B':{
                    r = 3;
                    c = 10;
                }
                break;
            }
            for(int j=1; j<10; j++){

                GridLayout.LayoutParams gl = new GridLayout.LayoutParams();
                gl.columnSpec = GridLayout.spec(c+x);
                gl.rowSpec = GridLayout.spec(r+y);
                gl.width = 30;
                gl.height = 30;
                imageView[i] = new ImageView(this);
                imageView[i].setTag(ch + j);
                imageView[i].setLayoutParams(gl);
                imageView[i].setBackgroundColor(Color.rgb(255,0,0));
                gridLayout.addView(imageView[i]);

                if(j%3 == 0){
                    y++;
                }
                if(x==2){
                    x = 0;
                } else {
                    x++;
                }
                i++;
            }
        }


        captureButton = (Button) findViewById(R.id.analiseButton);
        captureButton.setOnClickListener(buttonOnClickListener);

        leftColor = (ImageView) findViewById(R.id.leftColor);
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private Button.OnClickListener buttonOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            clicked = true;
            lockFocus();
            Bitmap mBitmap = mTextureView.getBitmap();



            int[] color = getColors(mBitmap);
            int start = countClick == 0 ? 0 : countClick * 8 +countClick;
            int index = 0;
            for(int i=start; i<start+8+1; i++){
                imageView[i].setBackgroundColor(color[index]);
                index++;
            }

//            captureButton.setText(String.valueOf(hsv[0]));
            Log.d("Button", clicked ? "true" : "false");
            countClick++;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        openBackgroundThread();
        openAnaliseThread();
        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(this);

        closeCamera();
        closeBackgroundThread();
        closeAnaliseThread();
        super.onPause();
    }





    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > 0 && countClick < 6) {
            Random rnd = new Random();
            int choose = rnd.nextInt(4) + 1;
            switch (choose) {
                case 1: {
                    leftColor.setBackgroundResource(R.drawable.black);
                }
                break;
                case 2: {
                    leftColor.setBackgroundResource(R.drawable.blue);
                }
                break;
                case 3: {
                    leftColor.setBackgroundResource(R.drawable.green);
                }
                break;
                case 4: {
                    leftColor.setBackgroundResource(R.drawable.red);
                }
                break;
            }
        } else {
            leftColor.setBackgroundResource(R.drawable.white);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("CameraIntent Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}

package pl.mazek.robotcontroll;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.TextView;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Random;

import pl.mazek.robotcontroll.Solver.Search;

import static pl.mazek.robotcontroll.OpenCv.Main.bitwise;
import static pl.mazek.robotcontroll.OpenCv.Main.contours;
import static pl.mazek.robotcontroll.OpenCv.Main.preproc;

public class CameraIntentActivity extends CameraActivity implements SensorEventListener {


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    String solve;


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private int countClick = 0;
    private CImageView[] imageView;
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
        imageView = new CImageView[54];
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
                imageView[i] = new CImageView(this);
                imageView[i].imageView.setTag(ch + j);
                imageView[i].imageView.setLayoutParams(gl);
                imageView[i].imageView.setBackgroundColor(Color.rgb(255,0,0));
                gridLayout.addView(imageView[i].imageView);

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
            if(countClick < 6) {
                lockFocus();
                Bitmap mBitmap = mTextureView.getBitmap();
                Mat sourceImage = new Mat(mBitmap.getWidth(), mBitmap.getHeight(),CvType.CV_64FC3);
                //Utils.bitmapToMat(mBitmap, sourceImage);
                for(int x=0; x < mBitmap.getWidth(); x++)
                    for(int y=0; y< mBitmap.getHeight(); y++){
                        sourceImage.put(x,y,
                                mBitmap.getPixel(x,y));
                    }

                Mat binary = preproc(sourceImage);
                Imgproc.cvtColor(binary, binary, Imgproc.COLOR_GRAY2BGR);

                int[] color = getColors(bitwise(sourceImage, preproc(sourceImage), contours(binary)));
                int start = countClick == 0 ? 0 : countClick * 8 + countClick;
                int index = 0;
                for (int i = start; i < start + 8 + 1; i++) {
                    imageView[i].imageView.setBackgroundColor(color[index]);
                    imageView[i].color = color[index];
                    index++;
                }

//            captureButton.setText(String.valueOf(hsv[0]));
                Log.d("Button", clicked ? "true" : "false");
                countClick++;
            } else {
                 char[] cubeCoord = new char[54];
                    for(int i =0; i < 54; i++){
                        if(imageView[i].color == imageView[4].color)
                            cubeCoord[i] = 'F';//
                        else if(imageView[i].color == imageView[4+9].color)
                            cubeCoord[i] = 'R';//
                        else if(imageView[i].color == imageView[4+9+9].color)
                            cubeCoord[i] = 'B';//
                        else if(imageView[i].color == imageView[4+9+9+9].color)
                            cubeCoord[i] = 'L';//
                        else if(imageView[i].color == imageView[4+9+9+9+9].color)
                            cubeCoord[i] = 'D';
                        else if(imageView[i].color == imageView[4+9+9+9+9+9].color)
                            cubeCoord[i] = 'U';//
                    }
                    //Log.e("Solve", Search.solution(cubeCoord.toString(), 5, 20, false));
                String solution = output(Search.solution(mindfreak(cubeCoord), 20, 5, false));
                        //Search.solution(new String(cubeCoord), 20, 5, false);

                TextView cubeView =(TextView) findViewById(R.id.textView8);
                cubeView.setText(mindfreak(cubeCoord));
                TextView solutionView =(TextView) findViewById(R.id.textView9);
                solutionView.setText(solution);
                //checkDone = true;
                solve = "";
                for(char c : output(solution).toCharArray()){
                    solve += c;
                }

                ((ImageView) findViewById(R.id.rightColor)).setBackgroundResource(R.drawable.green);
//
                checkDone = true;


                //dlgAlert.setMessage(Search.solution(cubeCoord.toString(), 5, 20, false));

                }
        }
    };

    private String output(String input){
        StringBuilder sb = new StringBuilder();
        char[] c = input.toCharArray();

        for(int i=0; i< c.length; i++){
            if(c[i] != 32){
                if(c[i] != 50 && c[i] != '\''){
                    sb.append(c[i]);
                } else {
                    if(c[i] == 50)
                        sb.append(c[i-1]);
                    else{
                        sb.append(c[i-1]);
                        sb.append(c[i-1]);
                    }

                }
            }
        }
        return sb.toString();
    }

    public String mindfreak(char[] charArray){
        StringBuilder sb = new StringBuilder();
        String s = new String(charArray);

        sb.append(s.substring(45,54));//
        sb.append(s.substring(9,18));
        sb.append(s.substring(0,9));
        sb.append(s.substring(36,45));
        sb.append(s.substring(27,36));
        sb.append(s.substring(18,27));

        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
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
        if(checkDone){
            if (event.values[0] > 0 && solve.length() > 0) {
                switch (solve.toCharArray()[0]) {
                    case 'R': {
                        ((TextView) findViewById(R.id.textView7)).setText("R " + solve.toCharArray()[0] + " " + solve.length());
                        leftColor.setBackgroundResource(R.drawable.orange);
                        solve = solve.substring(1,solve.length());
                    }
                    break;
                    case 'L': {
                        ((TextView) findViewById(R.id.textView7)).setText("L " + solve.toCharArray()[0] + " " + solve.length());
                        leftColor.setBackgroundResource(R.drawable.blue);
                        solve = solve.substring(1,solve.length());
                    }
                    break;
                    case 'F': {
                        ((TextView) findViewById(R.id.textView7)).setText("F " + solve.toCharArray()[0] + " " + solve.length());
                        leftColor.setBackgroundResource(R.drawable.green);
                        solve = solve.substring(1,solve.length());
                    }
                    break;
                    case 'D': {
                        ((TextView) findViewById(R.id.textView7)).setText("D " + solve.toCharArray()[0] + " " + solve.length());
                        leftColor.setBackgroundResource(R.drawable.red);
                        solve = solve.substring(1,solve.length());
                    }
                    break;
                    case 'B': {
                        ((TextView) findViewById(R.id.textView7)).setText("B " + solve.toCharArray()[0] + " " + solve.length());
                        leftColor.setBackgroundResource(R.drawable.yellow);
                        solve = solve.substring(1,solve.length());
                    }
                    break;
                    case 'U': {
                        ((TextView) findViewById(R.id.textView7)).setText("U " + solve.toCharArray()[0] + " " + solve.length());
                        leftColor.setBackgroundResource(R.drawable.white);
                        solve = solve.substring(1,solve.length());
                    }
                    break;
                }
            } else if(event.values[0] > 0 && solve.length() == 0 ){
                ((ImageView) findViewById(R.id.rightColor)).setBackgroundResource(R.drawable.red);
            }
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

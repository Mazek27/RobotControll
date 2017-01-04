package pl.mazek.robotcontroll;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ImageFormat;
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
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

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

    private boolean analiseButtonPressed = false;
    private Button analiseButton;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_intent);

        leftColor = (ImageView) findViewById(R.id.leftColor);
        mTextureView = (TextureView) findViewById(R.id.textureView);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

        openBackgroundThread();
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
        super.onPause();
    }





    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > 0) {
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


//        CameraManager mManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try{
//
//            CameraCharacteristics mCharacteristics = mManager.getCameraCharacteristics(mCameraId);
//            Size[] jpegSizes = null;
//            if(mCharacteristics != null)
//                jpegSizes = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
//            int width = 640;
//            int height = 480;
//            if(jpegSizes != null && 0< jpegSizes.length){
//                width = jpegSizes[0].getWidth();
//                height = jpegSizes[0].getHeight();
//            }
//            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
//            List<Surface> outputSurface = new ArrayList<>(2);
//            outputSurface.add(reader.getSurface());
//            outputSurface.add(new Surface(mTextureView.getSurfaceTexture()));
//            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.addTarget(reader.getSurface());
//            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
//
//            reader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    Toast.makeText(getApplicationContext(), "Click analisePreviewShot", Toast.LENGTH_SHORT).show();
//                    Image image = null;
//                    Toast.makeText(getApplicationContext(), "create buffer", Toast.LENGTH_LONG).show();
//                    try{
//                        image = reader.acquireLatestImage();
//                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                        Toast.makeText(getApplicationContext(), String.format("%d %d %d", buffer.get(0),buffer.get(1),buffer.get(2) ), Toast.LENGTH_SHORT).show();
//                        if(buffer != null){
//                            Toast.makeText(getApplicationContext(), "create buffer", Toast.LENGTH_LONG).show();
//                        }
//                    } finally {
//                        if(image != null)
//                            image.close();
//                    }
//                }
//            }, null);


        //mCameraCaptureSession.capture(captureBuilder.build(), captureCallback, null);
//
//        } catch (Exception e){
//            e.printStackTrace();
//        }








//imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
//                    @Override
//                    public void onImageAvailable(ImageReader reader) {
//                        Bitmap bitmap = null;
//                        Image img = null;
//                            img = reader.acquireLatestImage();
//                            if(img != null){
//                                Image.Plane[] planes = img.getPlanes();
//                                if(planes[0].getBuffer() == null){
//                                    return;
//                                } else {
//                                    int width = img.getWidth();
//                                    int height = img.getHeight();
//                                    int pixelStride = planes[0].getPixelStride();
//                                    int rowStride = planes[0].getRowStride();
//                                    int rowPadding = rowStride - pixelStride * width;
//                                    byte[] newData = new byte[width * height * 4];
//
//                                    int offset = 0;
//
//                                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//                                    ByteBuffer buffer = planes[0].getBuffer();
//                                    for (int i = 0; i < height; ++i) {
//                                        for (int j = 0; j < width; ++j) {
//                                            int pixel = 0;
//                                            pixel |= (buffer.get(offset) & 0xff) << 16;     // R
//                                            pixel |= (buffer.get(offset + 1) & 0xff) << 8;  // G
//                                            pixel |= (buffer.get(offset + 2) & 0xff);       // B
//                                            pixel |= (buffer.get(offset + 3) & 0xff) << 24; // A
//                                            bitmap.setPixel(j, i, pixel);
//                                            offset += pixelStride;
//                                        }
//                                        offset += rowPadding;
//                                    }
//
//                                    leftColor.setBackground(new BitmapDrawable(getResources(), bitmap));
//                                }
//                            }
//
//                            analiseButtonPressed = false;
//                        img.close();
//                        Toast.makeText(getApplicationContext(), "Make Photo", Toast.LENGTH_SHORT).show();
//                }}, null);
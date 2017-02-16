package pl.mazek.robotcontroll;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Mazek on 04.01.2017.
 */

public class CameraActivity extends Activity {

    protected boolean clicked;
    protected Button captureButton;
    protected ImageReader imageReader;
    protected TextureView mTextureView;
    protected Size mPreviewSize;
    protected String mCameraId;
    protected TextureView.SurfaceTextureListener mSurfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    setupCamera(width, height);
                    openCamera();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            };
    protected CameraDevice mCameraDevice;
    protected CameraDevice.StateCallback mCameraDeviceStateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    mCameraDevice = camera;
                    createCameraPreviewSession();
                    Log.d("CCPS", "createCameraPreviewSession");
                    Toast.makeText(getApplicationContext(), "Camera On", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    camera.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    camera.close();
                    mCameraDevice = null;
                }
            };
    protected HandlerThread mBackgroundThread;
    protected Handler mBackgroundHandler;

    protected HandlerThread mAnaliseThread;
    protected Handler mAnaliseHandler;
    protected static final int STATE_PREVIEW = 0;
    protected static final int STATE_WAIT_LOCK = 1;
    protected int mState;
    protected SensorManager mSensorManager;
    protected Sensor mProximity;
    protected ImageView leftColor;

    protected CaptureRequest mPreviewCaptureRequest;
    protected CaptureRequest.Builder mPreviewCaptureRequestBuilder;
    protected CameraCaptureSession mCameraCaptureSession;
    protected CameraCaptureSession.CaptureCallback mSessionCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                protected void process(CaptureResult result) {
                    switch (mState) {
                        case STATE_PREVIEW:
                            //Puste pole
                            break;
                        case STATE_WAIT_LOCK:
//                            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
//                            if (afState == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED) {
                            unlockFocus();
//                                Toast.makeText(getApplicationContext(), "Focus Lock Succesfull", Toast.LENGTH_SHORT).show();
//                            }
                            break;
                    }
                }

                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    if (clicked) {
                        //mBackgroundHandler.post(new ImageSaver(mTextureView.getBitmap()));
                        clicked = false;
                        Log.d("Button", clicked ? "true" : "false");
                    }
                    process(result);
                }

                @Override
                public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
                    super.onCaptureFailed(session, request, failure);
                    //Toast.makeText(getApplicationContext(), "Focus Lock Unsuccesfull", Toast.LENGTH_SHORT).show();

                    Log.i("oCF", String.valueOf(failure.getFrameNumber()));

                }
            };

    protected final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d("TAG: ", "The onImageAvailable thread id: " + Thread.currentThread().getId());
                    //Toast.makeText(getApplicationContext(), "Available", Toast.LENGTH_SHORT).show();
                    //mBackgroundHandler.post(new ImageSaver(reader.acquireLatestImage()));

                }
            };


    protected void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                Size largestImageSize = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size o1, Size o2) {
                                return Long.signum(o1.getWidth() * o1.getHeight() - o2.getWidth() * o2.getHeight());
                            }
                        }
                );

                imageReader = ImageReader.newInstance(largestImageSize.getWidth(), largestImageSize.getHeight(), ImageFormat.JPEG, 1);

                mPreviewSize = getPrefferedPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {

                    }
                }, null);
                //mOnImageAvailableListener
//
                mCameraId = cameraId;
                //cameraManager.setTorchMode(mCameraId, true);
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.d("SC", e.getMessage());
        }
    }

    protected void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
//            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    Activity#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for Activity#requestPermissions for more details.
////                AlertDialog alert = new AlertDialog.Builder(this).create();
////
////                alert.setTitle("Uprawnienia");
////                alert.setMessage("Kliknij ok");
//                return;
//            }
            //cameraManager.setTorchMode(mCameraId, true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void closeCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    protected Size getPrefferedPreviewSize(Size[] mapSizes, int width, int height) {
        List<Size> collectorSize = new ArrayList<>();
        for (Size option : mapSizes) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    collectorSize.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    collectorSize.add(option);
                }
            }
        }
        if (collectorSize.size() > 0) {
            return Collections.min(collectorSize, new Comparator<Size>() {
                @Override
                public int compare(Size o1, Size o2) {
                    return Long.signum((o1.getWidth() * o1.getHeight()) - (o2.getWidth() * o2.getHeight()));
                }
            });
        }
        return mapSizes[0];
    }

    protected void createCameraPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            mPreviewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //mPreviewCaptureRequestBuilder.addTarget(imageReader.getSurface());
            mPreviewCaptureRequestBuilder.addTarget(previewSurface);

            //mPreviewCaptureRequestBuilder.addTarget(imageReader.getSurface());
            mPreviewCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
            //mPreviewCaptureRequestBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (float) 1.0);
            //,imageReader.getSurface()

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    if (mCameraDevice == null) {
                        return;
                    }
                    try {
                        mPreviewCaptureRequest = mPreviewCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(
                                mPreviewCaptureRequest,
                                mSessionCaptureCallback,
                                mBackgroundHandler
                        );
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(getApplicationContext(), "Nie udalo sie ", Toast.LENGTH_SHORT);
                }
            },null);

        } catch (CameraAccessException e) {
            System.out.println("ErrorCamera");
            e.printStackTrace();
        }
    }

    protected void openBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera background thread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void closeBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void openAnaliseThread(Image image) {
        mAnaliseThread = new HandlerThread("Camera background thread");
        mAnaliseThread.start();
        mAnaliseHandler = new Handler(mAnaliseThread.getLooper());
    }

    protected void closeAnaliseThread() {
        mAnaliseThread.quitSafely();
        try {
            mAnaliseThread.join();
            mAnaliseThread = null;
            mAnaliseHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void lockFocus() {
        try {
            mState = STATE_WAIT_LOCK;
            mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START);
            mCameraCaptureSession.capture(mPreviewCaptureRequestBuilder.build(),
                    mSessionCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void unlockFocus() {
        try {
            mState = STATE_PREVIEW;
            mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
            mCameraCaptureSession.capture(mPreviewCaptureRequestBuilder.build(),
                    mSessionCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public int[] getColors(Bitmap bitmap){
        int[] color = new int[9];
        color[0] = avgColor(bitmap, (int) (0.05*bitmap.getWidth()), (int) (0.05*bitmap.getHeight()));
        color[1] = avgColor(bitmap, (int) (0.5 *bitmap.getWidth()) , (int) (0.05*bitmap.getHeight()));
        color[2] = avgColor(bitmap, (int) (0.95*bitmap.getWidth()), (int) (0.05*bitmap.getHeight()));
        color[3] = avgColor(bitmap, (int) (0.05*bitmap.getWidth()), (int) (0.5 *bitmap.getHeight()));
        color[4] = avgColor(bitmap, (int) (0.5 *bitmap.getWidth()) , (int) (0.5 *bitmap.getHeight()));
        color[5] = avgColor(bitmap, (int) (0.95*bitmap.getWidth()), (int) (0.5 *bitmap.getHeight()));
        color[6] = avgColor(bitmap, (int) (0.05*bitmap.getWidth()), (int) (0.95*bitmap.getHeight()));
        color[7] = avgColor(bitmap, (int) (0.5 *bitmap.getWidth()) , (int) (0.95*bitmap.getHeight()));
        color[8] = avgColor(bitmap, (int) (0.95*bitmap.getWidth()), (int) (0.95*bitmap.getHeight()));

        return  color;
    }

    private int avgColor(Bitmap bitmap, int x, int y){
        int[] pixels = new int[30*30];
        float[] hsv = new float[3];
        int sum= 0;
        bitmap.getPixels(pixels, 0, 30, x-15, y -15, 30, 30);
        for (int pixel : pixels) {
            Color.RGBToHSV(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsv);
            sum += hsv[0];

        }
        hsv[0] = sum/pixels.length;

        //RED
        if((hsv[0]>0 && hsv[0]<10) ||(hsv[0]>350 && hsv[0]<=360) && hsv[1] > 0.3){
            hsv[0] = 3;
            hsv[1] = 1;
            hsv[2] = (float) 0.5;
        } else
        //BLUE
        if((hsv[0]>215 && hsv[0]<240) && hsv[1] > 0.3){
            hsv[0] = 240;
            hsv[1] = 1;
            hsv[2] = (float) 0.5;
        }else
        //GREEN
        if((hsv[0]>75 && hsv[0]<140) && hsv[1] > 0.3){
            hsv[0] = 92;
            hsv[1] = 1;
            hsv[2] = (float) 0.5;
        }else
        //YELLOW
        if((hsv[0]>50 && hsv[0]<65) && hsv[1] > 0.3){
            hsv[0] = 60;
            hsv[1] = 1;
            hsv[2] = (float) 0.5;
        }else
        //ORANGE
        if((hsv[0]>20 && hsv[0]<40) && hsv[1] > 0.3){
            hsv[0] = 30;
            hsv[1] = 1;
            hsv[2] = (float) 0.5;
        }else
        if(hsv[1] < 0.3){
            hsv[1] = 0;
            hsv[2] = 1;
        } else {
            hsv[1] = 1;
            hsv[2] = 1;
        }
        return Color.HSVToColor(hsv);
    }



}

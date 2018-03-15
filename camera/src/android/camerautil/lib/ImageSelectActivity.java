package android.camerautil.lib;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Updated by xiaohui yu on 3/15/2018.
 * This class is used to pick a picture from Camera or Local storage.
 */
public abstract class ImageSelectActivity extends AppCompatActivity {

    public static final String TAG = "ImageSelectActivity";

    private static String PROJECT_FOLDER = ".cache";

    private final static int REQ_CODE_REQUEST_CAMERA_PERMISSION = 101;
    private final static int REQ_CODE_CROP_IMAGE = 102;
    private final static int REQ_CODE_SELECT_LOCAL_IMAGE = 103;

    private final static int MENU_IMAGE_FROM_CAMERA = 0;
    private final static int MENU_IMAGE_FROM_LOCAL_STORAGE = 1;

    private final static String MENU_TEXT_FROM_CAMERA = "Take a photo";
    private final static String MENU_TEXT_FROM_LOCAL_STORAGE = "Select a photo";

    private final static int MAX_IMAGE_WIDTH = 480;

    private final static String TARGET_IMG_PATH = "camera_image_output.jpg";

    private int mPictureWidth = 480;
    private String outputPicPath = "";

    public abstract void outputImage(Bitmap bmp, String imagePath);

    private CameraIntentHelper myCameraIntentHelper;

    public void setPictureWidth(int w) {
        mPictureWidth = w;
    }

    public void setCacheFolder (String name){
        PROJECT_FOLDER = name;
    }

    public void getImageFromLocalStorage() {
        selectPictureFromLocal();
    }

    public void takePictureByCamera() {
        requestCameraPermission();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        outputPicPath = getAppExternalFolder() + "/" + TARGET_IMG_PATH;
        setupCameraHelper();
    }

    public void selectPictureFromLocal() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_CODE_SELECT_LOCAL_IMAGE);
    }

    public void cutPictureBySysCall(String inFilePath, String outFilePath, int width, int height) {
        final Intent intent = new Intent("com.android.camera.action.CROP");

        File mFile = new File(inFilePath);
        intent.setDataAndType(Uri.fromFile(mFile), "image/*");
        if (width != 0 && height != 0) {
            intent.putExtra("outputX", width);
            intent.putExtra("outputY", height);
            intent.putExtra("aspectX", width);
            intent.putExtra("aspectY", height);
            intent.putExtra("scale", true);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(outFilePath)));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        startActivityForResult(intent, REQ_CODE_CROP_IMAGE);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, MENU_IMAGE_FROM_CAMERA, 0, MENU_TEXT_FROM_CAMERA);
        menu.add(0, MENU_IMAGE_FROM_LOCAL_STORAGE, 0, MENU_TEXT_FROM_LOCAL_STORAGE);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_IMAGE_FROM_CAMERA) {
            requestCameraPermission();

        }
        if (item.getItemId() == MENU_IMAGE_FROM_LOCAL_STORAGE) {
            selectPictureFromLocal();
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            //show error
            return;
        }
        switch (requestCode) {
//            case REQ_CODE_CROP_IMAGE:
//                Bitmap bp = BitmapFactory.decodeFile(outputPicPath);
//                displayImage(bp);
//                Bitmap resized = Bitmap.createScaledBitmap(bp, mCropWidth, (int) (mCropWidth * 1.0 * bp.getHeight() / bp.getWidth()), true);
//                saveBmpTofile(resized, outputPicPath, 80);
//                savedImage(outputPicPath);
//                break;

            case REQ_CODE_SELECT_LOCAL_IMAGE:
                Uri selectedImage = data.getData();

                Bitmap photo = BitmapHelper.readBitmap(this, selectedImage);
                createTargetPicture(selectedImage, photo, false);

                break;
        }

        myCameraIntentHelper.onActivityResult(requestCode, resultCode, data);
    }

    private String getAppExternalFolder() {
        /*
        1. Using the external storage because Camera app cannot access the internal storage.
        2. hide folder with .
         */
        String folder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + PROJECT_FOLDER;
        createDirectories(folder);
        return folder;
    }

    private static String createDirectories(String path) {

        File f = new File(path);
        if (!f.exists()) {
            boolean succ = f.mkdirs();
        }
        return f.getAbsolutePath();
    }

    private void requestCameraPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int storagePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (cameraPermission != PackageManager.PERMISSION_GRANTED ||
                storagePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_SETTINGS},
                    REQ_CODE_REQUEST_CAMERA_PERMISSION
            );

            return;
        }

        if (myCameraIntentHelper != null) {
            myCameraIntentHelper.startCameraIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == REQ_CODE_REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        if (myCameraIntentHelper != null) {
                            myCameraIntentHelper.startCameraIntent();
                        }
                    }else{
                        Toast.makeText(this, "The Camera needs the Storage Permission!!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(this, "The Camera Permission is needed to take a picture.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setupCameraHelper() {
        myCameraIntentHelper = new CameraIntentHelper(this, new CameraIntentHelperCallback() {
            @Override
            public void onPhotoUriFound(Date dateCameraIntentStarted, Uri photoUri, int rotateXDegrees) {

                Log.i(TAG, ">>>>>>>>>>" + photoUri.getPath());
                Bitmap photo = BitmapHelper.readBitmap(ImageSelectActivity.this, photoUri);
                if (photo != null) {
                    createTargetPicture(photoUri, photo, true);
                } else {
                    Log.i(TAG, ">>>>>>>>>>photo is null");
                }
            }

            @Override
            public void deletePhotoWithUri(Uri photoUri) {
                BitmapHelper.deleteImageWithUriIfExists(photoUri, ImageSelectActivity.this);
            }

            @Override
            public void onSdCardNotMounted() {
                Toast.makeText(getApplicationContext(), ErrorMessage.SD_CARD_UNMOUNTED, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCanceled() {
                Toast.makeText(getApplicationContext(), ErrorMessage.CAMERA_CANCELED, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCouldNotTakePhoto() {
                Toast.makeText(getApplicationContext(), ErrorMessage.COULD_NOT_TAKE_PHOTO, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onPhotoUriNotFound() {
//                messageView.setText(getString(R.string.activity_camera_intent_photo_uri_not_found));
            }

            @Override
            public void logException(Exception e) {
                Toast.makeText(getApplicationContext(), ErrorMessage.SOMETHING_WRONG, Toast.LENGTH_LONG).show();
                Log.d(getClass().getName(), e.getMessage());
            }
        });
    }

    private void createTargetPicture(Uri photoUri, Bitmap photo, boolean isDeleteSource) {
        Bitmap resized = Bitmap.createScaledBitmap(photo, mPictureWidth, (int) (mPictureWidth * 1.0 * photo.getHeight() / photo.getWidth()), true);
        saveBmpTofile(resized, outputPicPath, 80);

        outputImage(resized, outputPicPath);

        if (isDeleteSource) {
            File deleteFile = new File(photoUri.getPath());
            deleteFile.delete();
        }
    }

    private void saveOriginalBmp(String path) {
        Bitmap bp = BitmapFactory.decodeFile(path);
        outputImage(bp, path);
        Bitmap resized = Bitmap.createScaledBitmap(bp, MAX_IMAGE_WIDTH, (int) (MAX_IMAGE_WIDTH * 1.0 * bp.getHeight() / bp.getWidth()), true);

        saveBmpTofile(resized, path, 80);
    }

    public void saveBmpTofile(Bitmap bmp, String filename, int compression) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.JPEG, compression, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        myCameraIntentHelper.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        myCameraIntentHelper.onRestoreInstanceState(savedInstanceState);
    }
}

package com.bignerdranch.android.flashlight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final String LOG_TAG = "myLogs";
    private Camera camera;
    Camera.Parameters parameters;
    private SwitchCompat mySwitch;
    private static final int PERMISSION_REQUEST_CODE = 200;

    CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                setFlashLigthOn();
                mySwitch.setText(R.string.switch_text_off);
            } else {
                setFlashLightOff();
                mySwitch.setText(R.string.switch_text_on);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySwitch = findViewById(R.id.switchOnOff);
        mySwitch.setOnCheckedChangeListener(listener);

        int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initializeCamera();
                } else {
                    finishAffinity();
                }
                return;
        }

    }


    private void initializeCamera() {
        boolean isCameraFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!isCameraFlash) {
            Toast.makeText(this, R.string.error_text, Toast.LENGTH_SHORT).show();
        } else {
            camera = Camera.open();
        }
    }

    private void setFlashLightOff() {
        if (camera != null) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);
            camera.stopPreview();
        }
    }

    private void setFlashLigthOn() {
        if (camera != null) {
            parameters = camera.getParameters();

            if (parameters != null) {
                List supportedFlashModes = parameters.getSupportedFlashModes();

                if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                } else if (supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                } else camera = null;

                if (camera != null) {
                    camera.setParameters(parameters);
                    camera.startPreview();
                    try {
                        camera.setPreviewTexture(new SurfaceTexture(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
        mySwitch.setChecked(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        releaseCamera();
        mySwitch.setChecked(false);
        if (camera == null)
            camera = Camera.open();
    }
}
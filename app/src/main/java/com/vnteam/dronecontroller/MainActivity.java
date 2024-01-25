package com.vnteam.dronecontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.BaseComponent;
import dji.sdk.base.BaseProduct;
import dji.sdk.sdkmanager.DJISDKInitEvent;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.thirdparty.afinal.core.AsyncTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = "dji_sdk_connection_change";
    private static BaseProduct mProduct;
    private Handler mHandler;
    private static final String[] REQUIRED_PERMISSION_LIST = new String[] {
            android.Manifest.permission.VIBRATE, // Gimbal rotation
            android.Manifest.permission.INTERNET, // API requests
            android.Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
            android.Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
            android.Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
            android.Manifest.permission.ACCESS_FINE_LOCATION, // Maps
            android.Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
            // TODO Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            android.Manifest.permission.BLUETOOTH, // Bluetooth connected products
            android.Manifest.permission.BLUETOOTH_ADMIN, // Bluetooth connected products
            // TODO Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            android.Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
            Manifest.permission.RECORD_AUDIO // Speaker accessory
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private List<String> missingPermission = new ArrayList<>();
    private AtomicBoolean isRegistrationInProgress = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                missingPermission.add(eachPermission);
            }
        }
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[0]),
                    REQUEST_PERMISSION_CODE);
        }
    }

    private void startSDKRegistration() {
        if (isRegistrationInProgress.compareAndSet(false, true)) {
            AsyncTask.execute(() -> {
                showToast("registering, pls wait...");
                DJISDKManager.getInstance().registerApp(MainActivity.this.getApplicationContext(), new DJISDKManager.SDKManagerCallback() {
                    @Override
                    public void onRegister(DJIError djiError) {
                        if (djiError == DJISDKError.REGISTRATION_SUCCESS) {
                            showToast("Register Success");
                            DJISDKManager.getInstance().startConnectionToProduct();
                        } else {
                            showToast("Register sdk fails, please check the bundle id and network connection!");
                        }
                        Log.v(TAG, djiError.getDescription());
                    }

                    @Override
                    public void onProductDisconnect() {
                        Log.d(TAG, "onProductDisconnect");
                        showToast("Product Disconnected");
                        notifyStatusChange();

                    }
                    @Override
                    public void onProductConnect(BaseProduct baseProduct) {
                        Log.d(TAG, String.format("onProductConnect newProduct:%s", baseProduct));
                        showToast("Product Connected");
                        notifyStatusChange();

                    }

                    @Override
                    public void onProductChanged(BaseProduct baseProduct) {
                        Log.d(TAG, String.format("onProductChanged newProduct:%s", baseProduct));
                        notifyStatusChange();
                    }

                    @Override
                    public void onComponentChange(BaseProduct.ComponentKey componentKey, BaseComponent oldComponent,
                                                  BaseComponent newComponent) {

                        if (newComponent != null) {
                            newComponent.setComponentListener(new BaseComponent.ComponentListener() {

                                @Override
                                public void onConnectivityChange(boolean isConnected) {
                                    Log.d(TAG, "onComponentConnectivityChanged: " + isConnected);
                                    notifyStatusChange();
                                }
                            });
                        }
                        Log.d(TAG,
                                String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s",
                                        componentKey,
                                        oldComponent,
                                        newComponent));

                    }
                    @Override
                    public void onInitProcess(DJISDKInitEvent djisdkInitEvent, int i) {

                    }

                    @Override
                    public void onDatabaseDownloadProgress(long l, long l1) {

                    }
                });
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        if (missingPermission.isEmpty()) {
            startSDKRegistration();
        } else {
            showToast("Missing permissions!!!");
        }
    }

    private void notifyStatusChange() {
        mHandler.removeCallbacks(updateRunnable);
        mHandler.postDelayed(updateRunnable, 500);
    }

    private Runnable updateRunnable = new Runnable() {

        @Override
        public void run() {
            Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
            sendBroadcast(intent);
        }
    };

    private void showToast(final String toastMsg) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        });

    }
}
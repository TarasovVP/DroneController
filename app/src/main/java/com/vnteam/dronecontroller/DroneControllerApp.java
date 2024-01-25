package com.vnteam.dronecontroller;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

public class DroneControllerApp extends Application {

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(this);
    }
}
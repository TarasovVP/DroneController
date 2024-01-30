package com.vnteam.dronecontroller

import android.app.Application
import android.content.Context
import com.secneo.sdk.Helper

class DroneControllerApp : Application() {

    override fun attachBaseContext(paramContext: Context) {
        super.attachBaseContext(paramContext)
        Helper.install(this)
    }
}
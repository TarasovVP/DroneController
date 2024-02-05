package com.vnteam.dronecontroller

import android.app.Application
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.secneo.sdk.Helper

class DroneControllerApp : Application() {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    override fun attachBaseContext(paramContext: Context) {
        super.attachBaseContext(paramContext)
        Helper.install(this)
        instance = this
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    fun logCustomEvent(eventName: String, eventDetails: String) {
        val firebaseAnalytics = instance?.let { FirebaseAnalytics.getInstance(it) }
        val bundle = Bundle()
        bundle.putString(eventName, eventDetails)
        firebaseAnalytics?.logEvent(eventName, bundle)
    }
    companion object {
        var instance: DroneControllerApp? = null
    }
}
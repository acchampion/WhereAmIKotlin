package edu.ohiostate.whereamikotlin

import android.app.Application
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

class MapsApplication : Application(), OnMapsSdkInitializedCallback {
    override fun onCreate() {
        super.onCreate()
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d(
                TAG,
                "The latest version of the renderer is used."
            )
            MapsInitializer.Renderer.LEGACY -> Log.d(
                TAG,
                "The legacy version of the renderer is used."
            )
        }
    }

    companion object {
        private val TAG = MapsApplication::class.java.simpleName
    }
}

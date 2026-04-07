package com.humotron.app.core

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.util.Log
import com.humotron.app.bt.ring.RingBleManager
import com.humotron.app.util.ActivityLifecycleCb
import com.humotron.app.util.BandSyncManager
import com.humotron.app.util.FontScaleContextWrapper
import com.humotron.app.util.PrefUtils
import com.humotron.app.util.RingDeviceManager
import com.humotron.app.util.createDefaultSharedPreferences
import com.pluto.Pluto
import com.pluto.plugins.exceptions.PlutoExceptions
import com.pluto.plugins.exceptions.PlutoExceptionsPlugin
import com.pluto.plugins.logger.PlutoLoggerPlugin
import com.pluto.plugins.logger.PlutoTimberTree
import com.pluto.plugins.network.PlutoNetworkPlugin
import dagger.hilt.android.HiltAndroidApp
import lib.linktop.nexring.api.NexRingManager
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltAndroidApp
class App : Application() {

    companion object {
        @JvmStatic
        lateinit var instance: App
            private set
    }

    val mActivityLifecycleCb = ActivityLifecycleCb()

    @Inject
    lateinit var prefUtils: PrefUtils

    @Inject
    lateinit var bandSyncManager: BandSyncManager

    val accountSp by lazy { createDefaultSharedPreferences() }
    val ringBleManager by lazy {
        NexRingManager.init(this)
        RingBleManager(this)
    }
    val ringDeviceManager by lazy { RingDeviceManager(this) }


    override fun onCreate() {
        instance = this
        initializeStrictMode()
        super.onCreate()
        registerActivityLifecycleCallbacks(mActivityLifecycleCb)
        bandSyncManager.start()
        initPluto()
    }

    private fun initPluto() {
        Pluto.Installer(this)
            .addPlugin(PlutoNetworkPlugin())
            .addPlugin(PlutoLoggerPlugin())
            .addPlugin(PlutoExceptionsPlugin())
            .install()
        Pluto.showNotch(true)
        plantPlutoTimber()
        setExceptionListener()
    }

    private fun plantPlutoTimber() {
        Timber.plant(PlutoTimberTree())
    }

    private fun initializeStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
    }

    private fun setExceptionListener() {
        PlutoExceptions.setExceptionHandler { thread, throwable ->
            Log.e(
                "exception_demo",
                "uncaught exception handled on thread: " + thread.name,
                throwable
            )
            exitProcess(0)
        }

        PlutoExceptions.setANRHandler { thread, exception ->
            Log.e("anr_demo", "unhandled ANR handled on thread: " + thread.name, exception)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(FontScaleContextWrapper.wrap(base))
    }

}

const val SP_KEY_BOUND_DEVICE_ADDRESS = "BOUND_DEVICE_ADDRESS"
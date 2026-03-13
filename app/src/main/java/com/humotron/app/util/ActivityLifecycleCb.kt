package com.humotron.app.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

class ActivityLifecycleCb : Application.ActivityLifecycleCallbacks {

    /**
     * 如果是从后台打开APP的，此标志意味着可以从设备拉数据
     *
     *
    var readDataFromDevice: Boolean = false
     */

    private var flag = 0
    val activities = ArrayList<Activity>()

    /**
     * 判断APP是否在前台运行
     * */
    val isAppForeground: Boolean get() = flag > 0
    var backgroundFlag = true

    val currAct: Activity?
        get() = if (activities.isNotEmpty()) activities[activities.size - 1] else null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activities.add(activity)
//        loge("ActivityLifecycleCb", "onActivityCreated - ${activity.javaClass.name}")
    }

    override fun onActivityStarted(activity: Activity) {
        flag++
        Log.i("ActivityLifecycleCb", "onActivityStarted - flag:$flag")
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        flag--
        if (!isAppForeground) {
            backgroundFlag = true
        }
        Log.i("ActivityLifecycleCb", "onActivityStopped - flag:$flag")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activities.remove(activity)
//        loge("ActivityLifecycleCb", "onActivityDestroyed - ${activity.javaClass.name}")
    }
}
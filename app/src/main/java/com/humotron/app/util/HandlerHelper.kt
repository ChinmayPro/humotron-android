package com.humotron.app.util

import android.os.Handler
import android.os.Looper

private val uiHandler = Handler(Looper.getMainLooper())

fun postDelay(r: Runnable, delay: Long) = uiHandler.postDelayed(r, delay)

fun postDelay(r: Runnable) = postDelay(r, 100L)

fun post(r: Runnable) = uiHandler.post(r)

fun Runnable.handlerPost() = post(this)

fun Runnable.handlerPostDelay(delay: Long) = postDelay(this, delay)

fun Runnable.handlerRemove() = uiHandler.removeCallbacks(this)
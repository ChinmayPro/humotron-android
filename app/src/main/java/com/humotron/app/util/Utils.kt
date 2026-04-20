package com.humotron.app.util

import android.annotation.SuppressLint
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Process
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.room.Room
import androidx.room.RoomDatabase
import com.humotron.app.BuildConfig
import com.humotron.app.R
import lib.linktop.nexring.api.PRODUCT_COLOR_DEEP_BLACK
import lib.linktop.nexring.api.PRODUCT_COLOR_GOLDEN
import lib.linktop.nexring.api.PRODUCT_COLOR_GOLD_SILVER_MIXED
import lib.linktop.nexring.api.PRODUCT_COLOR_PURPLE_SILVER_MIXED
import lib.linktop.nexring.api.PRODUCT_COLOR_ROSE_GOLD
import lib.linktop.nexring.api.PRODUCT_COLOR_ROSE_GOLD_SILVER_MIXED
import lib.linktop.nexring.api.PRODUCT_COLOR_SILVER
import java.io.File
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.system.exitProcess

val DEBUG_ON: Boolean = BuildConfig.DEBUG
const val TAG = "NexRingSdkApp"
const val TAG_RING_DEBUG = "Ring Debug"
const val TAG_BAND_DEBUG = "Band Debug"

const val ONE_DAY_TS = 86400000L
const val ONE_HOUR_MINUTES = 60

@Throws(GeneralSecurityException::class, IOException::class)
fun Context.createDefaultSharedPreferences(): SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(this)

@Throws(GeneralSecurityException::class, IOException::class)
fun Application.createSharePreference(name: String): SharedPreferences =
    getSharedPreferences("${packageName}_${name}", Context.MODE_PRIVATE)

fun <T : RoomDatabase> Context.createRoom(
    klass: Class<T>,
    name: String,
): RoomDatabase.Builder<T> {
    return Room.databaseBuilder(this, klass, name)
}

fun loge(tag: String, msg: String) {
    if (DEBUG_ON) {
        Log.e(tag, msg)
    }
}

fun loge(tag: String, msg: String, e: Throwable) {
    if (DEBUG_ON) {
        Log.e(tag, msg, e)
    }
}

fun loge(msg: String) = loge(TAG, msg)

fun loge(msg: String, e: Throwable) = loge(TAG, msg, e)

fun logi(tag: String, msg: String) {
    if (DEBUG_ON) {
        Log.i(tag, msg)
    }
}

fun Context.complexUnitSp(value: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)

fun Context.complexUnitDip(value: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

fun Context.toColor(@ColorRes colorId: Int) = ActivityCompat.getColor(this, colorId)

fun Int.toNumberString(): String = String.format("%02d", this)

fun Float.extremeValue(spacing: Float, upOrDown: Boolean): Float =
    extremeValue(spacing, 0, upOrDown)

private fun Float.extremeValue(
    spacing: Float,
    scale: Int,
    upOrDown: Boolean,
): Float {
    val min = spacing * scale
    val max = spacing * (scale + 1)
    return if (this > max) {
        extremeValue(spacing, scale + 1, upOrDown)
    } else if (this < min) {
        extremeValue(spacing, scale - 1, upOrDown)
    } else if (upOrDown) {
        max
    } else {
        min
    }
}

/**
 * endTime 必须大于 startTime，且 endTime 一定是正数，startTime可能为负数
 * */
infix fun Float.floatTimeMinutes(startTime: Float): Int {
    val startH = startTime.toInt()
    val startM = startTime.minus(startH).times(100)
    val start = startH.times(60).plus(startM)

    val endH = this.toInt()
    val endM = this.minus(endH).times(100)
    val end = endH.times(60).plus(endM)

    return (end - start).roundToInt()
}

fun Float.floatTimePlus(minute: Int): Float {
    if (minute == 0) return this
    var h = this.toInt()
    var m = this.minus(h).times(100).roundToInt()
    val total = 60.times(h).plus(m).plus(minute)
    h = total / 60
    m = total % 60
    return h.toFloat().plus(m.div(100f))
}

fun Float.floatTimeString(): String {
    var hour = this.toInt()
    var min = this.minus(hour).times(100).roundToInt()
    if (hour <= 0 && min < 0) {
        hour += 23
        min += 60
    } else if (hour < 0 && min == 0) {
        hour += 24
    }
    return String.format("%02d:%02d", hour, min)
}

fun Float.floatTimeHourString(): String {
    var hour = this.toInt()
    val min = this.minus(hour).times(100).roundToInt()
    if (hour < 0 || min < 0) {
        hour += 23
    }
    return String.format("%02d", hour)
}

fun Double.round(): Double = this.round(1)

fun Double.round(times: Int): Double {
    val scale = 10.0.pow(times)
    return this.times(scale).roundToInt().div(scale)
}

fun Float.round(): Float = this.round(1)

fun Float.round(times: Int): Float {
    val scale = 10.0f.pow(times)
    return this.times(scale).roundToInt().div(scale)
}

fun Long.formatTime(): String {
    val totalMinutes = this / 60 / 1000L
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return "${h}h ${m}m"

}

fun Number.formatPercentWithUnit(): String = this.toFloat().times(100).toInt().toPercent()
fun Number.formatPercent(): String = this.toFloat().times(100).toInt().toString()

fun Int.toPercent(): String = "${this}%"


fun Context.goEnableLocationServicePage() {
    val intent = Intent()
        .setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(intent)
    } catch (ex: ActivityNotFoundException) {
        // The Android SDK doc says that the location settings activity
        // may not be found. In that case show the general settings.
        // General settings activity
        intent.action = Settings.ACTION_SETTINGS
        try {
            startActivity(intent)
        } catch (e: Exception) {
            toast("Can not find the LOCATION setting page.")
        }
    }
}

fun todayCalendar(): Calendar =
    Calendar.getInstance().apply {
        this[Calendar.HOUR_OF_DAY] = 0
        this[Calendar.MINUTE] = 0
        this[Calendar.SECOND] = 0
        this[Calendar.MILLISECOND] = 0
    }

fun Long.toDateString(format: String, locale: Locale): String =
    if (this == 0L) "-" else SimpleDateFormat(format, locale).format(Date(this))

fun Long.toDateString() = toDateString("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

fun Calendar.isToday(): Boolean =
    this.timeInMillis == todayCalendar().timeInMillis

fun Calendar.isYesterday(): Boolean {
    val today = todayCalendar().apply {
        //Is today the first day of this year?
        val is1stDayOfYear = this[Calendar.DAY_OF_YEAR] == 1
        roll(Calendar.DAY_OF_YEAR, false)
        if (is1stDayOfYear) {
            // If today is the first day of this year, you also need to roll back the year.
            roll(Calendar.YEAR, false)
        }
    }.timeInMillis
    return this.timeInMillis == today
}

fun Calendar.isThisYear(): Boolean =
    todayCalendar()[Calendar.YEAR] == this[Calendar.YEAR]

fun Calendar?.getDayStartCal(): Calendar {
    val clone = if (this == null) todayCalendar()
    else clone() as Calendar
    clone[Calendar.HOUR_OF_DAY] = 0
    clone[Calendar.MINUTE] = 0
    clone[Calendar.SECOND] = 0
    clone[Calendar.MILLISECOND] = 0
    return clone
}

fun Calendar?.getDayEndCal(): Calendar {
    val clone = if (this == null) todayCalendar()
    else clone() as Calendar
    clone[Calendar.HOUR_OF_DAY] = 23
    clone[Calendar.MINUTE] = 59
    clone[Calendar.SECOND] = 59
    clone[Calendar.MILLISECOND] = 999
    return clone
}

fun Calendar?.getDayEnd(): Long {
    return getDayEndCal().timeInMillis
}

fun Calendar?.getDayStart(): Long {
    return getDayStartCal().timeInMillis
}

/**
 * @param zeroClockTimestamp  Today's 0:00 time stamp.
 * @return Float. For example:
 *
 * -0.10 => 23:50, yesterday
 *
 * 0.10 => 0:10 ,today
 * */
fun Long.toFloatTime(zeroClockTimestamp: Long): Float {
    //求出总秒数
    val diffSeconds = this.minus(zeroClockTimestamp).div(1000L)
    val h = diffSeconds / 3600
    val m = diffSeconds % 3600 / 60
    return m.div(100f).plus(h).round(2)
}

fun Context.formatTime(duration: Long?): String {
    return if (duration == null) getString(R.string.null_value)
    else {
        val totalMinutes = duration / 60 / 1000L
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        if (hours > 0 && minutes > 0) {
            getString(R.string.time_format_hh_mm, hours, minutes)
        } else if (hours > 0) {
            getString(R.string.time_format_hh, hours)
        } else {
            getString(R.string.time_format_mm, minutes)
        }
    }
}

fun Long.toSizeString(): String {
    return String.format("%.2f KB", this / 1024.0f)
}

fun Number.toPercent(): String = "${this}%"

@SuppressLint("UnspecifiedImmutableFlag")
fun Application.restartApp() {
    postDelay({
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
        startActivity(intent)
        Process.killProcess(Process.myPid())
        loge("exitProcess")
        exitProcess(0)
    }, 200L)
}

@Suppress("UNCHECKED_CAST")
fun <T : Number> T.minus(value: T): T = (this.toFloat() - value.toFloat()) as T

infix fun <T : Number> T.moreThan(value: T): Boolean = this.toFloat() > value.toFloat()


fun Context.toRingColor(colorInt: Int): String {
    return when (colorInt) {
        PRODUCT_COLOR_DEEP_BLACK -> getString(R.string.ring_color_deep_black)
        PRODUCT_COLOR_SILVER -> getString(R.string.ring_color_sliver)
        PRODUCT_COLOR_GOLDEN -> getString(R.string.ring_color_golden)
        PRODUCT_COLOR_ROSE_GOLD -> getString(R.string.ring_color_rose_gold)
        PRODUCT_COLOR_GOLD_SILVER_MIXED -> getString(R.string.ring_color_gold_silver_mixed)
        PRODUCT_COLOR_PURPLE_SILVER_MIXED -> getString(R.string.ring_color_purple_silver_mixed)
        PRODUCT_COLOR_ROSE_GOLD_SILVER_MIXED -> getString(R.string.ring_color_rose_gold_silver_mixed)
        else -> "-"
    }
}

fun Context.getProviderFileUri(file: File): Uri {
    return FileProvider.getUriForFile(
        this,
        "${applicationContext.packageName}.fileProvider",
        file
    )
}


fun Context.shareFile(file: Pair<String, Uri>) {
    grantUriPermission(packageName, file.second, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val intent = Intent(Intent.ACTION_SEND)
        .apply {
            this.type = "application/octet-stream"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_STREAM, file.second)
            putExtra(Intent.EXTRA_TITLE, file.first)
            putExtra(Intent.EXTRA_SUBJECT, file.first)
            putExtra(Intent.EXTRA_TEXT, file.first)
        }
    startActivity(Intent.createChooser(intent, file.first))
}

fun TextView.setTextColorId(@ColorRes id: Int) {
    setTextColor(context.toColor(id))
}

fun asSimpleDateFormat(format: String, locale: Locale) = SimpleDateFormat(format, locale)

fun asSimpleDateFormat() = asSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
fun String.stringDateToTime(): String {
    return asSimpleDateFormat().parse(this).let {
        if (it == null) "-"
        else asSimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
    }
}

fun Long?.asCalendar(): Calendar {
    return if (this == null) todayCalendar()
    else Calendar.getInstance().run {
        timeInMillis = this@asCalendar
        this
    }
}

fun SimpleDateFormat.toFormat(ts: Long): String = if (ts == 0L) "-" else format(Date(ts))


fun Calendar?.toYD(): String {
    val cal = this ?: todayCalendar()
    return String.format("%d-%03d", cal[Calendar.YEAR], cal[Calendar.DAY_OF_YEAR])
}

fun TextView.drawableEnd(@DrawableRes id: Int = 0) =
    setCompoundDrawablesWithIntrinsicBounds(0, 0, id, 0)


@OptIn(ExperimentalStdlibApi::class)
fun md5Checker(file: ByteArray): String {
    val md: MessageDigest = MessageDigest.getInstance("MD5")
    md.update(file, 0, file.size)
    val md5Bytes = md.digest()
//    logi("md5Checker ${md5Bytes.toByteArrayString()}")
    val sb = StringBuilder()
    md5Bytes.forEach {
        sb.append(it.toHexString())
    }
//    logi("md5Checker $sb")
//    val bigInt = BigInteger(1, md5Bytes)
//    return bigInt.toString(16)
    return sb.toString()
}
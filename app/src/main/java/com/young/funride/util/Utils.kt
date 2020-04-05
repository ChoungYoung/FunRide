package com.young.funride.util

import android.content.Context
import android.location.LocationManager
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    private var mWakeLock: WakeLock? = null

    /**
     * 距离单位换算
     */
    fun distanceFormatter(distance: Int): String{
        return when {
            distance < 1000 -> {
                distance.toString() + "米"
            }
            distance % 1000 == 0 -> {
                (distance / 1000).toString() + "千米"
            }
            else -> {
                val df = DecimalFormat("0.0")
                val a1 = distance / 1000 // 十位
                val a2 = distance % 1000.toDouble()
                val a3 = a2 / 1000 // 得到个位
                val result = df.format(a3)
                val total = result.toDouble() + a1
                total.toString() + "千米"
            }
        }
    }

    /**
     * 时间单位换算
     */
    fun timeFormatter(minute: Int): String{
        return when {
            minute < 60 -> {
                minute.toString() + "分钟"
            }
            minute % 60 == 0 -> {
                (minute / 60).toString() + "小时"
            }
            else -> {
                val hour = minute / 60
                val minute1 = minute % 60
                hour.toString() + "小时" + minute1 + "分钟"
            }
        }
    }

    /**
     * long型时间转换
     */
    fun getDateFromMillisecond(millisecond: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(Date(millisecond))

    /**
     * 请求wakelock，息屏继续运行
     */
    fun acquireWakeLock(context: Context){
        if (mWakeLock == null){
            val pm = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.young.funride:mylockwaketag")
            mWakeLock?.run{
                setReferenceCounted(true)
                acquire(60*60*1000L /*60 minutes*/)
            }
        }
    }

    /**
     * 释放weaklock
     */
    fun releaseWakeLock(){
        mWakeLock?.release()
        mWakeLock = null
    }

    /**
     * 查看GPS是否开启
     */
    fun isGpsOPen(context: Context?): Boolean{
        val locationManager: LocationManager? = context?.applicationContext?.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
    }
}
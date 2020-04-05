package com.young.funride.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.GnssStatus
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.utils.DistanceUtil
import com.google.gson.Gson
import com.young.funride.db.RouteDatabase
import com.young.funride.entity.RoutePoint
import com.young.funride.entity.RoutePoints
import com.young.funride.entity.RouteRecord
import com.young.funride.util.Utils
import com.young.funride.util.toast
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.util.*
import kotlin.math.floor

class RouteService : Service() {

    private lateinit var mLocationClient: LocationClient
    private lateinit var mListener: MyLocationListener
    var routPointList: ArrayList<RoutePoint> = ArrayList<RoutePoint>()
    var totalDistance = 0
    var totalPrice = 0
    var beginTime: Long = 0
    var totalTime: Long = 0
    private var showDistance: String = "0米"
    private var showTime: String = "0分钟"
    private lateinit var callback: RouteCallback
    private lateinit var routePoints: RoutePoints

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        beginTime = System.currentTimeMillis()

        totalTime = 0
        totalDistance = 0
        totalPrice = 0
        routPointList.clear()

        routePoints = RoutePoints(routPointList, showTime, showDistance)

        initLocation() //初始化LocationgClient

        Utils.acquireWakeLock(applicationContext)

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.registerGnssStatusCallback(GPSListener())


    }

    @RequiresApi(Build.VERSION_CODES.O)
    inner class GPSListener : GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: GnssStatus?) {
            //信号弱提示
            status?.run {
                if (getSvid(GnssStatus.CONSTELLATION_GPS) < 4 || getCn0DbHz(GnssStatus.CONSTELLATION_GPS) < 25){
                    this@RouteService.toast { "GPS号弱" }
                }
            }
        }
    }

    private fun initLocation() {
        //定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        mLocationClient = LocationClient(this)
        //配置定位SDK各配置参数，比如定位模式、定位时间间隔、坐标系类型等
        val mOption = LocationClientOption()
        //设置坐标类型
        mOption.setCoorType("bd09ll")
        //设置是否需要地址信息，默认为无地址
        mOption.setIsNeedAddress(true)
        //设置是否打开gps进行定位
        mOption.isOpenGps = true
        //设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        mOption.setScanSpan(2000)
        //设置出行模式，高精度连续定位
//        mOption.setLocationPurpose(LocationClientOption.BDLocationPurpose.Transport)
        mOption.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        //设置 LocationClientOption
        mLocationClient.locOption = mOption

        mListener = MyLocationListener()
        //注册监听器
        mLocationClient.registerLocationListener(mListener)

        if (!mLocationClient.isStarted) {
            mLocationClient.start()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder()
    }

    inner class MyBinder : Binder() {
        fun getService(): RouteService = this@RouteService
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationClient.stop()
        Utils.releaseWakeLock()
        val gson = Gson()
        val routeListStr: String = gson.toJson(routPointList)
        if (routPointList.size > 2)
           insertData(routeListStr)
    }

    //所有的定位信息都通过接口回调来实现
    inner class MyLocationListener : BDAbstractLocationListener() {
        //定位请求回调函数,这里面会得到定位信息
        override fun onReceiveLocation(bdLocation: BDLocation?) {
            if (null == bdLocation) return
            //"4.9E-324"表示目前所处的环境（室内或者是网络状况不佳）造成无法获取到经纬度
            if ("4.9E-324" == bdLocation.latitude.toString() || "4.9E-324" == bdLocation.longitude.toString()
            ) {
                return
            } //过滤百度定位失败
            val routeLat = bdLocation.latitude
            val routeLng = bdLocation.longitude
            val routePoint = RoutePoint()
            routePoint.routeLat = routeLat
            routePoint.routeLng = routeLng
            if (routPointList.size == 0)
                routPointList.add(routePoint)
            else {
                val lastPoint: RoutePoint = routPointList[routPointList.size - 1]
                if (routeLat != lastPoint.routeLat || routeLng != lastPoint.routeLng) {
                    val lastLatLng = LatLng(
                        lastPoint.routeLat,
                        lastPoint.routeLng
                    )
                    val currentLatLng = LatLng(routeLat, routeLng)
                    if (routeLat > 0 && routeLng > 0) {
                        val distance = DistanceUtil.getDistance(lastLatLng, currentLatLng)
                        // 大于2米算作有效加入列表
                        if (distance > 2) {
                            //distance单位是米 转化为km/h
                            routePoint.speed = String.format("%.1f", distance / 1000 * 30 * 60).toDouble()
                            routePoint.time = System.currentTimeMillis()
                            routPointList.add(routePoint)
                            totalDistance += distance.toInt()
                        }
                    }
                }
            }
            totalTime = (System.currentTimeMillis() - beginTime).toInt() / 1000 / 60.toLong()
            totalPrice = (floor(totalTime / 60.toDouble()) * 1 + 1).toInt()
            showDistance = if (totalDistance > 1000) {
                val df = DecimalFormat("#.00")
                df.format(totalDistance.toFloat() / 1000.toDouble()) + "千米"
            } else totalDistance.toString() + "米"
            showTime = if (totalTime > 60) {
                (totalTime / 60).toString() + "时" + totalTime % 60 + "分"
            } else totalTime.toString() + "分钟"

            routePoints.time = showTime
            routePoints.distance = showDistance
            routePoints.routeList = routPointList

            //通知fragment刷新数据
            callback.positionChanged(routePoints)
        }
    }

    fun setCallback(callback: RouteCallback) {
        this.callback = callback
    }

    interface RouteCallback {
        fun positionChanged(routePoints: RoutePoints)
    }

    /**
     * 保存数据库，以便后续展示行程
     */
    private fun insertData(routeListStr: String) {
        val routeRecord = RouteRecord(0,routeListStr,showDistance,Utils.getDateFromMillisecond(beginTime),showTime)
        runBlocking {
            RouteDatabase.getInstance(applicationContext).routeDao().insertRoute(routeRecord)
        }
    }
}
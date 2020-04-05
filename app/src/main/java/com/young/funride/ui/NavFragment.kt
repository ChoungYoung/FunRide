package com.young.funride.ui

import android.app.Service
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.IBinder
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.route.*
import com.young.funride.R
import com.young.funride.base.BaseFragment
import com.young.funride.entity.RoutePoints
import com.young.funride.service.RouteService
import com.young.funride.util.BikingRouteOverlay
import com.young.funride.util.DialogUtil
import com.young.funride.util.ToastUtils
import com.young.funride.util.Utils
import com.young.funride.viewmodel.NavViewModel
import kotlinx.android.synthetic.main.fragment_nav.*

class NavFragment : BaseFragment<NavViewModel>() {

    lateinit var mBaiduMap: BaiduMap
    private lateinit var mLocationClient: LocationClient
    private lateinit var mRoutePlanSearch: RoutePlanSearch
    var isFirstLoc = true
    var onGetGpsSign = false
    var useDefaultIcon = true
    var overlay: MyBikingRouteOverlay? = null
    lateinit var myBinder :RouteService
    private lateinit var serviceConn: MyServiceConnection
    private var isBinded: Boolean = false

    /**
     * 选中的地点
     */
    var selectedPoint: LatLng? = null
    /**
     * 用户起始地点
     */
    var startPoint: LatLng? = null

    /**
     * 现在所在地点
     */
    var currPoint: LatLng? = null

    override fun providerVMClass() = NavViewModel::class.java

    override fun getLayoutResId() = R.layout.fragment_nav

    override fun initView(view: View) {
        mViewModel.isVisible.value = View.GONE
        mViewModel.title.value = getString(R.string.app_name)
    }

    override fun initData() {
        mBaiduMap = mapView.map

        mViewModel.navMode.observe(this, Observer {
            if (it){//行程中
                button.text = getString(R.string.stop)

                mViewModel.title.value = getString(R.string.in_ride)
                ll_bike_layout.visibility = View.VISIBLE
            }else{
                button.text = getString(R.string.start)

                mViewModel.title.value = getString(R.string.app_name)
                ll_bike_layout.visibility = View.GONE

                mBaiduMap.clear()
            }
        })

        mapView.showZoomControls(false)

        getLocation()

        setOnClickListener()

        setRoutePlanListener()

        button.setOnClickListener{
            if (!mViewModel.navMode.value!!){
                selectedPoint?.run {
                    val startNode = PlanNode.withLocation(startPoint)
                    val desNode = PlanNode.withLocation(selectedPoint)
                    mRoutePlanSearch.bikingSearch(
                        BikingRoutePlanOption()
                            .from(startNode)
                            .to(desNode)
                            // ridingType  0 普通骑行，1 电动车骑行
                            .ridingType(0)
                    )
                    beginNavigation()

                }?: run {
                    ToastUtils.toast(context,getString(R.string.no_des))
                }
            }else{
                AlertDialog.Builder(context!!).setMessage(getString(R.string.if_end_ride)).setPositiveButton(getString(R.string.confirm)) { dialog, _ ->
                    mViewModel.navMode.value = false
                    if (isBinded){
                        activity?.unbindService(serviceConn)
                        isBinded = false
                    }
                    selectedPoint = null
                    mViewModel.points.value?.routeList?.run {
                        if (size > 2){
                            val transaction = activity?.supportFragmentManager?.beginTransaction()
                            transaction?.add(R.id.container,RouteDetailFragment(),RouteDetailFragment::class.java.name)
                            transaction?.addToBackStack(null)
                            transaction?.commit()
                        }
                    }

                    dialog.dismiss()
                }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }.create().show()
            }
        }
    }

    /**
     * 开启导航模式
     */
    private fun beginNavigation() {
        mViewModel.navMode.value = true

        mBaiduMap.setMyLocationConfiguration(
            MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.FOLLOWING, false, null,
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )
        overlay?.removeFromMap()

        mBaiduMap.clear()
        mLocationClient.requestLocation()
        //开启导航服务
        activity?.run {
            serviceConn = MyServiceConnection()
            isBinded = bindService(Intent(this,RouteService::class.java),serviceConn,Service.BIND_AUTO_CREATE)
        }
    }

    inner class MyServiceConnection : ServiceConnection{
        override fun onServiceDisconnected(name: ComponentName?) {
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            myBinder = (service as RouteService.MyBinder).getService()
            myBinder.setCallback(object : RouteService.RouteCallback {
                override fun positionChanged(routePoints: RoutePoints) {
                    mViewModel.points.value = routePoints

                    time.text = routePoints.time
                    distance.text = routePoints.distance

                    routePoints.routeList[routePoints.routeList.size -1].run {
                        currPoint = LatLng(routeLat,routeLng)
                    }

                    //位置变化导航跟着改变
                    mRoutePlanSearch.bikingSearch(
                        BikingRoutePlanOption()
                            .from(PlanNode.withLocation(currPoint))
                            .to(PlanNode.withLocation(selectedPoint))
                            // ridingType  0 普通骑行，1 电动车骑行
                            .ridingType(0)
                    )
                }
            })
        }
    }

    /**
     * 获取当前定位
     */
    private fun getLocation(){
        //enable get user current location
        mBaiduMap.isMyLocationEnabled = true

        //定位初始化
        mLocationClient = LocationClient(context)

        //通过LocationClientOption设置LocationClient相关参数
        val option = LocationClientOption()
        option.isOpenGps = Utils.isGpsOPen(context?.applicationContext)

        option.setCoorType("bd09ll") // 设置坐标类型

        option.setScanSpan(3000)
//        option.setOpenAutoNotifyMode()
        //高精度模式
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy

        //设置locationClientOption
        mLocationClient.locOption = option

        val locationConfiguration = MyLocationConfiguration(
            MyLocationConfiguration.LocationMode.NORMAL,false,null,
            Color.TRANSPARENT,
            Color.TRANSPARENT)
        mBaiduMap.setMyLocationConfiguration(locationConfiguration)

        //注册LocationListener监听器
        val myLocationListener = MyLocationListener()
        mLocationClient.registerLocationListener(myLocationListener)
        //开启地图定位图层
        mLocationClient.start()
    }

    /**
     * 地图点击事件
     */
    private fun setOnClickListener(){
        mBaiduMap.setOnMapClickListener(object : BaiduMap.OnMapClickListener {
            override fun onMapClick(p0: LatLng?) {
                if (!mViewModel.navMode.value!!){
                    selectedPoint = p0
                    selectedPosition()
                }
            }

            override fun onMapPoiClick(p0: MapPoi?) {
                if (!mViewModel.navMode.value!!){
                    selectedPoint = p0?.position
                    selectedPosition()
                }
            }
        })
    }

    /**
     * 选中点击的地点
     */
    private fun selectedPosition(){
        selectedPoint?.let {
            val overLay = MarkerOptions().position(it).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding))
            mBaiduMap.clear()
            mBaiduMap.addOverlay(overLay)
        }
    }

    /**
     * 路径规划
     */
    private fun setRoutePlanListener(){
        mRoutePlanSearch = RoutePlanSearch.newInstance()
        val listener = object : OnGetRoutePlanResultListener {
            //只关心自行车路径规划
            override fun onGetBikingRouteResult(p0: BikingRouteResult?) {
                overlay = MyBikingRouteOverlay(mBaiduMap)
                p0?.run {
                    if (routeLines.size > 0){
                        //清除之前选中点的标记
                        mBaiduMap.clear()
                        //获取路径规划数据,(以返回的第一条路线为例）为BikingRouteOverlay实例设置数据
                        overlay?.setData(routeLines[0])
                        //在地图上绘制BikingRouteOverlay
                        overlay?.addToMap()
                    }
                }
            }

            override fun onGetIndoorRouteResult(p0: IndoorRouteResult?) {
            }

            override fun onGetTransitRouteResult(p0: TransitRouteResult?) {
            }

            override fun onGetDrivingRouteResult(p0: DrivingRouteResult?) {
            }

            override fun onGetWalkingRouteResult(p0: WalkingRouteResult?) {
            }

            override fun onGetMassTransitRouteResult(p0: MassTransitRouteResult?) {
            }
        }
        mRoutePlanSearch.setOnGetRoutePlanResultListener(listener)
    }

    inner class MyBikingRouteOverlay(baiduMap: BaiduMap) : BikingRouteOverlay(baiduMap){
        override fun getStartMarker(): BitmapDescriptor? {
            return if (useDefaultIcon) {
                BitmapDescriptorFactory.fromResource(R.drawable.icon_st)
            } else BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding)
        }

        override fun getTerminalMarker(): BitmapDescriptor? {
            return if (useDefaultIcon) {
                BitmapDescriptorFactory.fromResource(R.drawable.icon_en)
            } else BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding)
        }
    }

    /**
     * 百度地图定位回调
     */
    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            //获取不到位置
            if (location == null || location.locType == 62 || location.latitude == 4.9E-324 || location.longitude == 4.9E-324) {
                if (isFirstLoc && !onGetGpsSign && !Utils.isGpsOPen(context)){//首次开启获取不到定位
                    onGetGpsSign = true
                    DialogUtil.showConfirmDialog(context!!,
                        getString(R.string.no_gps),
                        false,
                        getString(R.string.confirm),
                        DialogInterface.OnClickListener { _, _ ->
                            run {
                                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            }
                        })
                }
                return
            }
            val locData = MyLocationData
                .Builder()
                .accuracy(location.radius) // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.direction)
                .latitude(location.latitude)
                .longitude(location.longitude)
                .build()
            mBaiduMap.setMyLocationData(locData)
            if (isFirstLoc) {
                isFirstLoc = false
                val ll = LatLng(
                    location.latitude,
                    location.longitude
                )
                startPoint = ll
                val builder = MapStatus.Builder()
                builder.target(ll).zoom(18.0f)
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))
            }
        }
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        onGetGpsSign = false
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mLocationClient.stop();
        mBaiduMap.isMyLocationEnabled = false
        mapView.onDestroy()
        super.onDestroy()
    }
}
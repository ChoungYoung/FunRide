package com.young.funride.ui

import android.graphics.Color
import android.view.View
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.young.funride.R
import com.young.funride.base.BaseFragment
import com.young.funride.entity.RoutePoint
import com.young.funride.entity.RoutePoints
import com.young.funride.viewmodel.NavViewModel
import kotlinx.android.synthetic.main.fragment_route_detail.*

class RouteDetailFragment : BaseFragment<NavViewModel>(){

    lateinit var routePoints: RoutePoints
    private lateinit var points: ArrayList<LatLng>
    lateinit var mBaiduMap: BaiduMap

    override fun getLayoutResId() = R.layout.fragment_route_detail

    override fun providerVMClass() = NavViewModel::class.java

    override fun initView(view: View) {
        mBaiduMap = routeDetailMapView.map
        mViewModel.title.value = getString(R.string.route_detail)
        mViewModel.isVisible.value = View.VISIBLE
    }

    override fun initData() {
        routePoints = mViewModel.points.value!!

        detailTime.text = getString(R.string.time_,routePoints.time)
        detailDistance.text = getString(R.string.distance_,routePoints.distance)

        drawRoute()
    }

    /**
     * 绘制路径图
     */
    private fun drawRoute() {
        points = ArrayList()
        for (point in routePoints.routeList) {
            val latLng = LatLng(point.routeLat, point.routeLng)
            points.add(latLng)
        }
        if (points.size > 2) {
            val ooPolyline: OverlayOptions = PolylineOptions().width(10).color(Color.BLUE).points(points)
            mBaiduMap.addOverlay(ooPolyline)
            val startPoint: RoutePoint = routePoints.routeList[0]
            val startPosition = LatLng(startPoint.routeLat, startPoint.routeLng)
            val builder = MapStatus.Builder()
            builder.target(startPosition).zoom(18.0f)
            mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()))
            val endPoint: RoutePoint = routePoints.routeList[routePoints.routeList.size - 1]
            val endPosition = LatLng(endPoint.routeLat, endPoint.routeLng)
            addOverLayout(startPosition, endPosition)
        }
    }

    private fun addOverLayout(startPosition: LatLng, endPosition: LatLng) {
        // 定义Maker坐标点 , 构建MarkerOption，用于在地图上添加Marker
        val options = MarkerOptions().position(startPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_st))
        mBaiduMap.addOverlay(options)
        val options2 = MarkerOptions().position(endPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_en))
        mBaiduMap.addOverlay(options2)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.title.value = getString(R.string.app_name)
        mViewModel.isVisible.value = View.GONE
    }

}
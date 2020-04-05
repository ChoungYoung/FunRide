package com.young.funride.util

import com.baidu.mapapi.map.*
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener
import com.baidu.mapapi.map.BaiduMap.OnPolylineClickListener
import com.baidu.mapapi.model.LatLngBounds

/**
 * 该类提供一个能够显示和管理多个Overlay的基类
 * 复写getOverlayOptions()设置欲显示和管理的Overlay列表
 * 通过BaiduMap.setOnMarkerClickListener()
 * 将覆盖物点击事件传递给OverlayManager后，OverlayManager才能响应点击事件。
 * 复写onMarkerClick处理Marker点击事件
 */
abstract class OverlayManager(baiduMap: BaiduMap) : OnMarkerClickListener, OnPolylineClickListener {
    var mBaiduMap: BaiduMap? = baiduMap
    var mOverlayOptionList: ArrayList<OverlayOptions>? = null
    var mOverlayList: ArrayList<Overlay>? = null

    /**
     * 通过一个BaiduMap 对象构造
     *
     * @param baiduMap 百度地图
     */
    init {
        if (mOverlayOptionList == null){
            mOverlayOptionList = ArrayList()
        }
        if (mOverlayList == null){
            mOverlayList = ArrayList()
        }
    }

    /**
     * 覆写此方法设置要管理的Overlay列表
     *
     * @return 管理的Overlay列表
     */
    abstract fun getOverlayOptions(): MutableList<OverlayOptions>?

    /**
     * 将所有Overlay 添加到地图上
     */
    fun addToMap() {
        if (mBaiduMap == null) {
            return
        }
        removeFromMap()
        val overlayOptions: List<OverlayOptions?>? = getOverlayOptions()
        if (overlayOptions != null) {
            mOverlayOptionList?.addAll(getOverlayOptions()!!)
        }
        for (option in mOverlayOptionList!!) {
            mOverlayList?.add(mBaiduMap!!.addOverlay(option))
        }
    }

    /**
     * 将所有Overlay 从 地图上消除
     */
    fun removeFromMap() {
        if (mBaiduMap == null) {
            return
        }
        for (marker in mOverlayList!!) {
            marker.remove()
        }
        mOverlayOptionList?.clear()
        mOverlayList?.clear()
    }

    /**
     * 缩放地图，使所有Overlay都在合适的视野内
     *
     *
     * 注： 该方法只对Marker类型的overlay有效
     *
     *
     */
    open fun zoomToSpan() {
        if (mBaiduMap == null) {
            return
        }
        if (mOverlayList!!.size > 0) {
            val builder = LatLngBounds.Builder()
            for (overlay in mOverlayList!!) {
                // polyline 中的点可能太多，只按marker 缩放
                if (overlay is Marker) {
                    builder.include(overlay.position)
                }
            }
            mBaiduMap!!.setMapStatus(
                MapStatusUpdateFactory
                    .newLatLngBounds(builder.build())
            )
        }
    }
}
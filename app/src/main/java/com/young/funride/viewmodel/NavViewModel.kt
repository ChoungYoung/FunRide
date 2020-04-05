package com.young.funride.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.young.funride.base.BaseViewModel
import com.young.funride.entity.RoutePoints

class NavViewModel : BaseViewModel() {

    /**
     * 是否在行程中
     */
    var navMode: MutableLiveData<Boolean> = MutableLiveData(false)

    /**
     * 返回按钮可见性
     */
    var isVisible: MutableLiveData<Int> = MutableLiveData(View.GONE)

    /**
     * 标题栏
     */
    var title: MutableLiveData<String> = MutableLiveData("快乐出行")

    /**
     * 路径点
     */
    var points: MutableLiveData<RoutePoints> = MutableLiveData(RoutePoints(ArrayList(),"0","0"))

}
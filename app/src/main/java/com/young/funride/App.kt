package com.young.funride

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer

class App :Application() {

    companion object {
        lateinit var INSTANCE: App
    }

    override fun onCreate() {
        super.onCreate()

        INSTANCE = this

        //initialize map sdk
        SDKInitializer.initialize(applicationContext)

        //set coordinate type as BD09LL
        SDKInitializer.setCoordType(CoordType.BD09LL)

    }
}
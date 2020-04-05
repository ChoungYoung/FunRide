package com.young.funride.ui

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.young.funride.base.BaseActivity
import com.young.funride.R
import com.young.funride.util.DialogUtil
import com.young.funride.viewmodel.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

class SplashActivity : BaseActivity<SplashViewModel>() {

    //要申请的权限
    private val mPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val REQUEST_CODE = 1
    private val PERMISSIN_SETTING_BACK = 1001

    override fun getLayoutResId() = R.layout.activity_splash

    override fun providerVMClass() = SplashViewModel::class.java

    override fun initView() {
    }

    override fun initData() {
        launch {
            //开屏画面一秒时间
            delay(1000)
            requestPermission()
        }
    }

    private fun requestPermission() {
        val locationPermission =
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            startActivity(MainActivity::class.java)
            finish()
        } else {
            val hasRefused = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (hasRefused) {
                DialogUtil.showConfirmDialog(this,
                    getString(R.string.permission_tips),
                    false,
                    getString(R.string.confirm),
                    DialogInterface.OnClickListener { _, _ ->
                        run {
                            ActivityCompat.requestPermissions(this, mPermissions, REQUEST_CODE)
                        }
                    })
//                //用户第一次拒绝后，可能没有看懂提示，第二次的时候就会执行到这里以友善的方式提示用户
            } else {
                //第一次提示 会以系统默认的弹框提示用户
                ActivityCompat.requestPermissions(this, mPermissions, REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //get permissions，enter MainActivity
                startActivity(MainActivity::class.java)
                finish()
            } else {
                DialogUtil.showConfirmDialog(this,
                    getString(R.string.permission_tips),
                    false,
                    getString(R.string.confirm),
                    DialogInterface.OnClickListener { _, _ ->
                        run {
                            val intent = Intent()
                            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                            intent.data = Uri.fromParts("package", packageName, null)
                            startActivityForResult(intent, PERMISSIN_SETTING_BACK)
                        }
                    })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSIN_SETTING_BACK) {
            requestPermission()
        }
    }
}
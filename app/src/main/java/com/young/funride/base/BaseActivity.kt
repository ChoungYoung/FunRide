package com.young.funride.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.young.funride.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

abstract class BaseActivity<VM : BaseViewModel> : AppCompatActivity(), CoroutineScope by MainScope() {

    protected lateinit var mViewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //7.0以上TRANSLUCENT_STATUS时部分手机状态栏有灰色遮罩，去掉它
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val decorView = window.decorView
            if ((window.attributes.flags and WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) == 0) {
                try {
                    val field = decorView.javaClass.getDeclaredField("mSemiTransparentStatusBarColor")
                    field.isAccessible = true
                    field.setInt(decorView, getColor(R.color.grey))
                } catch (ignored: Exception) {
                }
            }
        }

        setContentView(getLayoutResId())

        initVM()
        initView()
//        setSupportActionBar(mToolbar)
        initData()
        startObserve()
    }

    abstract fun getLayoutResId(): Int
    abstract fun initView()
    abstract fun initData()

    private fun initVM() {
        providerVMClass()?.let {
            mViewModel = ViewModelProvider(this).get(it)
            lifecycle.addObserver(mViewModel)
        }
    }
    open fun providerVMClass(): Class<VM>? = null

    open fun startObserve() {
        mViewModel.mException.observe(this, Observer { it?.let { onError(it) } })
    }
    open fun onError(e: Throwable) {}

    override fun onDestroy() {
        lifecycle.removeObserver(mViewModel)
        cancel()
        super.onDestroy()
    }

    protected fun startActivity(z: Class<*>) {
        startActivity(Intent(this, z))
    }

    protected fun startActivity(z: Class<*>, bundle: Bundle) {
        val intent = Intent(this, z).putExtras(bundle)
        startActivity(intent)
    }

}
package com.young.funride.ui

import androidx.lifecycle.Observer
import com.young.funride.R
import com.young.funride.base.BaseActivity
import com.young.funride.util.Utils
import com.young.funride.viewmodel.NavViewModel
import kotlinx.android.synthetic.main.action_bar.*

class MainActivity : BaseActivity<NavViewModel>() {

    override fun getLayoutResId() = R.layout.activity_main

    override fun providerVMClass() = NavViewModel::class.java

    override fun initView() {

        //初始化视图
        supportFragmentManager
            .beginTransaction()
            .add(R.id.container,NavFragment(),NavFragment::class.java.name)
            .commit()

        back.setOnClickListener{
            supportFragmentManager.popBackStack()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount <= 0){
            finish()
        }else{
            supportFragmentManager.popBackStack()
        }
    }

    override fun initData() {
        //更改状态栏
        mViewModel.isVisible.observe(this, Observer {
            back.visibility = it
        } )
        mViewModel.title.observe(this, Observer{
            titleText.text = it
        })
    }

}

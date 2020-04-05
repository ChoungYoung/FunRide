package com.young.funride.util

import android.content.Context
import android.widget.Toast

object ToastUtils{

    private var mToast: Toast? = null

    fun toast(context: Context?, text: String){
        if (null == mToast){
            mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
            mToast?.show()
        }else{
            mToast?.run {
                setText(text)
                show()
            }
        }
    }
}

//inline fun Context.toast(value: () -> String) =
//    Toast.makeText(this, value(), Toast.LENGTH_SHORT).show()
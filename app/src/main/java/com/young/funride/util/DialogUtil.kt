package com.young.funride.util

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog

object DialogUtil {
    var dialog: AlertDialog.Builder? = null
    fun showConfirmDialog(context: Context,message: String,cancelable: Boolean,confirmText: String,listener: DialogInterface.OnClickListener){
        if (null == dialog)
            dialog = AlertDialog.Builder(context)
        dialog?.apply {
            setMessage(message)
            setCancelable(cancelable)
            setPositiveButton(confirmText,listener)
            create().show()
        }
    }
}
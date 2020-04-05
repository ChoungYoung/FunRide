package com.young.funride.util

import android.content.Context
import android.widget.Toast

inline fun Context.toast(value: () -> String) =
    Toast.makeText(this, value(), Toast.LENGTH_SHORT).show()
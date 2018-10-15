package com.baikaleg.v3.autoinfo.data

import android.databinding.InverseMethod
import android.text.TextUtils

object Conversion {

    @JvmStatic
    @InverseMethod("toDouble")
    fun toString(value: Double): String {
        return if (value == 0.0) {
            ""
        } else {
            value.toString()
        }
    }

    @JvmStatic
    fun toDouble(str: String): Double {
        return if (TextUtils.isEmpty(str)) {
            0.0
        } else {
            str.toDouble()
        }
    }
}
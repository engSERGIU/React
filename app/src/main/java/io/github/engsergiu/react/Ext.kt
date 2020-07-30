package io.github.engsergiu.react

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

inline fun <reified CLASS> CLASS.logID(): String = CLASS::class.simpleName ?: "UNKNOWN CLASS"

inline fun Context.toast(length: Int = Toast.LENGTH_SHORT, message: () -> String) =
		Toast.makeText(this, message(), length).show()


inline fun Context.toastID(length: Int = Toast.LENGTH_SHORT, message: () -> Int) =
		Toast.makeText(this, message(), length).show()

fun Context.getResColor(@ColorRes int: Int) = ContextCompat.getColor(this, int)
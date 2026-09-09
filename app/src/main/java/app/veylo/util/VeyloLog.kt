package app.veylo.util

import android.util.Log
import app.veylo.BuildConfig

object VeyloLog {
    private const val TAG = "Veylo"

    fun debug(message: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        Log.e(TAG, message, throwable)
    }
}

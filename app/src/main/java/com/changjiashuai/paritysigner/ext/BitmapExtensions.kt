package com.changjiashuai.paritysigner.ext

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

/**
 * Email: changjiashuai@gmail.com
 *
 * Created by CJS on 2022/7/2 15:40.
 */
fun List<UByte>.toBitmap(): Bitmap {
    val picture = this.toUByteArray().toByteArray()
    return try {
        BitmapFactory.decodeByteArray(picture, 0, picture.size)
    } catch (e: java.lang.Exception) {
        Log.d("image decoding error", e.toString())
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }
}

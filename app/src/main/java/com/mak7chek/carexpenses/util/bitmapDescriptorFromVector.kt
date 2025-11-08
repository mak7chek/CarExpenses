package com.mak7chek.carexpenses.util
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
    @ColorInt tintColor: Int? = null,
    targetWidth: Int = -1,
    targetHeight: Int = -1
): BitmapDescriptor? {
    val vectorDrawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    val finalWidth = if (targetWidth != -1) targetWidth else vectorDrawable.intrinsicWidth
    val finalHeight = if (targetHeight != -1) targetHeight else vectorDrawable.intrinsicHeight

    vectorDrawable.setBounds(0, 0, finalWidth, finalHeight)

    tintColor?.let {
        vectorDrawable.colorFilter = PorterDuffColorFilter(it, PorterDuff.Mode.SRC_IN)
    }

    val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
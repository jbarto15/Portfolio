package com.example.msdpaint.viewmodels

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.geometry.Offset
import com.example.msdpaint.storage.Doodle


data class StudioModel(
    var bitmap: Bitmap = Bitmap.createBitmap(1440, 2160, Bitmap.Config.ARGB_8888),
    var brushColor: Int = Color.BLACK,
    var brushSize: Float = 15f,
    var brushShape: Shape = Shape.PATH,
    var doodle: Doodle = Doodle(),
    var eraserOn: Boolean = false,
    var marbleOffset: Offset = Offset(360f - 15f, 735.5f - 15f),
    var marbleOn: Boolean = false,
    var marbleTime: Boolean = false,
    var sliderPosition: Int = 0
)

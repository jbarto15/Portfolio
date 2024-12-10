package com.example.msdpaint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class Paper(context: Context, attrs: AttributeSet) : View(context, attrs) {


    private var bitmap = Bitmap.createBitmap(1080, 2340, Bitmap.Config.ARGB_8888)
    private val paint = Paint()
    val path = Path()
    lateinit var touchFunc: (Float, Float, Int) -> Unit


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.style = Paint.Style.STROKE
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        canvas.drawPath(path, paint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        touchFunc(event!!.x, event.y, event.action)
        invalidate()
        return true
    }

    fun updateDisplay(newBitmap: Bitmap) {
        bitmap = newBitmap
    }

    fun setTouchFunction(passedFunc: (Float, Float, Int) -> Unit) {
        touchFunc = passedFunc
    }


}


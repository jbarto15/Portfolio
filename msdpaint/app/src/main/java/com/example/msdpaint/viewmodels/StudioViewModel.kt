package com.example.msdpaint.viewmodels

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.msdpaint.storage.Doodle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch


class StudioViewModel :ViewModel() {


    companion object {
        private const val SHAKE_THRESHOLD = 1000f
        private val radii: List<Float> = generateSequence(15f) { it + 1f }
            .takeWhile { it <= 60f }
            .plus( generateSequence(60f) { it - 1f }
                    .takeWhile { it >= 15f } )
            .toList()
        const val BRUSH_SIZE_MIN = 15f
        const val BRUSH_SIZE_MAX = 150f
    }


    private var model: StudioModel = StudioModel()

    private var _bitmap :MutableLiveData<Bitmap> = MutableLiveData(model.bitmap)
    private var _brushColor: MutableLiveData<Int> = MutableLiveData(model.brushColor)
    private var _brushSize: MutableLiveData<Float> = MutableLiveData(model.brushSize)
    private var _brushShape: MutableLiveData<Shape> = MutableLiveData(model.brushShape)
    private var _doodle: MutableLiveData<Doodle> = MutableLiveData(model.doodle)
    private var _eraserOn: MutableLiveData<Boolean> = MutableLiveData(model.eraserOn)
    private var _marbleOn: MutableLiveData<Boolean> = MutableLiveData(model.marbleOn)
    private var _marbleTime: MutableLiveData<Boolean> = MutableLiveData(model.marbleTime)
    private var _marbleOffset : MutableLiveData<Offset> = MutableLiveData(model.marbleOffset)

    private var canvas: Canvas = Canvas(_bitmap.value!!)
    private var currentIndex = 0
    private var lastSelectedColor: Int = Color.BLACK
    private var paint: Paint = Paint(Color.BLACK)
    private var path: Path = Path()
    private var sensorRegistered = false

    val bitmap: LiveData<Bitmap> = _bitmap
    val brushColor: LiveData<Int> = _brushColor
    val brushSize: LiveData<Float> = _brushSize
    val brushShape: LiveData<Shape> = _brushShape
    val doodle: LiveData<Doodle> = _doodle
    var eraserOn: LiveData<Boolean> = _eraserOn
    val marbleOn: LiveData<Boolean> = _marbleOn
    val marbleOffset: LiveData<Offset> = _marbleOffset
    var marbleTime: LiveData<Boolean> = _marbleTime


    //region <Drawing>

    fun handleTouch(x :Float, y :Float, action :Int) {

        val currBrushShape = _brushShape.value ?: model.brushShape

        when (currBrushShape) {

            Shape.SQUARE -> {
                when (action)  {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> { drawSquare(x, y) }
                }
            }

            Shape.CIRCLE -> {
                when (action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> { drawCircle(x, y) }
                }
            }

            Shape.PATH -> {
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        path.moveTo(x, y)
                        drawPath()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        path.lineTo(x, y)
                        drawPath()
                    }
                    MotionEvent.ACTION_UP -> {
                        path.reset()
                        drawPath()
                    }
                }
            }

        } // outer when()

    }

    private fun drawSquare(x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        val currBrushSize = _brushSize.value ?: model.brushSize
        canvas.drawRect(x, y, x - currBrushSize * 2, y - currBrushSize * 2, paint)
    }

    private fun drawCircle(x: Float, y: Float) {
        paint.style = Paint.Style.FILL
        val currBrushSize = _brushSize.value ?: model.brushSize
        canvas.drawCircle(x, y, currBrushSize, paint)
    }

    private fun drawPath() {
        paint.style = Paint.Style.STROKE
        val currBrushSize = _brushSize.value ?: model.brushSize
        paint.strokeWidth = currBrushSize
        canvas.drawPath(path, paint)
    }

    fun drawMarblePath () {
        if (marbleOn.value != true) { return }
        val i = currentIndex++ % radii.size
        val radius = radii[i]
        val currMarbleOffset = _marbleOffset.value ?: model.marbleOffset
        canvas.drawCircle(currMarbleOffset.x, currMarbleOffset.y, radius, paint)
    }

    //endregion


    //region <Setters>

    fun updateDoodle(newBitmap: Bitmap?, newDoodle: Doodle) {
        // Create a copy of the bitmap from the database so that it is mutable.
        val bitmapCopy = newBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
        _bitmap.value = bitmapCopy
        val currBitmap = _bitmap.value ?: model.bitmap
        canvas = Canvas(currBitmap)
        _doodle.value = newDoodle
        resetStudio()
    }

    fun resetStudio() {
        _brushSize.value = model.brushSize
        _brushColor.value = model.brushColor
        _eraserOn.value = model.eraserOn
        paint.color = model.brushColor
        _brushShape.value = model.brushShape
        _marbleOn.value = model.marbleOn
    }

    fun toggleEraser() {

        _eraserOn.value = _eraserOn.value != true

        if (_eraserOn.value == true) {
            lastSelectedColor = paint.color
            paint.color = Color.WHITE
        } else {
            paint.color = lastSelectedColor
        }

    }

    fun toggleMarble() {
        _marbleOn.value = _marbleOn.value != true
       // no need to call prepare(); create() does that for you
    }

    fun toggleMarbleTime() {
        if ( _marbleTime.value  == true ) {
            _marbleTime.value = false
        }
        else {
            _marbleTime.value = true
        }
//        _marbleTime.value = _marbleTime.value != true

    }

    fun clearBitmap() {
        val currBitmap = _bitmap.value ?: model.bitmap
        currBitmap.eraseColor(Color.TRANSPARENT)
    }

    fun updateBrushColor(newColor: Int) {
        _brushColor.value = newColor
        paint.color = newColor
    }

    fun updateBrushShape(newShape: Shape) {
        _brushShape.value = newShape
    }

    fun updateBrushSize(newSize: Float) {
        _brushSize.value = newSize
    }

    //endregion


    //region <Sensor>

    fun collectAndApplySensor(flow: Flow<FloatArray> ) {

        // Prevent multiple registrations
        if (sensorRegistered) { return }

        sensorRegistered = true

        // Within coroutine to update both x and y values at same moment in time.
        viewModelScope.launch {

            flow.collect { sensorValues ->

                val xVelocity = -sensorValues[0]
                val yVelocity = sensorValues[1]

                // To increase realistic feel for animation
                val scale = 6f
                val paperWidth = 720f
                val paperHeight = 1471f
                val toolBarHeight = 112f
                val rightBounds = paperWidth - _brushSize.value!!
                val bottomBounds = paperHeight - toolBarHeight - _brushSize.value!!

                val constrainedX = ( marbleOffset.value!!.x + xVelocity * scale).coerceIn(_brushSize.value!!, rightBounds)
                val constrainedY = ( marbleOffset.value!!.y + yVelocity * scale).coerceIn(_brushSize.value!!, bottomBounds)

                _marbleOffset.value = Offset(constrainedX, constrainedY)
            }

        } // launch{}

    }


    fun getSensorData(sensor: Sensor, sensorManager: SensorManager): Flow<FloatArray> {

        return channelFlow {

            val listener = object : SensorEventListener {

                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        //Log.d("Sensor event!", event.values.toString())
                        var success = channel.trySend(event.values).isSuccess // todo get ride of E?
                        //Log.d("Success?", success.toString())
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

            }

            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

            awaitClose {
                sensorManager.unregisterListener(listener)
                // Reset registration status.
                sensorRegistered = false
            }

        } // channelFlow{}

    }


    fun collectLinearAccelerator( flow: Flow<Float> ) {

        // Within coroutine to update both x and y values at same moment in time.
        viewModelScope.launch {

            flow.collect { accelMag ->
                if ( accelMag > SHAKE_THRESHOLD ) {
                    clearBitmap()
                }
            }

        } // launch{}

    }


    fun getAccelMagnitude(accelerometer: Sensor, sensorManager: SensorManager): Flow<Float> {

        return channelFlow {

            val listener = object : SensorEventListener {

                override fun onSensorChanged(event: SensorEvent?) {

                    if (event !== null) {
                        channel.trySend(event.values.map{x -> x*x}.reduce(Float::plus))
                    }

                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

            }

            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

            awaitClose {
                sensorManager.unregisterListener(listener)
            }

        } // channelFlow{}

    }

    //endregion


}
package edu.umasslass.healthyair.fftmic.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet

/**
 * @author Adam Lechowicz 08/2021
 */

class FFTAirView(context: Context, attrs: AttributeSet?) : SimpleSurface(context, attrs), FFTView {

    var airText: String = "0.00"
    var rmsText: String = "0.0"
    var bucketText: String = "None"
    val paintText: Paint = textPaint()

    fun drawAudio(canvas: Canvas): Canvas {
        canvas.drawColor(Color.DKGRAY)

        canvas.drawText("Pred: $airText m/s, RMS: $rmsText", 20f.px, 28f.px, paintText)
        canvas.drawText("Bucket: $bucketText", 20f.px, 64f.px, paintText)

        return canvas
    }

    override fun onFFT(fft: FloatArray, air: String, rms: String, bucket: String) {
        this.airText = air
        this.rmsText = rms
        this.bucketText = bucket
        drawSurface(this::drawAudio)
    }
}
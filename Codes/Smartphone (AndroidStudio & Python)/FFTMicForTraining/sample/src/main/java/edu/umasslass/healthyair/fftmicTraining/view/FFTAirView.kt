package edu.umasslass.healthyair.fftmicTraining.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import java.lang.System.arraycopy

/**
 * @author Adam Lechowicz 08/2021
 */
class FFTAirView(context: Context, attrs: AttributeSet?) : SimpleSurface(context, attrs), FFTView {

    var airText: String = "0.00"
    var rmsText: String = "0.00"
    val paintText: Paint = textPaint()

    fun drawAudio(canvas: Canvas): Canvas {
        canvas.drawColor(Color.DKGRAY)

        canvas.drawText("Airflow: $airText m/s,  RMS value: $rmsText", 24f.px, 32f.px, paintText)

        return canvas
    }

    override fun onFFT(fft: FloatArray, air: String, rms: String) {
        this.airText = air
        this.rmsText = rms
        drawSurface(this::drawAudio)
    }
}
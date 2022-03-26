package edu.umasslass.healthyair.fftmic.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import java.lang.System.arraycopy

/**
 * @author Pär Amsen 06/2017
 */
class FFTBandView(context: Context, attrs: AttributeSet?) : SimpleSurface(context, attrs), FFTView {
    val frequency = 1000
    val size = 16
    val bands = 8
    val bandSize = size / bands
    val maxConst = 17500 //reference max value for accum magnitude
    var average = .0f

    val fft: FloatArray = FloatArray(size)
    var airText: String = "0.00"
    val paintBandsFill: Paint = Paint()
    val paintBands: Paint = Paint()
    val paintAvg: Paint = Paint()
    val paintText: Paint = textPaint()

    init {
        paintBandsFill.color = Color.parseColor("#00FFFF")
        paintBandsFill.style = Paint.Style.FILL

        paintBands.color = Color.parseColor("#FFFFFF")
        paintBands.strokeWidth = 1f
        paintBands.style = Paint.Style.STROKE

        paintAvg.color = Color.parseColor("#33FFFFFF")
        paintAvg.strokeWidth = 1f
        paintAvg.style = Paint.Style.STROKE
    }

    fun drawAudio(canvas: Canvas): Canvas {
        canvas.drawColor(Color.DKGRAY)
        for (i in 0..bands - 1) {
            var accum = .0f

            synchronized(fft) {
                for (j in 0..bandSize - 1 step 2) {
                    //convert real and imag part to get energy
                    accum += (Math.pow(fft[j + (i * bandSize)].toDouble(), 2.0) + Math.pow(fft[j + 1 + (i * bandSize)].toDouble(), 2.0)).toFloat()
                }

                accum /= bandSize / 2
            }

            average += accum

            canvas.drawRect(width * (i / bands.toFloat()), height - (height * Math.min(accum / maxConst.toDouble(), 1.0).toFloat()) - height * .02f, width * (i / bands.toFloat()) + width / bands.toFloat(), height.toFloat(), paintBandsFill)
            canvas.drawRect(width * (i / bands.toFloat()), height - (height * Math.min(accum / maxConst.toDouble(), 1.0).toFloat()) - height * .02f, width * (i / bands.toFloat()) + width / bands.toFloat(), height.toFloat(), paintBands)
        }

        average /= bands

        canvas.drawLine(0f, height - (height * (average / maxConst)) - height * .02f, width.toFloat(), height - (height * (average / maxConst)) - height * .02f, paintAvg)
        canvas.drawText("Predicted Airflow: $airText m/s", 16f.px, 24f.px, paintText)

        return canvas
    }

    override fun onFFT(fft: FloatArray, air: String, rms: String, bucket: String) {
        synchronized(this.fft) {
            arraycopy(fft, 2, this.fft, 0, fft.size - 2)
            this.airText = air
            drawSurface(this::drawAudio)
        }
    }
}
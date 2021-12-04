package edu.umasslass.healthyair.fftmicTraining.source

import android.os.Build
import android.os.Environment
import android.os.SystemClock.uptimeMillis
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter

/**
 * Class to handle logging FFT values to file on phone
 *
 * @author Adam Lechowicz 08/2021
 */

class Logger() {
    val frequency = 1000
    val sampleSize = 16
    val initTime = uptimeMillis()

    fun init(){
        val baseDir = Environment.getExternalStorageDirectory().absolutePath
        val fileName = "fft.txt"
        val filePath = baseDir + File.separator + fileName
        val myfile = File(filePath)
        myfile.createNewFile()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun logData(fft: FloatArray, air: String, sil: String){
        var value = ""
        var abscissa = 0.0
        val baseDir = Environment.getExternalStorageDirectory().absolutePath
        val fileName = "fft.txt"
        val filePath = baseDir + File.separator + fileName
        val myfile = File(filePath)

        val pw = PrintWriter(FileOutputStream(myfile, true))

        if (air == "0.0"){
            return
        }

        for (i in 0 until 17 step 2) {
            abscissa = ((i * 0.5 * frequency) / sampleSize)
            value = "%.4f".format(Math.sqrt(Math.pow(fft[i].toDouble(), 2.0) + Math.pow(fft[i + 1].toDouble(), 2.0)).toFloat())

            pw.appendln(abscissa.toString() + "," + value)
            if (i + 1 == 17) {
                pw.appendln("clocktime,"+(uptimeMillis()-initTime).toString())
                pw.appendln("airflow(m/s),$air")
                pw.appendln("silence,$sil")
                pw.appendln()
                pw.appendln()
            }
        }
        pw.close()
    }
    fun onFFT(fft: FloatArray){
        var value = .0f
        var abscissa = 0.0

        //Log.d("FFTMic", "onFFT!")
        for (i in 0 until 2048 - 1 step 2) {
            abscissa = ((i * 0.5 * frequency) / sampleSize)
            value =
                (Math.pow(fft[i].toDouble(), 2.0) + Math.pow(fft[i + 1].toDouble(), 2.0)).toFloat()
            //Log.d("FFTMic Abscissa", abscissa.toString())
            //Log.d("FFTMic Value", value.toString())
        }
    }
}
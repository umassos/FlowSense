package edu.umasslass.healthyair.fftmic.view

/**
 * @author Pär Amsen 07/2017
 */

interface FFTView {
    fun onFFT(fft: FloatArray, pred: String, rms: String, bucket: String)
}
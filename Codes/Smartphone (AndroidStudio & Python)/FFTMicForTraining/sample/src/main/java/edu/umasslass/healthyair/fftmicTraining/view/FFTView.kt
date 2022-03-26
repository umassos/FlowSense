package edu.umasslass.healthyair.fftmicTraining.view

interface FFTView {
    fun onFFT(fft: FloatArray, air: String, rms: String)
}
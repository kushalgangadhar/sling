package com.kushalgangadhar.sling.utils

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VisualizerHelper {

    private var visualizer: Visualizer? = null
    
    // StateFlow to hold the real-time FFT byte array
    private val _fftData = MutableStateFlow(ByteArray(0))
    val fftData: StateFlow<ByteArray> = _fftData.asStateFlow()

    fun startVisualizer(audioSessionId: Int) {
        stopVisualizer() // Ensure we don't have multiple instances running

        try {
            visualizer = Visualizer(audioSessionId).apply {
                // Set the capture size (e.g., 512 is a good balance of resolution and speed)
                captureSize = Visualizer.getCaptureSizeRange()[1] 
                
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        // We are ignoring raw waveform data for this FFT visualizer
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        // Emit the FFT data to our StateFlow
                        fft?.let { _fftData.value = it }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)
                
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopVisualizer() {
        visualizer?.apply {
            enabled = false
            release()
        }
        visualizer = null
        _fftData.value = ByteArray(0) // Clear the canvas
    }
}

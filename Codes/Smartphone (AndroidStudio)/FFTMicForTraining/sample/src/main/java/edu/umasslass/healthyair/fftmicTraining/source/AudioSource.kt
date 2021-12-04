package edu.umasslass.healthyair.fftmicTraining.source

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.util.Log
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import java.nio.FloatBuffer

const val RATE_HZ = 16000
const val SAMPLE_SIZE = 16

/**
 * Rx Flowable factory that expose a Flowable through stream() that while subscribed to emits
 * audio frames of size SAMPLE_SIZE. Uses Disposable to handle deallocation.
 *
 * @author Adam Lechowicz 07/2021
 */
class AudioSource() {
    private val src = MediaRecorder.AudioSource.UNPROCESSED
    private val cfg = AudioFormat.CHANNEL_IN_MONO
    private val format = AudioFormat.ENCODING_PCM_16BIT
    private val size = AudioRecord.getMinBufferSize(RATE_HZ, cfg, format)
    private val flowable: Flowable<FloatArray>
    private val recorder = AudioRecord(src, RATE_HZ, cfg, format, size)

    /**
     * The returned Flowable publish frames of two sizes; 4096 and 768. Roughly 10fps / 60fps.
     * Filter is used to distinguish the two types. Ideally this should be handled in two separate
     * Flowables, but AudioRecord makes that utterly complex.
     */
    init {
        flowable = Flowable.create<FloatArray>({ sub ->

            if (size <= 0) {
                sub.onError(RuntimeException("AudioSource / Could not allocate audio buffer on this device (emulator? no mic?)"))
                return@create
            }

            recorder.startRecording()
            sub.setCancellable {
                recorder.stop()
                recorder.release()
            }

            val buf = ShortArray(32)
            val out = FloatBuffer.allocate(SAMPLE_SIZE)
            var read = 0

            while (!sub.isCancelled) {
                read += recorder.read(buf, read, buf.size - read)

                if (read == buf.size) {
//                    for (element in buf) {
//                        out.put(element.toFloat())
//                    }
//                    //1000 hz version
                    var i = 0
                    for (element in buf) {
                        i++
                        if (i % 16 == 0){
                            out.put(element.toFloat())
                            i = 0
                        }
                    }
                   //2000 hz version
//                    var i = 0
//                    for (element in buf) {
//                        i++
//                        if (i % 8 == 0){
//                            out.put(element.toFloat())
//                            i = 0
//                        }
//                    }

                    if (!out.hasRemaining()) {
                        val cpy = FloatArray(out.array().size)
                        System.arraycopy(out.array(), 0, cpy, 0, out.array().size)
                        sub.onNext(cpy)
                        out.clear()
                    }

                    read = 0
                }
            }

            sub.onComplete()
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .share()
    }

    /**
     * All subscribers must unsubscribe in order for Flowable to cancel the microphone stream. The
     * stream is started automatically when subscribed to, the same mic stream is used for all subs.
     */
    fun stream(): Flowable<FloatArray> {
        return flowable
    }

    fun resume() {
        recorder.startRecording()
    }
}
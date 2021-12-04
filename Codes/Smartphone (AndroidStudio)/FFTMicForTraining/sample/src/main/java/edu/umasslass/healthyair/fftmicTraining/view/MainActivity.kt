package edu.umasslass.healthyair.fftmicTraining.view

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ActionMenuView
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import com.paramsen.noise.Noise
import edu.umasslass.healthyair.fftmicTraining.R
import edu.umasslass.healthyair.fftmicTraining.R.color.*
import edu.umasslass.healthyair.fftmicTraining.source.AudioSource
import edu.umasslass.healthyair.fftmicTraining.source.BLEComm
import edu.umasslass.healthyair.fftmicTraining.source.Logger
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    val TAG = javaClass.simpleName!!
    val frequency = 1000
    val sampleSize = 16
    var active = false
    var capable = false
    private var pred = ""

    val disposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scheduleAbout()
    }

    override fun onResume() {
        super.onResume()

        if (requestAudio() && disposable.size() == 0)
            capable = true
    }

    override fun onStop() {
        stop()
        super.onStop()
    }
    fun onClick(view: View?) {
        if (capable != true) {
            return
        }
        if (active){
            stop()
        } else {
            val button = findViewById<Button>(R.id.control_button)
            button!!.setBackgroundColor(getResources().getColor(colorPrimaryDark))
            Handler().postDelayed({
                start()
            }, 10000)
        }
    }
    /**
     * Subscribe to microphone
     */
    private fun start() {
        val src = AudioSource()
        val logger = Logger()
        val blue = BLEComm(this.baseContext)
        val noise = Noise.real(sampleSize)
        val mGain = 0.0044;
        var air = "0.0"
        var rms = 0.0
        var silence = ""
        var counter = 0;
        logger.init()
        blue.init()

        //AudioView
        disposable.add(src.stream().observeOn(Schedulers.newThread())
                .subscribe(
                    audioView::onWindow, { e -> Log.e(TAG, e.message) }
                ))
        //FFTView
        disposable.add(src.stream().observeOn(Schedulers.newThread())
                .map {
                    var rmsAcc = 0.0
                    for (i in it.indices) {
                        it[i] *= 2.0f
                        rmsAcc += it[i] * it[i] //rms computation here
                    }
                    if (rmsAcc != 0.0) {
                        rms = (Math.sqrt(rmsAcc / it.size) + rms)/2 //rolling average of value
                        //rms = Math.sqrt(rmsAcc / it.size)
                    }
                    silence = if (rms > 60) "1" else "0"
                    return@map it
                }
                .map { noise.fft(it, FloatArray(sampleSize + 2)) }
                .subscribe({ fft ->
                    counter++
                    air = blue.blueAirflow()
                    fftAirView.onFFT(fft, air, rms.toString()) //visualize rms here
                    if (counter >= 5) {
                        logger.logData(fft, air, rms.toString()) //pass rms value here
                        counter = 0
                    }
                }, { e -> Log.e(TAG, e.message) } ))
        active = true
        val button = findViewById<Button>(R.id.control_button)
        button!!.text = getString(R.string.cmd_stop)
        button!!.setBackgroundColor(getResources().getColor(colorRed))
    }

    /**
     * Dispose microphone subscriptions
     */
    private fun stop() {
        disposable.clear()
        active = false
        val button = findViewById<Button>(R.id.control_button)
        button!!.text = getString(R.string.cmd_start)
        button!!.setBackgroundColor(getResources().getColor(colorGreen))
    }

    /**
     * Output windows of sampleSize len, accumulates for FFT
     */
    private fun accumulate(o: Flowable<FloatArray>): Flowable<FloatArray> {
        val size = sampleSize

        return o.map(object : Function<FloatArray, FloatArray> {
            val buf = FloatArray(size * 2)
            val empty = FloatArray(0)
            var c = 0

            override fun apply(window: FloatArray): FloatArray {
                System.arraycopy(window, 0, buf, c, window.size)
                c += window.size

                if (c >= size) {
                    val out = FloatArray(size)
                    System.arraycopy(buf, 0, out, 0, size)

                    if (c > size) {
                        System.arraycopy(buf, c % size, buf, 0, c % size)
                    }

                    c = 0

                    return out
                }

                return empty
            }
        }).filter { fft -> fft.size == size } //filter only the emissions of complete 4096 windows
    }

    private fun accumulate1(o: Flowable<FloatArray>): Flowable<FloatArray> {
        return o.window(6).flatMapSingle { it.collect({ ArrayList<FloatArray>() }, { a, b -> a.add(b) }) }.map { window ->
            val out = FloatArray(sampleSize)
            var c = 0
            for (each in window) {
                if (c + each.size >= sampleSize)
                    break

                System.arraycopy(each, 0, out, c, each.size)
                c += each.size - 1
            }
            out
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestAudio(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(RECORD_AUDIO) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(RECORD_AUDIO), 1337)
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 1338)
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), 1339)
            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults[0] == PERMISSION_GRANTED)
            capable = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        info.onShow()

        return true
    }

    private fun scheduleAbout() {
        container.postDelayed({
            if (!info.showed) {
                try {
                    val anim = AnimationUtils.loadAnimation(this, R.anim.nudge).apply {
                        repeatCount = 3
                        repeatMode = Animation.REVERSE
                        duration = 200
                        interpolator = AccelerateDecelerateInterpolator()
                        onTerminate { scheduleAbout() }
                    }

                    (((((container.parent.parent as ViewGroup).getChildAt(1) as ViewGroup) //container
                            .getChildAt(0) as ViewGroup) //actionbar
                            .getChildAt(1) as ActionMenuView)
                            .getChildAt(0) as ActionMenuItemView)
                            .startAnimation(anim)
                } catch (e: Exception) {
                    Log.e(TAG, "Could not animate nudge / ${Log.getStackTraceString(e)}")
                }
            }
        }, 3000)
    }

}

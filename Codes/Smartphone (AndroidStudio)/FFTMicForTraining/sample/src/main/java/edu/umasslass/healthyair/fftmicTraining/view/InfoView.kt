package edu.umasslass.healthyair.fftmicTraining.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import edu.umasslass.healthyair.fftmicTraining.BuildConfig
import edu.umasslass.healthyair.fftmicTraining.R
import kotlinx.android.synthetic.main.view_info.view.*

/**
 * @author Adam Lechowicz 07/2021
 */

class InfoView : ConstraintLayout {
    var showed = false

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        layoutParams = LayoutParams(LayoutParams.MATCH_CONSTRAINT, LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.view_info, this, true)

        setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
        if (SDK_INT >= LOLLIPOP) elevation = 4f.px
        padding(16f.px.toInt())

        version.text = "v${BuildConfig.VERSION_NAME}"
        github.setOnClickListener { browser("https://github.com/umassos/healthyair") }
        me.setOnClickListener { browser("http://lass.cs.umass.edu/") }
        close.setOnClickListener { onClose() }
    }

    /**
     * Uses alpha=0 and post to render hierarchy before starting reveal animation, to avoid fugly
     * animation glitches
     */
    fun onShow() {
        if (visibility == View.VISIBLE) return onClose()

        clearAnimation()
        visibility = View.VISIBLE
        alpha = 0f
        requestLayout()

        post {
            showed = true

            if (SDK_INT >= LOLLIPOP) {
                if (alpha != 1.0f) alpha = 1f

                ViewAnimationUtils.createCircularReveal(this, width - (12f + 8f).px.toInt(), 0, width / 20f, Math.hypot(width.toDouble(), height.toDouble()).toFloat())
                        .setDuration(300)
                        .start()
            } else {
                val oldY = y
                alpha = 0f
                y = -20f

                animate().alpha(1f)
                        .y(20f.px)
                        .setDuration(200)
                        .setInterpolator(AccelerateInterpolator())
                        .onTerminate {
                            visibility = View.INVISIBLE
                            alpha = 1f
                            y = oldY
                        }.start()
            }
        }
    }

    fun onClose() {
        clearAnimation()

        val oldY = y

        animate().alpha(0f)
                .yBy(-20f.px)
                .setDuration(200)
                .setInterpolator(AccelerateInterpolator())
                .onTerminate {
                    visibility = View.INVISIBLE
                    alpha = 1f
                    y = oldY
                }.start()
    }

    private fun browser(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
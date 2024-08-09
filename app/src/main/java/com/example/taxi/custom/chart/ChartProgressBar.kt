package com.example.taxi.custom.chart


import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity.BOTTOM
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.taxi.R


class ChartProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var barHeight: Int = 150 // Default qiymat
    private var selectedTextView: TextView? = null
    private var selectedContainer: LinearLayout? = null
    private var onItemSelected: ((Int) -> Unit)? = null
    init {
        orientation = HORIZONTAL
        gravity = BOTTOM
        val padding = (16 * resources.displayMetrics.density).toInt()
        setPadding(padding, padding, padding, padding)

        context.obtainStyledAttributes(attrs, R.styleable.ChartProgressBar).apply {
            barHeight = getDimensionPixelSize(R.styleable.ChartProgressBar_barHeight, 150)
            recycle()
        }
    }

    fun setData(dataList: List<Pair<Int, String>>,onItemSelected: ((Int) -> Unit)?) {
        this.onItemSelected = onItemSelected
        removeAllViews() // Avvalgi viewlarni tozalash
        val maxValue = dataList.maxOf { it.first }
        Log.d("qiymat", "maximal qiymat $maxValue")
        for ((index, data) in dataList.withIndex()) {

            val progressBarView =
                LayoutInflater.from(context).inflate(R.layout.custom_progress_bar, null)

            val progressBar = progressBarView.findViewById<View>(R.id.progressBar)
            val textViewDate = progressBarView.findViewById<TextView>(R.id.textViewDate)
            val container = progressBarView.findViewById<LinearLayout>(R.id.liner_container)

            val targetHeight = ((barHeight * (data.first.toFloat() / maxValue)) * 0.85).toInt()
            Log.d("qiymat", "setData: ${barHeight}")
            Log.d("qiymat", "setData: ${targetHeight}")
            animateHeight(progressBar, targetHeight)

            // Maksimal balandlikni belgilash
//            val calculatedHeight = (barHeight * (data.first / 100.0)).toInt()

            // Balandlikni o'zgartirish
            val params = progressBar.layoutParams
//            params.height = calculatedHeight
            progressBar.layoutParams = params

            textViewDate.text = data.second

            val layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
            )
            layoutParams.weight = 1f
            layoutParams.leftMargin = if (data.second.length == 1) 12 else 8
            layoutParams.rightMargin = if (data.second.length == 1) 12 else 8

            addView(progressBarView, layoutParams)

            container.setOnClickListener {
                handleContainerClick(container)
                onItemSelected?.invoke(index)
            }

            if (index == dataList.size - 1) {
                handleContainerClick(container)
                onItemSelected?.invoke(index)
            }
            // TextView uchun click listener qo'shish

        }
    }

    private fun animateHeight(view: View, targetHeight: Int) {
        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            val params = view.layoutParams
            params.height = animatedValue
            view.layoutParams = params
        }
        animator.duration = 1000 // Animatsiya davomiyligi (1 soniya)
        animator.interpolator = DecelerateInterpolator() // Sekinlashuvchi interpolator
        animator.start()
    }

    private fun handleContainerClick(container: LinearLayout) {
        selectedContainer?.let {
            it.isSelected = false
            updateContainerBackground(it, false)
        }


        container.isSelected = true
        updateContainerBackground(container, true)


        selectedContainer = container
    }

    private fun updateContainerBackground(container: LinearLayout, isSelected: Boolean) {
        val progressBar = container.findViewById<View>(R.id.progressBar)

        progressBar.background = if (isSelected) {
            ContextCompat.getDrawable(context, R.drawable.selected_progress_drawable)
        } else {
            ContextCompat.getDrawable(context, R.drawable.custom_progress_drawable)
        }
    }
}
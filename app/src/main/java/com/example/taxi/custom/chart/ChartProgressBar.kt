package com.example.taxi.custom.chart


import android.content.Context
import android.util.AttributeSet
import android.view.Gravity.BOTTOM
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.taxi.R


class ChartProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var barHeight: Int = 500 // Default qiymat
    private var selectedTextView: TextView? = null
    private var selectedContainer: LinearLayout? = null

    init {
        orientation = HORIZONTAL
        gravity = BOTTOM
        val padding = (16 * resources.displayMetrics.density).toInt()
        setPadding(padding, padding, padding, padding)

        context.obtainStyledAttributes(attrs, R.styleable.ChartProgressBar).apply {
            barHeight = getDimensionPixelSize(R.styleable.ChartProgressBar_barHeight, 500)
            recycle()
        }
    }

    fun setData(dataList: List<Pair<Int, String>>) {
        removeAllViews() // Avvalgi viewlarni tozalash
        for (data in dataList) {
            val progressBarView =
                LayoutInflater.from(context).inflate(R.layout.custom_progress_bar, null)

            val progressBar = progressBarView.findViewById<View>(R.id.progressBar)
            val textViewDate = progressBarView.findViewById<TextView>(R.id.textViewDate)
            val container = progressBarView.findViewById<LinearLayout>(R.id.liner_container)


            // Maksimal balandlikni belgilash
            val calculatedHeight = (barHeight * (data.first / 100.0)).toInt()

            // Balandlikni o'zgartirish
            val params = progressBar.layoutParams
            params.height = calculatedHeight
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
            }
            // TextView uchun click listener qo'shish

        }
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
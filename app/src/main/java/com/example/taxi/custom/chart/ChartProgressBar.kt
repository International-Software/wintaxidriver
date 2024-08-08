package com.example.taxi.custom.chart


import android.content.Context
import android.util.AttributeSet
import android.view.Gravity.BOTTOM
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import com.example.taxi.R



class ChartProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var barHeight: Int = 500 // Default qiymat

    init {
        orientation = HORIZONTAL
        gravity = BOTTOM
        val padding = (16 * resources.displayMetrics.density).toInt()
        setPadding(padding, padding, padding, padding)

        context.withStyledAttributes(attrs, R.styleable.ChartProgressBar) {
            barHeight = getDimensionPixelSize(R.styleable.ChartProgressBar_barHeight, 500)
        }
    }

    fun setData(dataList: List<Pair<Int, String>>) {
        removeAllViews() // Avvalgi viewlarni tozalash
        for (data in dataList) {
            val progressBarView = LayoutInflater.from(context).inflate(R.layout.custom_progress_bar, null)

            val progressBar = progressBarView.findViewById<View>(R.id.progressBar)
            val textViewDate = progressBarView.findViewById<TextView>(R.id.textViewDate)

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
            layoutParams.leftMargin = 12
            layoutParams.rightMargin = 12

            addView(progressBarView, layoutParams)
        }
    }
}
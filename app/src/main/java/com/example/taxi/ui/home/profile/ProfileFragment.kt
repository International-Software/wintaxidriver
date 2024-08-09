package com.example.taxi.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.taxi.R
import com.example.taxi.custom.chart.ChartProgressBar
import com.example.taxi.databinding.FragmentProfileBinding
import com.example.taxi.domain.model.IsCompletedModel
import com.example.taxi.domain.model.MainResponse
import com.example.taxi.domain.model.selfie.SelfieAllData
import com.example.taxi.domain.model.selfie.StatusModel
import com.example.taxi.domain.model.statistics.StatisticsResponse
import com.example.taxi.domain.model.statistics.StatisticsResponseValue
import com.example.taxi.domain.preference.UserPreferenceManager
import com.example.taxi.ui.home.dashboard.DashboardViewModel
import com.example.taxi.utils.PhoneNumberUtil
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProfileFragment : Fragment() {


    private val profileViewModel: ProfileViewModel by viewModel()
    private val dashboardViewModel: DashboardViewModel by viewModel()
    private val userPreferenceManager: UserPreferenceManager by inject()
    private lateinit var viewBinding: FragmentProfileBinding

    private lateinit var chartProgressBar: ChartProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentProfileBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (userPreferenceManager.getDriverName() != "" || userPreferenceManager.getDriverPhone() != "") {
            viewBinding.driverIdHeader.text = userPreferenceManager.getDriverID().toString()
            viewBinding.driverNameTextView.text = userPreferenceManager.getDriverName()
            viewBinding.driverPhoneNumberTextView.text = userPreferenceManager.getDriverPhone()
        } else {
            dashboardViewModel.getDriverData()
        }
        viewBinding.fbnBackHome.setOnClickListener { findNavController().navigateUp() }
        viewBinding.btnSettings.setOnClickListener { findNavController().navigate(R.id.settingsFragment) }
        profileViewModel.statisticsType.observe(viewLifecycleOwner) {
            setStatisticsType(it)
        }

        profileViewModel.statisticsValue.observe(viewLifecycleOwner) {
            setStatisticsValues(it)
        }
        dashboardViewModel.driverDataResponse.observe(viewLifecycleOwner) {
            setDriverData(it)
        }

        viewBinding.buttonWeek.setOnClickListener { profileViewModel.setWeeks() }
        viewBinding.buttonMonth.setOnClickListener { profileViewModel.setMonths() }
        viewBinding.buttonDay.setOnClickListener { profileViewModel.setDays() }

        chartProgressBar = view.findViewById(R.id.chartProgressBar)


//        viewBinding.horizontalScrollView.post {
//            viewBinding.horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
//        }


    }



    private fun setStatisticsValues(data: Resource<MainResponse<List<StatisticsResponse<StatisticsResponseValue>>>>) {

        when (data.state) {
            ResourceState.ERROR -> {}
            ResourceState.LOADING -> {
                viewBinding.shimmerDayStatistics.visibility = View.VISIBLE
                viewBinding.horizontalScrollView.visibility = View.GONE
                viewBinding.shimmerDayStatistics.startShimmer()
            }

            ResourceState.SUCCESS -> {
                viewBinding.shimmerDayStatistics.visibility = View.GONE
                viewBinding.horizontalScrollView.visibility = View.VISIBLE
                viewBinding.shimmerDayStatistics.startShimmer()

                var dataList = data.data?.data ?: emptyList()
//                val dataList = data.data?.data?.map { day ->
//                    Pair(day.data.totalSum, getLastDay(day.period_between_date))
//                } ?: emptyList()
                dataList = dataList.reversed()
                val style = if (dataList.isNotEmpty() && dataList[0].period_between_date.length > 13) R.layout.custom_week_progress_bar else
                    R.layout.custom_day_progress_bar

                chartProgressBar.setData(dataList ,{period,total ->
                    viewBinding.totalPriceTv.text = PhoneNumberUtil.formatMoneyNumberPlate(total.toString())
                    viewBinding.periodTimeTv.text = if (period.length > 13) convertDateRangeWithMonth(period) else period
                },style)
            }
        }

    }

    fun convertDateRangeWithMonth(range: String): String {
        val monthsUzbek = mapOf(
            "01" to "yan",
            "02" to "fev",
            "03" to "mart",
            "04" to "apr",
            "05" to "may",
            "06" to "iyun",
            "07" to "iyul",
            "08" to "avg",
            "09" to "sent",
            "10" to "okt",
            "11" to "noy",
            "12" to "dek"
        )

        val dates = range.split(" - ")
        val startDate = dates[0]
        val endDate = dates[1]

        val startDay = startDate.substring(8, 10).toInt()
        val startMonth = startDate.substring(5, 7)

        val endDay = endDate.substring(8, 10).toInt()
        val endMonth = endDate.substring(5, 7)

        val startMonthName = monthsUzbek[startMonth] ?: "noma'lum"
        val endMonthName = monthsUzbek[endMonth] ?: "noma'lum"

        return if (startMonth == endMonth) {
            "$startDay $startMonthName - $endDay $endMonthName"
        } else {
            "$startDay $startMonthName - $endDay $endMonthName"
        }
    }

    private fun setDriverData(resource: Resource<MainResponse<SelfieAllData<IsCompletedModel, StatusModel>>>?) {
        when (resource?.state) {
            ResourceState.ERROR -> {

            }

            ResourceState.SUCCESS -> {
                stopShimmerLoading(resource.data?.data)
            }

            ResourceState.LOADING -> {
                startShimmerLoading()
            }

            else -> {}
        }
    }

    private fun stopShimmerLoading(data: SelfieAllData<IsCompletedModel, StatusModel>?) {
        viewBinding.shimmerUserData.visibility = View.GONE
        viewBinding.layoutUserData.visibility = View.VISIBLE
        viewBinding.shimmerUserData.stopShimmer()

        viewBinding.driverIdHeader.text = data?.id.toString()
        viewBinding.driverNameTextView.text = data?.first_name
        viewBinding.driverPhoneNumberTextView.text = data?.phone
    }

    private fun startShimmerLoading() {
        viewBinding.shimmerUserData.visibility = View.VISIBLE
        viewBinding.layoutUserData.visibility = View.GONE
        viewBinding.shimmerUserData.startShimmer()
    }

    private fun setStatisticsType(type: Int?) {
        when (type) {
            0 -> {
                viewBinding.buttonDay.isSelected = false
                viewBinding.buttonWeek.isSelected = false
                viewBinding.buttonMonth.isSelected = true
            }

            1 -> {
                viewBinding.buttonDay.isSelected = false
                viewBinding.buttonWeek.isSelected = true
                viewBinding.buttonMonth.isSelected = false
            }

            2 -> {
                viewBinding.buttonDay.isSelected = true
                viewBinding.buttonWeek.isSelected = false
                viewBinding.buttonMonth.isSelected = false
            }
        }
        type?.let { profileViewModel.getStatistics(it) }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel.setDays()


    }


}
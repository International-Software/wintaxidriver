package com.example.taxi.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.taxi.R
import com.example.taxi.custom.chart.ChartProgressBar
import com.example.taxi.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProfileFragment : Fragment() {


    private val profileViewModel: ProfileViewModel by viewModel()
    private lateinit var viewBinding: FragmentProfileBinding

    private lateinit var chartProgressBar: ChartProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentProfileBinding.inflate(inflater,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.statisticsType.observe(viewLifecycleOwner){
            setStatisticsType(it)
        }

        viewBinding.buttonWeek.setOnClickListener { profileViewModel.setWeeks() }
        viewBinding.buttonMonth.setOnClickListener { profileViewModel.setMonths() }
        viewBinding.buttonDay.setOnClickListener { profileViewModel.setDays() }

        chartProgressBar = view.findViewById(R.id.chartProgressBar)

        val dataList = List(30) { index ->
            Pair((50 + index % 50), "${index + 1}")
        }


        chartProgressBar.setData(dataList)


    }

    private fun setStatisticsType(type: Int?) {
        when(type){
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
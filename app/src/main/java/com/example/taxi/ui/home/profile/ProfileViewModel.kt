package com.example.taxi.ui.home.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.taxi.domain.exception.traceErrorException
import com.example.taxi.domain.usecase.main.GetMainResponseUseCase
import com.example.taxi.utils.Resource
import com.example.taxi.utils.ResourceState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ProfileViewModel(private val getMainResponseUseCase: GetMainResponseUseCase) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    private val _statisticType = MutableLiveData<Int>().apply {
        value = 2
    }

    val statisticsType: LiveData<Int> get() = _statisticType


    fun getStatistics(type: Int){
        compositeDisposable.add(
            getMainResponseUseCase.getStatisticsData(0)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe {
                    // Perform any setup tasks before the subscription starts
                }
                .doOnTerminate {
                    // Perform any cleanup tasks after the subscription ends
                }
                .subscribe(
                    { response ->
                        Log.d("xizmat ", "getService: $response")
//                        _serviceResponse.postValue(Resource(ResourceState.SUCCESS, response))
                    },
                    { error ->
                        Log.e("xizmat ", "getService: $error")

//                        _serviceResponse.postValue(
//                            Resource(
//                                ResourceState.ERROR,
//                                message = traceErrorException(error).getErrorMessage()
//                            )
//                        )
                    }
                )
        )
    }


    fun setWeeks() {
        _statisticType.postValue(1)
    }

    fun setDays() {
        _statisticType.postValue(2)
    }

    fun setMonths() {
        _statisticType.postValue(0)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}
package com.assessmenttest.models

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.assessmenttest.R
import com.assessmenttest.database.AppDatabase
import com.assessmenttest.models.directionapi.DirectionApiResponse
import com.assessmenttest.ui.MainApp
import com.assessmenttest.webservice.IPlacesRestClient
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import kotlin.coroutines.CoroutineContext

class TravellingViewModel : ViewModel(), CoroutineScope {

    val isApiCalling = MutableLiveData<Boolean>().apply { value = false }

    private val job = Job()
    var travelDataList: MutableLiveData<MutableList<TravellingData>> = MutableLiveData()
    var allDateList: MutableLiveData<MutableList<TravellingData>> = MutableLiveData()

    var directionApiResponse: MutableLiveData<DirectionApiResponse> = MutableLiveData()

    var listOfTravellingDates =
        ObservableField<MutableList<TravellingData>>().apply { set(ArrayList()) }
    var listOfTravellingDetail =
        ObservableField<MutableList<TravellingData>>().apply { set(ArrayList()) }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    fun getTravelDates(date: String) {

        isApiCalling.value=true
        launch(Dispatchers.Main) {
            val list: MutableList<TravellingData> = async(Dispatchers.IO) {
                AppDatabase.getDatabase(MainApp.getContext()).travelDao()
                    ?.getTravelListByDate(date) as ArrayList<TravellingData>
            }.await()
            travelDataList.value = list
            isApiCalling.value=false
        }
    }


    fun getDirection(startLatLng: String, endLatLng: String) {

        IPlacesRestClient.restClient.getSmartRoute(
            startLatLng,
            endLatLng,
            MainApp.getContext().resources.getString(R.string.api_key)
        )
            .enqueue(object : Callback<DirectionApiResponse> {
                override fun onResponse(
                    call: Call<DirectionApiResponse>,
                    response: retrofit2.Response<DirectionApiResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        directionApiResponse.value = response.body()
                    } else {

                    }
                }

                override fun onFailure(call: Call<DirectionApiResponse>, t: Throwable) {

                }
            })
    }

    fun fetchTravellingData() {
        GlobalScope.launch(Dispatchers.IO) {
            var list = AppDatabase.getDatabase(MainApp.getContext()).travelDao()?.getDates()!!
            listOfTravellingDates.set(list)
        }
    }

    fun fetchAllData() {
        GlobalScope.launch(Dispatchers.IO) {
            var list = AppDatabase.getDatabase(MainApp.getContext()).travelDao()?.getAll()!!
            allDateList.postValue(list)
        }
    }
}
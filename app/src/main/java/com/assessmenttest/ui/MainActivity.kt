package com.assessmenttest.ui

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessmenttest.R
import com.assessmenttest.common.LastAdapter
import com.assessmenttest.database.AppDatabase
import com.assessmenttest.databinding.ActivityMainBinding
import com.assessmenttest.extensions.bindViewModel
import com.assessmenttest.extensions.replaceFragment
import com.assessmenttest.helper.AppPreferences
import com.assessmenttest.models.TravellingData
import com.assessmenttest.models.TravellingViewModel
import com.assessmenttest.ui.fragments.TravellingDetailFragment
import com.assessmenttest.utility.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val viewModel by bindViewModel<TravellingViewModel>()

    lateinit var binding: ActivityMainBinding
    lateinit var textView: AppCompatTextView
    lateinit var activity: Activity
    lateinit var travelAdapter: LastAdapter<TravellingData>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        init()
        setRecyclerview()
        GlobalScope.launch(Dispatchers.IO) {
            var list = AppDatabase.getDatabase(activity).travelDao()?.getDates()!!
            viewModel.listOfTravellingDates.set(list)
        }
    }

    // setup recyclerview by plotting all dates fetched from db
    private fun setRecyclerview() {
        val layoutManager = LinearLayoutManager(activity)
        binding.rvTravel.layoutManager = layoutManager
        binding.rvTravel.setHasFixedSize(true)
        travelAdapter = LastAdapter(
            R.layout.layout_row_travel_date,
            object : LastAdapter.OnItemClickListener<TravellingData> {
                override fun onItemClick(model: TravellingData) {

                    replaceFragment(
                        R.id.container,
                        TravellingDetailFragment.newInstance(model.date.toString()),
                        true
                    )
                }
            })

        binding.rvTravel.adapter = travelAdapter
    }

    private fun init() {
        activity = this
        binding.viewmodel = viewModel

        if (!AppPreferences.isFirstTime) {
            Utils.importCSV()
            AppPreferences.isFirstTime = true
        }
    }
}

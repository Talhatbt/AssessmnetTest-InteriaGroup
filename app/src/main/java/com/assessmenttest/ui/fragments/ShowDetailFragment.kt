package com.assessmenttest.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessmenttest.R
import com.assessmenttest.common.LastAdapter
import com.assessmenttest.common.SimpleCallback
import com.assessmenttest.constants.Consts
import com.assessmenttest.databinding.FragmentShowDetailBinding
import com.assessmenttest.extensions.replaceFragment
import com.assessmenttest.helper.CallDialogs
import com.assessmenttest.models.TravelDestinations
import com.assessmenttest.models.TravellingData
import com.assessmenttest.models.TravellingViewModel
import com.assessmenttest.utility.Utils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class ShowDetailFragment : Fragment() {


    var listToSend = ArrayList<TravelDestinations>()
    lateinit var binding: FragmentShowDetailBinding
    lateinit var viewModel: TravellingViewModel
    var travellingList: MutableList<TravelDestinations> = ArrayList()
    lateinit var destinationAdapter: LastAdapter<TravelDestinations>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentShowDetailBinding.inflate(inflater, container, false).apply {
            binding = this
        }.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setListener()
        setRecyclerview()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setListener() {

        binding.btnRouteDetail.setOnClickListener {
            replaceFragment(
                R.id.container,
                ShortestDistanceDetailWiseFragment.newInstance(listToSend,
                    Consts.ViewType.SORTED_BY_SHORTEST_PATH,
                    object : SimpleCallback<MutableList<TravelDestinations>> {

                        override fun onCallBack(list: MutableList<TravelDestinations>) {
                            travellingList = list
                        }
                    }),
                true
            )
        }

        binding.btnTravelDetail.setOnClickListener {
            replaceFragment(
                R.id.container,
                ShortestDistanceDetailWiseFragment.newInstance(
                    listToSend,
                    Consts.ViewType.SORTED_DISTANCE_WISE,
                    object : SimpleCallback<MutableList<TravelDestinations>> {

                        override fun onCallBack(list: MutableList<TravelDestinations>) {
                            travellingList = list
                        }
                    }),
                true
            )
        }


        /// Observer for getting the date list
        viewModel.allDateList.observe(requireActivity(), Observer {

            if (it.isNotEmpty()) {
                var travelList = ArrayList<TravellingData>()

                GlobalScope.launch {

                    // divide list in chunks in order to load the data of around 500 rows to reduce the time
                    var chunkSize = it.size / 10
                    var subLists: List<List<TravellingData>> = it.chunked(chunkSize)
                    subLists.parallelStream().forEach { list ->

                        list.forEachIndexed { index, element ->
                            var points =
                                Utils.getLocationFromAddress(activity, it[index].street)
                            element.geoPoints = points
                            element.date = element.date?.let { date -> Utils.parseDate(date) }
                            travelList.addAll(listOf(element))
                            Log.e("index", index.toString())
                        }
                    }

                    travelList.sortBy { item -> item.date }
                    var list = calculateDistance(travelList)
                    listToSend = list as ArrayList<TravelDestinations>
                    Log.e("list size", travelList.size.toString())

                    requireActivity().runOnUiThread {
                        destinationAdapter.items = list
                        destinationAdapter.notifyDataSetChanged()
                        CallDialogs.INSTANCE.dismissDialog()
                    }
                }
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(
            viewModel: TravellingViewModel
        ): ShowDetailFragment {
            val fragment = ShowDetailFragment()
            fragment.viewModel = viewModel
            return fragment
        }
    }

    /*
    *  param list
    * Calculate distance between current and previous index, by passing the list
    * */
    private fun calculateDistance(list: MutableList<TravellingData>): MutableList<TravelDestinations> {
        var location = ""
        var totalSize = list.size!!
        var origin = ""
        var destination = ""
        var destinationList = ArrayList<TravelDestinations>()
        list?.forEachIndexed { index, element ->
            if (element.id?.toInt()!! < totalSize!!) {
                var distanceCovered =
                    list.let {
                        Utils.calculateDistanceBetweenDestinations(
                            it, element.id!!.toInt() - 1
                        )
                    }

                var distance =
                    DecimalFormat("##.##").format(distanceCovered?.div(1000.0)).toDouble()
                        .toString()

                origin = list[element.id!!.toInt() - 1].city.toString()
                destination = list[element.id!!.toInt()].city.toString()

                destinationList.add(
                    TravelDestinations(
                        element.date,
                        origin,
                        destination,
                        element.street,
                        distance
                    )
                )
            }
        }

        return destinationList
    }


    private fun setRecyclerview() {
        val layoutManager = LinearLayoutManager(activity)
        binding.rvTravelHistory.layoutManager = layoutManager
        binding.rvTravelHistory.setHasFixedSize(true)
        destinationAdapter = LastAdapter(
            R.layout.layout_row_travel_detail_history,
            object : LastAdapter.OnItemClickListener<TravelDestinations> {
                override fun onItemClick(model: TravelDestinations) {

                }
            })

        binding.rvTravelHistory.adapter = destinationAdapter
    }


    override fun onPause() {
        super.onPause()
        viewModel.allDateList.removeObservers(this)
    }

    private fun init() {
        if (travellingList.isEmpty()) {
            CallDialogs.INSTANCE.showLoader(requireContext())
            viewModel.fetchAllData()
        } else destinationAdapter.items = travellingList

    }
}
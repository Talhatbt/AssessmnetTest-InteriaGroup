package com.assessmenttest.ui.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessmenttest.R
import com.assessmenttest.common.LastAdapter
import com.assessmenttest.common.SimpleCallback
import com.assessmenttest.constants.Consts
import com.assessmenttest.databinding.FragmentShortestDistanceWiseBinding
import com.assessmenttest.models.DestinationModel
import com.assessmenttest.models.TravelDestinations
import com.assessmenttest.models.TravellingViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class ShortestDistanceDetailWiseFragment : Fragment() {

    lateinit var binding: FragmentShortestDistanceWiseBinding
    lateinit var viewModel: TravellingViewModel

    lateinit var viewType: String
    lateinit var callBack: SimpleCallback<MutableList<TravelDestinations>>
    lateinit var travelList: MutableList<TravelDestinations>
    lateinit var returnList: MutableList<TravelDestinations>
    lateinit var destinationAdapter: LastAdapter<DestinationModel>
    lateinit var distanceAdapter: LastAdapter<TravelDestinations>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentShortestDistanceWiseBinding.inflate(inflater, container, false).apply {
            binding = this
        }.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if (viewType == Consts.ViewType.SORTED_BY_SHORTEST_PATH) {
            setupShortestPathTravelledRecyclerview()
            processShortestPathData()
            binding.llRow1.visibility = View.VISIBLE
            binding.llRow2.visibility = View.GONE

        } else {
            setupDistanceWiseRecyclerview()
            processDistanceWiseData()
            binding.llRow1.visibility = View.GONE
            binding.llRow2.visibility = View.VISIBLE
        }
    }


    companion object {
        @JvmStatic
        fun newInstance(
            travelList: MutableList<TravelDestinations>,
            viewType: String, callBack: SimpleCallback<MutableList<TravelDestinations>>
        ): ShortestDistanceDetailWiseFragment {
            val fragment = ShortestDistanceDetailWiseFragment()
            fragment.travelList = travelList
            fragment.viewType = viewType
            fragment.callBack = callBack
            fragment.returnList = travelList
            return fragment
        }
    }


    private fun setupShortestPathTravelledRecyclerview() {
        val layoutManager = LinearLayoutManager(activity)
        binding.rvTravelHistory.layoutManager = layoutManager
        binding.rvTravelHistory.setHasFixedSize(true)
        destinationAdapter = LastAdapter(
            R.layout.layout_row_shortest_path_wise,
            object : LastAdapter.OnItemClickListener<DestinationModel> {
                override fun onItemClick(model: DestinationModel) {

                }
            })

        binding.rvTravelHistory.adapter = destinationAdapter
    }


    private fun setupDistanceWiseRecyclerview() {
        val layoutManager = LinearLayoutManager(activity)
        binding.rvTravelHistory.layoutManager = layoutManager
        binding.rvTravelHistory.setHasFixedSize(true)
        distanceAdapter = LastAdapter(
            R.layout.layout_row_travel_detail_city_wise,
            object : LastAdapter.OnItemClickListener<TravelDestinations> {
                override fun onItemClick(model: TravelDestinations) {

                }
            })

        binding.rvTravelHistory.adapter = distanceAdapter
    }

    private fun processShortestPathData() {

        GlobalScope.launch {

            // sort list by date
            var uniqueList = travelList.distinctBy { it.date }

            var distanceList = ArrayList<DestinationModel>()
            uniqueList.forEachIndexed { index, element ->
                var datesList: List<TravelDestinations> =
                    travelList.filter { item -> item.date == element.date?.let { it } }

                var distance = calculateTotalDistance(datesList as MutableList<TravelDestinations>)

                var formattedValue = DecimalFormat("##.##").format(distance.toDouble())
                distanceList.add(DestinationModel(element.date, formattedValue))
                Log.e("list 1", datesList.size.toString())
                Log.e("distance", formattedValue)
            }

            requireActivity().runOnUiThread {
                destinationAdapter.items = distanceList
            }
        }
    }

    private fun processDistanceWiseData() {

        travelList.sortBy { it.distance?.toDouble() }
        distanceAdapter.items = travelList
    }

    private fun calculateTotalDistance(list: MutableList<TravelDestinations>): String {
//        Log.e("list item", (list[0].distance+list[1].distance))
        return list.sumByDouble { it.distance!!.toDouble() }.toString()
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        returnList.sortBy { it.date }
        callBack.onCallBack(returnList)
    }
}
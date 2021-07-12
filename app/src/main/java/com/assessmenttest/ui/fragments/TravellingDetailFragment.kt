package com.assessmenttest.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.assessmenttest.R
import com.assessmenttest.common.LastAdapter
import com.assessmenttest.common.SimpleCallback
import com.assessmenttest.constants.Consts
import com.assessmenttest.databinding.FragmentTravellingDetailBinding
import com.assessmenttest.extensions.bindViewModel
import com.assessmenttest.extensions.popStack
import com.assessmenttest.extensions.replaceFragment
import com.assessmenttest.helper.CallDialogs
import com.assessmenttest.models.TravellingData
import com.assessmenttest.models.TravellingViewModel
import com.assessmenttest.utility.Utils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.text.DecimalFormat

class TravellingDetailFragment : Fragment() {

    lateinit var travelDetailsAdapter: LastAdapter<TravellingData>
    private val viewModel by bindViewModel<TravellingViewModel>()

    lateinit var callback: SimpleCallback<Any>
    private var mMap: GoogleMap? = null
    lateinit var binding: FragmentTravellingDetailBinding
    var mapFragment: SupportMapFragment? = null
    lateinit var date: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentTravellingDetailBinding.inflate(inflater, container, false).apply {
            binding = this
            binding.viewmodel = viewModel
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().toolbar.visibility = View.GONE
        binding.distance = getString(R.string.loading)
        setListener()
        setRecyclerview()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            checkLocationPermission()
        }

//        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        viewModel.getTravelDates(date)
        binding.title = date
    }

    private fun setListener() {

        viewModel.isApiCalling.observe(requireActivity(), Observer {
            if (it) CallDialogs.INSTANCE.showLoader(activity)
            else CallDialogs.INSTANCE.dismissDialog()
        })


        // set observer for travel list data
        viewModel.travelDataList.observe(requireActivity(), Observer {
            if (it.isNotEmpty()) {

                var travelList = ArrayList<TravellingData>()
                it.forEachIndexed { index, element ->
                    var points = Utils.getLocationFromAddress(requireContext(), it[index].street)
                    element.geoPoints = points

                    element.id = index.toLong() + 1
                    travelList.addAll(listOf(element))
                }

                // Call function to setup markers on available LatLng
//                setMarkersOnGoogleMap(travelList)

                viewModel.listOfTravellingDetail.set(travelList)

                // will get total distance covered on specific date
                var startingLocationStreet = travelList[0].street
                var endingLocationStreet = travelList[travelList.size - 1].street

                var startingPoint = travelList[0].geoPoints
                var endingPoint = travelList[travelList.size - 1].geoPoints

                var totalDistance = startingPoint?.let { startPoint ->
                    endingPoint?.let { endPoint ->
                        Utils.calculateDistance(
                            startPoint,
                            endPoint,
                            startingLocationStreet.toString(),
                            endingLocationStreet.toString()
                        )
                    }
                }

                // Formatted to 2 decimal places
                var value = DecimalFormat("##.##").format(totalDistance?.div(1000.0)).toDouble()
                binding.distance = "$value km"
                Log.e("total covered", totalDistance.toString())
                showDetail()
            }
        })

        binding.btnDrawRoute.setOnClickListener {
            replaceFragment(R.id.container, DrawRouteFragment.newInstance(viewModel), true)
        }

        binding.btnBack.setOnClickListener {
            popStack()
        }
    }

//    override fun onMapReady(map: GoogleMap?) {
//        mMap = map
//        mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
//        mMap?.uiSettings?.isZoomControlsEnabled = true
//        mMap?.uiSettings?.isZoomGesturesEnabled = true
//        mMap?.uiSettings?.isCompassEnabled = true
//        //Initialize Google Play Services
//        if (ContextCompat.checkSelfPermission(
//                requireActivity(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            mMap?.isMyLocationEnabled = true
//        }
//    }


    @SuppressLint("RestrictedApi")
    override fun onResume() {
        super.onResume()
//        if (Utils.isLocationEnabled(requireContext())) {
//            mapFragment?.getMapAsync(this)
//        } else {
//            displayPromptForEnablingGPS(requireActivity())
//        }
    }

    private fun displayPromptForEnablingGPS(activity: Activity) {
        val builder = AlertDialog.Builder(activity)
        val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        val message = "Do you want open GPS setting?"
        builder.setMessage(message)
            .setPositiveButton(
                "OK"
            ) { d, id ->
                activity.startActivity(Intent(action))
                d.dismiss()
            }
            .setNegativeButton(
                "Cancel"
            ) { d, id -> d.cancel() }
        builder.create().show()
    }

    private fun checkLocationPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Consts.PERMISSIONS_REQUEST_LOCATION
                )
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Consts.PERMISSIONS_REQUEST_LOCATION
                )
            }
            false
        } else true
    }

    private fun setRecyclerview() {
        val layoutManager = LinearLayoutManager(activity)
        binding.rvDetails.layoutManager = layoutManager
        binding.rvDetails.setHasFixedSize(true)
        travelDetailsAdapter = LastAdapter(
            R.layout.layout_row_travel_detail,
            object : LastAdapter.OnItemClickListener<TravellingData> {
                override fun onItemClick(model: TravellingData) {
                }
            })

        binding.rvDetails.adapter = travelDetailsAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(date: String, callback: SimpleCallback<Any>): TravellingDetailFragment {
            val fragment = TravellingDetailFragment()
            fragment.date = date
            fragment.callback = callback
            return fragment
        }
    }

    private fun setMarkersOnGoogleMap(listOfPoints: MutableList<TravellingData>) {

        for (data in listOfPoints) {
            val markerOptions = MarkerOptions()
            // Setting the position for the marker
            data.geoPoints?.let { markerOptions.position(it) }

            markerOptions.title(data.postalCode)
//            mMap?.clear()
            mMap?.animateCamera(CameraUpdateFactory.newLatLng(data.geoPoints))

            val circleDrawable: Drawable = resources.getDrawable(R.drawable.circle_shape)
            val markerIcon: BitmapDescriptor? = Utils.getMarkerIconFromDrawable(circleDrawable)

            markerOptions.title(data.id.toString())
            markerOptions.icon(markerIcon)
            mMap?.addMarker(markerOptions)
        }

        Handler().postDelayed({
            listOfPoints[0].geoPoints?.let { moveCamera(it) }
            drawPolyLine(listOfPoints)
        }, 3000)
    }

    private fun drawPolyLine(listOfPoints: MutableList<TravellingData>) {

        val options: PolylineOptions =
            PolylineOptions().width(Utils.dpToPx(5).toFloat()).color(Color.RED)
        for (z in 0 until listOfPoints.size) {
            val point: LatLng = listOfPoints[z].geoPoints!!
            options.add(point)
        }
        mMap?.addPolyline(options)
    }

    private fun moveCamera(latLng: LatLng) {
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(6f))
    }

    fun showDetail() {

        var distanceCovered = 0.0
        var totalSize = viewModel.listOfTravellingDetail.get()?.size!!

        var location = ""

        viewModel.listOfTravellingDetail.get()?.forEachIndexed { index, element ->
            if (element.id?.toInt()!! < totalSize!!) {
                distanceCovered =
                    viewModel.listOfTravellingDetail.get()?.let {
                        Utils.calculateDistanceBetweenDestinations(
                            it, element.id!!.toInt() - 1
                        )
                    }!!

                var value =
                    DecimalFormat("##.##").format(distanceCovered?.div(1000.0)).toDouble()
                        .toString() + " km"

                location = location +
                        viewModel.listOfTravellingDetail.get()!![element.id!!.toInt() - 1].city + " - " +
                        viewModel.listOfTravellingDetail.get()!![element.id!!.toInt()].city + " - $value \n"
            }
        }
        binding.tvDetails.text = location
    }

    override fun onDetach() {
        super.onDetach()
        callback.onCallBack(ArrayList())
    }
}
package com.assessmenttest.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
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
import com.assessmenttest.constants.Consts
import com.assessmenttest.databinding.FragmentTravellingDetailBinding
import com.assessmenttest.extensions.bindViewModel
import com.assessmenttest.extensions.popStack
import com.assessmenttest.extensions.replaceFragment
import com.assessmenttest.helper.CallDialogs
import com.assessmenttest.models.TravellingData
import com.assessmenttest.models.TravellingViewModel
import com.assessmenttest.utility.Utils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.text.DecimalFormat

class TravellingDetailFragment : Fragment(), OnMapReadyCallback, ConnectionCallbacks,
    OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener {

    lateinit var travelDetailsAdapter: LastAdapter<TravellingData>
    private val viewModel by bindViewModel<TravellingViewModel>()

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

        binding.distance = getString(R.string.loading)
        setListener()
        setRecyclerview()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission()
        }

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        viewModel.getTravelDates(date)
        binding.title = date
    }

    private fun setListener() {

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
                setMarkersOnGoogleMap(travelList)

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
            }
        })

        binding.btnDrawRoute.setOnClickListener {
            replaceFragment(R.id.container, DrawRouteFragment.newInstance(viewModel), true)
        }

        binding.btnBack.setOnClickListener {
            popStack()
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        mMap = map
        mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isZoomGesturesEnabled = true
        mMap?.uiSettings?.isCompassEnabled = true
        //Initialize Google Play Services
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap?.isMyLocationEnabled = true
        }
    }

    override fun onConnected(p0: Bundle?) {
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onLocationChanged(p0: Location?) {
    }

    @SuppressLint("RestrictedApi")
    override fun onResume() {
        super.onResume()
        if (Utils.isLocationEnabled(requireContext())) {
            mapFragment?.getMapAsync(this)
        } else {
            displayPromptForEnablingGPS(requireActivity())
        }
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

                    var distanceCovered = 0.0
                    var totalSize = viewModel.listOfTravellingDetail.get()?.size!!
                    if (model.id?.toInt()!! < totalSize!!) {
                        distanceCovered =
                            viewModel.listOfTravellingDetail.get()?.let {
                                Utils.calculateDistanceBetweenDestinations(
                                    it, model.id!!.toInt()-1
                                )
                            }!!

                        var value =
                            DecimalFormat("##.##").format(distanceCovered?.div(1000.0)).toDouble()
                                .toString() + "km"

                        var location =
                            viewModel.listOfTravellingDetail.get()!![model.id!!.toInt()-1].city + " - " +
                                    viewModel.listOfTravellingDetail.get()!![model.id!!.toInt()].city
                        CallDialogs.INSTANCE.showDistanceDialog(requireContext(),
                            value.toString(),
                            location,
                            View.OnClickListener {
                                CallDialogs.INSTANCE.dismissDialog()
                            })
                    }
                }
            })

        binding.rvDetails.adapter = travelDetailsAdapter
    }

    companion object {
        @JvmStatic
        fun newInstance(date: String): TravellingDetailFragment {

            val fragment = TravellingDetailFragment()
            fragment.date = date
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
}
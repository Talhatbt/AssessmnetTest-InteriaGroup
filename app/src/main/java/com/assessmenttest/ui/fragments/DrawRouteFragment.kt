package com.assessmenttest.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.assessmenttest.R
import com.assessmenttest.constants.Consts
import com.assessmenttest.databinding.FragmentDrawRouteBinding
import com.assessmenttest.models.TravellingData
import com.assessmenttest.models.TravellingViewModel
import com.assessmenttest.models.directionapi.DirectionApiResponse
import com.assessmenttest.utility.Utils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class DrawRouteFragment : Fragment(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    com.google.android.gms.location.LocationListener {

    private val mRouteLatLng: MutableList<LatLng> = java.util.ArrayList()
    private var mapPolylines: Polyline? = null
    private var mMap: GoogleMap? = null
    lateinit var binding: FragmentDrawRouteBinding
    lateinit var viewModel: TravellingViewModel
    var mapFragment: SupportMapFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentDrawRouteBinding.inflate(inflater, container, false).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        init()
        setListener()
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
        }
    }

    private fun onRoutingSuccess(response: DirectionApiResponse) {
        Log.e("CurvedPolyline", "onRoutingSuccess")
        if (requireActivity() != null && mMap != null && response.isOK
            && response.routeList != null && response.routeList.size > 0
        ) {
            for (route in response.routeList[0].legList) {
                val currentLatLngList: java.util.ArrayList<LatLng> = route.directionPoint
                mRouteLatLng.addAll(currentLatLngList)
            }
            if (mapPolylines != null) {
                mapPolylines?.remove()
            }
            val polyOptions = PolylineOptions()
            polyOptions.color(ContextCompat.getColor(requireContext(), R.color.orange_red))
            polyOptions.width(Utils.dpToPx(5).toFloat())
            polyOptions.addAll(mRouteLatLng)
            mapPolylines = mMap?.addPolyline(polyOptions)
        } else {
        }
    }

    private fun setListener() {

        var listSize = viewModel.listOfTravellingDetail.get()?.size!! - 1
        var start = viewModel.listOfTravellingDetail.get()?.get(0)?.geoPoints
        var end = viewModel.listOfTravellingDetail.get()?.get(listSize)?.geoPoints

        // passing start LATLNG and end LATLNG to get the direction route
        viewModel.getDirection(
            start?.latitude.toString() + Consts.Character.COMMA + start?.longitude,
            end?.latitude.toString() + Consts.Character.COMMA + end?.longitude
        )

        // set observer to get the direction api response
        viewModel.directionApiResponse.observe(requireActivity(), Observer {
            if (it != null) {
                onRoutingSuccess(it)
                Handler().postDelayed({
                    viewModel.listOfTravellingDetail.get()!![0].geoPoints?.let { moveCamera(it) }
                    setMarkersOnGoogleMap(viewModel.listOfTravellingDetail.get()!!)
                }, 3000)
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance(viewModel: TravellingViewModel): DrawRouteFragment {

            val fragment = DrawRouteFragment()
            fragment.viewModel = viewModel
            return fragment
        }
    }

    private fun setMarkersOnGoogleMap(listOfPoints: MutableList<TravellingData>) {

        var size=listOfPoints.size-1
        val markerOptionStart = MarkerOptions()
        listOfPoints[0].geoPoints?.let { markerOptionStart.position(it) }
        markerOptionStart.title(listOfPoints[0].postalCode)
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(listOfPoints[0].geoPoints))
        var circleDrawable: Drawable = resources.getDrawable(R.drawable.circle_shape)
        var markerIcon: BitmapDescriptor? = Utils.getMarkerIconFromDrawable(circleDrawable)
        markerOptionStart.title(listOfPoints[0].id.toString())
        markerOptionStart.icon(markerIcon)
        mMap?.addMarker(markerOptionStart)

        val markerOptionEnd = MarkerOptions()
        listOfPoints[size].geoPoints?.let { markerOptionEnd.position(it) }
        markerOptionEnd.title(listOfPoints[size].postalCode)
        mMap?.animateCamera(CameraUpdateFactory.newLatLng(listOfPoints[size].geoPoints))
        circleDrawable = resources.getDrawable(R.drawable.circle_shape)
        markerIcon = Utils.getMarkerIconFromDrawable(circleDrawable)
        markerOptionEnd.title(listOfPoints[size].id.toString())
        markerOptionEnd.icon(markerIcon)
        mMap?.addMarker(markerOptionEnd)
    }


    private fun moveCamera(latLng: LatLng) {
        mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap?.animateCamera(CameraUpdateFactory.zoomTo(6f))
    }
}
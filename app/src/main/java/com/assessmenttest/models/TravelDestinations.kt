package com.assessmenttest.models

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.assessmenttest.database.AppDatabase
import com.assessmenttest.ui.MainApp
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

data class TravelDestinations(
    var date: String? = "",
    var originCity: String? = "",
    var destinationCity: String? = "",
    var address: String? = "",
    var distance: String? = ""
)

data class DestinationModel(
    var date: String? = "",
    var distance: String? = ""
)

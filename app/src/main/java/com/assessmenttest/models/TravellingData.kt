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

@Entity(tableName = "travelling")
class TravellingData(
    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    var id: Long? = 0,

    @NonNull
    @ColumnInfo(name = "date")
    var date: String? = "",

    @NonNull
    @ColumnInfo(name = "street")
    var street: String? = "",

    @NonNull
    @ColumnInfo(name = "postal_code")
    var postalCode: String? = "",

    @NonNull
    @ColumnInfo(name = "city")
    var city: String? = "",

    @NonNull
    @ColumnInfo(name = "extra")
    var extra: String? = "",

    @Ignore
    var geoPoints: LatLng? = null,

    @Ignore
    var totalDistanceCovered: Double? = 0.0
) {

    suspend fun getTravelDataByDate(date: String): MutableList<TravellingData> {
        var list = ArrayList<TravellingData>()
//        fetchData(date)
        return list
    }


    suspend fun fetchData(date: String): MutableList<TravellingData> {
        return GlobalScope.async {
            AppDatabase.getDatabase(MainApp.getContext()).travelDao()
                ?.getTravelListByDate(date) as ArrayList<TravellingData>
        }.await()
    }
}

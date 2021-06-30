package com.assessmenttest.models.directionapi;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model class for Polyline data
 */
@SuppressWarnings("WeakerAccess")
public class RoutePolyline implements Parcelable {
    @SerializedName("points")
    private String rawPointList;

    protected RoutePolyline(Parcel in) {
        rawPointList = in.readString();
    }

    public List<LatLng> getPointList() {
        return DirectionConverter.decodePoly(rawPointList);
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(rawPointList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RoutePolyline> CREATOR = new Creator<RoutePolyline>() {
        @Override
        public RoutePolyline createFromParcel(Parcel in) {
            return new RoutePolyline(in);
        }

        @Override
        public RoutePolyline[] newArray(int size) {
            return new RoutePolyline[size];
        }
    };
}

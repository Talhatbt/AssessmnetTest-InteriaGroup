package com.assessmenttest.models.directionapi;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * Model class for Coordinates in Google Direction API response
 */
@SuppressWarnings("WeakerAccess")
public class Coordination implements Parcelable {
    @SerializedName("lat")
    private double latitude;
    @SerializedName("lng")
    private double longitude;

    public Coordination() {
    }

    protected Coordination(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getCoordination() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Coordination> CREATOR = new Creator<Coordination>() {
        @Override
        public Coordination createFromParcel(Parcel in) {
            return new Coordination(in);
        }

        @Override
        public Coordination[] newArray(int size) {
            return new Coordination[size];
        }
    };
}

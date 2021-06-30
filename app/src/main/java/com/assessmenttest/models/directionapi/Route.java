package com.assessmenttest.models.directionapi;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model class for Routes data
 */
@SuppressWarnings("WeakerAccess")
public class Route implements Parcelable {
    @SerializedName("legs")
    private List<Leg> legList;

    public Route() {
    }


    protected Route(Parcel in) {
        legList = in.createTypedArrayList(Leg.CREATOR);
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    public List<Leg> getLegList() {
        return legList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(legList);
    }
}

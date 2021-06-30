package com.assessmenttest.models.directionapi;

import android.os.Parcel;
import android.os.Parcelable;


import com.assessmenttest.constants.Consts;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectionApiResponse implements Parcelable {

    @SerializedName("routes")
    private List<Route> routeList;
    private String status;

    private DirectionApiResponse(Parcel in) {
        routeList = in.createTypedArrayList(Route.CREATOR);
        status = in.readString();
    }

    public static final Creator<DirectionApiResponse> CREATOR = new Creator<DirectionApiResponse>() {
        @Override
        public DirectionApiResponse createFromParcel(Parcel in) {
            return new DirectionApiResponse(in);
        }

        @Override
        public DirectionApiResponse[] newArray(int size) {
            return new DirectionApiResponse[size];
        }
    };

    public List<Route> getRouteList() {
        return routeList;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public boolean isOK() {
        return Consts.ApiStatusCode.REQUEST_RESULT_OK.equals(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(routeList);
        parcel.writeString(status);
    }
}

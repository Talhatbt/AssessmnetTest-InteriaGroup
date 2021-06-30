package com.assessmenttest.models.directionapi;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model class for steps data
 */
@SuppressWarnings("WeakerAccess")
public class Step implements Parcelable {
    private Info distance;
    private Info duration;
    @SerializedName("end_location")
    private Coordination endLocation;
    @SerializedName("start_location")
    private Coordination startLocation;
    @SerializedName("steps")
    private List<Step> stepList;
    private RoutePolyline polyline;

    protected Step(Parcel in) {
        distance = in.readParcelable(Info.class.getClassLoader());
        duration = in.readParcelable(Info.class.getClassLoader());
        endLocation = in.readParcelable(Coordination.class.getClassLoader());
        startLocation = in.readParcelable(Coordination.class.getClassLoader());
        stepList = in.createTypedArrayList(Step.CREATOR);
    }

    public Info getDistance() {
        return distance;
    }

    public Info getDuration() {
        return duration;
    }

    public Coordination getEndLocation() {
        return endLocation;
    }

    public Coordination getStartLocation() {
        return startLocation;
    }

    public RoutePolyline getPolyline() {
        return polyline;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(distance, flags);
        dest.writeParcelable(duration, flags);
        dest.writeParcelable(endLocation, flags);
        dest.writeParcelable(startLocation, flags);
        dest.writeTypedList(stepList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Step> CREATOR = new Creator<Step>() {
        @Override
        public Step createFromParcel(Parcel in) {
            return new Step(in);
        }

        @Override
        public Step[] newArray(int size) {
            return new Step[size];
        }
    };
}

package com.assessmenttest.models.directionapi;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class for Legs data
 */
public class Leg implements Parcelable {
    private Info distance;
    private Info duration;

    @SerializedName("steps")
    private List<Step> stepList;

    public Info getDistance() {
        return distance;
    }

    public Info getDuration() {
        return duration;
    }

    public List<Step> getStepList(){
        return stepList;
    }

    protected Leg(Parcel in) {
        distance = in.readParcelable(Info.class.getClassLoader());
        duration = in.readParcelable(Info.class.getClassLoader());
        stepList = in.createTypedArrayList(Step.CREATOR);
    }

    public static final Creator<Leg> CREATOR = new Creator<Leg>() {
        @Override
        public Leg createFromParcel(Parcel in) {
            return new Leg(in);
        }

        @Override
        public Leg[] newArray(int size) {
            return new Leg[size];
        }
    };

    public ArrayList<LatLng> getDirectionPoint() {
        return DirectionConverter.getDirectionPoint(stepList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(distance, i);
        parcel.writeParcelable(duration, i);
        parcel.writeTypedList(stepList);
    }
}

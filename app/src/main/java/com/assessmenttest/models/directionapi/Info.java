package com.assessmenttest.models.directionapi;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Model class for duration & distance data in Directions API response
 */
@SuppressWarnings("WeakerAccess")
public class Info implements Parcelable {
    private String text;
    private int value;

    public Info() {
    }

    protected Info(Parcel in) {
        text = in.readString();
        value = in.readInt();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeInt(value);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Info> CREATOR = new Creator<Info>() {
        @Override
        public Info createFromParcel(Parcel in) {
            return new Info(in);
        }

        @Override
        public Info[] newArray(int size) {
            return new Info[size];
        }
    };
}

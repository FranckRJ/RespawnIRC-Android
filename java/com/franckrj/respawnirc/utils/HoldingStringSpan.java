package com.franckrj.respawnirc.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

public class HoldingStringSpan extends CharacterStyle implements ParcelableSpan {
    private String infoToHold = "";

    public static final Parcelable.Creator<HoldingStringSpan> CREATOR = new Parcelable.Creator<HoldingStringSpan>() {
        @Override
        public HoldingStringSpan createFromParcel(Parcel in) {
            return new HoldingStringSpan(in);
        }

        @Override
        public HoldingStringSpan[] newArray(int size) {
            return new HoldingStringSpan[size];
        }
    };

    public HoldingStringSpan(String stringToHold) {
        infoToHold = stringToHold;
    }

    public HoldingStringSpan(Parcel src) {
        infoToHold = src.readString();
    }

    public String getString() {
        return infoToHold;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        //rien
    }

    @Override
    public int getSpanTypeId() {
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(infoToHold);
    }
}

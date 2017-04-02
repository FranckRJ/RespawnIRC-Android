package com.franckrj.respawnirc.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;

public class ParcelableLongSparseStringArray extends LongSparseArray<String> implements Parcelable {
    public static final Parcelable.Creator<ParcelableLongSparseStringArray> CREATOR = new Parcelable.Creator<ParcelableLongSparseStringArray>() {
        @Override
        public ParcelableLongSparseStringArray createFromParcel(Parcel in) {
            return new ParcelableLongSparseStringArray(in);
        }

        @Override
        public ParcelableLongSparseStringArray[] newArray(int size) {
            return new ParcelableLongSparseStringArray[size];
        }
    };

    public ParcelableLongSparseStringArray() {
        //rien
    }

    private ParcelableLongSparseStringArray(Parcel in) {
        final int tmpSize = in.readInt();

        for (int i = 0; i < tmpSize; ++i) {
            long tmpKey = in.readLong();
            String tmpValue = in.readString();

            this.append(tmpKey, tmpValue);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        final int currentSize = this.size();
        out.writeInt(currentSize);

        for (int i = 0; i < currentSize; ++i) {
            out.writeLong(this.keyAt(i));
            out.writeString(this.valueAt(i));
        }
    }
}

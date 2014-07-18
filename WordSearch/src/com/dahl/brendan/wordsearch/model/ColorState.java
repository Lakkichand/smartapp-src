package com.dahl.brendan.wordsearch.model;

import android.os.Parcel;
import android.os.Parcelable;

public enum ColorState implements Parcelable {
	FOUND, SELECTED, NORMAL;
	public static final Parcelable.Creator<ColorState> CREATOR = new Parcelable.Creator<ColorState>() {
		public ColorState createFromParcel(Parcel in) {
			return ColorState.values()[in.readInt()];
		}

		public ColorState[] newArray(int size) {
			return new ColorState[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.ordinal());
	}
}

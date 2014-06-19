package com.jiubang.go.backup.pro.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 结果Bean
 * 
 * @author maiyongshen
 */
public class ResultBean implements Parcelable {
	public boolean result;
	public String title;
	public String desc;

	public ResultBean() {
		result = false;
		title = null;
		desc = null;
	}

	public ResultBean(Parcel parcel) {
		result = parcel.readInt() != 0;
		title = parcel.readString();
		desc = parcel.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(result ? 1 : 0);
		dest.writeString(title);
		dest.writeString(desc);
	}

	public static final Parcelable.Creator<ResultBean> CREATOR = new Parcelable.Creator<ResultBean>() {

		@Override
		public ResultBean createFromParcel(Parcel source) {
			return new ResultBean(source);
		}

		@Override
		public ResultBean[] newArray(int size) {
			return new ResultBean[size];
		}
	};
}
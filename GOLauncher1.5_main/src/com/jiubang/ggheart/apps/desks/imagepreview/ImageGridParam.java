package com.jiubang.ggheart.apps.desks.imagepreview;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 图片宫格参数
 * 
 * @author masanbing
 * 
 */

public class ImageGridParam implements Parcelable {
	public int mWidth;
	public int mHeight;
	public int mLeftPadding;
	public int mTopPadding;
	public int mRightPadding;
	public int mBottomPadding;

	public ImageGridParam() {
		mWidth = 0;
		mHeight = 0;

		mLeftPadding = 0;
		mTopPadding = 0;
		mRightPadding = 0;
		mBottomPadding = 0;
	}

	private ImageGridParam(Parcel parcel) {
		mWidth = parcel.readInt();
		mHeight = parcel.readInt();
		mLeftPadding = parcel.readInt();
		mTopPadding = parcel.readInt();
		mRightPadding = parcel.readInt();
		mBottomPadding = parcel.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mWidth);
		dest.writeInt(mHeight);
		dest.writeInt(mLeftPadding);
		dest.writeInt(mTopPadding);
		dest.writeInt(mRightPadding);
		dest.writeInt(mBottomPadding);
	}

	public static final Parcelable.Creator<ImageGridParam> CREATOR = new Parcelable.Creator<ImageGridParam>() {
		@Override
		public ImageGridParam createFromParcel(Parcel source) {
			return new ImageGridParam(source);
		}

		@Override
		public ImageGridParam[] newArray(int size) {
			return new ImageGridParam[size];
		}
	};
}
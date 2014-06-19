package com.jiubang.go.backup.pro.net.version;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 版本信息
 * 
 * @author maiyongshen
 */
public class VersionInfo implements Parcelable {
	public static final int ACTION_FORCE_UPDATE = 1; // 强制更新
	public static final int ACTION_PROMPT_TO_UPDATE = 2; // 提示更新
	public static final int ACTION_NORMAL_VERSION = 3; // 正常版本
	public static final int ACTION_SOFT_MAINTENANCE = 4; // 软件维护
	public static final int ACTION_SYSTEM_PROMPT = 5; // 系统提示
	public static final int ACTION_USER_DEFINED = 6; // 自定义

	public int mAction = 0; // 动作
	public String mTipInfo; // 提示信息
	public String mDownloadUrl; // 下载地址
	public String mLatestReleaseDate; // 最新版本的发布日期
	public String mLatestVersionNumber; // 最新版本的版本号
	public String mLatestReleaseNote; // 最新版本的描述信息

	public static final Parcelable.Creator<VersionInfo> CREATOR = new Parcelable.Creator<VersionInfo>() {
		@Override
		public VersionInfo createFromParcel(Parcel source) {
			return new VersionInfo(source);
		}

		@Override
		public VersionInfo[] newArray(int size) {
			return new VersionInfo[size];
		}
	};

	public VersionInfo() {

	}

	public VersionInfo(Parcel parcel) {
		mAction = parcel.readInt();
		mTipInfo = parcel.readString();
		mDownloadUrl = parcel.readString();
		mLatestReleaseDate = parcel.readString();
		mLatestVersionNumber = parcel.readString();
		mLatestReleaseNote = parcel.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mAction);
		dest.writeString(mTipInfo);
		dest.writeString(mDownloadUrl);
		dest.writeString(mLatestReleaseDate);
		dest.writeString(mLatestVersionNumber);
		dest.writeString(mLatestReleaseNote);
	}
}

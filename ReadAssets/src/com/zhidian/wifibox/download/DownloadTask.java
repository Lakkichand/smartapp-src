package com.zhidian.wifibox.download;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 下载任务
 * 
 * @author xiedezhi
 * 
 */
public class DownloadTask implements Parcelable {
	/**
	 * 下载未开始
	 */
	public static final int NOT_START = 23001;
	/**
	 * 下载中
	 */
	public static final int DOWNLOADING = 23002;
	/**
	 * 已暂停
	 */
	public static final int PAUSING = 23003;
	/**
	 * 下载完成
	 */
	public static final int COMPLETE = 23004;
	/**
	 * 下载失败
	 */
	public static final int FAIL = 23005;
	/**
	 * 安装中
	 */
	public static final int INSTALLING = 23006;
	/**
	 * 等待下载
	 */
	public static final int WAITING = 23007;
	/**
	 * 当前下载状态
	 */
	public int state = NOT_START;

	/**
	 * 任务唯一标示，用于统计
	 */
	public String unique = "";
	/**
	 * 下载地址，作为下载任务的唯一标示
	 */
	public String url = "";
	/**
	 * 已经下载的百分比
	 */
	public int alreadyDownloadPercent = 0;

	/**
	 * 盒子编号
	 */
	public String boxNum = "";
	public String code = "";
	public int versionCode = -1;
	public int rank = -1;// 正在下载第几个

	/**
	 * 该应用的大小，用在下载管理的显示
	 */
	public int size = 0;

	public String config = "";

	public DownloadTask() {
	}

	public DownloadTask(Parcel source) {
		unique = source.readString();
		state = source.readInt();
		url = source.readString();
		alreadyDownloadPercent = source.readInt();
		boxNum = source.readString();
		code = source.readString();
		versionCode = source.readInt();
		size = source.readInt();
		rank = source.readInt();
		config = source.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(unique);
		dest.writeInt(state);
		dest.writeString(url);
		dest.writeInt(alreadyDownloadPercent);
		dest.writeString(boxNum);
		dest.writeString(code);
		dest.writeInt(versionCode);
		dest.writeInt(size);
		dest.writeInt(rank);
		dest.writeString(config);
	}

	// 实例化静态内部对象CREATOR实现接口Parcelable.Creator
	public static final Parcelable.Creator<DownloadTask> CREATOR = new Creator<DownloadTask>() {

		@Override
		public DownloadTask[] newArray(int size) {
			return new DownloadTask[size];
		}

		// 将Parcel对象反序列化为ParcelableDate
		@Override
		public DownloadTask createFromParcel(Parcel source) {
			return new DownloadTask(source);
		}
	};

}

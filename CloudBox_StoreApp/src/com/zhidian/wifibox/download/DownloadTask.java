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
	 * 该任务所属的盒子WIFI，为空则代表外网任务
	 */
	public String src;
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
	 * 该应用的图标URL，用在下载管理的显示
	 */
	public String iconUrl = "";
	/**
	 * 该应用的名字，用在下载管理的显示
	 */
	public String name = "";
	/**
	 * 该应用的大小，用在下载管理的显示
	 */
	public int size = 0;
	/**
	 * 该应用的包名，用在下载管理的显示
	 */
	public String packName = "";
	/**
	 * 应用id
	 */
	public long appId = -1;
	/**
	 * 版本
	 */
	public String version = "";
	/**
	 * 下载速度
	 */
	public long speed = 0;

	public DownloadTask() {
	}

	/**
	 * 复制一个新的对象出来
	 * 
	 * @return
	 */
	public DownloadTask copyObj() {
		DownloadTask copy = new DownloadTask();
		copy.unique = this.unique;
		copy.state = this.state;
		copy.url = this.url;
		copy.alreadyDownloadPercent = this.alreadyDownloadPercent;
		copy.iconUrl = this.iconUrl;
		copy.src = this.src;
		copy.name = this.name;
		copy.size = this.size;
		copy.packName = this.packName;
		copy.appId = this.appId;
		copy.version = this.version;
		copy.speed = this.speed;
		return copy;
	}

	public DownloadTask(Parcel source) {
		unique = source.readString();
		state = source.readInt();
		url = source.readString();
		alreadyDownloadPercent = source.readInt();
		iconUrl = source.readString();
		name = source.readString();
		size = source.readInt();
		packName = source.readString();
		appId = source.readLong();
		version = source.readString();
		speed = source.readLong();
		src = source.readString();
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
		dest.writeString(iconUrl);
		dest.writeString(name);
		dest.writeInt(size);
		dest.writeString(packName);
		dest.writeLong(appId);
		dest.writeString(version);
		dest.writeLong(speed);
		dest.writeString(src);
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

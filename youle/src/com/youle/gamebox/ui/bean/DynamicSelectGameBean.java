package com.youle.gamebox.ui.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 社区动态选择游戏bean
 * 
 * @author xiedezhi
 * 
 */
public class DynamicSelectGameBean implements Parcelable {
	/**
	 * 游戏Id
	 */
	public long id;
	/**
	 * 游戏名
	 */
	public String name;
	/**
	 * 图标url
	 */
	public String iconUrl;
	/**
	 * 游戏来源：1：Y6 2：91 3：媒介 4：效果
	 */
	public int source;

	@Override
	public int describeContents() {
		return 0;
	}

	public DynamicSelectGameBean(Parcel src) {
		id = src.readLong();
		name = src.readString();
		iconUrl = src.readString();
		source = src.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(iconUrl);
		dest.writeInt(source);
	}

	// 实例化静态内部对象CREATOR实现接口Parcelable.Creator
	public static final Parcelable.Creator<DynamicSelectGameBean> CREATOR = new Creator<DynamicSelectGameBean>() {

		@Override
		public DynamicSelectGameBean[] newArray(int size) {
			return new DynamicSelectGameBean[size];
		}

		// 将Parcel对象反序列化为ParcelableDate
		@Override
		public DynamicSelectGameBean createFromParcel(Parcel source) {
			return new DynamicSelectGameBean(source);
		}
	};
}

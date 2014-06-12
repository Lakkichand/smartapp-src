package com.jiubang.ggheart.apps.gowidget.gostore.net.databean;

import java.util.ArrayList;

/**
 * GO精品更多下载渠道信息网络数据BEAN
 * 
 * @author wangzhuobin
 * 
 */
public class ChannelBean extends BaseBean {

	public int mChannelCount; // 下发的渠道数量
	public ArrayList<ChannelInfo> mChannelInfoArrayList; // 下发渠道信息集合

	public class ChannelInfo {
		public byte[] mIconData; // 渠道图片数据
		public String mChannelName; // 渠道名
		public String mChannelUrl; // 渠道地址

	}

	public void recycle() {
		if (mChannelInfoArrayList != null && mChannelInfoArrayList.size() > 0) {
			for (ChannelInfo channelInfo : mChannelInfoArrayList) {
				channelInfo.mIconData = null;
				channelInfo.mChannelName = null;
				channelInfo.mChannelUrl = null;
			}
			mChannelInfoArrayList.clear();
			mChannelInfoArrayList = null;
		}
	}
}
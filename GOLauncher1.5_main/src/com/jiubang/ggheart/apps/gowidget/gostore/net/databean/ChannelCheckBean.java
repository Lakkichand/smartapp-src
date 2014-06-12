package com.jiubang.ggheart.apps.gowidget.gostore.net.databean;

/**
 * 渠道包验证网络数据BEAN
 * 
 * @author wangzhuobin
 * 
 */
public class ChannelCheckBean extends BaseBean {
	public String mChannelId = null; // 渠道ID
	public String mChannelName = null; // 渠道要显示的文本
	public int mImgDataLength = 0; // 图片数据长度
	public byte[] mImgData = null; // 图片数据

	public void recycle() {
		mChannelId = null;
		mChannelName = null;
		mImgData = null;
	}
}

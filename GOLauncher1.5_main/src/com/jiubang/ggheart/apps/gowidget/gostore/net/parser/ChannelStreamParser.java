package com.jiubang.ggheart.apps.gowidget.gostore.net.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ChannelBean;

/**
 * 更多下载渠道网络数据解释器
 * 
 * @author wangzhuobin
 * 
 */
public class ChannelStreamParser extends HttpStreamParser {

	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {
		// TODO Auto-generated method stub
		ChannelBean channelBean = null;
		if (dis != null) {
			try {
				channelBean = new ChannelBean();
				int totalDataLength = dis.readInt();
				channelBean.mLength = totalDataLength;
				if (totalDataLength > 0) {

					// 时间戳
					channelBean.mTimeStamp = dis.readLong();

					// 渠道数量
					int channelCount = dis.readInt();
					channelBean.mChannelCount = channelCount;

					// 渠道集合
					ArrayList<ChannelBean.ChannelInfo> channelInfosArrayList = new ArrayList<ChannelBean.ChannelInfo>();
					ChannelBean.ChannelInfo channelInfo = null;

					int iconDataLength = 0;
					byte[] iconData = null;

					for (int i = 0; i < channelCount; i++) {

						channelInfo = channelBean.new ChannelInfo();

						// 渠道图片
						iconDataLength = dis.readInt();
						if (iconDataLength > 0) {
							iconData = new byte[iconDataLength];
							dis.readFully(iconData);
							channelInfo.mIconData = iconData;
						}

						// 渠道名称
						channelInfo.mChannelName = dis.readUTF();
						// 渠道URL
						channelInfo.mChannelUrl = dis.readUTF();

						channelInfosArrayList.add(channelInfo);
					}

					channelBean.mChannelInfoArrayList = channelInfosArrayList;

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (dis != null) {
					try {
						dis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return channelBean;
	}

}

package com.jiubang.ggheart.apps.gowidget.gostore.net.parser;

import java.io.DataInputStream;
import java.io.IOException;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ChannelCheckBean;

/**
 * 渠道包验证网络数据解释器
 * 
 * @author wangzhuobin
 * 
 */
public class ChannelCheckStreamParser extends HttpStreamParser {

	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {
		// TODO Auto-generated method stub
		ChannelCheckBean channelCheckBean = null;
		if (dis != null) {

			channelCheckBean = new ChannelCheckBean();

			try {
				// 数据总长度
				int totalDataLength = dis.readInt();
				channelCheckBean.mLength = totalDataLength;
				if (totalDataLength > 0) {
					// 时间戳
					channelCheckBean.mTimeStamp = dis.readLong();
					// 渠道ID
					channelCheckBean.mChannelId = dis.readUTF();
					// 渠道信息
					channelCheckBean.mChannelName = dis.readUTF();
					// 图片数据长度
					channelCheckBean.mImgDataLength = dis.readInt();
					if (channelCheckBean.mImgDataLength > 0) {
						// 如果有图片数据，则读取
						channelCheckBean.mImgData = new byte[channelCheckBean.mImgDataLength];
						dis.readFully(channelCheckBean.mImgData); // 图片数据
					}
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
		return channelCheckBean;
	}

}

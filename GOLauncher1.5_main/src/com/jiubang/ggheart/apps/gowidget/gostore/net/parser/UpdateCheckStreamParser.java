package com.jiubang.ggheart.apps.gowidget.gostore.net.parser;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.UpdateCheckBean;

public class UpdateCheckStreamParser extends HttpStreamParser {

	@Override
	public BaseBean parseHttpStreamData(DataInputStream dis) {
		// TODO Auto-generated method stub
		UpdateCheckBean updateCheckBean = null;
		if (dis != null) {

			updateCheckBean = new UpdateCheckBean();

			try {
				// 数据总长度
				int totalDataLength = dis.readInt();
				updateCheckBean.mLength = totalDataLength;
				if (totalDataLength > 0) {
					// 下发数据时间戳
					updateCheckBean.mTimeStamp = dis.readLong();
					// 数据更新时间戳
					updateCheckBean.mUpdateTimestamp = dis.readLong();

					// 分类时间戳个数
					int num = dis.readInt();
					if (num > 0) {
						updateCheckBean.mUpdateMap = new HashMap<Integer, Long>(num);
						while (num > 0) {
							// 获取最新内容时间
							int typeid = dis.readInt();
							Long timestamp = dis.readLong();
							updateCheckBean.mUpdateMap.put(typeid, timestamp);
							--num;
						}
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
		return updateCheckBean;
	}

}

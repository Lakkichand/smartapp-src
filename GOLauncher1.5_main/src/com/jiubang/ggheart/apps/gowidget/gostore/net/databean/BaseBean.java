package com.jiubang.ggheart.apps.gowidget.gostore.net.databean;

import java.io.Serializable;

@SuppressWarnings("serial")
public class BaseBean implements Serializable {
	public int mFunId = 0; // 数据块的功能号
	public int mLength = 0; // 下发的数据长度
	public long mTimeStamp = 0L; // 下发的数据时间戳
}

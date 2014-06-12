package com.jiubang.ggheart.apps.gowidget.gostore.net.databean;

import java.util.HashMap;

public class UpdateCheckBean extends BaseBean {
	public long mUpdateTimestamp = 0L; // 数据更新时间戳
	public HashMap<Integer, Long> mUpdateMap = null;// 各分类的更新时间
}

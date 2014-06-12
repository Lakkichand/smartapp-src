package com.jiubang.ggheart.data.info;

import java.util.List;

import com.jiubang.ggheart.data.BroadCaster;

/**
 * 数据抽象
 * 
 * @author guodanyang
 * 
 */
public class BaseItemInfo extends BroadCaster implements BroadCaster.BroadCasterObserver {
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		broadCast(msgId, param, object, objects);
	}
}

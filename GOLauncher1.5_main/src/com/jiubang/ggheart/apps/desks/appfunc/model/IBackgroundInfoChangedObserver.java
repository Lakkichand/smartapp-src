package com.jiubang.ggheart.apps.desks.appfunc.model;

import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;

/**
 * 后台数据状态监听器
 * 
 * @author tanshu
 * 
 */
public interface IBackgroundInfoChangedObserver {
	/**
	 * 后台信息发生改变的监听接口
	 * 
	 * @param msgId
	 *            消息ID
	 * @param obj1
	 *            参数1
	 * @param obj2
	 *            参数2
	 * @return 处理返回true 不处理返回false
	 */
	public boolean handleChanges(AppFuncConstants.MessageID msgId, Object obj1, Object obj2);

}

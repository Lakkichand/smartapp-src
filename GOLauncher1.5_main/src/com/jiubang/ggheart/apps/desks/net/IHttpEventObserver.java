package com.jiubang.ggheart.apps.desks.net;

/**
 * 
 * @author huyong
 * @version 1.0
 */
public interface IHttpEventObserver {

	public final static int ACTION_CHECKING = -1; // 正在检测中
	public final static int EVENT_AUTOCHECKVERSION = 1; // 自动检查版本
	public final static int EVENT_CHECKVERSION = 2; // 手动检查版本
	public final static int EVENT_CHECKVERSIONFINISH = 3; // 手动检查版本

	/**
	 * 
	 * @param object
	 *            待处理的对象
	 */
	public abstract void onHandleHttpEvent(final int eventId, Object object);
}

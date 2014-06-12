package com.jiubang.ggheart.billing;

/**
 * 
 * <br>类描述: 付费平台界面，加载状态监听类
 * <br>功能详细描述:
 * 
 * @author  zhoujun
 * @date  [2012-9-24]
 */
public interface IWebViewLoadListener {

	/**
	 * <br>功能简述:  开始加载页面
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onPageStarted();

	/**
	 * <br>功能简述: 加载页面完成
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onPageFinished();

	/**
	 * <br>功能简述: 加载页面失败
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onReceivedError(int errorCode) ;
}

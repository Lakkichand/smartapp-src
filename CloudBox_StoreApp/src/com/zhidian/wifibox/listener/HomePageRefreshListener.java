package com.zhidian.wifibox.listener;

/***
 * 
 * @author zhaoyl
 * ListView刷新回调接口
 */
public interface HomePageRefreshListener {
	void callbackRefresh(boolean falg,int type,Object object);
	void callbackScroll(boolean updateFlag,int upDown);
}

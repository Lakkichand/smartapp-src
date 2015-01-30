package com.zhidian.wifibox.listener;

import com.zhidian.wifibox.data.DetailDataBean;


public interface AppdetailListener {
	void onCancle();
	void onShow();
	void getData(DetailDataBean bean);
	void getAppData(String title);
	//void onShowExceptionClick();

}

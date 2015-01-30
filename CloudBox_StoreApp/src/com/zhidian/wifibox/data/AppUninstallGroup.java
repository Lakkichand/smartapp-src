package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户应用卸载组合数据
 * 
 * @author xiedezhi
 * 
 */
public class AppUninstallGroup {
	public String mTitle;
	public List<AppUninstallBean> mList = new ArrayList<AppUninstallBean>();
	/**
	 * 1显示上次打开时间 2显示安装时间
	 */
	public int type = 1;
}

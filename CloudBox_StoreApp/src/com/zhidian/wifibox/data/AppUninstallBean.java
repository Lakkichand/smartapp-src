package com.zhidian.wifibox.data;

/**
 * 应用卸载数据bean
 * 
 * @author xiedezhi
 * 
 */
public class AppUninstallBean {
	// 名字
	public String appname;
	// 包名
	public String packname;
	// 大小
	public long size;
	// 系统应用
	public boolean isSystemApp;
	// 上次打开时间
	public long lastOpenTime;
	// 安装时间
	public long installTime;
	// 是否选中
	public boolean isSelect;
}

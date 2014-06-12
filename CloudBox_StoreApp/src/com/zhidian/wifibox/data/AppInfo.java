package com.zhidian.wifibox.data;

import android.graphics.drawable.Drawable;

public class AppInfo {

	private Drawable icon;
	private String appname;
	private String packname;	
	private long size;
	private boolean isSystemApp;
	private String sourceDir; //绝对路径
	private String dataDir; 
	
	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	public String getSourceDir() {
		return sourceDir;
	}

	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}
	
	/**
	 * 是否被选中
	 */
	public boolean isSelect = false;
	
	public long getSize() {
		return size;
	}
	
	public void setSize(long size) {
		this.size = size;
	}	
		
	public Drawable getIcon() {
		return icon;
	}
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public String getAppname() {
		return appname;
	}
	public void setAppname(String appname) {
		this.appname = appname;
	}
	public String getPackname() {
		return packname;
	}
	public void setPackname(String packname) {
		this.packname = packname;
	}
	public boolean isSystemApp() {
		return isSystemApp;
	}
	public void setSystemApp(boolean isSystemApp) {
		this.isSystemApp = isSystemApp;
	}
}

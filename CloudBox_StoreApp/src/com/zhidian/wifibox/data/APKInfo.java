package com.zhidian.wifibox.data;



public class APKInfo {
	private String appname;//名称
	private String packname;//包名
	private String path; //绝对路径
	private int isInstall; //是否已安装
	private long size; //安装包大小
	private boolean isSystemApp;
	private boolean isDamage = false; //是否破损
	private String versionName; //版本名称
	/**
	 * 是否被选中
	 */
	public boolean isSelect = false;
	
	
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}	
	
	public boolean isDamage() {
		return isDamage;
	}
	public void setDamage(boolean isDamage) {
		this.isDamage = isDamage;
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
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getIsInstall() {
		return isInstall;
	}
	public void setIsInstall(int isInstall) {
		this.isInstall = isInstall;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public boolean isSystemApp() {
		return isSystemApp;
	}
	public void setSystemApp(boolean isSystemApp) {
		this.isSystemApp = isSystemApp;
	}
	
}

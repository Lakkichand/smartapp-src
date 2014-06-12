package com.jiubang.ggheart.data.theme.bean;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 
 *
 */
public class AppDataThemeBean extends ThemeBean {

	public static float DEFALUT_SCALE_FACTOR = 0.7f;
	// 以应用程序component为key，资源名称为value
	private ConcurrentHashMap<String, String> mFilterAppsMap; // 需要替换资源的应用程序map

	// private String mIconbackName; //底座图
	// private String mIconuponName; //罩子图
	private float mScaleFactor; // 缩放比率

	private ArrayList<String> mIconbackNameList; // 底座图集合
	private ArrayList<String> mIconuponNameList; // 罩子图集合
	private ArrayList<String> mIconmaskNameList; // mask图集合

	public AppDataThemeBean(String pkgName) {
		super(pkgName);
		mFilterAppsMap = new ConcurrentHashMap<String, String>();
		mBeanType = THEMEBEAN_TYPE_APPDATA;
		mScaleFactor = DEFALUT_SCALE_FACTOR;
		mIconbackNameList = new ArrayList<String>();
		mIconuponNameList = new ArrayList<String>();
		mIconmaskNameList = new ArrayList<String>();
	}

	public ConcurrentHashMap<String, String> getFilterAppsMap() {
		return mFilterAppsMap;
	}

	/*
	 * public String getIconbackName() { return mIconbackName; }
	 * 
	 * public void setIconbackName(String mIconbackName) { this.mIconbackName =
	 * mIconbackName; }
	 * 
	 * public String getIconuponName() { return mIconuponName; }
	 * 
	 * public void setIconuponName(String mIconUponName) { this.mIconuponName =
	 * mIconUponName; }
	 */
	public float getScaleFactor() {
		return mScaleFactor;
	}

	public void setScaleFactor(float scaleFactor) {
		this.mScaleFactor = scaleFactor;
	}

	public ArrayList<String> getIconbackNameList() {
		return mIconbackNameList;
	}

	public ArrayList<String> getIconuponNameList() {
		return mIconuponNameList;
	}
	
	/**
	 * @return the mIconmaskNameList
	 */
	public ArrayList<String> getmIconmaskNameList() {
		return mIconmaskNameList;
	}
}

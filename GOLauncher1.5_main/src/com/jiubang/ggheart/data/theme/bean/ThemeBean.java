package com.jiubang.ggheart.data.theme.bean;

public class ThemeBean {

	public static int THEMEBEAN_TYPE_THEMEINFO = 0;
	public static int THEMEBEAN_TYPE_APPDATA = 1;
	public static int THEMEBEAN_TYPE_FUNCAPP = 2;
	public static int THEMEBEAN_TYPE_DESK = 3;
	public static int THEMEBEAN_TYPE_DRAWRESOURCE = 4;
	public static int THEMEBEAN_TYPE_WIDGET = 5;

	// 主题版本
	private int mVerId;
	protected int mBeanType;
	protected String mPackageName;

	public ThemeBean() {
		// TODO Auto-generated constructor stub
	}

	public ThemeBean(final String pkgName) {
		mPackageName = pkgName;
	}

	public ThemeBean(final ThemeBean themeBean) {
		// TODO Auto-generated constructor stub
		if (themeBean != null && themeBean instanceof ThemeBean) {
			mVerId = themeBean.getVerId();
			mBeanType = themeBean.getBeanType();
			mPackageName = themeBean.getPackageName();
		}
	}

	public int getVerId() {
		return mVerId;
	}

	public void setVerId(int mVerId) {
		this.mVerId = mVerId;
	}

	public int getBeanType() {
		return mBeanType;
	}

	public String getPackageName() {

		return mPackageName;
	}

	public void setPackageName(String packageName) {
		this.mPackageName = packageName;
	}
}

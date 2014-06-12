package com.jiubang.ggheart.components.facebook;
/**
 * 
 * @author xiangliang
 *
 */
public class OpenGraphObject {
	String mPkgName;
	String mThemeName;
	
	public OpenGraphObject(String pkg, String name) {
		mPkgName = pkg;
		mThemeName = name;
	}

	public void setPkgName(String pkgName) {
		mPkgName = pkgName;
	}

	public void setThemeName(String name) {
		mThemeName = name;
	}

	public String getPkgName() {
		return mPkgName;
	}

	public String getThemeName() {
		return mThemeName;
	}
}

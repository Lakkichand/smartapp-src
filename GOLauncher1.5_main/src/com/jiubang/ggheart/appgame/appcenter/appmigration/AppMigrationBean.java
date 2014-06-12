package com.jiubang.ggheart.appgame.appcenter.appmigration;

/**
 * 
 * <br>类描述:应用搬家的数据bean
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-11-20]
 */
public class AppMigrationBean {
	private String mName = "";

	private String mSize = "";

	private String mPackage = "";

	private int mType = sTYPE_SD;

	public static final int sTYPE_SD = 0;

	public static final int sTYPE_INTERNAL_STORAGE = 1;

	public static final int sTYPE_SYSTEM = 2;
	
	public static final int sTYPE_GROUP = 3;

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getSize() {
		return mSize;
	}

	public void setSize(String size) {
		mSize = size;
	}

	public String getPackageName() {
		return mPackage;
	}

	public void setPackageName(String pkg) {
		mPackage = pkg;
	}

	public int getType() {
		return mType;
	}

	public void setType(int type) {
		mType = type;
	}
}

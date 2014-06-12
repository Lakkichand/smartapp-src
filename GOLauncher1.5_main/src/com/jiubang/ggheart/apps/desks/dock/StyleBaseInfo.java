package com.jiubang.ggheart.apps.desks.dock;

import com.jiubang.core.framework.ICleanable;

/**
 * @author ruxueqin dock风格包基础信息
 */
public class StyleBaseInfo implements ICleanable {
	public float mVersion; // 版本号
	public float mVersionCode;
	public String mPkgName; // 包名
	public String mStyleName; // 风格名

	@Override
	public void cleanup() {

	}
}

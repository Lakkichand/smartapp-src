package com.gau.go.launcherex.theme.cover;

import android.content.Context;
/**
 * <br>罩子层数据控制器
 * <br>
 * @author jiangxuwen
 */
public class CoverDataControl {

	private static CoverDataControl sControl;
	private CoverParser mCoverParser;
	private CoverBean mCoverBean;

	public CoverDataControl(Context context) {
		mCoverParser = new CoverParser();
		mCoverBean = mCoverParser.autoParseAppThemeXml(context, CoverBean.PACKAGE_NAME);
	}

	public static synchronized CoverDataControl getInstance(Context context) {
		if (sControl == null) {
			sControl = new CoverDataControl(context);
		}
		return sControl;
	}

	public CoverBean getCoverBean() {
		return mCoverBean;
	}

	public void cleanUp() {
		mCoverBean.cleanUp();
		mCoverParser = null;
	}
}

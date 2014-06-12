package com.gau.go.launcherex.theme.cover.utils;

/**
 * 罩子层的苏醒守卫者
 * @author jiangxuwen
 *
 */
public class WakeUpGuard {

	private static final float LAUNCHER_MASK_VERSION = 1.0f;
	private static final String LAUNCHER_MATCH_CODE = "TRHello_this_is_CoverFrame_welcome_youEM";
	private static final int START_INDEX = 3;
	private static final int END_INDEX = 12;
	private static final int START_INDEX2 = 5;
	private static final int END_INDEX2 = 14;

	public WakeUpGuard() {

	}

	public float getLauncherMaskVersion(String version) {
		if (null == version) {
			return 0;
		}
		if (version.length() == 0) {
			return 0;
		}
		return Float.parseFloat(version);
	}

	public boolean isSafe(Object object) {
		if (object != null && object instanceof String) {
			String subSrc = ((String) object).substring(START_INDEX, END_INDEX);
			String subDst = LAUNCHER_MATCH_CODE.substring(START_INDEX2, END_INDEX2);
			return subSrc.equals(subDst);
		}
		return false;
	}
}

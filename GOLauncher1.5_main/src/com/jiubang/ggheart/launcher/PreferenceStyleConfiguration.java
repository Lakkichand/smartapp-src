package com.jiubang.ggheart.launcher;

import com.gau.go.launcherex.R;

public class PreferenceStyleConfiguration {
	public static final int STYLE_NONE = 0;
	public static final int STYLE_YELLOW = 1;

	public static int getStyle(int style) {
		int ret = 0;

		switch (style) {
			case STYLE_YELLOW :
				ret = R.style.yellow;
				break;

			default :
				break;
		}

		return ret;
	}
}

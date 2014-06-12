package com.jiubang.ggheart.apps.font;

import android.graphics.Typeface;

public class FontStyle {
	public static final String NORMAL = "NORMAL";
	public static final String BOLD = "BOLD";
	public static final String ITALIC = "ITALIC";
	public static final String BOLD_ITALIC = "BOLD_ITALIC";

	public static int style(String style) {
		int ret = 0;

		if (style.equals(BOLD)) {
			ret = Typeface.BOLD;
		} else if (style.equals(ITALIC)) {
			ret = Typeface.ITALIC;
		} else if (style.equals(BOLD_ITALIC)) {
			ret = Typeface.BOLD_ITALIC;
		} else {
			ret = Typeface.NORMAL;
		}

		return ret;
	}
}

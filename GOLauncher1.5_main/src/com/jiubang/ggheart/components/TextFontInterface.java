package com.jiubang.ggheart.components;

import android.graphics.Typeface;

public interface TextFontInterface {
	public void onInitTextFont();

	public void onUninitTextFont();

	public void onTextFontChanged(Typeface typeface, int style);
}

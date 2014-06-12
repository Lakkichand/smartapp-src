package com.jiubang.ggheart.apps.gowidget.widgetThemeChoose;

public interface IWidgetChooseFrame {
	public void removeView(int position);

	public void addView(int position);

	public void updateCurrentView(int newScreen, int oldScreen);
}

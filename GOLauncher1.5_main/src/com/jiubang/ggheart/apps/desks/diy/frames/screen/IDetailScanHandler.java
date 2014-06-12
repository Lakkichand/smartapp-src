package com.jiubang.ggheart.apps.desks.diy.frames.screen;

public interface IDetailScanHandler {
	public abstract void updateIndicatorTotal(int total);

	public abstract void updateIndicatorCurrent(int current);

	public abstract void updateSliderIndecator(int offset);

	public abstract void loadFinish();
}

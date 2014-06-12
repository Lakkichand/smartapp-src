package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import com.jiubang.ggheart.apps.desks.diy.frames.screen.IIndicatorUpdateListner;
/**
 * 
 * <br>类描述:添加界面指示器监听
 * <br>功能详细描述:
 * 
 */
public interface TabIndicatorUpdateListner extends IIndicatorUpdateListner {

	public void onScrollChanged(int offset);

	public void onScreenChanged(int newScreen);
}

package com.jiubang.ggheart.appgame.base.component;

/**
 * sd卡事件处理接口，负责处理sd卡开启和关闭的事件
 * 
 * @author xiedezhi
 * 
 */
public interface SDCardEventHandler {

	/**
	 * sd卡已经关闭
	 */
	public void sdCardTurnOff();

	/**
	 * sd卡已经开启
	 */
	public void sdCardTurnOn();

}

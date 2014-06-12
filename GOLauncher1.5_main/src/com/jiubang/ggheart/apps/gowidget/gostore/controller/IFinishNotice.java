package com.jiubang.ggheart.apps.gowidget.gostore.controller;

/**
 * Acitivity关闭时发出广播接口 用于通知GosStore其它窗体关闭MainView中的Banner继续滚动
 * 
 * @author zhaojunjie
 * 
 */
public interface IFinishNotice {

	/**
	 * 发出广播
	 * 
	 * @author zhaojunjie
	 */
	public void onSendFinishBocast();
}

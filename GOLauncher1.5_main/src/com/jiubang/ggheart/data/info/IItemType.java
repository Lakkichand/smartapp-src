package com.jiubang.ggheart.data.info;

/**
 * 桌面元素类型定义
 * 
 * @author yuankai
 * @version 1.0
 */
public interface IItemType {
	/**
	 * 应用
	 */
	public final static int ITEM_TYPE_APPLICATION = 1;

	/**
	 * 快捷方式
	 */
	public final static int ITEM_TYPE_SHORTCUT = 2;

	/**
	 * Widget
	 */
	public final static int ITEM_TYPE_APP_WIDGET = 3;

	/**
	 * 用户文件夹
	 */
	public final static int ITEM_TYPE_USER_FOLDER = 4;

	/**
	 * 系统文件夹
	 */
	public final static int ITEM_TYPE_LIVE_FOLDER = 5;
	/**
	 * 初始化时推荐的gowidget
	 */
	public final static int ITEM_TYPE_FAVORITE = 6;

	// /**
	// * dock 5个特殊图标
	// */
	// public final static int ITEM_TYPE_DOCK = 7;
}

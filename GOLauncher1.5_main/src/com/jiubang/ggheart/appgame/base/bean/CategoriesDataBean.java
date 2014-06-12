package com.jiubang.ggheart.appgame.base.bean;

import com.jiubang.ggheart.appgame.base.component.MainViewGroup;

/**
 * 分类信息单元封装类，分类信息单元可以是分类推荐单元，也可以是tab栏单元
 * 
 * @author xiedezhi
 * 
 */
public class CategoriesDataBean implements Comparable<CategoriesDataBean> {
	/**
	 * 分类id
	 */
	public int typeId = -1;
	/**
	 * 分类名称
	 */
	public String name;
	/**
	 * 顺序
	 */
	public int seq = 0;
	/**
	 * 是否为首屏，默认:0；1:首屏；
	 */
	public int isHome = 0;
	/**
	 * 顶层展现的功能按钮(多个以#分隔);例如："2#3" 1:搜索2:应用排序3:批量删除
	 */
	public String funButton = "";
	/**
	 * 默认:0；1：一键装/玩机；2：我的游戏/应用3：应用更新 4：GO应用&主题 5：搜索 6: 本地管理 7:跳转应用中心 8:跳转游戏中心
	 */
	public int feature = 0;
	/**
	 * 分类的未选中图标url，仅当父分类datatype为1，viewtype为3时有效，根据UI2.0新增
	 */
	public String icon = null;
	/**
	 * 分类的选中图标url，仅当父分类datatype为1，viewtype为3时有效，根据UI2.0新增
	 */
	public String cicon = null;
	/**
	 * 首屏设置(多个以#分隔)表示该分类id在哪些入口可以为首页，入口定义见{@link MainViewGroup}
	 */
	public String accesshome = "";
	/**
	 * 分类描述，该分类下所包含的前三个应用的名称，以分号分隔
	 */
	public String desc = "";
	/**
	 * 九宫格Banner图
	 */
	public String pic = "";
	/**
	 * 默认feature值
	 */
	public static final int FEATURE_FOR_DEFAULT = 0;
	/**
	 * 一键装/玩机
	 */
	public static final int FEATURE_FOR_YJZWJ = 1;
	/**
	 * 我的游戏/应用
	 */
	public static final int FEATURE_FOR_GAME_AND_APP = 2;
	/**
	 * 应用更新
	 */
	public static final int FEATURE_FOR_APP_UPDATE = 3;
	/**
	 * GO应用&主题
	 */
	public static final int FEATURE_FOR_THEME = 4;
	/**
	 * 搜索（根据UI2.0新增）
	 */
	public static final int FEATURE_FOR_SEARCH = 5;
	/**
	 * 本地管理（根据UI2.0新增）
	 */
	public static final int FEATURE_FOR_MANAGEMENT = 6;

	@Override
	public int compareTo(CategoriesDataBean another) {
		return this.seq - another.seq;
	}
}

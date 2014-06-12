package com.jiubang.ggheart.appgame.base.bean;

import java.util.List;

/**
 * 分类项单元数据封装类，一个分类id对应一个分类项单元。 分类项单元可以有不同的数据类型，根据dataType取不同的数据列表
 * 
 * @author xiedezhi
 * 
 */
public class ClassificationDataBean {
	/**
	 * 顶层tab栏的分类id
	 */
	public static final int TOP_TYPEID = 0;

	/**
	 * 该数据对应的分类id
	 */
	public int typeId = -1;
	/**
	 * 分类名称
	 */
	public String typename;
	/**
	 * 我的应用数据类型
	 */
	public static final int MY_APP_TYPE = 1;
	/**
	 * 应用更新数据类型
	 */
	public static final int UPDATE_APP_TYPE = 3;
	/**
	 * 精品推荐数据类型
	 */
	public static final int FEATURE_TYPE = 11;
	/**
	 * 分类推荐数据类型
	 */
	public static final int CATEGORIES_TYPE = 12;
	/**
	 * 专题推荐数据类型
	 */
	public static final int SPECIALSUBJECT_TYPE = 13;
	/**
	 * 分级tab栏数据类型
	 */
	public static final int TAB_TYPE = 14;
	/**
	 * 一键装机数据类型
	 */
	public static final int YJZJ_TYPE = 15;
	/**
	 * 一键玩机数据类型
	 */
	public static final int YJWJ_TYPE = 16;
	/**
	 * 搜索界面数据类型（根据UI2.0新增）
	 */
	public static final int SEARCH_TYPE = 17;
	/**
	 * 本地管理界面数据类型（根据UI2.0新增）
	 */
	public static final int MANAGEMENT_TYPE = 18;
	/**
	 * 标题栏采用图标加文字展现（根据UI2.0新增）
	 */
	public static final int ICON_TAB_TYPE = 19;
	/**
	 * 一行一列展示专题列表类型
	 */
	public static final int ONEPERLINE_SPECIALSUBJECT_TYPE = 20;
	/**
	 * 编辑推荐数据类型
	 */
	public static final int EDITOR_RECOMM_TYPE = 21;
	/**
	 * coverflow展示类型
	 */
	public static final int COVER_FLOW = 22;
	
	/**
	 * 高级管理
	 */
	public static final int ADVANCED_MANAGEMENT = 23;
	/**
	 * 按钮型tab展示类型
	 */
	public static final int BUTTON_TAB = 24;
	/**
	 * 九宫格分类
	 */
	public static final int GRID_SORT = 25;
	/**
	 * 九宫格
	 */
	public static final int GRID_TYPE = 26;
	/**
	 * 壁纸九宫格
	 */
	public static final int WALLPAPER_GRID = 27;
	/**
	 * 价格变动列表
	 */
	public static final int PRICE_ALERT = 28;
	/**
	 * 广告推荐位数据
	 */
	public static final int AD_BANNER = 29;
	/**
	 * 空白数据类型
	 * 进入应用中心时，只读取解释展示首屏数据，其他屏暂时用EmptyContainer代替，等滑到该屏时再读取该屏的本地数据，生成真正的container
	 */
	public static final int EMPTY_TYPE = 999;

	/**
	 * 数据类型
	 */
	public int dataType = -1;

	/**
	 * 显示类型，应用中心widget根据此类型区分显示icon或是banner
	 */
	public int mViewType = -1;
	/**
	 * 数据类型，应用中心widget根据此类型跳转到应用中心或游戏中心
	 */
	public int mWidgetDataType = -1;

	// TODO:wangzhuobin 为什么不用继承？
	/**
	 * 精品推荐、双栏两列专题、一栏一列专题、编辑推荐专题数据列表
	 */
	public List<BoutiqueApp> featureList = null;
	/**
	 * 分类信息单元列表，分类信息单元可以是分类推荐单元，也可以是tab栏单元，由dataType决定
	 */
	public List<CategoriesDataBean> categoriesList = null;
	/**
	 * 总页数，专题数据类型用到
	 */
	public int pages;
	/**
	 * 当前分页，专题数据类型用到
	 */
	public int pageid;
	/**
	 * 标题栏展现的位置，1：靠顶部2：靠底部（仅在datatype为1，viewtype为3时有效），根据UI2.0新增
	 */
	public int viewlocal = -1;
	/**
	 * 分类简介，该值为空则不显示
	 */
	public String summary;
	/**
	 * 顶层展现的功能按钮(多个以#分隔);例如：2#3 1:搜索2:应用排序3:批量删除，isIconTab为false时有意义
	 */
	public String funbutton;
	/**
	 * tab头标题列表，isIconTab为false时有意义
	 */
	public String title;
	/**
	 * 在应用详情中是否展现该列表页
	 */
	public int showlist;
	/**
	 * 该应用列表是否过滤已安装 仅当datatype=2时处理该字段 0：不过滤已安装 1：过滤已安装
	 */
	public int filter;
	/**
	 * 广告位在垂直方向占用的单元格数量 默认：0 垂直方向无限高、1：1个单元格的高度、2：2个单元格的高度、n:n个单元格的高度 单元格高度需要结合展现类型（viewtype）由UI确定
	 */
	public int versize;
	
	/**
	 * 把另外一个实例的所有变量赋赋值给自己相应的变量
	 */
	public void copyFrom(ClassificationDataBean src) {
		if (src == null) {
			return;
		}
		this.typeId = src.typeId;
		this.typename = src.typename;
		this.dataType = src.dataType;
		this.mViewType = src.mViewType;
		this.mWidgetDataType = src.mWidgetDataType;
		this.featureList = src.featureList;
		this.categoriesList = src.categoriesList;
		this.pages = src.pages;
		this.pageid = src.pageid;
		this.viewlocal = src.viewlocal;
		this.summary = src.summary;
		// 这两个属性不是从服务器下发的JSON读取数值，所以保留旧的数据
//		this.funbutton = src.funbutton;
//		this.title = src.title;
		this.showlist = src.showlist;
		this.filter = src.filter;
		this.versize = src.versize;
	}
}

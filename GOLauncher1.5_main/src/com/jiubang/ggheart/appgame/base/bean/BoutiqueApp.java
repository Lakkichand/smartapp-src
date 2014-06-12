package com.jiubang.ggheart.appgame.base.bean;

import java.io.Serializable;

/**
 * 精品应用数据、双栏两列专题数据、单栏单列专题数据、编辑推荐专题数据单元
 * 
 * @author xiedezhi
 * 
 */
public class BoutiqueApp implements Serializable {
	/**
	 * 精品应用的跳转类型，跳到电子市场
	 */
	public static final int FEATURE_ACTTYPE_MARKET = 2;
	/**
	 * 精品应用的跳转类型，跳到web版电子市场
	 */
	public static final int FEATURE_ACTTYPE_BROWSER = 3;
	/**
	 * 精品应用跳转类型，跳到GoStore详情页
	 */
	public static final int FEATURE_ACTTYPE_FTP = 4;
	
	/**
	 * 应用详情打开式,1：详情界面
	 */
	public static final int DETAIL_TYPE_FTP = 1;
	/**
	 * 应用详情打开式,2：电子市场页面
	 */
	public static final int DETAIL_TYPE_MARKET = 2;
	/**
	 * 应用详情打开式,3：电子市场web版页面
	 */
	public static final int DETAIL_TYPE_WEB = 3;
	
	
	/**
	 * 数据单元所属的分类id
	 */
	public int typeid;
	/**
	 * type=1则为专题分类id，type=2则为应用id
	 */
	public int rid;
	/**
	 * 类型 1：专题，2：应用
	 */
	public int type;
	/**
	 * 跳转类型 1：打开专题应用列表，2：在电子市场中打开应用详情，3：打开电子市场http应用详情，4：跳至go精品打开应用详情，5:打开一键装机
	 */
	public int acttype;
	/**
	 * 跳转信息 acttype =1-3: url连接， acttype =4：marketid#packname
	 */
	public String actvalue;
	/**
	 * 展示占用的格子数 分为1-4属于Google
	 * play样式；5：appstore样式的大格；6：appstore样式的小格子(该字段只用于精品推荐页）
	 */
	public int cellsize;
	/**
	 * banner图url
	 */
	public String pic;
	/**
	 * 图片的本地保存路径
	 */
	public String picLocalPath;
	/**
	 * 图片的本地保存名
	 */
	public String picLocalFileName;
	/**
	 * 本地特性图片名
	 */
	public String localFeatureFileName;
	/**
	 * 专题名字
	 */
	public String name;
	/**
	 * 当前应用在页面上的位置，从1开始，用于统计信息。注意：专题应用和一键装（玩）机也算一个位置
	 */
	public int index = -1;
	/**
	 * 应用信息单元
	 */
	public BoutiqueAppInfo info = new BoutiqueAppInfo();
	/**
	 * 分类基本信息单元，用于编辑推荐列表
	 */
	public BoutiqueTypeInfo typeInfo = new BoutiqueTypeInfo();
	/**
	 * 应用的下载状态
	 */
	public BoutiqueDownloadState downloadState = new BoutiqueDownloadState();

	/**
	 * 应用信息单元
	 */
	public class BoutiqueAppInfo implements Serializable {
		/**
		 * 应用特性类型：默认
		 */
		public static final int FEATURE_TYPE_DEFAULT = 0;
		/**
		 * 应用特性类型：NEW
		 */
		public static final int FEATURE_TYPE_NEW = 1;
		/**
		 * 应用特性类型：推荐
		 */
		public static final int FEATURE_TYPE_RECOMMEND = 2;
		/**
		 * 应用特性类型：首发
		 */
		public static final int FEATURE_TYPE_FIRST = 3;
		/**
		 * 应用特性类型：精品
		 */
		public static final int FEATURE_TYPE_BOUTIQUE = 4;
		/**
		 * 应用特性类型：必备
		 */
		public static final int FEATURE_TYPE_MUSTHAVE = 5;

		/**
		 * 需要回调url
		 */
		public static final int NEED_TO_CBACK = 1;
		/**
		 * 查看详情时，需要回调
		 */
		public static final int CBACK_URL_FOR_DETAIL = 1;
		/**
		 * 点击下载时， 需要回调
		 */
		public static final int CBACK_URL_FOR_DOWNLOAD = 2;
		/**
		 * 详情和下载，需要回调
		 */
		public static final int CBACK_URL_FOR_ALL = 3;

		/**
		 * 应用id
		 */
		public String appid;
		/**
		 * 应用包名
		 */
		public String packname;
		/**
		 * 应用名称
		 */
		public String name;
		/**
		 * 图标url
		 */
		public String icon;
		/**
		 * 版本名称 例如：2.4beta
		 */
		public String version;
		/**
		 * 版本code 例如：26
		 */
		public String versioncode;
		/**
		 * 安装包大小 例如：5.07M
		 */
		public String size;
		/**
		 * 简介
		 */
		public String summary;
		/**
		 * 应用的等级 分为1—10个级别
		 */
		public int grade;
		/**
		 * 是否收费 0：免费1：收费
		 */
		public int isfree;
		/**
		 * 应用的旧价格
		 */
		public String oldprice;
		/**
		 * 应用的价格
		 */
		public String price;
		/**
		 * 应用开发者
		 */
		public String developer;
//		/**
//		 * 开发者等级 0：普通，1：高级
//		 */
//		public int devgrade;
//		/**
//		 * 特性 默认:0；1、最新2、推荐3、首发4、精品5、必备
//		 */
//		public int feature;
		/**
		 * 特性图标url
		 */
		public String ficon;
		/**
		 * 下载方式 1：ftp下载，2：电子市场下载 3：电子市场web版页面
		 */
		public int downloadtype;
		/**
		 * 下载地址 根据downloadtype不同采用不同的处理方式
		 */
		public String downloadurl;
		/**
		 * 下载应用时，是否需要回调 ； 默认0：不需要回调；1：需要回调
		 */
		public int cback;
		/**
		 * 1：查看详情回调，2：下载时回调；木瓜的为3=1+2即这两种行为都需要回调
		 */
		public int cbacktype;
		/**
		 * 回调地址； 下载应用时，同时回调改url
		 */
		public String cbackurl;
//		/**
//		 * 推荐类型: 星级位置显示类型 默认0：显示星级 1：显示编辑推荐(remdmsg)
//		 */
//		public int remdtype;
//		/**
//		 * 推荐描述
//		 */
//		public String remdmsg;
		/**
		 * 特效 0：无特效 1：火焰特效，默认为0
		 */
		public int effect;
		/**
		 * 应用下载安装处理 0:无处理 ;1：安装完成后提醒打开应用
		 */
		public int treatment;
		/**
		 * 应用类别信息
		 */
		public String typeinfo;
		/**
		 * 应用详情打开式 1：详情界面，2：电子市场页面3：电子市场web版页面
		 */
		public int detailtype;
		/**
		 * 应用详情url
		 */
		public String detailurl;
//		/**
//		 * 是否特别推荐(备用) 默认为0；1：特别推荐
//		 */
//		public String unusual;
//		/**
//		 * 下载量
//		 */
//		public int downloadcount;
		/**
		 * 格式化的下载量数据
		 */
		public String dlcs;
//		/**
//		 * 应用类型 1：应用（默认）；2：主题；3：壁纸
//		 */
//		public int apptype;
//		/**
//		 * 付费类型
//		 */
//		public String paytype;
//		/**
//		 * 付费ID
//		 */
//		public String payid;
		/**
		 * 截图url串
		 */
		public String pics;
//		/**
//		 * 大图url列表
//		 */
//		public ArrayList<String> bigPics;
//		/**
//		 * 小图url列表
//		 */
//		public ArrayList<String> smallPics;
		
		/**
		 * 价格变更时间
		 */
		public String changetime = "";
		/**
		 * 评论数
		 */
		public String commentsnum = "";
		/**
		 * 详情样式
		 */
		public int detailstyle ;
		/**
		 * 资源包下载地址
		 */
//		public String resourceurl = "";
		/**
		 * 安装成功时的回调地址
		 */
		public String icbackurl = "";
		/**
		 * TODO 木瓜sdk回调地址
		 */
		public String iAfCbackurl = "";
		/**
		 * 应用主题标签
		 * 0:普通应用
		 * 1:GO系列软件
		 * 2:GO插件
		 * 20:动态壁纸
		 * 21:GO桌面主题
		 * 22:GO锁屏主题
		 * 23:GO短信主题
		 * 24:超级主题
		 */
		public int tag;
	}

	/**
	 * 分类基本信息单元，用于编辑推荐列表
	 */
	public class BoutiqueTypeInfo implements Serializable {
		/**
		 * 分类id
		 */
		public int typeid;
		/**
		 * 分类名称
		 */
		public String name;
		/**
		 * 简介
		 */
		public String summary;
//		/**
//		 * 应用的等级 分为1—10个级别，用于编辑推荐样式 显示推荐度
//		 */
//		public int grade;
	}

	/**
	 * 应用下载状态
	 */
	public class BoutiqueDownloadState implements Serializable {
		/**
		 * 下载状态标志
		 */
		public int state;
		/**
		 * 已经下载的百分比
		 */
		public int alreadyDownloadPercent;
	}

}

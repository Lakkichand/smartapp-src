package com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans;


/**
 * 
 * 类描述: MessageBaseBean类
 * 功能详细描述: 存放消息内容，具体可参考消息中心的文档
 * @date  [2012-9-28]
 */
public class MessageBaseBean {
	// 单个消息
	public static final String TAG_MSG_ID = "id"; // 消息id(String)
	public static final String TAG_MSG_TITLE = "title"; // 标题(String)
	public static final String TAG_MSG_TYPE = "type"; // 消息类型(int)

	public static final int TYPE_NEWS = 1;
	public static final int TYPE_RECOMMEND = 2;
	public static final int TYPE_HTML = 3;
	/**
	 * 展现方式(int)
	 * 1:普通展现，2: 通知栏展现，4: 弹窗展现
	 * 8：桌面罩子层展现。
	 * 3=1+2则有1和2两种属性，类似5=1+4，7=1+2+4
	 * 可以采用'与'运算判断是否拥有该属性：3&2=2,5&4=4
	 * 以上4种展现方式中，普通展现为消息的必选方式，
	 * 同时可以组合2，4，8中的一种展现。2， 4，8不能同时出现
	 */
	public static final String TAG_MSG_VIEWTYPE = "viewtype";
	public final static int VIEWTYPE_NORMAL = 0X01;
	public final static int VIEWTYPE_STATUS_BAR = 0X02;
	public final static int VIEWTYPE_DIALOG = 0X04;
	public final static int VIEWTYPE_DESK_TOP = 0X08;
	
	public static final String TAG_MSG_TIME = "time"; // 发布时间(String)
	public static final String TAG_MSG_URL = "msgurl"; // html类型消息的url
	/**
	 * 展示起始时间
	 * 格式：HH:mm:ss，用于控制客户端展示消息的时间点。
	 * 无论是用户主动获取消息还是系统自动化获取，都需要严格按照这个时间进行控制
	 */
	public static final String TAG_MSG_START = "stime_start"; 
	/**
	 *  展示结束时间 格式：HH:mm:ss 用于控制客户端展示消息的时间点。
	 * 无论是用户主动获取消息还是系统自动化获取，都需要严格按照这个时间进行控制
	 */
	public static final String TAG_MSG_END = "stime_end"; 
	public static final String TAG_MSG_ICON = "icon"; // 主题消息的icon url
	public static final String TAG_MSG_INTRO = "intro"; // 主题消息的简介 
	public static final String TAG_MSG_ACTTYPE = "acttype"; // 点击的动作类型
	public static final String TAG_MSG_ACTVALUE = "actvalue"; // 点击动作对应的参数值
	/**
	 * 罩子层图标表演动画url
	 * 仅当viewtype包含了8：罩子层展现方式才有.
	 *一套动画图片打包成Zip文件进行下载。内部的文件名按照1,2,3,4命名。
	 *表示动画的帧顺序
	 */
	public static final String TAG_MSG_ZICON1 = "zicon1"; 
	/**
	 * 罩子层图标出场动画url
	 * 仅当viewtype包含了8：罩子层展现方式才有.
	 * 一套动画图片打包成Zip文件进行下载。内部的文件名按照1,2,3,4命名。表示动画的帧顺序
	 */
	public static final String TAG_MSG_ZICON2 = "zicon2"; 
	/*罩子层图标位置  仅当viewtype包含了8：罩子层展现方式才有
	* 1：左上角
	* 2：左侧中间
	* 3：右上角
	* 4：右侧中间
	*/
	public static final String TAG_MSG_ZPOS = "zpos"; 

	public static final String TAG_MSG_ZTIME = "ztime"; // 动画重复时间
	
	public static final String TAG_MSG_ISCLOSED = "isclosed";
	
	public static final String TAG_MSG_FILTET_MSGS = "filter_pkgs";
	
	//通知栏样式变更所需字段
	public static final String TAG_MSG_DYNAMIC = "dynamic";
	
	public static final String TAG_MSG_ICONPOS = "iconpos";
	
	public static final String TAG_MSG_FULL_SCREEN_ICON = "fullscreenicon";
	
	//白名单
	public static final String TAG_MSG_WHITE_LIST = "whitelist";
	
	public static final String TAG_MSG_IS_NEW = "isnew";
	
	/**
	 * 在ftp下载动作规则中
	* MessageContentBean中的murl的内容的分割字符串常量,  url格式如下
	*    http://xxx.apk?packagename=xxx.xxx.xxxx##GO桌面   ，中间不允许有空格 
	*/
	public static final String URL_SPLIT = "packagename=";
	public static final String URL_SPLIT_NAME = "##";

	public String mId;
	public String mTitle;
	public int mType; // 1:资讯消息，2:个人消息(个人推荐等)，html消息（通过webview打开url连接)
	public String mMsgTimeStamp;
	public String mUrl; // htmlurl
	public String mStartTime; // 展示开始时间
	public String mEndTime; // 展示结束时间
	public String mIcon; // 展示图标
	public String mSummery; // 概述
	public int mActType; // 动作类型
	public String mActValue; // 动作参数
	public String mZicon1; // 进场动画资源包
	public String mZicon2; // 表演动画资源包
	public int mZpos; // 位置
	public long mZtime; // 动画重复时间间隔
	public boolean mIsColsed; //是否要点击关闭罩子层
	public String mFilterPkgs; //需过滤的包名列表
	public int mDynamic; //图片是否动态
	public int mIconpos; //图片放置位置
	public String mFullScreenIcon; //全屏图url
	public String mWhiteList; //白名单信息列表
	public int mIsNew;	//已读消息是否重新设为新消息
	
}

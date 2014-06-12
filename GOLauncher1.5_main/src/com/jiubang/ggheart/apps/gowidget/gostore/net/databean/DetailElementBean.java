package com.jiubang.ggheart.apps.gowidget.gostore.net.databean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * <br>类描述:详情界面数据Element
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-9-12]
 */
@SuppressWarnings("serial")
public class DetailElementBean extends BaseBean {
	// 软件id
	public int mId;
	//来源id
	public int mSource;
	//回调URL
	public String mCallbackUrl;
	// 商品图标
	public String mIcon;
	// 产品名称
	public String mName;
	// 包大小，单位为kb
	public String mSize;
	// 版本
	public String mVersion;
	// 版本code
	public int mVersionCode;
	// 包名
	public String mPkgName;
	// 更新时间
	public String mUpdatetime;
	// 价格
	public String mPrice;
	// 固件支持
	public String mSupport;
	// 开发商
	public String mDevelop;
	// 产品简介
	public String mSummary;
	// 产品描述，产品详情
	public String mDetail;
	// 更新内容
	public String mUpdatelog;
	// 缩略图id串
	public ArrayList<String> mImgids;
	// 浏览图片的图片ID集合
	public ArrayList<String> mBigImgIds;
	// 软件包的下载地址
	public String mDownurl;
	// 电子市场下载地址
	public String mMarketurl;
	// 其它下载地址
	public String mOtherurl;
	// 是否热门标识，1：是 0：否
	public int mIsHot;
	// 是否新应用标识，1：是 0：否
	public int mIsNew;
	// 特定渠道区域下软件的星级
	public String mStar;
	// 下载量
	public String mDownloadCount;
	// 推荐ID
	public int mRecomId;
	// 推荐个数
	public int mRecomCount;
	// 推荐应用的List
	public ArrayList<DetailElement> mElementsList = null;
	public String[] mPaytype = null; // 付费类型
	public String mPayid = null; // 电子市场付费ID
	public String mZipDownurl = null; // 资源包下载url(zip包)
	public String mLocker = null; // 配套锁屏包名
	public String mWidget = null; // 配套插件包名
	
	/**
	 * 
	 * <br>类描述:详情对应的相关推荐 的数据Element
	 * <br>功能详细描述:
	 * 
	 * @author  zhouxuewen
	 * @date  [2012-9-12]
	 */
	public class DetailElement implements Serializable {
		public int mElementType = 0; // 类型
		public int mIsHot = 0; // 是否热门标识，1：是 0：否
		public int mIsNew = 0; // 是否新应用标识，1：是 0：否
		public String mStar = null; // 特定渠道区域下软件的星级
		public String mLogoIconId = null; // 应用iconid
		public int mId = 0; // 软件id
		public int mStyle; //详情风格
		public int mSource; //来源id
		public String mCallbackUrl; //回调URL
		public String mName = null; // 产品名称
		public String mType = null; // 产品类别
		public String mDetail = null; // 产品描述
		public String mVersion = null; // 版本
		public int mVersionCode = 0; // 版本code
		public String mPkgName = null; // 包名
		public String mPrice = null; // 价格（单位美元）
		public String mSize = null; // 包大小
		public int mUrlNum = 0; // 下发url地址个数
		public HashMap<Integer, String> mUrlMap = null; // 链接地址
		public String mImgId = null; // 缩略图id
		public String mUpdateTime = null; // 更新时间
		public String mDownloadCount = null; // 下载量
		public String[] mPaytype = null; // 付费类型
		public String mPayid = null; // 电子市场付费ID
		public String mDownurl = null; // 资源包下载url(zip包)
		public String mLocker = null; // 配套锁屏包名
		public String mWidget = null; // 配套插件包名

	}
}

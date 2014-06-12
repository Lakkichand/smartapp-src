package com.jiubang.ggheart.data.theme.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
/**
 * @date  [2012-9-28]
 */
public class FeaturedDataListBean extends BaseBean {
	public int mTotalNum = 0; // 总结果数量 暂未用到
	public int mCurrentPage = 0; // 当前页码 暂未用到
	public int mTotalPage = 0; // 总页数 暂未用到
	public int mElementCount = 0; // 下发数据条数
	public boolean mHasNewTheme = false; // 是否有新推主题
	public boolean mShowStatusNotify = false; //是否在通之栏上显示
	public long mShowStartTime; //显示开始时间
	public long mShowEndTime;
	public String mShowContent; //显示内容
	public String mShowIconUrl;
	public int mRetryPayTimes;
	public ArrayList<FeaturedElement> mElementsList = null;

	/**
	 * 
	 * @date  [2012-9-28]
	 */
	public class FeaturedElement implements Serializable {
		public int mId = 0; // 软件id
		public boolean mIsNew = false;
		public String mName = null; // 产品名称
		public String mType = null; // 产品类别
		public String mDetail = null; // 产品描述
		public String mVersion = null; // 版本
		public int mVersionCode = 0; // 版本code
		public String mPkgName = null; // 包名
		public String mPrice = null; // 价格（单位美元）
		public int mFeeType = 0; // 应用费用，0：免费，1：收费，2：getjar，3：Google Play包月
		public String mSize = null; // 包大小
		public int mUrlNum = 0; // 下发url地址个数
		public HashMap<Integer, String> mUrlMap = null; // 链接地址
		public String mImgId = null; // 缩略图id
		public String mUpdateTime = null; // 更新时间
		public String mDownloadCount = null; // 下载量
		public int mSortId = 0; // 分类id
		public int mParentId = 0; // 分类夫分类ID
		public int mPropertyId = 0; // 分类属性ID
		public byte mChildtype = 0; // 分类类别
		public String mSortName = null; // 分类名字
		public int mIsall = 0; // 是否大主题
		public int mIssale = 0; // 是否限时优惠
		public List<String> mPayType;
		public String mPayId; //电子市场付费ID
		public String mDownurl;
		public String mMlocker;
		public String mMwidget;
		public int mImgsource; //0:使用imgid拿图片		1：使用imgurl拿图片，如果imgurl为空，则用imgid拿图片
		public List<String> mImgUrlArrary;
		//		public String mSummary;//产品简介

	}
}

package com.zhidian.wifibox.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 一个页面的数据封装
 * 
 * 根据mDataType读不同的数据列表
 * 
 * @author xiedezhi
 * 
 */
public class PageDataBean implements Serializable {
	/**
	 * 专题内容列表应用数据
	 */
	public static final int TOPICCONTENT_DATATYPE = 1002;
	/**
	 * 专题数据
	 */
	public static final int TOPIC_DATATYPE = 1003;
	/**
	 * 下载管理页数据
	 */
	public static final int DOWNLOADPAGE_DATATYPE = 1005;
	/**
	 * 应用更新页数据
	 */
	public static final int UPDATEPAGE_DATATYPE = 1006;
	/**
	 * 管理界面
	 */
	public static final int MANAGER_DATATYPE = 1014;

	/**
	 * 首页推荐
	 */
	public static final int HOME_FEATURE_DATATYPE = 2001;
	/**
	 * 空界面，但该界面下有N个子界面
	 */
	public static final int EMPTY_DATATYPE = 2002;
	/**
	 * 应用游戏列表
	 */
	public static final int EXTRA_DATATYPE = 2003;
	/**
	 * 超速下载
	 */
	public static final int SPEEDINGDOWNLOAD_DATATYPE = 2004;
	/**
	 * 分类
	 */
	public static final int CATEGORIES_DATATYPE = 2005;

	/**
	 * 排行
	 */
	public static final int EXTRA_RANK = 2006;

	/**
	 * 代表下载管理页面的URL
	 */
	public static final String DOWNLOADMANAGER_URL = "PAGEDATABEAN_DOWNLOADMANAGER_URL";
	/**
	 * 代表管理页面的URL
	 */
	public static final String MANAGER_URL = "PAGEDATABEAN_MANAGER_URL";

	/**
	 * 显示类型是否排行榜，是的话列表要展示应用序号
	 */
	public boolean mIsRanking = false;

	/**
	 * 该页面的请求URL，这个应该是第一页的请求URL，作为页面的id
	 */
	public String mUrl;
	/**
	 * 页面标题
	 */
	public String mTitle;
	/**
	 * 0=请求成功，1=服务器内部错误，2=客户端联网错误，-1=还没开始请求数据，9=file no found
	 */
	public int mStatuscode = -1;
	/**
	 * 通知页面第一次初始化时先加载本地数据再读取网络数据，只用在第一页
	 */
	public boolean mLoadLocalDataFirst = false;
	/**
	 * 服务器返回的信息
	 */
	public String mMessage;
	/**
	 * 当前页码，从1开始
	 */
	public int mPageIndex;
	/**
	 * 列表总页数
	 */
	public int mTotalPage;
	/**
	 * 数据类型
	 */
	public int mDataType;
	/**
	 * 分类数据列表，当datatype为CATEGORIES_DATATYPE时使用
	 */
	public List<CategoriesDataBean> mCatList = new ArrayList<CategoriesDataBean>();
	/**
	 * 应用数据列表， 当datatype为TOPICCONTENT_DATATYPE或EXTRA_DATATYPE时使用
	 */
	public List<AppDataBean> mAppList = new ArrayList<AppDataBean>();
	/**
	 * 专题数据列表，当datatype为TOPIC_DATATYPE时使用
	 */
	public List<TopicDataBean> mTopicList = new ArrayList<TopicDataBean>();
	/**
	 * 应用更新数据列表，当datatype为UPDATEPAGE_DATATYPE时使用
	 */
	public List<UpdateAppBean> uAppList = new ArrayList<UpdateAppBean>();
	/**
	 * 极速模式最新推荐应用数据列表
	 */
	public List<XAppDataBean> mXAppList = new ArrayList<XAppDataBean>();
	/**
	 * 极速模式装机必备数据列表
	 */
	public List<XMustDataBean> mXMustList = new ArrayList<XMustDataBean>();
	/**
	 * 首页推荐列表数据，当datatype为HOME_FEATURE_DATATYPE时使用
	 */
	public HomeFeatureDataBean mHomeFeatureDataBean;
	/**
	 * 该页面的子页面，只用在mDataType为EMPTY_DATATYPE的情况
	 */
	public List<PageDataBean> mSubContainer = new ArrayList<PageDataBean>();
	/**
	 * 当前显示的子页面，只用在mDataType为EMPTY_DATATYPE的情况
	 */
	public int mIndex = 0;

	/**
	 * 应用评论列表
	 */
	// TODO 这个移走
	public List<CommentBean> commentList = new ArrayList<CommentBean>();
	/**
	 * 专题大小信息，用于专题详情列表
	 */
	public String sizeMessage = "";
	/**
	 * 专题名字，用于专题详情列表
	 */
	public String titleMessage = "";
	/**
	 * 专题详情描述，用于专题详情列表
	 */
	public String detailMessage = "";
	/**
	 * 专题详情图片，用于专题详情列表
	 */
	public String iconMessage = "";
	/**
	 * 专题详情描述，大小
	 */
	public String AppSize = "";
	/**
	 * 专题详情描述，个数
	 */
	public int amount = 0;
	/**
	 * 统计标题
	 */
	public String mStatisticsTitle = "";

	/**
	 * 重置数据
	 */
	public void reset() {
		mStatuscode = -1;
		mMessage = null;
		mPageIndex = 0;
		mTotalPage = 0;
		mCatList = new ArrayList<CategoriesDataBean>();
		mAppList = new ArrayList<AppDataBean>();
		mTopicList = new ArrayList<TopicDataBean>();
		uAppList = new ArrayList<UpdateAppBean>();
		mXAppList = new ArrayList<XAppDataBean>();
		mXMustList = new ArrayList<XMustDataBean>();
		mHomeFeatureDataBean = null;
		sizeMessage = "";
		titleMessage = "";
		detailMessage = "";
		iconMessage = "";
		amount = 0;
		AppSize = "";
	}

	/**
	 * 把follow的页面数据添加到当前数据的后面
	 */
	public void append(PageDataBean follow) {
		if (follow == this) {
			return;
		}
		if (follow == null || follow.mStatuscode != 0
				|| follow.mDataType != this.mDataType
				|| (!follow.mUrl.equals(this.mUrl))) {
			return;
		}
		// 判断follow是否为当前数据的下一页
		if (follow.mPageIndex != mPageIndex + 1
				&& follow.mDataType != PageDataBean.SPEEDINGDOWNLOAD_DATATYPE) {
			return;
		}
		if (follow.mCatList != null && follow.mCatList.size() > 0) {
			this.mCatList = follow.mCatList;
		}
		if (follow.mAppList != null && follow.mAppList.size() > 0) {
			if (this.mAppList != null) {
				this.mAppList.addAll(follow.mAppList);
			} else {
				this.mAppList = follow.mAppList;
			}
		}
		if (follow.mTopicList != null && follow.mTopicList.size() > 0) {
			if (this.mTopicList != null) {
				this.mTopicList.addAll(follow.mTopicList);
			} else {
				this.mTopicList = follow.mTopicList;
			}
		}
		if (follow.uAppList != null && follow.uAppList.size() > 0) {
			this.uAppList = follow.uAppList;
		}
		if (follow.mHomeFeatureDataBean != null) {
			this.mHomeFeatureDataBean = follow.mHomeFeatureDataBean;
		}
		if (follow.mXAppList != null && follow.mXAppList.size() > 0) {
			this.mXAppList = follow.mXAppList;
		}
		if (follow.mXMustList != null && follow.mXMustList.size() > 0) {
			this.mXMustList = follow.mXMustList;
		}
		// 更新页码
		if (follow.mDataType != PageDataBean.SPEEDINGDOWNLOAD_DATATYPE) {
			mPageIndex++;
		}
		mTotalPage = follow.mTotalPage;
		// 更新statuscode
		mStatuscode = 0;
		// 更新专题详情列表信息
		this.sizeMessage = follow.sizeMessage;
		this.titleMessage = follow.titleMessage;
		this.detailMessage = follow.detailMessage;
		this.iconMessage = follow.iconMessage;
		this.amount = follow.amount;
		this.AppSize = follow.AppSize;

	}

	/**
	 * 用newPage第一页数据替换当前page的第一页数据
	 */
	public void replace(PageDataBean newPage) {
		if (newPage == null || newPage.mStatuscode != 0
				|| newPage.mDataType != this.mDataType
				|| (!newPage.mUrl.equals(this.mUrl))) {
			return;
		}
		if (this.mCatList != null && this.mCatList.size() > 0
				&& newPage.mCatList != null && newPage.mCatList.size() > 0) {
			this.mCatList = newPage.mCatList;
		}
		if (this.mAppList != null && this.mAppList.size() > 0
				&& newPage.mAppList != null && newPage.mAppList.size() > 0) {
			// if (this.mAppList.size() <= newPage.mAppList.size()) {
			this.mAppList = newPage.mAppList;
			this.mPageIndex = newPage.mPageIndex;
			this.mTotalPage = newPage.mTotalPage;
			// } else {
			// for (int i = 0; i < newPage.mAppList.size(); i++) {
			// this.mAppList.set(i, newPage.mAppList.get(i));
			// }
			// }
		}
		if (this.mTopicList != null && this.mTopicList.size() > 0
				&& newPage.mTopicList != null && newPage.mTopicList.size() > 0) {
			this.mTopicList = newPage.mTopicList;
		}
		if (newPage.mHomeFeatureDataBean != null) {
			this.mHomeFeatureDataBean = newPage.mHomeFeatureDataBean;
		}
	}
}

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
	 * 推荐列表应用数据
	 */
	public static final int FEATURE_DATATYPE = 1001;
	/**
	 * 专题内容列表应用数据
	 */
	public static final int TOPICCONTENT_DATATYPE = 1002;
	/**
	 * 专题数据
	 */
	public static final int TOPIC_DATATYPE = 1003;
	/**
	 * 幻灯片数据
	 */
	public static final int BANNER_DATATYPE = 1004;
	/**
	 * 下载管理页数据
	 */
	public static final int DOWNLOADPAGE_DATATYPE = 1005;
	/**
	 * 应用更新页数据
	 */
	public static final int UPDATEPAGE_DATATYPE = 1006;
	/**
	 * 应用分类页数据
	 */
	public static final int CATEGORIES_DATATYPE = 1007;
	/**
	 * 应用游戏列表数据
	 */
	public static final int EXTRA_DATATYPE = 1008;
	/**
	 * 分类内容列表
	 */
	public static final int CATEGORY_CONTENT_DATATYPE = 1009;
	/**
	 * 排行榜列表
	 */
	public static final int RANKING_DATATYPE = 1010;
	/**
	 * 管理界面
	 */
	public static final int MANAGER_DATATYPE = 1014;

	/**
	 * 极速模式最新
	 */
	public static final int XNEW_DATATYPE = 1011;
	/**
	 * 极速模式装机必备
	 */
	public static final int XMUST_DATATYPE = 1012;
	/**
	 * 极速模式全部
	 */
	public static final int XALL_DATATYPE = 1013;

	/**
	 * 代表下载管理页面的URL
	 */
	public static final String DOWNLOADMANAGER_URL = "PAGEDATABEAN_DOWNLOADMANAGER_URL";
	/**
	 * 代表管理页面的URL
	 */
	public static final String MANAGER_URL = "PAGEDATABEAN_MANAGER_URL";

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
	 * 应用数据列表，
	 * 当datatype为FEATURE_DATATYPE或TOPICCONTENT_DATATYPE或EXTRA_DATATYPE或CATEGORY_CONTENT_DATATYPE时使用
	 */
	public List<AppDataBean> mAppList = new ArrayList<AppDataBean>();
	/**
	 * 专题数据列表，当datatype为TOPIC_DATATYPE时使用
	 */
	public List<TopicDataBean> mTopicList = new ArrayList<TopicDataBean>();
	/**
	 * 幻灯片数据列表，当datatype为BANNER_DATATYPE时使用
	 */
	public List<BannerDataBean> mBannerList = new ArrayList<BannerDataBean>();
	/**
	 * 应用更新数据列表，当datatype为UPDATEPAGE_DATATYPE时使用
	 */
	public List<UpdateAppBean> uAppList = new ArrayList<UpdateAppBean>();
	/**
	 * 极速模式全部应用数据列表
	 */
	public List<XAllDataBean> mXAllList = new ArrayList<XAllDataBean>();
	/**
	 * 极速模式最新推荐应用数据列表
	 */
	public List<XAppDataBean> mXAppList = new ArrayList<XAppDataBean>();
	/**
	 * 极速模式装机必备数据列表
	 */
	public List<XMustDataBean> mXMustList = new ArrayList<XMustDataBean>();

	/**
	 * 应用评论列表
	 */
	public List<CommentBean> commentList = new ArrayList<CommentBean>();
	/**
	 * 是否展示Banner，默认为false
	 */
	public boolean mShowBanner = false;
	/**
	 * 用于向服务器获取数据的名字
	 */
	public String mBannerMark;
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
	 * 把follow的页面数据添加到当前数据的后面
	 */
	public void append(PageDataBean follow) {
		if (follow == null || follow.mStatuscode != 0
				|| follow.mDataType != this.mDataType
				|| (!follow.mUrl.equals(this.mUrl))) {
			return;
		}
		// 判断follow是否为当前数据的下一页
		if (follow.mPageIndex != mPageIndex + 1) {
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
		if (follow.mXAllList != null && follow.mXAllList.size() > 0) {
			this.mXAllList = follow.mXAllList;
		}
		if (follow.mXAppList != null && follow.mXAppList.size() > 0) {
			this.mXAppList = follow.mXAppList;
		}
		if (follow.mXMustList != null && follow.mXMustList.size() > 0) {
			this.mXMustList = follow.mXMustList;
		}
		// 更新页码
		mPageIndex++;
		mTotalPage = follow.mTotalPage;
		// 更新statuscode
		mStatuscode = 0;
		// 更新专题详情列表信息
		this.sizeMessage = follow.sizeMessage;
		this.titleMessage = follow.titleMessage;
		this.detailMessage = follow.detailMessage;
		this.iconMessage = follow.iconMessage;
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
			// if (this.mTopicList.size() <= newPage.mTopicList.size()) {
			this.mTopicList = newPage.mTopicList;
			// } else {
			// for (int i = 0; i < newPage.mTopicList.size(); i++) {
			// this.mTopicList.set(i, newPage.mTopicList.get(i));
			// }
			// }
		}
		if (this.mBannerList != null && this.mBannerList.size() > 0
				&& newPage.mBannerList != null
				&& newPage.mBannerList.size() > 0) {
			this.mBannerList = newPage.mBannerList;
		}
	}
}

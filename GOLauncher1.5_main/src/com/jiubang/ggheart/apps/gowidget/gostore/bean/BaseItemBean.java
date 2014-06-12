package com.jiubang.ggheart.apps.gowidget.gostore.bean;

import java.util.HashMap;

/**
 * 内容列表项的BEAN
 * 
 * @author wangzhuobin
 * 
 */
public class BaseItemBean {

	// 数据类型常量
	// 软件类型
	public static final int DATA_TYPE_SOFTWARE = 1;
	// 链接类型
	public static final int DATA_TYPE_LINKED = 2;
	// TAB类型
	public static final int DATA_TYPE_TAB = 3;
	// 专题类型
	public static final int DATA_TYPE_TOPIC = 4;
	// 分类类型
	public static final int DATA_TYPE_SORT = 5;
	// 壁纸类型
	public static final int DATA_TYPE_WALLPAPER = 6;
	// 数据类型
	private int mDataType;
	// 显示图片
	private String mItemIconBitmapId;
	// 来源ID
	private int mSource;
	// 回调URL
	private String mCallbackUrl;
	// 显示图标
	private String mItemIconId;
	// 名称
	private String mItemNameString;
	// 类别
	private String mCategory;
	// 版本
	private String mVerString;
	// 版本code
	private int mVerCode;
	// 包名
	private String mPkgName = null;
	// 价格
	private String mPrice;
	// 是否免费,默认为免费
	private boolean mIsFree = true;
	// 下载次数
	private String mDownloadCount;
	// 安装包大小
	private String mInstallPackageSize;
	// 更新日期
	private String mUpateTime;
	// 操作数据，如果是点击下载的话这操作数据就是链接地址
	// 由于分类界面直接使用跳转地址，因此此处尚保留分类的跳转地址
	private String mOperateDataString;
	// 链接地址
	public HashMap<Integer, String> mUrlMap = null;
	// 是否热门标识，1：是 0：否
	public int mIsHot = 0;
	// 是否新应用标识，1：是 0：否
	public int mIsNew = 0;
	// 特定渠道区域下软件的星级
	public String mStar = null;
	// TagId用于记录Item所有列表位置
	public int mTagId = 0;
	// 描述信息
	private String mDescriptionString;
	// 广告ID
	private int mAdID;
	// 推荐位，如果不是推荐项，则为默认值0
	// 推荐位从1开始计数
	private int mPosition = 0;

	// 分类ID
	private int mSortId = 0;

	// 分类属性id,值保存在表
	private int mPropertyId = 0;

	// 该分类下一级的展示形式
	private byte mChildType = 0;

	// 分类名字
	private String mSortName = null;

	// 夫分类名字
	private int mParentId = 0;
	
	// 详情风格
	private int mStyle = 0;

	private String[] mPaytype = null; // 付费类型
	private String mPayid = null; // 电子市场付费ID
	private String mDownurl = null; // 资源包下载url(zip包)
	private String mLocker = null; // 配套锁屏包名
	private String mWidget = null; // 配套插件包名

	public int getStyle() {
		return mStyle;
	}

	public void setStyle(int mStyle) {
		this.mStyle = mStyle;
	}
	
	public HashMap<Integer, String> getUrlMap() {
		return mUrlMap;
	}

	public void setUrlMap(HashMap<Integer, String> urlMap) {
		this.mUrlMap = urlMap;
	}

	public String getPkgName() {
		return mPkgName;
	}

	public void setPkgName(String pkgName) {
		this.mPkgName = pkgName;
	}

	public void setSource(int soruce) {
		this.mSource = soruce;
	}

	public int getSource() {
		return this.mSource;
	}

	public void setCallbackUrl(String callbackUrl) {
		this.mCallbackUrl = callbackUrl;
	}

	public String getCallbackUrl() {
		return this.mCallbackUrl;
	}

	public String getItemIconBitmapId() {
		return mItemIconBitmapId;
	}

	public void setItemIconBitmapId(String itemIconBitmapId) {
		this.mItemIconBitmapId = itemIconBitmapId;
	}

	public String getItemIconId() {
		return mItemIconId;
	}

	public void setItemIconId(String itemIconId) {
		this.mItemIconId = itemIconId;
	}

	public String getItemNameString() {
		return mItemNameString;
	}

	public void setItemNameString(String itemNameString) {
		this.mItemNameString = itemNameString;
	}

	public String getCategory() {
		return mCategory;
	}

	public void setCategory(String category) {
		this.mCategory = category;
	}

	public String getVerString() {
		return mVerString;
	}

	public void setVerString(String verString) {
		this.mVerString = verString;
	}

	public String getPrice() {
		return mPrice;
	}

	public void setPrice(String price) {
		this.mPrice = price;
		// 根据判断免费还是收费
		if (price != null && !"".equals(price.trim())) {
			price = price.trim();
			if (price.equals("0") || price.equals("0.0") || price.equals("0.00")
					|| price.equals("$0.0") || price.contains("$0.00")) {
				setIsFree(true);
			} else {
				setIsFree(false);
			}
		}
	}

	public boolean isFree() {
		return mIsFree;
	}

	public void setIsFree(boolean isFree) {
		this.mIsFree = isFree;
	}

	public String getDownloadCount() {
		return mDownloadCount;
	}

	public void setDownLoadCount(String downLoadCount) {
		this.mDownloadCount = downLoadCount;
	}

	public String getInstallPackageSize() {
		return mInstallPackageSize;
	}

	public void setInstallPackageSize(String installPackageSize) {
		this.mInstallPackageSize = installPackageSize;
	}

	public String getUpateTime() {
		return mUpateTime;
	}

	public void setUpateTime(String updateTime) {
		this.mUpateTime = updateTime;
	}

	public String getOperateDataString() {
		return mOperateDataString;
	}

	public void setOperateDataString(String operateDataString) {
		this.mOperateDataString = operateDataString;
	}

	public String getDescriptionString() {
		return mDescriptionString;
	}

	public void setDescriptionString(String descriptionString) {
		this.mDescriptionString = descriptionString;
	}

	public int getVerCode() {
		return mVerCode;
	}

	public void setVerCode(int mVerCode) {
		this.mVerCode = mVerCode;
	}

	public int getAdID() {
		return mAdID;
	}

	public void setAdID(int adID) {
		this.mAdID = adID;
	}

	public int getPosition() {
		return mPosition;
	}

	public void setPosition(int position) {
		this.mPosition = position;
	}

	public int getDataType() {
		return mDataType;
	}

	public void setDataType(int dataType) {
		this.mDataType = dataType;
	}

	public int getIsHot() {
		return mIsHot;
	}

	public void setIsHot(int isHot) {
		this.mIsHot = isHot;
	}

	public int getIsNew() {
		return mIsNew;
	}

	public void setIsNew(int isNew) {
		this.mIsNew = isNew;
	}

	public String getStar() {
		return mStar;
	}

	public void setStar(String star) {
		this.mStar = star;
	}

	public void recycle() {
		mItemIconBitmapId = null;
		mItemNameString = null;
		mCategory = null;
		mVerString = null;
		mPrice = null;
		mInstallPackageSize = null;
		mUpateTime = null;
		mPkgName = null;
		mOperateDataString = null;
		if (mUrlMap != null) {
			mUrlMap.clear();
			mUrlMap = null;
		}
		mDescriptionString = null;
		mStar = null;
		mPaytype = null;
		mPayid = null;
		mDownurl = null;
		mWidget = null;
		mWidget = null;
	}

	public void setSortId(int sortid) {
		this.mSortId = sortid;
	}

	public int getSortId() {
		return mSortId;
	}

	public void setPropertyId(int propertyid) {
		this.mPropertyId = propertyid;
	}

	public int getPropertyId() {
		return mPropertyId;
	}

	public void setChildType(byte childtype) {
		this.mChildType = childtype;
	}

	public byte getChildType() {
		return mChildType;
	}

	public void setSortName(String sortname) {
		this.mSortName = sortname;
	}

	public String getSortName() {
		return mSortName;
	}

	public void setParentId(int parentid) {
		this.mParentId = parentid;
	}

	public int getParentId() {
		return mParentId;
	}

	public void setPaytype(String[] paytype) {
		this.mPaytype = paytype;
	}

	public String[] getPaytype() {
		return mPaytype;
	}

	public void setPayId(String payid) {
		this.mPayid = payid;
	}

	public String getPayId() {
		return mPayid;
	}

	public void setDownurl(String downurl) {
		this.mDownurl = downurl;
	}

	public String getDownurl() {
		return mDownurl;
	}

	public void setLocker(String locker) {
		this.mLocker = locker;
	}

	public String getLocker() {
		return mLocker;
	}

	public void setWidget(String widget) {
		this.mWidget = widget;
	}

	public String getWidget() {
		return mWidget;
	}

	
	
}

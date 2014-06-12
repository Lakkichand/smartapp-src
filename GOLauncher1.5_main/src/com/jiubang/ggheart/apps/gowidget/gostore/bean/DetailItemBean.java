package com.jiubang.ggheart.apps.gowidget.gostore.bean;

import java.util.ArrayList;

import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.DetailElementBean.DetailElement;

/**
 * 
 * <br>类描述:精品详情界面BEAN
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-9-12]
 */
public class DetailItemBean {
	// 商品ID
	private int mId;
	// 显示图片
	private String mItemIconBitmapId;
	//来源
	private int mSource;
	// 回调URL
	private String mCallbackUrl;
	// 名称
	private String mItemNameString;
	// 安装包大小
	private String mInstallPackageSize;
	// 版本
	private String mVerString;
	// 版本code
	private int mVerCode;
	// 包名
	private String mPkgName;
	// 更新日期
	private String mUpateTime;
	// 价格
	private String mPrice;
	// 是否免费,默认为免费
	private boolean mIsFree = true;;
	// 固件支持
	private String mFirmwareSupport;
	// 开发商
	private String mDeveloper;
	// 产品详情
	private String mDetailDescriptionString;
	// 更新内容
	private String mUpdateContent;
	// 产品缩略图ID集合
	private ArrayList<String> mItemThumbnailIdsArrayList;
	// 图片浏览的图片ID集合
	private ArrayList<String> mScanImageIdsArrayList;
	// 软件包的下载地址
	private String mDownurl;
	// 电子市场下载地址
	private String mMarketurl;
	// 其它下载地址
	private String mOtherurl;
	// 是否热门标识，1：是 0：否
	private int mIsHot;
	// 是否新应用标识，1：是 0：否
	private int mIsNew;
	// 特定渠道区域下软件的星级
	private String mStar;
	// 下载次数
	private String mDownloadCount;

	// 推荐ID
	private int mRecomId;
	// 推荐个数
	private int mRecomCount;
	// 推荐应用的List
	private ArrayList<DetailElement> mElementsList = null;

	private String[] mPaytype = null; // 付费类型
	private String mPayid = null; // 电子市场付费ID
	private String mZipDownurl = null; // 资源包下载url(zip包)
	private String mLocker = null; // 配套锁屏包名
	private String mWidget = null; // 配套插件包名

	// 各字段的get、set方法
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void setRecomId(int recomId) {
		this.mRecomId = recomId;
	}

	public int getRecomId() {
		return mRecomId;
	}

	public void setSource(int source) {
		this.mSource = source;
	}

	public int getSource() {
		return this.mSource;
	}

	public void setCallbackUrl(String url) {
		this.mCallbackUrl = url;
	}

	public String getCallbackUrl() {
		return this.mCallbackUrl;
	}

	public void setRecomCount(int recomCount) {
		this.mRecomCount = recomCount;
	}

	public int getRecomCount() {
		return mRecomCount;
	}

	public void setElementsList(ArrayList<DetailElement> elementList) {
		mElementsList = elementList;
	}

	public ArrayList<DetailElement> getDetailElement() {
		return mElementsList;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
	}

	public String getItemIconBitmapId() {
		return mItemIconBitmapId;
	}

	public void setItemIconBitmapId(String itemIconBitmapId) {
		this.mItemIconBitmapId = itemIconBitmapId;
	}

	public String getItemNameString() {
		return mItemNameString;
	}

	public void setItemNameString(String itemNameString) {
		this.mItemNameString = itemNameString;
	}

	public String getInstallPackageSize() {
		return mInstallPackageSize;
	}

	public void setInstallPackageSize(String installPackageSize) {
		this.mInstallPackageSize = installPackageSize;
	}

	public String getVerString() {
		return mVerString;
	}

	public void setVerString(String verString) {
		this.mVerString = verString;
	}

	public int getVerCode() {
		return mVerCode;
	}

	public void setVerCode(int verCode) {
		this.mVerCode = verCode;
	}

	public String getPkgName() {
		return mPkgName;
	}

	public void setPkgName(String pkgName) {
		this.mPkgName = pkgName;
	}

	public String getUpateTime() {
		return mUpateTime;
	}

	public void setUpateTime(String upateTime) {
		this.mUpateTime = upateTime;
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

	public String getFirmwareSupport() {
		return mFirmwareSupport;
	}

	public void setFirmwareSupport(String firmwareSupport) {
		this.mFirmwareSupport = firmwareSupport;
	}

	public String getDeveloper() {
		return mDeveloper;
	}

	public void setDeveloper(String developer) {
		this.mDeveloper = developer;
	}

	public String getDetailDescriptionString() {
		return mDetailDescriptionString;
	}

	public void setDetailDescriptionString(String detailDescriptionString) {
		this.mDetailDescriptionString = detailDescriptionString;
	}

	public String getUpdateContent() {
		return mUpdateContent;
	}

	public void setUpdateContent(String updateContent) {
		this.mUpdateContent = updateContent;
	}

	public ArrayList<String> getItemThumbnailIdsArrayList() {
		return mItemThumbnailIdsArrayList;
	}

	public void setItemThumbnailIdsArrayList(ArrayList<String> itemThumbnailIdsArrayList) {
		this.mItemThumbnailIdsArrayList = itemThumbnailIdsArrayList;
	}

	public ArrayList<String> getScanImageIdsArrayList() {
		return mScanImageIdsArrayList;
	}

	public void setScanImageIdsArrayList(ArrayList<String> scanImageIdsArrayList) {
		this.mScanImageIdsArrayList = scanImageIdsArrayList;
	}

	public String getDownurl() {
		return mDownurl;
	}

	public void setDownurl(String downurl) {
		this.mDownurl = downurl;
	}

	public String getMarketurl() {
		return mMarketurl;
	}

	public void setMarketurl(String marketurl) {
		this.mMarketurl = marketurl;
	}

	public String getOtherurl() {
		return mOtherurl;
	}

	public void setOtherurl(String otherurl) {
		this.mOtherurl = otherurl;
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

	public String getDownloadCount() {
		return mDownloadCount;
	}

	public void setDownloadCount(String downloadCount) {
		this.mDownloadCount = downloadCount;
	}

	/**
	 * 添加缩略图的方法
	 * 
	 * @param bitmap
	 *            缩略图
	 */
	public void addItemThumbnail(String bitmapId) {
		if (mItemThumbnailIdsArrayList == null) {
			mItemThumbnailIdsArrayList = new ArrayList<String>();
		}
		mItemThumbnailIdsArrayList.add(bitmapId);
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

	public void setZipDownurl(String zipdownurl) {
		this.mZipDownurl = zipdownurl;
	}

	public String getZipDownurl() {
		return mZipDownurl;
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

	public void recycle() {
		mPaytype = null;
		mPayid = null;
		mZipDownurl = null;
		mWidget = null;
		mWidget = null;
		mItemIconBitmapId = null;
		mItemNameString = null;
		mInstallPackageSize = null;
		mVerString = null;
		mPkgName = null;
		mUpateTime = null;
		mPrice = null;
		mFirmwareSupport = null;
		mDeveloper = null;
		mDetailDescriptionString = null;
		mUpdateContent = null;
		if (mItemThumbnailIdsArrayList != null) {
			mItemThumbnailIdsArrayList.clear();
			mItemThumbnailIdsArrayList = null;
		}
		if (mScanImageIdsArrayList != null) {
			mScanImageIdsArrayList.clear();
			mScanImageIdsArrayList = null;
		}
		mDownurl = null;
		mMarketurl = null;
		mOtherurl = null;
		mStar = null;
		mDownloadCount = null;
	}

}

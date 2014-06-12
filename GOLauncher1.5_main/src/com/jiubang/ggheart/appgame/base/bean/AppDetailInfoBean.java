/**
 * 
 */
package com.jiubang.ggheart.appgame.base.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liguoliang
 * 
 */
public class AppDetailInfoBean {
	public int mAppId; // 应用id
	public String mPkgName; // 应用包名
	public String mName; // 应用程序名
	public String mIconUrl; // 应用图标url
	public String mVersion; // 版本名称
	public String mVersionCode; // 版本code
	public String mSize; // 安装包大小
	public String mAtype; //软件所属分类,暂不需要用到
	public String mPrice; // 价格
	public boolean mIsFree; // 是否免费
	public int mGrade; // 应用等级
	public String mDownloadCount; // 下载次数
	public String mFirmwareSupport; // 固件支持,如android1.6以上
	public String mDeveloper; // 开发者
	public String mDetail; // 产品详情
	public String mUpdateLog; // 软件更新日志
	public int mDownloadType; // 下载方式
	public String mDownloadUrl; // 下载地址
	public String mPicUrl; // 截图的下载地址
	public ArrayList<String> mSmallPicIds; // 小图片id串
	public ArrayList<String> mLargePicIds; // 大图片id串
	public ArrayList<String> mSmallPicUrls; // 小图片url串
	public ArrayList<String> mLargePicUrls; // 大图片url串
	public String mUpdateTime; // 更新时间
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

	/**
	 * 安全认证信息
	 */
	public SecurityInfo mSecurityInfo;
	
	/**
	 * 编辑推荐语
	 */
	public String mRemdmsg;
	
	/**
	 * 付费类型，如果没有付费类型则为空
	 */
	public int[] mPayType;
	
	/**
	 * 电子市场付费id
	 */
	public String mPayId;
	
	/**
	 * 资源包下载地址
	 */
	public String mResourceUrl;
	/**
	 * 回调地址
	 */
	public String mIcbackUrl;
	/**
	 * TODO 木瓜sdk回调
	 */
	public String mAfCbackUrl;
	/**
	 * 详情风格
	 */
	public int mDetailStlye;
	/**
	 * 应用类型
	 */
	public int mTag;
	
	public List<BoutiqueApp> mRecomApps; // 相关推荐应用
	public int mRecmdId; // 相关推荐分类id

	public String mLocalIconName; // 保存在本地ICON图片的文件名

	public static final String IMAGE_URL = "http://goappcenter.3g.net.cn/recommendedapp/pic.do?k=";

	public static final int DOWNLOAD_TYPE_FTP = 1; // 下载方式
	public static final int DOWNLOAD_TYPE_MARKET = 2;
	public static final int DOWNLOAD_TYPE_WEBMARKET = 3;

	public void recycle() {
		mPkgName = null;
		mName = null;
		mIconUrl = null;
		mVersion = null;
		mVersionCode = null;
		mSize = null;
		// mAtype = null;
		mPrice = null;
		mDownloadCount = null;
		mFirmwareSupport = null;
		mDeveloper = null;
		mDetail = null;
		mUpdateLog = null;
		mDownloadUrl = null;
		if (mSmallPicIds != null) {
			mSmallPicIds.clear();
			mSmallPicIds = null;
		}
		if (mLargePicIds != null) {
			mLargePicIds.clear();
			mLargePicIds = null;
		}
		if (mSmallPicUrls != null) {
			mSmallPicUrls.clear();
			mSmallPicUrls = null;
		}
		if (mLargePicUrls != null) {
			mLargePicUrls.clear();
			mLargePicUrls = null;
		}
		mUpdateTime = null;
		if (mRecomApps != null) {
			mRecomApps.clear();
			mRecomApps = null;
		}
		mPayType = null;
		mPayId = null;
		mResourceUrl = null;
	}
}

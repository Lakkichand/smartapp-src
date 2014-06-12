package com.jiubang.ggheart.data.theme.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.jiubang.ggheart.data.BroadCaster;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.theme.ThemeManager;
/**
 * 
 * 类描述:
 * 功能详细描述:
 * @date  [2012-9-28]
 */
public class ThemeInfoBean extends ThemeBean implements Parcelable {

	public static final int EVENT_TYPE_CHANGE_THEME_NAME = 1; // 改变名称
	public static final int EVENT_TYPE_CHANGE_THEME_INFO = 2; // 改变描述信息
	public static final int EVENT_TYPE_CHANGE_THEME_PREVIEW = 3; // 改变主题预览图
	public static final int FEETYPE_FREE = 0; // 免费
	public static final int FEETYPE_PAID = 1; // 收费的
	public static final int FEETYPE_GETJAR = 2; // getjar
	public static final int FEETYPE_PLAY_MONTHLY = 3; // 包月

	public static final String THEMETYPE_GETJAR = "Getjar"; //
	public static final String THEMETYPE_LAUNCHER_FEATURED = "LauncherFeatured"; //
	public static final String THEMETYPE_LOCKER_FEATURED = "LockererFeatured"; //
	private String mThemeName = null; // 主题名称
	private String mThemeInfo = null; // 主题描述信息
	private String mVersionCode = null; // 版本号
	private String mVersionName = null; // 版本名称
	private int mMinGOLauncherVersion = -1; // 最低桌面版本号
	private String mThemeType = null;
	private ArrayList<String> mPreViewDrawableNames = null; // 预览效果图

	private boolean mIsNewTheme = false; // 标记是否为大主题
	private boolean mIsCurTheme = false; // 标记是否为正在使用的主题
	private ArrayList<String> mGoWidgetPkgName = null; // 大主题，gowidget的包名
	private boolean mExistGolauncher = false; // 大主题，标记是否存在GOwidget主题
	private boolean mExistGolock = false; // 大主题，标记是否存在GO锁屏主题
	private NewThemeInfo mNewThemeInfo = null; // 大主题信息

	private BroadCaster mBroadCaster = null;
	private boolean mIsZipTheme = false; // 是否是zip格式主题
	private boolean mIsEncrypt = false; // 是否加密

	public static final int URL_KEY_FTP = 1; // 服务器下载地址
	public static final int URL_KEY_GOOGLEMARKET = 2; // google电子市场下载或者详情地址
	public static final int URL_KEY_GOSTORE = 3; // 精品详情地址
	public static final int URL_KEY_OTHER = 4; // OTHER渠道下载或者详情地址
	public static final int URL_KEY_WEB_GOOGLEMARKET = 5; // Web版google电子市场下载或者详情地址

	public static final int DOWN_TYPE_NORMAL = 0;
	public static final int DOWN_TYPE_ZIP = 1;
	public static final int PAY_TYPE_NORMAL = 0; //普通谷歌收费
	public static final int PAY_TYPE_INBILLING = 1; //内付费
	public static final int PAY_TYPE_GETJAR = 2; //getjar付费
	public static final int PAY_TYPE_SMS = 3; //短信付费
	public static final int PAY_TYPE_SDK = 4; //SDK付费
	private HashMap<Integer, String> mUrlMap; // 跳转URL
	private int mFeaturedId = 0; // 推荐ID
	private int mFeeType; // 主题费用，0：免费，1：收费，2：getjar，3：Google Play包月
	private String mPayid; //付费ID
	private int mDowntype; //0：普通下载，按原下载流程 	1：资源包下载
	private String mDownurl; //资源包下载url(zip包)
	private String mMlocker; //配套锁屏包名
	private String mMwidget; //配套锁屏包名
	private List<String> mPayType; //收费类型
	private boolean mIsNew; //判断是不是新主题
	private boolean mDownLoading; //是否正在下载
	private int mStar;
	private boolean mMaskView = false; // 是否遮罩桌面（是有具有罩子层的功能）
	private String mPrice;
	private List<String> mPriViewImgUrls; //图片url，预览图以url形式下载时使用
	private int mImgSource; //0:使用imgid拿图片 1：使用imgurl拿图片，如果imgurl为空，则用imgid拿图片
	private int mSortId = -1; //分类ID
	private List<String> mPriViewImgIds; //预览图以id形式下载时使用
	private MiddleViewBean mMiddleViewBean; // 是否中间层view（是有具有中间的功能）
	private String[] mClassDexNames; // 收费版主题的classes文件的id 
	private String mMaskViewPath;
	private String mMiddleViewPath;

	private boolean mNeedActivationCode = false;
	private String mActivationCodeUrl;

	private String mVimgUrl; //视频图片url
	private String mVurl; //视频url

	private int mUserType = 0; // 主题的专属性：0: all; 1: vip; 2: hight-vip
	/**
	 * 类描述:
	 * 功能详细描述:
	 * @date  [2012-9-28]
	 */
	public class NewThemeInfo {
		private ArrayList<String> mNames = null;

		public NewThemeInfo() {
			mNames = new ArrayList<String>();
		}

		/**
		 * 获得action
		 * 
		 * @return
		 */
		public ArrayList<String> getNewThemePkg() {
			return mNames;
		}

		public void addNewThemePkg(String pkgName) {
			if (pkgName == null) {
				return;
			}
			if (mNames == null) {
				mNames = new ArrayList<String>();
			}
			mNames.add(pkgName);
		}
	}

	public ThemeInfoBean() {
		// TODO Auto-generated constructor stub
		super();
		mPreViewDrawableNames = new ArrayList<String>();
		mBeanType = THEMEBEAN_TYPE_THEMEINFO;

		mNewThemeInfo = new NewThemeInfo();

		// TODO:默认配置
		mThemeName = ThemeManager.DEFAULT_THEME_PACKAGE;
	}

	// 收费主题的构造方法
	public ThemeInfoBean(String themeName, String packageName) {
		super();
		mThemeName = themeName;
		mPackageName = packageName;
	}

	public ThemeInfoBean(final ThemeInfoBean themeInfoBean) {
		// TODO Auto-generated constructor stub
		// 基类成员初始化
		super(themeInfoBean);

		if (themeInfoBean == null) {
			mPreViewDrawableNames = new ArrayList<String>();
			mBeanType = THEMEBEAN_TYPE_THEMEINFO;
			mNewThemeInfo = new NewThemeInfo();
			// TODO:默认配置
			mThemeName = ThemeManager.DEFAULT_THEME_PACKAGE;
		} else {
			mThemeName = themeInfoBean.getThemeName();
			mThemeInfo = themeInfoBean.getThemeInfo();
			mPreViewDrawableNames = new ArrayList<String>(themeInfoBean.getPreViewDrawableNames());
			mBroadCaster = themeInfoBean.getBroadCaster();
		}
	}

	public ArrayList<String> getPreViewDrawableNames() {
		return mPreViewDrawableNames;
	}

	public void addDrawableName(String drawableName) {
		if (drawableName == null) {
			return;
		}
		/*
		 * if (mPreViewDrawableNames != null) { mPreViewDrawableNames.clear();
		 * mPreViewDrawableNames = null; }
		 */
		// mPreViewDrawableNames = new ArrayList<String>();
		if (mPreViewDrawableNames == null) {
			mPreViewDrawableNames = new ArrayList<String>();
		}
		mPreViewDrawableNames.add(drawableName);
		if (mBroadCaster != null) {
			mBroadCaster.broadCast(EVENT_TYPE_CHANGE_THEME_PREVIEW, 0, null, null);
		}
	}

	public String getThemeInfo() {
		return mThemeInfo;
	}

	public void setThemeInfo(String mThemeInfo) {
		this.mThemeInfo = mThemeInfo;
		if (mBroadCaster != null) {
			mBroadCaster.broadCast(EVENT_TYPE_CHANGE_THEME_INFO, 0, null, null);
		}
	}

	public String getThemeName() {
		return mThemeName;
	}

	public void setThemeName(String mThemeName) {
		this.mThemeName = mThemeName;
		if (mBroadCaster != null) {
			mBroadCaster.broadCast(EVENT_TYPE_CHANGE_THEME_NAME, 0, null, null);
		}
	}

	public String getVersionCode() {
		return mVersionCode;
	}

	public void setVersionCode(String versionCode) {
		this.mVersionCode = versionCode;
	}

	public String getVersionName() {
		return mVersionName;
	}

	public void setVersionName(String versionName) {
		this.mVersionName = versionName;
	}

	public BroadCaster getBroadCaster() {
		return mBroadCaster;
	}

	public void setBroadCaster(BroadCaster mBroadCaster) {
		this.mBroadCaster = mBroadCaster;
	}

	public long getThemeInstalledTime(PackageManager packageMgr) {
		String sourceDir = null;
		long modifyTime = 0;
		try {
			sourceDir = packageMgr.getApplicationInfo(mPackageName, 0).sourceDir;
			// sourceDir = packageMgr.getActivityInfo(mIntent.getComponent(),
			// 0).applicationInfo.sourceDir;
			File file = new File(sourceDir);
			modifyTime = file.lastModified();
			file = null;
		} catch (Exception e) {
			Log.i("ThemeInfoBean", "getThemeInstalledTime has exception = " + e.getMessage());
		}
		return modifyTime;
	}

	public void registerObserver(BroadCasterObserver oberver) {
		if (mBroadCaster == null) {
			mBroadCaster = new BroadCaster();
		}
		mBroadCaster.registerObserver(oberver);
	}

	public boolean unRegisterObserver(BroadCasterObserver observer) {
		return mBroadCaster.unRegisterObserver(observer);
	}

	public void clearPreviewName() {
		if (null != mPreViewDrawableNames) {
			mPreViewDrawableNames.clear();
		}
	}

	/**
	 * 返回是否为大主题 true：是，false：不是
	 * 
	 * @return
	 */
	public boolean isNewTheme() {
		return mIsNewTheme;
	}

	public void setIsNewTheme(boolean isNewTheme) {
		this.mIsNewTheme = isNewTheme;
	}

	/**
	 * 是否存在GO锁屏主题
	 * 
	 * @return
	 */
	public boolean ismExistGolock() {
		return mExistGolock;
	}

	public void setExistGolock(boolean existGolock) {
		this.mExistGolock = existGolock;
	}

	/**
	 * 是否存在GOwidget主题
	 * 
	 * @return
	 */
	public boolean ismExistGolauncher() {
		return mExistGolauncher;
	}

	public void setExistGolauncher(boolean existGolauncher) {
		this.mExistGolauncher = existGolauncher;
	}

	public ArrayList<String> getGoWidgetPkgName() {
		return mGoWidgetPkgName;
	}

	public void setGoWidgetPkgName(ArrayList<String> goWidgetPkgName) {
		this.mGoWidgetPkgName = goWidgetPkgName;
	}

	public void addGoWidgetPkgName(String pkgName) {
		if (pkgName == null) {
			return;
		}
		if (mGoWidgetPkgName == null) {
			mGoWidgetPkgName = new ArrayList<String>();
		}
		mGoWidgetPkgName.add(pkgName);
	}

	public NewThemeInfo getNewThemeInfo() {
		if (mNewThemeInfo == null) {
			mNewThemeInfo = new NewThemeInfo();
		}
		return mNewThemeInfo;
	}

	public String getThemeType() {
		return mThemeType;
	}

	public void setThemeType(String mThemeType) {
		this.mThemeType = mThemeType;
	}

	public int getMinGOLauncherVersion() {
		return mMinGOLauncherVersion;
	}

	public void setMinGOLauncherVersion(int minGOLauncherVersion) {
		this.mMinGOLauncherVersion = minGOLauncherVersion;
	}

	/**
	 * 返回是否为当前正在使用的主题
	 * 
	 * @author yangbing
	 */
	public boolean isCurTheme() {
		return mIsCurTheme;
	}

	/**
	 * 设置是否为当前正在使用的主题
	 * 
	 * @author yangbing
	 */
	public void setIsCurTheme(boolean mIsCurTheme) {
		this.mIsCurTheme = mIsCurTheme;
	}

	/**
	 * 
	 * 取预览图片的第一张
	 * 
	 * @author yangbing
	 * 
	 * */
	public String getFirstPreViewDrawableName() {
		if (mPreViewDrawableNames != null && mPreViewDrawableNames.size() > 0) {
			return mPreViewDrawableNames.get(0);
		}
		return null;

	}

	/**
	 * 
	 * 设置主题类型
	 * 
	 * @author yangbing
	 * 
	 * */
	public void setBeanType(int type) {

		mBeanType = type;
	}

	/**
	 * 设置是否为zip格式主题
	 * 
	 * @param isZip
	 */
	public void setIsZipTheme(boolean isZip) {
		mIsZipTheme = isZip;
	}

	/**
	 * 是否为zip格式主题
	 * 
	 * @param isZip
	 */
	public boolean isZipTheme() {
		return mIsZipTheme;
	}

	/**
	 * 设置主题是否加密
	 * 
	 * @param encrypt
	 */
	public void setIsEncrypt(boolean encrypt) {
		mIsEncrypt = encrypt;
	}

	/**
	 * 主题是否被加密
	 * 
	 * @return
	 */
	public boolean isEncrypt() {
		return mIsEncrypt;
	}

	/**
	 * 设置主题是否有罩子层
	 */
	public void setMaskView(boolean maskView) {
		mMaskView = maskView;
	}

	/**
	 * 主题是否有罩子层
	 * @return
	 */
	public boolean isMaskView() {
		return mMaskView;
	}

	/**
	 * 获取跳转列表
	 * 
	 * @return
	 */
	public HashMap<Integer, String> getUrlMap() {
		return mUrlMap;
	}

	/**
	 * 设置跳转列表
	 * 
	 * @param map
	 */
	public void setUrlMap(HashMap<Integer, String> map) {
		mUrlMap = map;
	}

	// public String getFeaturedImageId(){
	// return mFeaturedImageId;
	// }
	// public void setFeaturedImageId(String id){
	// mFeaturedImageId = id;
	// }
	/**
	 * 获得精品主题推荐ID
	 */
	public int getFeaturedId() {
		return mFeaturedId;
	}

	/**
	 * 设置精品主题推荐ID
	 */
	public void setFeaturedId(int id) {
		mFeaturedId = id;
	}

	/**
	 * 设置费用情况
	 * 
	 * @param feeType
	 */
	public void setFeeType(int feeType) {
		mFeeType = feeType;
	}

	/**
	 * 获得费用情况
	 */
	public int getFeeType() {
		return mFeeType;
	}

	/**
	 * <br>功能简述:获得付费ID
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public String getPayId() {
		return mPayid;
	}
	/**
	 * <br>功能简述:设置付费ID
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param payId
	 */
	public void setPayId(String payId) {
		mPayid = payId;
	}

	public void setDownType(int type) {
		mDowntype = type;
	}

	public int getDownType() {
		return mDowntype;
	}
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public String getMlcokerThemeName() {
		return mMlocker;
	}
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packageName
	 */
	public void setMlcokerThemeName(String packageName) {
		mMlocker = packageName;
	}
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public String getMwidgetThemeName() {
		return mMwidget;
	}
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packageName
	 */
	public void setMwidgetThemeName(String packageName) {
		mMwidget = packageName;
	}
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param type
	 */
	public void setPayType(List<String> type) {
		mPayType = type;
	}

	public List<String> getPayType() {
		return mPayType;
	}

	public String getDownLoadUrl() {
		return mDownurl;
	}

	public void setDownloadUrl(String url) {
		mDownurl = url;
	}

	public void setDownloadState(boolean bool) {
		mDownLoading = bool;
	}

	public boolean getDownloadState() {
		return mDownLoading;
	}

	public void setSar(int star) {
		mStar = star;
	}

	public int getStar() {
		return mStar;
	}

	/**
	 * <br>功能简述:设置是否是新推主题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bool
	 */
	public void setIsNew(boolean bool) {
		mIsNew = bool;
	}
	/**
	 * <br>功能简述:返回是否是新推主题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public boolean getIsNew() {
		return mIsNew;
	}

	public void setPrice(String price) {
		mPrice = price;
	}

	public String getPrice() {
		return mPrice;
	}

	public void setImgUrls(List<String> urls) {
		mPriViewImgUrls = urls;
	}

	public List<String> getImgUrls() {
		return mPriViewImgUrls;
	}

	public void setImgSource(int source) {
		mImgSource = source;
	}

	public int getImgSource() {
		return mImgSource;
	}

	public int getSortId() {
		return mSortId;
	}

	public void setSortId(int sordId) {
		mSortId = sordId;
	}

	public void setImgIds(List<String> ids) {
		mPriViewImgIds = ids;
	}

	public List<String> getImgIds() {
		return mPriViewImgIds;
	}

	public boolean isNormalPay() {
		if (mPayType == null || mPayType.isEmpty()
				|| mPayType.contains(String.valueOf(PAY_TYPE_NORMAL))) {
			return true;
		}
		return false;
	}
	public boolean isInAppPay() {
		if (mPayType != null && !mPayType.isEmpty()
				&& mPayType.contains(String.valueOf(PAY_TYPE_INBILLING))) {
			return true;
		}
		return false;
	}

	public boolean isSdkPay() {
		if (mPayType != null && !mPayType.isEmpty()
				&& mPayType.contains(String.valueOf(PAY_TYPE_SDK))) {
			return true;
		}
		return false;
	}

	public boolean isGetJarPay() {
		if (mPayType != null && !mPayType.isEmpty()
				&& mPayType.contains(String.valueOf(PAY_TYPE_GETJAR))) {
			return true;
		}
		return false;
	}

	public boolean isSmsPay() {
		if (mPayType != null && !mPayType.isEmpty()
				&& mPayType.contains(String.valueOf(PAY_TYPE_SMS))) {
			return true;
		}
		return false;
	}

	public void setNeedActivationCode(boolean need) {
		this.mNeedActivationCode = need;
	}

	public boolean getNeedActivationCode() {
		return mNeedActivationCode;
	}

	public String getActivationCodeUrl() {
		return mActivationCodeUrl;
	}

	public void setActivationCodeUrl(String url) {
		mActivationCodeUrl = url;
	}

	/**
	 * 
	 * @author jiangxuwen
	 *
	 */
	public class MiddleViewBean {
		public boolean mHasMiddleView;
		public boolean mIsSurfaceView;
	}

	public void setMiddleViewBean(boolean hasMiddleView, boolean isSurfaceView) {
		if (mMiddleViewBean == null) {
			mMiddleViewBean = new MiddleViewBean();
		}
		mMiddleViewBean.mHasMiddleView = hasMiddleView;
		mMiddleViewBean.mIsSurfaceView = isSurfaceView;
	}

	public MiddleViewBean getMiddleViewBean() {
		return mMiddleViewBean;
	}

	public void setClassDexNames(String[] names) {
		mClassDexNames = names;
	}

	public String[] getClassDexNames() {
		return mClassDexNames;
	}

	public void setMaskViewPath(String path) {
		mMaskViewPath = path;
	}

	public String getMaskViewPath() {
		return mMaskViewPath;
	}

	public void setMiddleViewPath(String path) {
		mMiddleViewPath = path;
	}

	public String getMiddleViewPath() {
		return mMiddleViewPath;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(mBeanType);
		dest.writeStringList(mPreViewDrawableNames);
		dest.writeString(mPackageName);
	}

	public String getVimgUrl() {
		return mVimgUrl;
	}

	public void setVimgUrl(String mVimgUrl) {
		this.mVimgUrl = mVimgUrl;
	}

	public String getVurl() {
		return mVurl;
	}

	public void setVurl(String mVurl) {
		this.mVurl = mVurl;
	}

	public static final Parcelable.Creator<ThemeInfoBean> CREATOR = new Creator() {
		@Override
		public ThemeInfoBean createFromParcel(Parcel source) {
			// TODO Auto-generated method stub
			// 必须按成员变量声明的顺序读取数据，不然会出现获取数据出错
			ThemeInfoBean p = new ThemeInfoBean();
			p.mBeanType = source.readInt();
			p.mPreViewDrawableNames = new ArrayList<String>();
			source.readStringList(p.mPreViewDrawableNames);
			p.mPackageName = source.readString();
			return p;
		}

		@Override
		public ThemeInfoBean[] newArray(int size) {
			// TODO Auto-generated method stub
			return new ThemeInfoBean[size];
		}
	};

	public void setUserType(int user) {
		mUserType = user;
	}

	public int getUserType() {
		return mUserType;
	}

}

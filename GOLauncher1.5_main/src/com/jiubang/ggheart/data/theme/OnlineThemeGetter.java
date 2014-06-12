package com.jiubang.ggheart.data.theme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.AppUtils;
import com.go.util.ConvertUtils;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.desks.diy.INotificationId;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageManager;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeDataManager;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeDetailActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageActivity;
import com.jiubang.ggheart.apps.gowidget.gostore.GoStoreHttpTool;
import com.jiubang.ggheart.data.BroadCaster;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.bean.FeaturedDataListBean;
import com.jiubang.ggheart.data.theme.bean.FeaturedDataListBean.FeaturedElement;
import com.jiubang.ggheart.data.theme.bean.FeaturedThemeDetailBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBannerBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBannerBean.BannerElement;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.bean.ThemeNotifyBean;
import com.jiubang.ggheart.data.theme.bean.ThemeSpecDataBean;
import com.jiubang.ggheart.data.theme.cache.XmlCacheParser;
import com.jiubang.ggheart.data.theme.parser.ThemeInfoParser;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 类描述: 功能详细描述:
 * 
 * @author
 * @date [2012-9-28]
 */
public class OnlineThemeGetter extends BroadCaster {

	private static final String LANGUAGE = "language";
	private Context mContext = null;

	public static final String LAUNCHER_HOTTHEME_STAMP = "launcher_hottheme_stamp";
	public static final String LCOKER_FEATUREDTHEME_STAMP = "lcoker_featuredtheme_stamp";
	public static final String BANNER_POST_STAMP = "banner_post_stamp";
	public static final String LOCKER_BANNER_POST_STAMP = "locker_banner_post_stamp";
	public static final String SPEC_POST_STAMP = "spec_post_stamp";
	//	private final static String FEATURED_THEME_DETAIL_FILE_PATH = LauncherEnv.Path.GOTHEMES_PATH
	//			+ "ThemeDetailCatch.xml";
	private final static String BANNER_CATCH_FILE_PATH = LauncherEnv.Path.GOTHEMES_PATH
			+ "ThemeBannerCatch.xml";
	private final static String LOCKERBANNER_CATCH_FILE_PATH = LauncherEnv.Path.GOTHEMES_PATH
			+ "LockerBannerCatch.xml";
	private final static String SPEC_THEME_CATCH_FILE_PATH = LauncherEnv.Path.GOTHEMES_PATH
			+ "SpecThemeCatch.xml";
	private final static String SPEC_LOCKER_THEME_CATCH_FILE_PATH = LauncherEnv.Path.GOTHEMES_PATH
			+ "SpecThemeCatch.xml";
	/**
	 * 协议中的ty类型 0 是主题 ， 1是锁屏
	 */
	public static final int TYPE_LAUNCHER_FEATURED = 0;
	public static final int TYPE_LOCKER_FEATURED = 1;

	/**
	 * tab ID 区分热门主题和精选主题
	 */
	public static final int TAB_LAUNCHER_FEATURED_ID = 0;
	public static final int TAB_LAUNCHER_HOT_ID = 1;

	public static final int FEATURED_THEME_REQUEST_FUNID = 25; // 推荐主题获取功能号
	public static final int FEATURED_THEME_DETAIL_REQUEST_FUNID = 28; // 推荐主题详情获取功能号
	public static final int BANNER_THEME_REQUEST_FUNID = 31; // banner获取功能号
	public static final int SPEC_THEME_REQUEST_FUNID = 32; // 专题主题获取功能号

	public static final int THEME_APPUID = 6; // 协议请求id

	public OnlineThemeGetter(Context context) {
		mContext = context;
	}

	/**
	 * <br>
	 * 功能简述: <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param installThemes
	 * @param type
	 * @param tab
	 * @param background
	 *            是否是后台拉取
	 * @return
	 */
	public ArrayList<ThemeInfoBean> getFeaturedThemeInfoBeans(
			ArrayList<ThemeInfoBean> installThemes, int type, boolean background,
			BroadCasterObserver observer) {

		ArrayList<ThemeInfoBean> featuredThemeInfoBeans = new ArrayList<ThemeInfoBean>();
		// 当前广告位版本号
		// 解析本地文件生成themeinfobean列表
		final StringBuffer curVersionBuf = new StringBuffer();
		final StringBuffer recommendThemesBuf = new StringBuffer();
		// 这里面同时需要区分热门主题和精选主题
		// 获取缓存文件的语言
		InputStream inputStream = null;
		String xmlFile = null;
		try {
			// 判断缓存文件语言是否与目前系统语言相同,不同则从新请求当前系统语言的widget列表
			String language = null;
			if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
				xmlFile = ThemeManager.XMLFILE;
			} else if (type == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
				xmlFile = ThemeManager.HOT_XMLFILE;
			} else {
				xmlFile = LockerManager.XMLFILE;
			}
			inputStream = new FileInputStream(xmlFile);
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
			parser.setInput(inputStream, null);
			while (parser.next() != XmlPullParser.END_DOCUMENT) {
				if (parser.getName() != null && parser.getName().equals(LANGUAGE)) {
					language = parser.getAttributeValue("", LANGUAGE);
					break;
				}
			}
			String tmpLanguage = Locale.getDefault().getLanguage()
					+ Locale.getDefault().getCountry();
			if (language == null || !language.equals(tmpLanguage)) {
				// 旧版缓存文件不存在language标签，在此把旧版缓存文件删除
				FileUtil.deleteFile(xmlFile);
			}
		} catch (XmlPullParserException e) {
			Log.i("ThemeGetter", "getFeaturedThemeInfoBeans exception = " + e.getMessage());
		} catch (FileNotFoundException e) {
			Log.i("ThemeGetter", "getFeaturedThemeInfoBeans exception = " + e.getMessage());
		} catch (IOException e) {
			Log.i("ThemeGetter", "getFeaturedThemeInfoBeans exception = " + e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
					inputStream = null;
				} catch (IOException e) {
					Log.i("ThemeGetter", "getFeaturedThemeInfoBeans exception = " + e.getMessage());
				}
			}
		}

		new ThemeInfoParser().parseLauncherThemeXml(mContext, xmlFile, curVersionBuf,
				recommendThemesBuf, featuredThemeInfoBeans, type);
		// 过滤排序
		int recomendThemesCount = 0;
		if (recommendThemesBuf != null && recommendThemesBuf.toString() != null
				&& recommendThemesBuf.toString().trim() != null
				&& recommendThemesBuf.toString().trim().length() > 0) {
			try {
				recomendThemesCount = Integer.valueOf(recommendThemesBuf.toString().trim());
			} catch (Exception e) {
				Log.i("ThemeGetter", "Integer.valueOf has exception");
			}
		}
		featuredThemeInfoBeans = filterRecommendFeaturedTheme(featuredThemeInfoBeans,
				recomendThemesCount);
		// 网络拉数据
		GoStoreHttpTool.getInstance(GOLauncherApp.getContext()).getHttpData(
				FEATURED_THEME_REQUEST_FUNID, getPostValuePairs(installThemes, type),
				new ConnectListener(type, background, observer));

		return featuredThemeInfoBeans;
	}

	/**
	 * 发送信息到通知栏
	 * 
	 * @author huyong
	 */
	public void sendNewThemesNotification(int type, String content, Bitmap icon) {
		String noteText = null;
		if (content != null && !content.equals("")) {
			noteText = content;
		}
		if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
				|| type == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			Intent intent = new Intent(mContext, ThemeManageActivity.class);
			intent.setAction(ICustomAction.ACTION_SHOW_THEME_PREVIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			int tab = TAB_LAUNCHER_FEATURED_ID;
			if (type == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
				tab = TAB_LAUNCHER_HOT_ID;
			}
			intent.putExtra(ThemeConstants.TAB_THEME_KEY, tab);

			String title = mContext.getString(R.string.notification_launcher_theme_title);
			String noteTitle = mContext.getString(R.string.notification_launcher_theme_notetitle);
			if (noteText == null) {
				noteText = mContext.getString(R.string.notification_launcher_theme_notetext);
			}
			AppUtils.sendIconNotification(mContext, intent, R.drawable.icon, title, noteTitle,
					noteText, INotificationId.GOTO_THEME_PREVIEW, icon);

		} else {
			Intent intent = new Intent(mContext, ThemeManageActivity.class);
			intent.setAction(ICustomAction.ACTION_SHOW_LOCKER_THEME_PREVIEW);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

			String title = mContext.getString(R.string.notification_locker_theme_title);
			String noteTitle = mContext.getString(R.string.notification_locker_theme_title);
			if (noteText == null) {
				noteText = mContext.getString(R.string.notification_locker_theme_notetext);
			}
			AppUtils.sendIconNotification(mContext, intent, R.drawable.screen_edit_golocker, title,
					noteTitle, noteText, INotificationId.GOTO_LOCKERTHEME_PREVIEW, icon);
		}
	}

	public void sendAsynNewThemesNotification(final int type, final String content, final String url) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Bitmap iconFromUrl = null;
				try {
					iconFromUrl = getImage(url);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				new OnlineThemeGetter(mContext).sendNewThemesNotification(type, content,
						iconFromUrl);
			}
		}).start();
	}

	/**
	 * 根据网络URL地址解析返回bitmap
	 * 
	 * @param address
	 * @return Bitmap
	 * @throws Exception
	 */
	public Bitmap getImage(String address) throws Exception {
		if (address == null) {
			return null;
		}
		URL url = new URL(address);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(3000);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;
		while ((len = conn.getInputStream().read(buffer)) != -1) {
			baos.write(buffer, 0, len);
		}
		baos.close();
		byte[] data = baos.toByteArray();
		Options options = new Options();
		options.inPurgeable = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length, options); // 转化为图片
		return bm;
	}

	/**
	 * 排序精选主题
	 * 
	 * @author huyong
	 * @param arrayList
	 * @param recommendCount
	 * @return
	 */
	private ArrayList<ThemeInfoBean> filterRecommendFeaturedTheme(
			ArrayList<ThemeInfoBean> arrayList, int recommendCount) {
		ArrayList<ThemeInfoBean> result = null;
		if (arrayList == null || arrayList.size() <= 0) {
			return result;
		}

		result = new ArrayList<ThemeInfoBean>();
		int firstCount = recommendCount; // 前面固定的个数
		if (arrayList.size() < recommendCount) {
			firstCount = arrayList.size();
		}
		for (int i = 0; i < firstCount; i++) {
			result.add(arrayList.get(i));
		}
		// 获取待显示的收费主题
		ArrayList<Integer> toShowPaidThemeIndexList = getRandomInts(arrayList.size()
				- recommendCount, recommendCount, arrayList.size());
		if (toShowPaidThemeIndexList != null) {
			for (int i = 0; i < toShowPaidThemeIndexList.size(); i++) {
				int index = toShowPaidThemeIndexList.get(i);
				result.add(arrayList.get(index));
			}
		}

		return result;
	}

	/**
	 * 产生一个半闭包随机数的集合的方法
	 * 
	 * @param count
	 *            随机数的数目
	 * @param min
	 *            随机数最小值
	 * @param max
	 *            随机数最大值（不包含）
	 * @return 返回一个在 [min - max)区间内的不重复整数数组.
	 */
	private ArrayList<Integer> getRandomInts(int count, int min, int max) {
		if (count < 0 || min > max) {
			return null;
		}
		if (count > (max - min)) {
			count = max - min;
		}
		ArrayList<Integer> result = new ArrayList<Integer>();
		Random rand = new Random();
		for (int i = 0, randomNumber = 0; i < count;) {
			randomNumber = min + rand.nextInt(max - min);
			if (!result.contains(randomNumber)) {
				Log.i("ThemeGetter", "random = " + randomNumber);
				result.add(randomNumber);
				i++;
			}
		}
		return result;
	}

	private ArrayList<ThemeInfoBean> saveAdElementAsPaidBeanToSDCard(
			ArrayList<FeaturedElement> elementList, String version, int recommendCount, int type) {
		if (elementList == null || elementList.size() <= 0) {
			return null;
		}
		int count = elementList.size();
		ArrayList<ThemeInfoBean> beansList = new ArrayList<ThemeInfoBean>(count);
		String pkgName = null;
		ThemeInfoBean paidBean = null;
		FeaturedElement element = null;
		for (int i = 0; i < count; i++) {
			element = elementList.get(i);
			pkgName = element.mPkgName;
			String imgName = null;
			imgName = element.mImgId;
			paidBean = new ThemeInfoBean();
			paidBean.setPackageName(pkgName);
			paidBean.setVersionCode(String.valueOf(element.mVersionCode));
			paidBean.setVersionName(element.mVersion);
			paidBean.setIsNewTheme(ConvertUtils.int2boolean(element.mIsall));
			paidBean.setThemeInfo(element.mDetail);
			paidBean.setThemeName(element.mName);
			paidBean.addDrawableName(imgName);
			paidBean.setUrlMap(element.mUrlMap);
			paidBean.setFeaturedId(element.mId);
			paidBean.setFeeType(element.mFeeType);
			paidBean.setPayId(element.mPayId);
			paidBean.setPayType(element.mPayType);
			paidBean.setDownloadUrl(element.mDownurl);
			paidBean.setIsNew(element.mIsNew);
			paidBean.setImgSource(element.mImgsource);
			paidBean.setImgUrls(element.mImgUrlArrary);
			paidBean.setBeanType(type);
			paidBean.setPrice(element.mPrice);

			beansList.add(paidBean);

			// System.out.println("iconPath + imgName:" + iconPath + imgName);
		}
		// 将新下载推荐信息保存至xml文件中
		String xmFile = null;
		if (type == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			xmFile = ThemeManager.HOT_XMLFILE;
		} else if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			xmFile = ThemeManager.XMLFILE;
		} else if (type == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
			xmFile = LockerManager.XMLFILE;
		}
		new ThemeInfoParser().writeGoThemeToXml(version, recommendCount, beansList, xmFile);
		return beansList;
	}

	private void saveNotifyData(FeaturedDataListBean resData, int type) {
		if (resData != null) {
			ThemeNotifyBean bean = new ThemeNotifyBean();
			bean.setType(type);
			bean.setShowIconUrl(resData.mShowIconUrl);
			bean.setShowEndTime(resData.mShowEndTime);
			bean.setShowStatTime(resData.mShowStartTime);
			bean.setShowContent(resData.mShowContent);
			new ThemeInfoParser().writeNotifyDataToXml(bean);
			ThemeManager.getInstance(mContext).addNotifyBean(bean);
		}
	}

	private StringBuffer getAllInstallThemePackageNameData(ArrayList<ThemeInfoBean> infoBeans) {
		StringBuffer packageNameData = new StringBuffer();
		for (int i = 0; i < infoBeans.size(); i++) {
			ThemeInfoBean bean = infoBeans.get(i);
			packageNameData.append(bean.getPackageName() + ",");
		}
		return packageNameData;
	}
	/**
	 * 该方法对于nameValuePairsList数据的添加顺序会关联到ThemeHttp类中的compoundNameValuePairs对于锁屏type的判断
	 * @param infoBeans
	 * @param type
	 * @return
	 */
	private ArrayList<NameValuePair> getPostValuePairs(ArrayList<ThemeInfoBean> infoBeans, int type) {
		StringBuffer packageNameData = getAllInstallThemePackageNameData(infoBeans);
		String installList = null;
		try {
			installList = gzip(packageNameData.toString().getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		BasicNameValuePair basicNameValuePair = new BasicNameValuePair("owns", installList);
		nameValuePairs.add(basicNameValuePair);
		basicNameValuePair = new BasicNameValuePair("appuid", String.valueOf(THEME_APPUID));
		nameValuePairs.add(basicNameValuePair);
		int requestType = TYPE_LAUNCHER_FEATURED;
		if (type == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
			requestType = TYPE_LOCKER_FEATURED;
		}
		basicNameValuePair = new BasicNameValuePair("ty", String.valueOf(requestType));
		nameValuePairs.add(basicNameValuePair);
		int tab = TAB_LAUNCHER_FEATURED_ID;
		if (type == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			tab = TAB_LAUNCHER_HOT_ID;
		}
		basicNameValuePair = new BasicNameValuePair("tab", String.valueOf(tab));
		nameValuePairs.add(basicNameValuePair);

		// 获取时间戳
		long timeStamp = getPostTimeStamp(type, mContext);
		String filePath = LockerManager.XMLFILE;
		if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			filePath = ThemeManager.XMLFILE;
		} else if (type == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			filePath = ThemeManager.HOT_XMLFILE;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			timeStamp = 0;
		}
		file = null;
		basicNameValuePair = new BasicNameValuePair("timestamp", String.valueOf(timeStamp));
		nameValuePairs.add(basicNameValuePair);
		return nameValuePairs;
	}

	private ArrayList<NameValuePair> getFeaturedDetailPostValuePairs(ThemeInfoBean infoBean) {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		BasicNameValuePair basicNameValuePair = new BasicNameValuePair("id",
				String.valueOf(infoBean.getFeaturedId()));
		nameValuePairs.add(basicNameValuePair);
		basicNameValuePair = new BasicNameValuePair("pkgname", infoBean.getPackageName());
		nameValuePairs.add(basicNameValuePair);
		return nameValuePairs;
	}

	public long getPostTimeStamp(int type, Context context) {
		if (type == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			// 根据tab来判断是热门主题还是精选主题
			return new PreferencesManager(context, IPreferencesIds.FEATUREDTHEME_CONFIG,
					Context.MODE_PRIVATE).getLong(LAUNCHER_HOTTHEME_STAMP, 0);
		} else if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			return new PreferencesManager(context, IPreferencesIds.FEATUREDTHEME_CONFIG,
					Context.MODE_PRIVATE).getLong(IPreferencesIds.LAUNCHER_FEATUREDTHEME_STAMP, 0);
		} else {
			return new PreferencesManager(context, IPreferencesIds.FEATUREDTHEME_CONFIG,
					Context.MODE_PRIVATE).getLong(LCOKER_FEATUREDTHEME_STAMP, 0);
		}
	}

	public void setPostTimeStamp(int type, long timeStamp, Context context) {
		PreferencesManager pm = new PreferencesManager(context,
				IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
		if (type == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			// 根据themeType来判断是热门主题还是精选主题
			pm.putLong(LAUNCHER_HOTTHEME_STAMP, timeStamp);
		} else if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			pm.putLong(IPreferencesIds.LAUNCHER_FEATUREDTHEME_STAMP, timeStamp);
		} else {
			pm.putLong(LCOKER_FEATUREDTHEME_STAMP, timeStamp);
		}
		pm.commit();
	}

	public long getBannerPostTimeStamp(Context context) {
		return new PreferencesManager(context, IPreferencesIds.FEATUREDTHEME_CONFIG,
				Context.MODE_PRIVATE).getLong(BANNER_POST_STAMP, 0);
	}

	public long getLockerBannerPostTimeStamp(Context context) {
		return new PreferencesManager(context, IPreferencesIds.FEATUREDTHEME_CONFIG,
				Context.MODE_PRIVATE).getLong(LOCKER_BANNER_POST_STAMP, 0);
	}

	public void setBannerPostTimeStamp(Context context, long timeStamp) {
		PreferencesManager sp = new PreferencesManager(context,
				IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
		sp.putLong(BANNER_POST_STAMP, timeStamp);
		sp.commit();
	}

	public void setLockerBannerPostTimeStamp(Context context, long timeStamp) {
		PreferencesManager sp = new PreferencesManager(context,
				IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
		sp.putLong(LOCKER_BANNER_POST_STAMP, timeStamp);
		sp.commit();
	}

	public long getSpecPostTimeStamp(Context context) {
		return new PreferencesManager(context, IPreferencesIds.FEATUREDTHEME_CONFIG,
				Context.MODE_PRIVATE).getLong(SPEC_POST_STAMP, 0);
	}

	public void setSpecPostTimeStamp(Context context, long timeStamp) {
		PreferencesManager sp = new PreferencesManager(context,
				IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
		sp.putLong(SPEC_POST_STAMP, timeStamp);
		sp.commit();
	}

	/**
	 * 
	 * 类描述: 功能详细描述:
	 * 
	 * @author
	 * @date [2012-9-28]
	 */
	class ConnectListener implements IConnectListener {
		private String mNewVersion; // 服务器版本号
		private int mRecommendCount; // 推荐个数
		private int mType; // 桌面主题或者还是锁屏主题
		private boolean mBackground = false;
		private BroadCasterObserver mObserver;

		public ConnectListener(int type, boolean background, BroadCasterObserver observer) {
			// TODO Auto-generated constructor stub
			mType = type;
			mBackground = background;
			mObserver = observer;
		}

		@Override
		public void onStart(THttpRequest request) {
		}

		@Override
		public void onFinish(THttpRequest request, IResponse response) {
			if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
				// 定义一种新的类型
				ArrayList<FeaturedDataListBean> list = (ArrayList<FeaturedDataListBean>) response
						.getResponse();
				if (list.isEmpty()) {
					return;
				}
				FeaturedDataListBean resData = list.get(0);
				// //返回解析后的数据，以列表形式存储。
				ArrayList<FeaturedElement> elementList = resData.mElementsList;
				PreferencesManager sp = new PreferencesManager(mContext,
						IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
				if (elementList != null && elementList.size() > 0) {
					if (!ThemeManageActivity.sRuning) {
						if (mType == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
							sp.putBoolean(IPreferencesIds.HASNEWTHEME, resData.mHasNewTheme);

						} else {
							sp.putBoolean(IPreferencesIds.LOCKER_HASNEWTHEME, resData.mHasNewTheme);
						}
						sp.commit();
					}
					setPostTimeStamp(mType, resData.mTimeStamp, GOLauncherApp.getContext());
					mRecommendCount = elementList.size();
					mNewVersion = String.valueOf(resData.mTimeStamp);
					ArrayList<ThemeInfoBean> result = saveAdElementAsPaidBeanToSDCard(elementList,
							mNewVersion, mRecommendCount, mType);
					if (resData.mShowStatusNotify && !ThemeManageActivity.sRuning) {
						saveNotifyData(resData, mType);
					}

					if (result != null) {
						if (mObserver != null) {
							mObserver.onBCChange(ThemeDataManager.MSG_GET_FEATURED_FINISHED, mType,
									null, result);
						}
						// 发布信息到通知栏
						if (mContext != null) {
							Intent intent = new Intent(ICustomAction.ACTION_FEATURED_THEME_CHANGED);
							intent.setData(Uri.parse("package://"));
							mContext.sendBroadcast(intent);
						}
						if (mBackground && !ThemeManageActivity.sRuning) {
							Intent intent = new Intent(ICustomAction.ACTION_CHECK_NEWTHEME_NOTIFY);
							mContext.sendBroadcast(intent);
							if (resData.mShowStatusNotify) {
								int staticsID = ThemeConstants.STATICS_ID_FEATURED_NOTIFY;
								String configId = IPreferencesIds.SHAREDPREFERENCES_MSG_THEME_NOTIFY_STATICS_DATA;
								if (mType == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
									staticsID = ThemeConstants.STATICS_ID_LOCKER_NOTIFY;
									configId = IPreferencesIds.SHAREDPREFERENCES_MSG_LOCKER_THEME_NOTIFY_STATICS_DATA;
								}
								PreferencesManager manager = new PreferencesManager(mContext,
										configId, Context.MODE_PRIVATE);
								int cnt = manager.getInt(
										IPreferencesIds.SHAREDPREFERENCES_MSG_PUSH_TIMES, 0);
								manager.putInt(IPreferencesIds.SHAREDPREFERENCES_MSG_PUSH_TIMES,
										cnt + 1);
								manager.commit();
								MessageManager.getMessageManager(mContext.getApplicationContext())
										.updateThemeNotifyStatisticsData(staticsID, 0, false);
							}
						} else {
							String id = null;
							if (mType == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
								id = IPreferencesIds.HASSHOWLOCKERNOTIFY;
							} else if (mType == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
								id = IPreferencesIds.HASSHOWFEATURENOTIFY;
							} else {
								id = IPreferencesIds.HASSHOWHOTNOTIFY;
							}
							boolean hasShow = false; // 如果之前已有后台获取主题弹出过通知下次进入就不再弹出
							if (id != null) {
								hasShow = sp.getBoolean(id, false);
							}

							if (!hasShow && !mBackground) {
								sendNewThemesNotification(mType, null, null);
							}
						}
					}

				}
				if (!mBackground) {
					sp = new PreferencesManager(mContext, IPreferencesIds.FEATUREDTHEME_CONFIG,
							Context.MODE_PRIVATE);
					String id = IPreferencesIds.HASSHOWLOCKERNOTIFY;
					sp.putBoolean(id, false); // 恢复标志位
					id = IPreferencesIds.HASSHOWFEATURENOTIFY;
					sp.putBoolean(id, false); // 恢复标志位
					id = IPreferencesIds.HASSHOWHOTNOTIFY;
					sp.putBoolean(id, false); // 恢复标志位
					sp.commit();
				}
			}
		}

		@Override
		public void onException(THttpRequest request, int reason) {
			StatisticsData.saveHttpExceptionDate(mContext, request, reason);
		}
	}

	/**
	 * 
	 * 类描述: 功能详细描述:
	 * 
	 * @author
	 * @date [2012-9-28]
	 */
	class DetailConnectListener implements IConnectListener {

		private Handler mHandler;

		public DetailConnectListener(Handler handler) {
			// TODO Auto-generated constructor stub
			mHandler = handler;
		}

		@Override
		public void onStart(THttpRequest request) {
		}

		@Override
		public void onFinish(THttpRequest request, IResponse response) {
			if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
				// 定义一种新的类型
				ArrayList<FeaturedThemeDetailBean> list = (ArrayList<FeaturedThemeDetailBean>) response
						.getResponse();
				FeaturedThemeDetailBean resData = list.get(0);
				Message msg = Message.obtain();
				if (resData.mBigimgids != null) {
					// XmlCacheParser cacheParser = new XmlCacheParser(
					// LauncherEnv.Path.GOTHEMES_DATACAHE_PATH);
					// cacheParser.saveCacheData(mContext,
					// FEATURED_THEME_DETAIL_FILE_PATH, request,
					// response.getResponse());
					msg.obj = themeDetailBean2ThemeInfo(resData);
					msg.what = ThemeDetailActivity.MSG_GET_DETAIL_FINISHED;

				} else {
					msg.what = ThemeDetailActivity.MSG_GET_DETAIL_FAILED;
				}
				if (mHandler != null) {
					mHandler.sendMessage(msg);
				}

			}
		}

		@Override
		public void onException(THttpRequest request, int reason) {
			if (mHandler != null) {
				Message msg = Message.obtain();
				msg.what = ThemeDetailActivity.MSG_GET_DETAIL_FAILED;
				mHandler.sendMessage(msg);
			}
		}
	}

	public static String gzip(byte[] bs) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		GZIPOutputStream gzout = null;
		try {
			gzout = new GZIPOutputStream(bout);
			gzout.write(bs);
			gzout.flush();
		} catch (Exception e) {
			throw e;

		} finally {
			if (gzout != null) {
				try {
					gzout.close();
				} catch (Exception ex) {
				}
			}
		}
		String result = null;
		if (bout != null) {
			result = bout.toString("ISO-8859-1");
		}
		return result;
	}

	public ThemeInfoBean getFeatureThemeDetailInfo(ThemeInfoBean infoBean, Handler handler) {
		ThemeInfoBean result = null;
		//		XmlCacheParser cacheParser = new XmlCacheParser(LauncherEnv.Path.GOTHEMES_DATACAHE_PATH);
		GoStoreHttpTool httpTool = GoStoreHttpTool.getInstance(mContext);
		if (httpTool == null) {
			return null;
		}
		THttpRequest request = httpTool.createTHttpRequest(FEATURED_THEME_DETAIL_REQUEST_FUNID,
				getFeaturedDetailPostValuePairs(infoBean), new DetailConnectListener(handler));
		// if (FileUtil.isSDCardAvaiable()) {
		// Object object = cacheParser.getCacheData(mContext,
		// FEATURED_THEME_DETAIL_FILE_PATH,
		// request);
		// if (object != null && object instanceof ArrayList<?>) {
		// result =
		// themeDetailBean2ThemeInfo(((ArrayList<FeaturedThemeDetailBean>)
		// object)
		// .get(0));
		// }
		// }
		if (result == null) {
			httpTool.getDataForeNetByRequest(request);
		}
		return result;
	}

	private ThemeInfoBean themeDetailBean2ThemeInfo(FeaturedThemeDetailBean detailBean) {
		ThemeInfoBean infoBean = new ThemeInfoBean(detailBean.mName, detailBean.mPackageName);
		infoBean.setDownloadUrl(detailBean.mDownurl);
		infoBean.setFeaturedId(detailBean.mId);
		infoBean.setPayId(detailBean.mPayId);
		infoBean.setPayType(detailBean.mPayType);
		infoBean.setMlcokerThemeName(detailBean.mMlocker);
		infoBean.setMwidgetThemeName(detailBean.mMwidget);
		infoBean.setIsNewTheme(detailBean.mIsAll);
		infoBean.setSar(detailBean.mStar);
		infoBean.setVersionName(detailBean.mVersion);
		infoBean.setVersionCode(String.valueOf(detailBean.mVersionNum));
		infoBean.setFeeType(detailBean.mFeeType);
		infoBean.setPackageName(detailBean.mPackageName);
		infoBean.setImgSource(detailBean.mBigImgSource);
		infoBean.setImgUrls(detailBean.mBigImgUrl);
		infoBean.setPrice(detailBean.mPrice);
		infoBean.setVimgUrl(detailBean.mVimgUrl);
		infoBean.setVurl(detailBean.mVurl);
		String info = detailBean.mDevelop + "\n" + detailBean.mSummary + "\n" + detailBean.mDetail
				+ detailBean.mUpdateLog;
		infoBean.setThemeInfo(info);
		if (detailBean.mPackageName.contains(ThemeConstants.LAUNCHER_THEME_PREFIX)) {
			infoBean.setBeanType(ThemeConstants.LAUNCHER_FEATURED_THEME_ID);
		}
		if (detailBean.mBigimgids != null) {
			String[] ids = detailBean.mBigimgids.split("#");
			if (ids != null && ids.length > 0) {
				infoBean.setImgIds(Arrays.asList(ids));
				for (int i = 0; i < ids.length; i++) {
					infoBean.addDrawableName(ids[i]);
				}

			}
		}
		infoBean.setUrlMap(detailBean.mUrlMap);
		return infoBean;
	}

	public ThemeBannerBean getBannerData(int type, BroadCasterObserver observer) {
		ThemeBannerBean result = null;
		String cacheFile = LauncherEnv.Path.GOTLOCKER_BANNER_DATACAHE_PATH;
		if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			cacheFile = LauncherEnv.Path.GOTHEMES_BANNER_DATACAHE_PATH;
		}
		XmlCacheParser cacheParser = new XmlCacheParser(cacheFile);
		GoStoreHttpTool httpTool = GoStoreHttpTool.getInstance(mContext);
		if (httpTool == null) {
			return null;
		}
		THttpRequest request = httpTool.createTHttpRequest(BANNER_THEME_REQUEST_FUNID,
				getBannerPostValuePairs(type), new BannerConnectListener(type, observer));
		if (FileUtil.isSDCardAvaiable()) {
			Object object = null;
			if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
				object = cacheParser.getCacheData(mContext, BANNER_CATCH_FILE_PATH, request);
			} else {
				object = cacheParser.getCacheData(mContext, LOCKERBANNER_CATCH_FILE_PATH, request);
			}
			if (object != null && object instanceof ArrayList<?>) {
				result = ((ArrayList<ThemeBannerBean>) object).get(0);
				if (result != null) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Date date = new Date();
					String today = sdf.format(date);
					if (result.mElements != null && result.mElements.size() > 0) {
						BannerElement element = result.mElements.get(0);
						if (element.mSDate != null && element.mEDate != null) {
							if (today.compareTo(element.mSDate) < 0) {
								result = null;
							} else if (today.compareTo(element.mEDate) > 0) {
								if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
									cacheParser
											.cleanCacheData(LauncherEnv.Path.GOTHEMES_BANNER_DATACAHE_PATH);
									FileUtil.deleteFile(BANNER_CATCH_FILE_PATH);
									FileUtil.deleteFile(SPEC_THEME_CATCH_FILE_PATH);
								} else {
									cacheParser
											.cleanCacheData(LauncherEnv.Path.GOTLOCKER_BANNER_DATACAHE_PATH);
									FileUtil.deleteFile(LOCKERBANNER_CATCH_FILE_PATH);
									FileUtil.deleteFile(SPEC_LOCKER_THEME_CATCH_FILE_PATH);
								}
							}
						}
					}
				}
			}
		}
		if (result == null) {
			httpTool.getDataForeNetByRequest(request);
		}
		return result;
	}

	private ArrayList<NameValuePair> getBannerPostValuePairs(int type) {
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

		// 获取时间戳
		int ty = 0;
		long timeStamp = 0;
		File file = null;
		if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			timeStamp = getBannerPostTimeStamp(mContext);
			file = new File(BANNER_CATCH_FILE_PATH);
			ty = 0;
		} else {
			timeStamp = getLockerBannerPostTimeStamp(mContext);
			file = new File(LOCKERBANNER_CATCH_FILE_PATH);
			ty = 1;
		}
		if (timeStamp > 0 && !file.exists()) {
			timeStamp = 0;
		}
		BasicNameValuePair basicNameValuePair = new BasicNameValuePair("timestamp",
				String.valueOf(timeStamp));
		nameValuePairs.add(basicNameValuePair);
		basicNameValuePair = new BasicNameValuePair("ty", String.valueOf(ty));
		nameValuePairs.add(basicNameValuePair);
		return nameValuePairs;
	}

	/**
	 * 
	 * 类描述: 功能详细描述:
	 * 
	 * @author
	 * @date [2012-9-28]
	 */
	class BannerConnectListener implements IConnectListener {
		private BroadCasterObserver mObserver;
		private int mType;
		BannerConnectListener(int type, BroadCasterObserver observer) {
			mObserver = observer;
			mType = type;
		}

		@Override
		public void onStart(THttpRequest request) {
		}

		@Override
		public void onFinish(THttpRequest request, IResponse response) {
			if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
				// 定义一种新的类型
				ArrayList<ThemeBannerBean> list = (ArrayList<ThemeBannerBean>) response
						.getResponse();
				if (list != null && list.size() > 0) {
					ThemeBannerBean resData = list.get(0);
					resData.mType = mType;
					ArrayList<BannerElement> elements = resData.mElements;
					if (elements != null && elements.size() > 0) {
						try {
							BannerElement element = elements.get(0);
							if (element.mGroup != null) {
								int index = element.mGroup.lastIndexOf("pkgnames=");
								if (index > 0) {
									String pkgs = element.mGroup.substring(index
											+ "pkgnames=".length());
									if (pkgs != null) {
										element.mPkgs = pkgs.split(",");
									}
								}
							}

						} catch (Exception e) {
							// TODO: handle exception
						}
						String cachfilePath = null;
						String bannerCachfilePath = null;
						String specCachfilePath = null;
						if (mType == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
							setBannerPostTimeStamp(mContext, resData.mTimeStamp);
							cachfilePath = LauncherEnv.Path.GOTHEMES_BANNER_DATACAHE_PATH;
							bannerCachfilePath = BANNER_CATCH_FILE_PATH;
							specCachfilePath = SPEC_THEME_CATCH_FILE_PATH;
						} else {
							setLockerBannerPostTimeStamp(mContext, resData.mTimeStamp);
							cachfilePath = LauncherEnv.Path.GOTLOCKER_BANNER_DATACAHE_PATH;
							bannerCachfilePath = LOCKERBANNER_CATCH_FILE_PATH;
							specCachfilePath = SPEC_LOCKER_THEME_CATCH_FILE_PATH;
						}
						XmlCacheParser cacheParser = new XmlCacheParser(cachfilePath);
						cacheParser.cleanCacheData(cachfilePath);
						FileUtil.deleteFile(bannerCachfilePath);
						FileUtil.deleteFile(specCachfilePath);
						cacheParser.saveCacheData(mContext, bannerCachfilePath, request,
								response.getResponse());

						mObserver.onBCChange(ThemeDataManager.MSG_GET_BANNER_FINISHED, -1, resData,
								null);
					}
				}

			}
		}

		@Override
		public void onException(THttpRequest request, int reason) {
		}
	}

	/**
	 * <br>
	 * 功能简述:获取专题数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param installThemes
	 * @return
	 */
	public ArrayList<ThemeInfoBean> getSpecThemeThemeInfoBeans(
			ArrayList<ThemeInfoBean> installThemes, int ty, BroadCasterObserver observer) {

		ArrayList<ThemeInfoBean> result = null;
		ThemeSpecDataBean bean = null;
		XmlCacheParser cacheParser = new XmlCacheParser(
				LauncherEnv.Path.GOTHEMES_SPEC_DATACAHE_PATH);
		GoStoreHttpTool httpTool = GoStoreHttpTool.getInstance(mContext);
		if (httpTool == null) {
			return null;
		}
		THttpRequest request = httpTool.createTHttpRequest(SPEC_THEME_REQUEST_FUNID,
				getSpecThemePostValuePairs(installThemes, ty), new SpecThemeConnectListener(
						observer));
		if (FileUtil.isSDCardAvaiable()) {
			Object object = cacheParser.getCacheData(mContext, SPEC_THEME_CATCH_FILE_PATH, request);
			if (object != null && object instanceof ArrayList<?>) {
				bean = ((ArrayList<ThemeSpecDataBean>) object).get(0);
				Intent it = new Intent(ICustomAction.ACTION_SPEC_THEME_TITLE);
				it.putExtra("title", bean.mName);
				it.putExtra("config", bean.mStylepack);
				it.setData(Uri.parse("package://"));
				mContext.sendBroadcast(it);
			}
		}
		if (bean != null) {
			result = specData2ThemeInfoBean(bean);
		} else {
			httpTool.getDataForeNetByRequest(request);
		}
		return result;
	}

	private ArrayList<NameValuePair> getSpecThemePostValuePairs(ArrayList<ThemeInfoBean> infoBeans,
			int ty) {
		StringBuffer packageNameData = getAllInstallThemePackageNameData(infoBeans);
		String installList = null;
		try {
			installList = gzip(packageNameData.toString().getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		BasicNameValuePair basicNameValuePair = new BasicNameValuePair("owns", installList);
		nameValuePairs.add(basicNameValuePair);
		basicNameValuePair = new BasicNameValuePair("appuid", String.valueOf(THEME_APPUID));
		nameValuePairs.add(basicNameValuePair);
		basicNameValuePair = new BasicNameValuePair("ty", String.valueOf(ty));
		nameValuePairs.add(basicNameValuePair);
		basicNameValuePair = new BasicNameValuePair("pn", "1");
		nameValuePairs.add(basicNameValuePair);

		// 获取时间戳
		long timeStamp = getSpecPostTimeStamp(mContext);
		File file = new File(SPEC_THEME_CATCH_FILE_PATH);
		if (timeStamp > 0 && !file.exists()) {
			timeStamp = 0;
		}
		basicNameValuePair = new BasicNameValuePair("timestamp", String.valueOf(timeStamp));
		nameValuePairs.add(basicNameValuePair);
		return nameValuePairs;
	}

	/**
	 * 
	 * 类描述: 功能详细描述:
	 * 
	 * @author
	 * @date [2012-9-28]
	 */
	class SpecThemeConnectListener implements IConnectListener {

		private BroadCasterObserver mObserver;

		public SpecThemeConnectListener(BroadCasterObserver observer) {
			mObserver = observer;
		}

		@Override
		public void onStart(THttpRequest request) {
		}

		@Override
		public void onFinish(THttpRequest request, IResponse response) {
			if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
				// 定义一种新的类型
				ArrayList<ThemeSpecDataBean> list = (ArrayList<ThemeSpecDataBean>) response
						.getResponse();
				ThemeSpecDataBean resData = list.get(0);
				ArrayList<ThemeSpecDataBean.FeaturedElement> elements = resData.mElementsList;
				if (elements != null && elements.size() > 0) {
					setSpecPostTimeStamp(mContext, resData.mTimeStamp);
					XmlCacheParser cacheParser = new XmlCacheParser(
							LauncherEnv.Path.GOTHEMES_SPEC_DATACAHE_PATH);
					cacheParser.cleanCacheData(LauncherEnv.Path.GOTHEMES_SPEC_DATACAHE_PATH);
					FileUtil.deleteFile(BANNER_CATCH_FILE_PATH);
					FileUtil.deleteFile(SPEC_THEME_CATCH_FILE_PATH);
					cacheParser.saveCacheData(mContext, SPEC_THEME_CATCH_FILE_PATH, request,
							response.getResponse());
					if (mObserver != null) {
						mObserver.onBCChange(ThemeDataManager.MSG_GET_SPEC_FINISHED, -1,
								specData2ThemeInfoBean(resData), null);
					}
					if (mContext != null) {
						Intent it = new Intent(ICustomAction.ACTION_SPEC_THEME_TITLE);
						it.putExtra("title", resData.mName);
						it.putExtra("config", resData.mStylepack);
						it.setData(Uri.parse("package://"));
						mContext.sendBroadcast(it);
					}

				}
			}
		}

		@Override
		public void onException(THttpRequest request, int reason) {
		}
	}

	/**
	 * <br>
	 * 功能简述:专题数据转到ThemeInfoBean <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param bean
	 * @return
	 */
	public ArrayList<ThemeInfoBean> specData2ThemeInfoBean(ThemeSpecDataBean bean) {
		ArrayList<ThemeInfoBean> list = null;
		if (bean != null && bean.mElementsList != null && bean.mElementsList.size() > 0) {
			ArrayList<ThemeSpecDataBean.FeaturedElement> elements = bean.mElementsList;
			list = new ArrayList<ThemeInfoBean>();
			for (int i = 0; i < elements.size(); i++) {
				ThemeSpecDataBean.FeaturedElement element = elements.get(i);
				ThemeInfoBean paidBean = null;
				String imgName = element.mImgId;
				if (element.mPkgName != null) {
					paidBean = new ThemeInfoBean();
					paidBean.setPackageName(element.mPkgName);
					paidBean.setVersionCode(String.valueOf(element.mVersionCode));
					paidBean.setVersionName(element.mVersion);
					paidBean.setIsNewTheme(ConvertUtils.int2boolean(element.mIsall));
					paidBean.setThemeInfo(element.mDetail);
					paidBean.setThemeName(element.mName);
					paidBean.addDrawableName(LauncherEnv.Path.GOTHEMES_PATH + "icon/" + imgName);
					paidBean.setUrlMap(element.mUrlMap);
					paidBean.setFeaturedId(element.mId);
					paidBean.setFeeType(element.mFeeType);
					paidBean.setPayId(element.mPayId);
					paidBean.setPayType(element.mPayType);
					paidBean.setDownloadUrl(element.mDownurl);
					paidBean.setIsNew(element.mIsNew);
					paidBean.setImgSource(element.mSource);
					paidBean.setImgUrls(element.mIconUrls);
					paidBean.setBeanType(ThemeConstants.LAUNCHER_SPEC_THEME_ID);
					paidBean.setSortId(element.mSortId);
					paidBean.setPrice(element.mPrice);
					list.add(paidBean);
				}
			}
		}

		return list;
	}

}

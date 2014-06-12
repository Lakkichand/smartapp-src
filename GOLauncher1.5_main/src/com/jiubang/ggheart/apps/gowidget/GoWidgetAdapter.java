package com.jiubang.ggheart.apps.gowidget;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.device.Machine;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.adrecommend.AdElement;
import com.jiubang.ggheart.data.theme.adrecommend.AdHttpAdapter;
import com.jiubang.ggheart.data.theme.adrecommend.AdHttpAdapter.AdResponseData;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.parser.ThemeInfoParser;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-9-17]
 */
public class GoWidgetAdapter extends BaseAdapter {
	public static final int INSTALL_VIEW = 0;
	public static final int UNINSTALL_VIEW = 1;

	public static final int NOTINSTALLED = 0; // 未安装
	public static final int INSTALLED = 1; // 已安装
	public static final int CO_VERSION = 2; // 合作版本

	public static final float DEFAULT_DENSITY = 240f;
	public static final String TASK_MANAGER = "com.gau.go.launcherex.gowidget.taskmanager";

	private static final String LANGUAGE = "language";

	public ArrayList<ThemeInfoBean> mThemeInfos;

	private Context mContext;
	private ArrayList<GoWidgetProviderInfo> mGoWidgetInfoList;
	private LayoutInflater mInflater;

	private GoWidgetFinder mFinder;
	private HashMap<String, GoWidgetProviderInfo> mProviderMap;
	// 标记是否进入卸载界面
	private boolean mIsDeleteView = false;

	public GoWidgetAdapter(Context context) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
		initList();
	}

	/**
	 * 初始化Go Widget列表
	 */
	private void initList() {
		initData();
		mGoWidgetInfoList = new ArrayList<GoWidgetProviderInfo>();

		getAdapter(INSTALLED);
	}

	private void initData() {
		if (mThemeInfos != null) {
			mThemeInfos.clear();
			mThemeInfos = null;
		}
		mThemeInfos = new ArrayList<ThemeInfoBean>();

		// 从本地或网络获取到数据
		addTollTheme(mThemeInfos);

	}

	public GoWidgetAdapter getAdapter(int inWhichList) {
		if (mGoWidgetInfoList != null) {
			mGoWidgetInfoList.clear();
		}
		mFinder = new GoWidgetFinder(mContext);
		mFinder.scanAllInstalledGoWidget();
		mProviderMap = mFinder.getGoWidgetInfosMap();

		// 加载所有Go Widget到列表
		Set<Entry<String, GoWidgetProviderInfo>> entryset = mProviderMap.entrySet();
		// 添加推荐widget，如果已经安装的widget没有推荐的widget，就添加到列表后面
		GoWidgetProviderInfo providerInfo = null;
		int size = 0;
		if (mThemeInfos != null) {
			size = mThemeInfos.size();
		}
		switch (inWhichList) {
		// 未安装
			case NOTINSTALLED :
				ThemeInfoBean infoBean = null;
				for (int i = 0; i < size; i++) {
					infoBean = mThemeInfos.get(i);
					providerInfo = mProviderMap.get(infoBean.getPackageName());
					if (providerInfo == null) {
						GoWidgetProviderInfo info = new GoWidgetProviderInfo("", "");
						info.mProvider.label = infoBean.getThemeName();
						if (infoBean.getPackageName() != null) {
							info.mGoWidgetPkgName = infoBean.getPackageName();
						}
						if (infoBean.getPreViewDrawableNames() != null
								&& infoBean.getPreViewDrawableNames().size() > 0) {
							info.mIconPath = infoBean.getPreViewDrawableNames().get(0);
						}
						if (infoBean.getThemeInfo() != null) {
							info.mDownloadUrl = infoBean.getThemeInfo().trim();
						}
						mGoWidgetInfoList.add(info);
					}
					providerInfo = null;
				}
				this.notifyDataSetChanged();
				return this;
				// 已安装
			case INSTALLED :
				ThemeInfoBean bean = null;
				if (mThemeInfos != null && mThemeInfos.size() > 0) {
					bean = mThemeInfos.get(0);
					if (bean != null && bean.getVersionCode() == null) {
						// 缓存文件不存在
						for (Entry<String, GoWidgetProviderInfo> entry : entryset) {
							mGoWidgetInfoList.add(entry.getValue());
						}
					} else {
						String pkgName, beanPkgName;
						for (Entry<String, GoWidgetProviderInfo> entry : entryset) {
							pkgName = entry.getValue().mProvider.provider.getPackageName();
							for (int i = 0; i < size; i++) {
								bean = mThemeInfos.get(i);
								beanPkgName = bean.getPackageName();
								String version = bean.getVersionCode();
								if (pkgName.equals(beanPkgName) && version != null) {
									entry.getValue().mVersionCode = Integer.parseInt(version);
									break;
								}
							}
							mGoWidgetInfoList.add(entry.getValue());
						}
					}
				}

				final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
				final ArrayList<InnerWidgetInfo> innerWidgets = widgetManager.getInnerWidgetList();
				if (innerWidgets != null) {
					int count = innerWidgets.size();
					for (int i = 0; i < count; i++) {
						final InnerWidgetInfo innerWidgetInfo = innerWidgets.get(i);
						if (innerWidgetInfo.mBuildin == InnerWidgetInfo.BUILDIN_ALL) {
							providerInfo = new GoWidgetProviderInfo(innerWidgetInfo.mWidgetPkg, "");
							providerInfo.mProvider.label = innerWidgetInfo.mTitle;
							providerInfo.mProvider.icon = innerWidgetInfo.mIconId;
							providerInfo.mInnerWidgetInfo = innerWidgetInfo;
							if (mIsDeleteView) {
								mGoWidgetInfoList.add(providerInfo);
							} else {
								mGoWidgetInfoList.add(0, providerInfo);
							}
						}
					}
				}
				this.notifyDataSetChanged();
				return this;
			default :
				return null;
		}
	}

	@Override
	public int getCount() {
		return mGoWidgetInfoList.size();
	}

	@Override
	public Object getItem(int position) {
		return mGoWidgetInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}

	public void addTollTheme(ArrayList<ThemeInfoBean> arrayList) {

		// 获取未安装的所有收费主题
		ArrayList<ThemeInfoBean> paidThemeInfoBeans = getPaidThemeInfoBeans();
		if (paidThemeInfoBeans == null || paidThemeInfoBeans.size() <= 0) {
			return;
		}

		// 获取待显示的收费主题
		int size = paidThemeInfoBeans.size();
		for (int i = size - 1; i >= 0; --i) {
			arrayList.add(0, paidThemeInfoBeans.get(i));
		}

		paidThemeInfoBeans.clear();
		paidThemeInfoBeans = null;

	}

	private ArrayList<ThemeInfoBean> getPaidThemeInfoBeans() {

		ArrayList<ThemeInfoBean> paidThemeInfoBeans = new ArrayList<ThemeInfoBean>();
		final String xmlFile = LauncherEnv.Path.GOTHEMES_PATH + "gowidget.xml"; // 配置文件名
		final String iconPath = LauncherEnv.Path.GOTHEMES_PATH + "icon/"; // 图片保存路径

		// 当前广告位版本号
		// 解析本地文件生成themeinfobean列表
		final StringBuffer curVersionBuf = new StringBuffer();
		final StringBuffer recommendThemesBuf = new StringBuffer();

		if (xmlFile != null) {
			// 判断缓存文件语言是否与目前系统语言相同,不同则从新请求当前系统语言的widget列表
			InputStream inputStream = null;
			// 获取缓存文件的语言
			try {
				String language = null;
				XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
				inputStream = new FileInputStream(xmlFile);
				parser.setInput(inputStream, null);
				while (parser.next() != XmlPullParser.END_DOCUMENT) {
					if (parser.getName() != null && parser.getName().equals(LANGUAGE)) {
						language = parser.getAttributeValue("", LANGUAGE);
						break;
					}
				}
				String tmpLaunguage = Locale.getDefault().getLanguage()
						+ Locale.getDefault().getCountry();
				if (language == null || !language.equals(tmpLaunguage)) {
					// 旧版缓存文件不存在language标签，在此把旧版缓存文件删除
					FileUtil.deleteFile(xmlFile);
				}
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
						inputStream = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		new ThemeInfoParser().parseLauncherThemeXml(mContext, xmlFile, curVersionBuf,
				recommendThemesBuf, paidThemeInfoBeans, ThemeConstants.LAUNCHER_FEATURED_THEME_ID);
		boolean isHasSinaWeibo = false;
		for (ThemeInfoBean bean : paidThemeInfoBeans) {
			String packageName = bean.getPackageName();
			if ("com.gau.go.launcherex.gowidget.weibowidget".equals(packageName)) {
				isHasSinaWeibo = true;
				break;
			}
		}
		if (!isHasSinaWeibo && Machine.isCnUser(mContext)) {
			ThemeInfoBean bean = new ThemeInfoBean("新浪微博",
					"com.gau.go.launcherex.gowidget.weibowidget");
			bean.addDrawableName("sina_weibo");
			bean.setThemeInfo("http://smsftp.3g.cn/soft/3GHeart/golauncher/widget/FTP/SinaWeibo.apk");
			paidThemeInfoBeans.add(bean);
		}

		/**
		 * 
		 * <br>类描述:
		 * <br>功能详细描述:
		 * 
		 * @author  zhouxuewen
		 * @date  [2012-9-17]
		 */
		class ConnectListener implements IConnectListener {
			private String mNewVersion; // 服务器版本号
			private int mRecommendCount; // 推荐个数

			public ConnectListener() {
			}

			public ConnectListener(String version, int recommendCount) {
				mNewVersion = version;
				mRecommendCount = recommendCount;
			}

			@Override
			public void onStart(THttpRequest request) {
			}

			@Override
			public void onFinish(THttpRequest request, IResponse response) {
				if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
					// 定义一种新的类型
					AdResponseData resData = (AdResponseData) response.getResponse();

					// 返回解析后的数据，以列表形式存储。
					ArrayList<AdElement> adList = resData.mAdList;
					if (adList != null && adList.size() > 0
							&& adList.get(0).mAdName.equals("version")) {
						mNewVersion = adList.get(0).mAdText;
						if (mNewVersion == null || mNewVersion.trim() == null
								|| mNewVersion.trim().length() <= 0) {
							mNewVersion = "0";
						}
						mRecommendCount = adList.get(0).mMaxDisplayCount;
						String curVersion = null; // 本地版本号
						if (curVersionBuf != null && curVersionBuf.toString() != null
								&& curVersionBuf.toString().trim() != null
								&& curVersionBuf.toString().trim().length() > 0) {
							curVersion = curVersionBuf.toString().trim();

						}
						if (curVersion == null
								|| Integer.valueOf(mNewVersion) > (Integer.valueOf(curVersion))) {
							// 特殊处理，获取广告位数据
							AdHttpAdapter adHttpAdapter = new AdHttpAdapter(mContext,
									new ConnectListener(mNewVersion, mRecommendCount));
							String verString = mContext.getResources().getString(
									R.string.curVersion);
							String fm = Statistics.getUid(mContext);
							String pid = String.valueOf("1013"); // 链接地址的id
							adHttpAdapter.getAdData(null, null, verString, null, pid, 10, null, fm,
									Statistics.getVirtualIMEI(mContext));
						}
					} else {
						boolean result = saveAdElementAsPaidBeanToSDCard(adList, mNewVersion,
								mRecommendCount);
						// if (result) {
						// //发布信息到通知栏
						// sendNewThemesNotification();
						// }

					}
				}
			}

			@Override
			public void onException(THttpRequest request, int reason) {
				StatisticsData.saveHttpExceptionDate(mContext, request, reason);
			}

			private boolean saveAdElementAsPaidBeanToSDCard(ArrayList<AdElement> adList,
					String version, int recommendCount) {
				boolean result = false;
				if (adList == null || adList.size() <= 0) {
					return result;
				}
				int count = adList.size();
				ArrayList<ThemeInfoBean> beansList = new ArrayList<ThemeInfoBean>(count);
				String pkgName = null;
				ThemeInfoBean paidBean = null;
				AdElement adElement = null;
				for (int i = 0; i < count; i++) {
					adElement = adList.get(i);
					pkgName = adElement.mAdOptData;
					String imgName = "gowidget" + String.valueOf(i);
					paidBean = new ThemeInfoBean();
					paidBean.setPackageName(pkgName);
					paidBean.setThemeName(adElement.mAdName);
					paidBean.setThemeInfo(adElement.mAdText);
					// 将版本号转化为String类型保存，方便进行xml写操作
					String versionCode = String.valueOf(adElement.mDelay);
					paidBean.setVersionCode(versionCode);
					FileUtil.saveBitmapToSDFile(adElement.mIcon, iconPath + imgName,
							CompressFormat.PNG);
					paidBean.addDrawableName(imgName);

					beansList.add(paidBean);
				}
				// 将新下载推荐信息保存至xml文件中
				result = new ThemeInfoParser().writeGoThemeToXml(version, recommendCount,
						beansList, xmlFile);
				return result;
			}
		}

		// 过滤是否已安装
		// filterPaidTheme(paidThemeInfoBeans);

		// 过滤排序
		int recomendThemesCount = 0;
		if (recommendThemesBuf != null && recommendThemesBuf.toString() != null
				&& recommendThemesBuf.toString().trim() != null
				&& recommendThemesBuf.toString().trim().length() > 0) {
			try {
				recomendThemesCount = Integer.valueOf(recommendThemesBuf.toString().trim());
			} catch (Exception e) {
				Log.i("ThemeManager", "Integer.valueOf has exception");
			}
		}
		// paidThemeInfoBeans = filterRecommendPaidTheme(paidThemeInfoBeans,
		// recomendThemesCount);

		// 本地无推荐内容，则通过网络获取推荐内容。
		AdHttpAdapter adHttpAdapter = new AdHttpAdapter(mContext, new ConnectListener());
		final String pid = String.valueOf("1012"); // 链接地址的id
		int pageCount = 10; // 每页数据条数
		String fm = Statistics.getUid(mContext);
		String curVerString = mContext.getResources().getString(R.string.curVersion);
		// 获取版本信息的数据
		adHttpAdapter.getAdData(null, null, curVerString, null, pid, pageCount, null, fm,
				Statistics.getVirtualIMEI(mContext));

		return paidThemeInfoBeans;
	}

	public static String getDownloadUrl(Context context, CharSequence name) {
		String url = "";
		if (context == null || name == null) {
			return url;
		}

		GoWidgetAdapter widgetAdapter = new GoWidgetAdapter(context);
		ArrayList<GoWidgetProviderInfo> infoList = widgetAdapter.mGoWidgetInfoList;
		if (infoList == null || infoList.size() == 0) {
			return url;
		}

		for (GoWidgetProviderInfo info : infoList) {
			if (info.mProvider.label != null && info.mProvider.label.contains(name)) {
				url = info.mDownloadUrl;
				break;
			}
		}

		if (url == null || url.equals("")) {
			// 再从mThemesInfo里取，以免因为本地已安装，则没有下载地址添加到
			ArrayList<ThemeInfoBean> themeInfos = widgetAdapter.mThemeInfos;
			if (themeInfos != null && themeInfos.size() > 0) {
				for (ThemeInfoBean infoBean : themeInfos) {
					if (infoBean.getThemeName() != null
							&& name.toString().toLowerCase()
									.contains(infoBean.getThemeName().toLowerCase())) {
						url = infoBean.getThemeInfo();
						break;
					}
				}
			}
		}

		return url;
	}

	private void deleteTheme(String pkgName) {
		OutOfMemoryHandler.handle();

		Uri packageURI = Uri.parse("package:" + pkgName);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		Activity activity = (Activity) mContext;
		activity.startActivityForResult(uninstallIntent,
				IRequestCodeIds.REQUEST_THEME_SCAN_VIEW_REFRESH);
	}

	/**
	 * 获取指定包的版本号
	 * 
	 * @author huyong
	 * @param context
	 * @param pkgName
	 */
	private int getVersionCodeByPkgName(Context context, String pkgName) {
		int versionCode = 0;
		if (pkgName != null) {
			PackageManager pkgManager = context.getPackageManager();
			try {
				PackageInfo pkgInfo = pkgManager.getPackageInfo(pkgName, 0);
				versionCode = pkgInfo.versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return versionCode;
	}
}

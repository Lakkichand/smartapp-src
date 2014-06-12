package com.jiubang.ggheart.components.gohandbook;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * 类描述: GO手册主页
 * 功能详细描述:
 * 
 * @author licanhui
 * @date [2012-8-24]
 */
public class GoHandBookMainActivity extends Activity implements GoHandBookIndexListner {
	private SharedPreferencesUtil mPreferencesUtil;
	private LinearLayout mViewGroupLayout;
	private GoHandBookItemWebView mBookItemWebView;
	private int mBrowseName; // 正在浏览那一页
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			//请求URL地址成功
				case GoHandBookUtils.REQUEST_URL_SUCCESS :
					if (msg.obj != null && !msg.obj.toString().equals("")) {
						String indexUrlString = msg.obj.toString();
						//						Log.i("lch", "request success:address:" + indexUrlString);
						setCacheIndexUrl(indexUrlString); //设置URL缓存
						setCacheLanguageAndVersion(getCurLanguageAndVersion()); //设置版本更新缓存
						mBookItemWebView.loadUrl(indexUrlString);
					}
					break;

				//请求URL地址失败
				case GoHandBookUtils.REQUEST_URL_FAIL :
					//					Log.i("lch", "request fail");
					mBookItemWebView.setReTryBtnListner();
					//					mBookItemWebView.loadUrl(getIndexUrl());
					break;

				default :
					break;
			}
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.go_handbook_main);

		initGoSettingControler();
		mPreferencesUtil = new SharedPreferencesUtil(this);
		LinearLayout.LayoutParams params = new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);

		mViewGroupLayout = (LinearLayout) findViewById(R.id.viewGroup);
		mBookItemWebView = new GoHandBookItemWebView(this, null, -1, -1);
		mViewGroupLayout.addView(mBookItemWebView, params);
		mBookItemWebView.getWebView().addJavascriptInterface(new DemoJavaScriptInterface(), "demo");
		mBookItemWebView.setGoHandBookIndexListner(this);
		checkNeedUpdateIndexUrl(); //检查是否需要重新请求新的URL地址
		//		mBookItemWebView.loadUrl(getIndexUrl());
	}

	/**
	 * <br>功能简述:由于Go手册是一个独立进程，静态变量不共享，是没有GoSettingControler，和ImageExplorer的对象的
	 * <br>功能详细描述:GoHandBookContentActivity包含指示器。会用到GoSettingControler和ImageExplorer，如果在GoHandBookContentActivity创建，这退出时间
	 * 会无法注销GoSettingControler和ImageExplorer，到时无法注销GoHandBookContentActivity，内存会始终包含一份GoHandBookContentActivity。
	 * <br>注意:
	 */
	public void initGoSettingControler() {
		GoSettingControler.getInstance(this);
		ImageExplorer.getInstance(this);

	}

	@Override
	protected void onResume() {
		mBookItemWebView.loadJavaScript("initData()"); // 返回调用JS方法就刷新更新和已读数量
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// 杀掉当前进程
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onDestroy();
	}

	/**
	 * 获取打开的首页
	 * @return
	 */
	public String getIndexUrl() {
		Locale locale = Locale.getDefault();
		String language = locale.getLanguage().toLowerCase();
		if (language.equals("zh")) {
			return GoHandBookConstants.INDEX_URL_CN; // 中文地址
		} else {
			return GoHandBookConstants.INDEX_URL_EN; // 英文地址
		}
	}

	/**
	 * 网页 JS 需要调用的方法类
	 * @author licanhui
	 */
	final class DemoJavaScriptInterface {

		DemoJavaScriptInterface() {

		}

		/**
		 * 点解9宫格每个ITEM
		 * 
		 * @param serverAddress 服务器地址
		 * @param title 标题
		 * @param urlList 网页队列
		 * @param keyList 网页ID队列
		 * @param openPage 要打开哪个页面
		 * @param browseName 当前浏览的页面。用来判断“继续浏览”跳到哪页，和“马上试用”打开的页面
		 */
		public void clickOnAndroidOpenPage(final String serverAddress, final String title,
				final String[] urlList, final String[] keyList, final int openPage,
				final int browseName) {

			// 用handler来更新UI
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mBrowseName = browseName; // 当前浏览的页面。用来判断“继续浏览”跳到哪页，和“马上试用”打开的页面
					// Java调用js方法
					Intent intent = new Intent(GoHandBookMainActivity.this,
							GoHandBookContentActivity.class);
					//					Log.i("serverAddress:", serverAddress);
					intent.putExtra(GoHandBookConstants.SERVER_ADDRESS, serverAddress); // 服务器地址
					intent.putExtra(GoHandBookConstants.TITLE, title); // 标题
					intent.putExtra(GoHandBookConstants.URL_LIST, urlList); // 地址队列
					intent.putExtra(GoHandBookConstants.ID_LIST, keyList); // ID队列
					intent.putExtra(GoHandBookConstants.OPEN_PAGE, openPage); // 需要打开的页码
					GoHandBookMainActivity.this.startActivityForResult(intent, 1);
				}
			});
		}

		/**
		 * <br>功能简述:获取完成百分比，统计数据
		 * <br>功能详细描述:
		 * <br>注意:
		 * @param persent
		 */
		public void clickOnAndroidSetPersent(int persent) {
			if (0 <= persent && persent <= 100) {
				StatisticsData.countMenuData(GoHandBookMainActivity.this,
						StatisticsData.MENU_ID_MANUAL_READ_PERCENAGE, persent);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				int defaultValue = -1;
				int type = data.getIntExtra(GoHandBookConstants.RESULT_TYPE, defaultValue);
				// 马上试用
				if (type == GoHandBookConstants.TRY_NOW_TYPE) {
					tryNow();
				}
				// 继续浏览
				else if (type == GoHandBookConstants.CONTINUE_BROWSE_TYPE) {
					continueBrowse();
				}
			}
		}
	}

	/**
	 * 马上试用
	 */
	public void tryNow() {
		switch (mBrowseName) {
			case GoHandBookConstants.BROWSE_PAGE_DESK :

				break;

			case GoHandBookConstants.BROWSE_PAGE_DOCK :
				break;

			case GoHandBookConstants.BROWSE_PAGE_FUNCTION :
				// 功能表
				sendUseNowBroadcast(GoHandBookConstants.BROWSE_PAGE_FUNCTION);
				break;

			case GoHandBookConstants.BROWSE_PAGE_FOLDER :
				break;

			case GoHandBookConstants.BROWSE_PAGE_WIDGET :
				break;

			case GoHandBookConstants.BROWSE_PAGE_GESTURE :
				break;

			case GoHandBookConstants.BROWSE_PAGE_CUSTOM :
				// 进入桌面预览状态
				sendUseNowBroadcast(GoHandBookConstants.BROWSE_PAGE_CUSTOM);

				break;

			case GoHandBookConstants.BROWSE_PAGE_PERIPHERAL :
				// 打开菜单——周边
				sendUseNowBroadcast(GoHandBookConstants.BROWSE_PAGE_PERIPHERAL);

				break;

			case GoHandBookConstants.BROWSE_PAGE_MORE :
				// 桌面设置界面
				sendUseNowBroadcast(GoHandBookConstants.BROWSE_PAGE_MORE);
				break;

			default :
				break;
		}
		GoHandBookMainActivity.this.finish();
	}

	/**
	 * 发送"马上试用"广播
	 * @param useNowType	类型
	 */
	public void sendUseNowBroadcast(int useNowType) {
		Intent intent = new Intent(ICustomAction.ACTION_GO_HANDBOOK_USE_NOW);
		intent.putExtra(GoHandBookConstants.GO_HANDBOOK_USE_NOW_TYPE, useNowType);
		sendBroadcast(intent);
	}

	/**
	 * 继续浏览
	 */
	public void continueBrowse() {
		switch (mBrowseName) {
			case GoHandBookConstants.BROWSE_PAGE_DESK :
				mBookItemWebView.loadJavaScript("openActivityDock()");
				break;

			case GoHandBookConstants.BROWSE_PAGE_DOCK :
				mBookItemWebView.loadJavaScript("openActivityFunction()");
				break;

			case GoHandBookConstants.BROWSE_PAGE_FUNCTION :
				mBookItemWebView.loadJavaScript("openActivityFolder()");
				break;

			case GoHandBookConstants.BROWSE_PAGE_FOLDER :
				mBookItemWebView.loadJavaScript("openActivityWidget()");
				break;

			case GoHandBookConstants.BROWSE_PAGE_WIDGET :
				mBookItemWebView.loadJavaScript("openActivityGesture()");
				break;

			case GoHandBookConstants.BROWSE_PAGE_GESTURE :
				mBookItemWebView.loadJavaScript("openActivityCustom()");
				break;

			case GoHandBookConstants.BROWSE_PAGE_CUSTOM :
				mBookItemWebView.loadJavaScript("openActivityPeripheral()");
				break;

			case GoHandBookConstants.BROWSE_PAGE_PERIPHERAL :
				mBookItemWebView.loadJavaScript("openActivityMore()");
				break;

			case GoHandBookConstants.BROWSE_PAGE_MORE :
				mBookItemWebView.loadJavaScript("openActivityDesk()");
				break;

			default :
				break;
		}
	}

	/**
	 * 获取URL缓存
	 * @return
	 */
	public String getCacheIndexUrl() {
		return mPreferencesUtil.getString(GoHandBookConstants.GO_HANDBOOK_URL, "");
	}

	/**
	 * 设置URL缓存
	 * @param urlString
	 */
	public void setCacheIndexUrl(String urlString) {
		mPreferencesUtil.saveString(GoHandBookConstants.GO_HANDBOOK_URL, urlString);
	}

	/**
	 * 检查是否需要重新请求新的URL地址
	 */
	public void checkNeedUpdateIndexUrl() {
		String curLanAndVerString = getCurLanguageAndVersion(); // 获取当前语言和版本号
		String cacheLanAndVerString = getCacheLanguageAndVersion(); // 获取语言和版本缓存
		String cacheIndexUrlString = getCacheIndexUrl(); // 获取语言和版本缓存

		//当前语言信息和缓存信息匹配 && 地址HTTP开头
		if (!cacheLanAndVerString.equals("") && cacheLanAndVerString.equals(curLanAndVerString)
				&& !cacheIndexUrlString.equals("") && cacheIndexUrlString.startsWith("http://")) {
			mBookItemWebView.loadUrl(cacheIndexUrlString);
		} else {
			GoHandBookUtils.getNetworkIndexUrlData(this, mHandler); // 请求网络Url地址

		}
	}

	/**
	 * 获取当前语言和版本号
	 * @return
	 */
	public String getCurLanguageAndVersion() {
		String languageString = GoHandBookUtils.getLanguage(this);
		String countryString = GoHandBookUtils.getCountry(this);
		String curVersion = this.getString(R.string.curVersion);
		String curLanAndVerString = String.format("%s_%s_%s", languageString, countryString,
				curVersion);
		//Log.i("lch", "curLanAndVerString：" + curLanAndVerString);
		return curLanAndVerString;
	}

	/**
	 * 设置语言和版本缓存
	 * @param urlString
	 */
	public void setCacheLanguageAndVersion(String lanAndVerString) {
		mPreferencesUtil.saveString(GoHandBookConstants.GO_HANDBOOK_LANGUAGE_AND_VERSION,
				lanAndVerString);
	}

	/**
	 * 获取语言和版本缓存
	 * @return
	 */
	public String getCacheLanguageAndVersion() {
		return mPreferencesUtil.getString(GoHandBookConstants.GO_HANDBOOK_LANGUAGE_AND_VERSION, "");
	}

	@Override
	public void retTry() {
		GoHandBookUtils.getNetworkIndexUrlData(this, mHandler); //请求网络Url地址
	}

	@Override
	public Resources getResources() {
		//由于是跨进程。所以需要重新创建一个新的
		DeskResourcesConfiguration configuration = DeskResourcesConfiguration.createInstance(this
				.getApplicationContext());
		if (null != configuration) {
			Resources resources = configuration.getDeskResources();
			if (null != resources) {
				return resources;
			}
		}
		return super.getResources();
	}
	
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
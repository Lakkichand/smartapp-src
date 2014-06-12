package com.jiubang.ggheart.apps.gowidget.widgetThemeChoose;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.mars.MImage;
import com.jiubang.ggheart.apps.desks.appfunc.WidgetStyleChooseView;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.themescan.DotProgressBar;
import com.jiubang.ggheart.apps.gowidget.GoWidgetAdapter;
import com.jiubang.ggheart.apps.gowidget.GoWidgetConstant;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.gostore.GoStoreChannelControl;
import com.jiubang.ggheart.components.DeskAlertDialog;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.PreviewSpecficThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.data.theme.parser.ParseSpecficWidgetTheme;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

public class WidgetThemeChooseFrame extends AbstractFrame
		implements
			android.view.View.OnClickListener,
			IWidgetChooseFrame {

	public static final String THEME_CATEGORY = "android.intent.category.DEFAULT"; // 主题包category
//	public static final String WIDGET_THEME_PACKAGE = "com.gau.go.launcherex.theme.gowidget";
	public static final String WIDGET_PACKAGE_PREFIX = "com.gau.go.launcherex.gowidget.";

	private static final int VIEW_TYPE_MORESKIN = 1;
	private static final int VIEW_TYPE_NORMSKIN = 2;

	// for handler msg
	public static final int EVENT_THEME_PARSE_OK = 1; // 主题信息解析完毕
	public static final int EVENT_ADD_REMOVE_SKIN = 2; // 一个样式有多套皮肤时会动态增加或删减皮肤
	/**
	 * “更多主题界面”显示的情况有： 1、滑动到最后一屏 2、当前显示屏动态删除成了最后一屏
	 */
	// public static final int EVENT_ADD_DOWNLOAD_VIEW = 3;

	private static boolean mDeskAlertDlgCanShow = true;
	private RecycleView mRecycleView;
	private DotProgressBar mProgressBar = null;

	private RelativeLayout mLayout;
	private LinearLayout mContenView;
	private WidgetStyleChooseView mChooseView;
	private DeskTextView mTitle;
	private Context mContext;

	private GoWidgetBaseInfo mWidgetInfo;
	private DeskTextView mToDeskButton;

	// 底下的button
	private LinearLayout mAddToDesktop = null;

	// widget的样式列表数据
	private LinkedList<WidgetInfo> mWidgetDatas;

	private Handler mHandler = null;

	private LayoutInflater mInflater;

	// 主题预览界面的主题是同一个宽高
	private int mCol;
	private int mRow;

	// 点击底部button的行为
	private enum ClickAction {
		ApplyWidgetTheme, UpgradeWidget, DownloadTheme
	}

	private ClickAction mClickAction = ClickAction.ApplyWidgetTheme;
	private static final String KEYWORD = "\"gowidget theme\"";

	// upgrade界面
	private ImageView mImgDownloadView;
	private ThemeParseTask mParseTask = null;

	private boolean isParseOnce;

	private final static HashMap<String, Integer> versionMap = new HashMap<String, Integer>();
	static {
		versionMap.put("com.gau.go.launcherex.gowidget.calendarwidget", 15);
		versionMap.put("com.gau.go.launcherex.gowidget.smswidget", 8);
		versionMap.put("com.gau.go.launcherex.gowidget.taskmanager", 4);
		versionMap.put("com.gau.go.launcherex.gowidget.contactwidget", 6);
		versionMap.put("com.gau.go.launcherex.gowidget.fbwidget", 3);
		versionMap.put("com.gau.go.launcherex.gowidget.searchwidget", 2);
	}

	public WidgetThemeChooseFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);

		mLayout = new RelativeLayout(activity);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		mLayout.setLayoutParams(rlp);
		mLayout.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// 判断当前是否锁屏
				if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
					LockScreenHandler.showLockScreenNotification(mActivity);
					return true;
				}

				return true;
			}
		});

		mWidgetDatas = new LinkedList<WidgetInfo>();
		mClickAction = ClickAction.ApplyWidgetTheme;
		mContext = activity;
		mRecycleView = new RecycleView();
		mInflater = LayoutInflater.from(activity);
		isParseOnce = false;

		creatView();
		initHandler();
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	private void initHandler() {
		if (mHandler != null) {
			return;
		}
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				int themeSize = 0;
				int position = msg.arg1;

				WidgetInfo widgetInfo = null;
				synchronized (mWidgetDatas) {
					themeSize = mWidgetDatas.size();
					// 当position为themeSize时，代表对“更多主题”界面进行操作
					if (position < 0 || position > themeSize) {
						return;
					}

					if (position != themeSize) {
						widgetInfo = mWidgetDatas.get(position);
						if (null == widgetInfo) {
							return;
						}
					}
				}

				switch (msg.what) {
					case EVENT_THEME_PARSE_OK : {
						// 解析成功，则设置当前主题。
						int index = mChooseView.mShowView.indexOf(position);
						if (index == -1) {
							return;
						}

						View view = mChooseView.getChildAt(index);
						ViewHolder viewHolder = (ViewHolder) view.getTag();
						if (VIEW_TYPE_MORESKIN == viewHolder.viewType) {
							viewHolder.downloadTxt.setText(null);
							viewHolder.downloadInfo.setText(null);
							// ((FrameLayout)view).removeView(viewHolder.downloadTxt);
							// ((FrameLayout)view).removeView(viewHolder.downloadInfo);

							viewHolder.imagePreview.setClickable(false);
							viewHolder.imagePreview.setFocusable(false);

							mToDeskButton.setText(mContext.getString(R.string.apply_widget_theme));
							mClickAction = ClickAction.ApplyWidgetTheme;
						}

						if (null != widgetInfo) {
							if (widgetInfo.title != null) {
								viewHolder.name.setText(widgetInfo.title);
							} else {
								viewHolder.name.setText("");
							}

							if (widgetInfo.resouces != null && widgetInfo.resouceId != 0) {
								final Drawable drawable = widgetInfo.resouces
										.getDrawable(widgetInfo.resouceId);
								viewHolder.imagePreview.setImageDrawable(drawable);
							}
						}
					}
						break;

					case EVENT_ADD_REMOVE_SKIN : {
						mChooseView.resetView(themeSize + 1);
						if (themeSize > 0) {
							refreshIndicator();
						} else {
							mProgressBar.setIsShow(false);
						}
					}
						break;

					/*
					 * case EVENT_ADD_DOWNLOAD_VIEW: { if (position !=
					 * themeSize) return;
					 * 
					 * int index = mChooseView.mShowView.indexOf(position); if
					 * (index == -1) return;
					 * 
					 * removeView(position);
					 * mChooseView.mShowView.add(position); addMoreThemeView();
					 * 
					 * mToDeskButton.setText(mContext.getString(R.string.
					 * widget_theme_download)); mClickAction =
					 * ClickAction.DownloadTheme; } break;
					 */
					default :
						break;
				}
			};
		};
	}

	@Override
	public void onAdd() {
		super.onAdd();
		// 注册按键事件
		mFrameManager.registKey(this);
	}

	@Override
	public void onRemove() {
		super.onRemove();
		// 反注册按键事件
		mFrameManager.unRegistKey(this);
		removeAndLeave();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		switch (msgId) {
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED : {
				Log.i("DiyThemeScanFrame", "orientaiton is " + param);
				creatView();
				parseData();

				break;
			}

			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : {
				// 移除自己
				GoLauncher.postMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
				break;
			}
			case IDiyMsgIds.WIDGETCHOOSE_SKIN : {
				if (object != null && object instanceof GoWidgetBaseInfo) {
					// TODO 应用主题
					mWidgetInfo = (GoWidgetBaseInfo) object;
					if (!isParseOnce) {
						parseData();
						isParseOnce = true;
					}

				}
				break;
			}
			default :
				break;
		}
		return false;
	}

	@Override
	public View getContentView() {
		// return mContenView;
		return mLayout;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			// 移除自己
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					getId(), null, null);

			return true;
		}

		// key事件交给view处理
		if (mContenView != null && mContenView.onKeyUp(keyCode, event)) {
			return true;
		}

		return true;
	}

	private void initView() {
		MImage lightImg = new MImage(mContenView.getResources(), R.drawable.lightbar);
		MImage normalImg = new MImage(mContenView.getResources(), R.drawable.normalbar);

		// mProgressBar.setTotalNum(mChooseView.getCount());
		mProgressBar.setImage(normalImg, lightImg);

	}

	private void refreshIndicator() {
		int themeSize = 0;
		synchronized (mWidgetDatas) {
			themeSize = mWidgetDatas.size();
		}
		mProgressBar.setTotalNum(themeSize + 1);
	}

	private void removeAndLeave() {
		// 作清理工作
		recycle();
		// 要求回收一下
		OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
	}

	/**
	 * 回收资源
	 */
	protected void recycle() {
		// 取消解析线程
		if (mParseTask != null && mParseTask.getStatus() == Status.RUNNING) {
			mParseTask.cancel(true);
		}

		if (mProgressBar != null) {
			mProgressBar.recycle();
		}

		if (mRecycleView != null) {
			mRecycleView.clear();
		}

		if (mWidgetDatas != null) {
			synchronized (mWidgetDatas) {
				mWidgetDatas.clear();
			}
		}
	}

	private static class WidgetInfo {
		// public String layoutID;
		public Resources resouces;
		public int resouceId;
		public String title;
		// public int row;
		// public int col;
		// public int type;

		public String packageName;
		public PreviewSpecficThemeBean themeBean;
		public int themeId;

		WidgetInfo() {
			// layoutID = "";
			resouces = null;
			resouceId = -1;
			title = "";
			// row = 0;
			// col = 0;
			// type = -1;
			themeId = -1;
		}
	}

	@Override
	public void onClick(View v) {
		if (mAddToDesktop == v && mClickAction == ClickAction.ApplyWidgetTheme) {
			int gowidgetId = mWidgetInfo.mWidgetId;
			int currentIndex = mChooseView.getCurrentScreen();

			int themeSize = 0;
			WidgetInfo widgetInfo = null;
			synchronized (mWidgetDatas) {
				themeSize = mWidgetDatas.size();
				if (themeSize == 0) {
					// Toast ret = Toast.makeText(mContext,
					// R.string.themeNotFound, Toast.LENGTH_SHORT);
					// ret.show();
					return;
				}

				// 越界保护
				if (currentIndex > (themeSize - 1)) {
					currentIndex = themeSize - 1;
				} else if (currentIndex < 0) {
					currentIndex = 0;
				}

				widgetInfo = mWidgetDatas.get(currentIndex);
				if (null == widgetInfo) {
					return;
				}
			}

			Bundle bundle = new Bundle();
			bundle.putInt(GoWidgetConstant.GOWIDGET_ID, gowidgetId);
			bundle.putString(GoWidgetConstant.GOWIDGET_THEME, widgetInfo.packageName);
			// bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, widgetInfo.type);
			bundle.putInt(GoWidgetConstant.GOWIDGET_THEMEID, widgetInfo.themeId);

			if (widgetInfo.themeId < 0) {
				// ADT-2561 (非必现)widget长按切换皮肤时候,皮肤预览界面预览图显示空白
				Toast.makeText(mActivity, R.string.widget_theme_data_parse_error,
						Toast.LENGTH_SHORT).show();
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.WIDGET_THEME_CHOOSE, null, null);
				return;
			}

			// 发送到桌面添加widget
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.APPLY_GO_WIDGET_THEME, gowidgetId, bundle, null);

			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					getId(), null, null);

			bundle = null;
			return;
		}

		// ImageView view = mChooseView.getDownloadView();

		if (mAddToDesktop == v || (mImgDownloadView != null && v.equals(mImgDownloadView))) {
			if (mClickAction == ClickAction.UpgradeWidget) {
				// widget包名
				final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
				final String widgetPackName = widgetManager.getWidgetPackage(mWidgetInfo);
				if (widgetPackName != null) {
					String uriString = LauncherEnv.Market.APP_DETAIL + widgetPackName;
					gotoMarketForAPK(uriString);
				}
			} else if (mClickAction == ClickAction.DownloadTheme) {
				// 更多主题
				final String uriString = LauncherEnv.Market.BY_KEYWORD + KEYWORD;
				AppUtils.gotoBrowserIfFailtoMarket(mContext, uriString,
						LauncherEnv.Url.GOWIDGET_THEME_DOWNLOAD_URL);

				// 退出自己
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
			}
		}
	}

	private void gotoMarketForAPK(String uriString) {
		if (mContext != null) {
			// TODO:url
			String path = GoWidgetAdapter.getDownloadUrl(mContext, mTitle.getText());
			try {
				// 如果是中国用户
				if (Machine.isCnUser()) {
					String title = mContext.getString(R.string.downDialog_title);
					String linkArray[] = { uriString, path };
					showTip((Activity) mContext, title, linkArray);
					linkArray = null;
				} else {
					if (!AppUtils.gotoMarket(mContext, uriString)) {
						// 如果跳转市场不成功
						gotoFtp(path);
					}
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
				}
			} catch (ActivityNotFoundException e) {

				gotoFtp(path);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);

			} catch (Exception e) {
				e.printStackTrace();
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
			}
		}
	}

	public void showTip(final Activity activity, final String title, final String[] linkArray) {

		if (mDeskAlertDlgCanShow) {
			String content = null;// 对话框内容
			String positiveBtnText = null;// 左边按钮文字
			String negativeBtnText = null;// 右边按钮文字
			// 是否为中国用户
			final boolean isCN = Machine.isCnUser(activity);
			if (isCN) {
				content = activity.getString(R.string.downDialog_downForWhere);
				positiveBtnText = activity.getString(R.string.downDialog_downForMarket);
				negativeBtnText = activity.getString(R.string.downDialog_downForGoLauncher);
			} else {
				content = activity.getString(R.string.downDialog_downForWhere);
				positiveBtnText = activity.getString(R.string.ok);
				negativeBtnText = activity.getString(R.string.cancle);
			}

			final DeskAlertDialog deskAlertDialog = new DeskAlertDialog(activity);
			deskAlertDialog.setTitle(title);
			deskAlertDialog.setMessage(content);
			deskAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveBtnText,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (null == linkArray) {
								return;
							}

							// 如果有電子市場
							if (AppUtils.isMarketExist(mActivity)) {
								AppUtils.gotoMarket(mActivity, linkArray[0]);
							} else {
								AppUtils.gotoBrowser(mActivity, linkArray[1]);
							}

							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
						}
					});
			deskAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeBtnText,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							if (isCN) {
								AppUtils.gotoBrowser(mActivity, linkArray[1]);
							}
							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
						}
					});
			deskAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					mDeskAlertDlgCanShow = true;
					deskAlertDialog.selfDestruct();
				}
			});

			mDeskAlertDlgCanShow = false;
			deskAlertDialog.show();
		}

	}

	private void gotoFtp(String uriString) {
		if (mContext != null) {
			try {
				Uri browserUri = Uri.parse(uriString);
				if (null != browserUri) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserUri);
					browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					AppUtils.safeStartActivity(mContext, browserIntent);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void creatView() {
		mLayout.removeAllViews();
		mContenView = (LinearLayout) mInflater.inflate(R.layout.widgetchoosestyle, null);
		mProgressBar = (DotProgressBar) mContenView.findViewById(R.id.indicate);

		mChooseView = (WidgetStyleChooseView) mContenView.findViewById(R.id.choosecontentview);
		mChooseView.setIndicator(mProgressBar);
		mChooseView.setWidgetStyleChooseFrame(this);

		mTitle = (DeskTextView) mContenView.findViewById(R.id.widgettitle);

		mAddToDesktop = (LinearLayout) mContenView.findViewById(R.id.widgetchoosebutton);
		mToDeskButton = (DeskTextView) mContenView.findViewById(R.id.widgetchoosedesktoptext);

		mToDeskButton.setText(R.string.apply_widget_theme);

		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		mLayout.addView(mContenView, rlp);

		if (mAddToDesktop != null) {
			mAddToDesktop.setOnClickListener(this);
		}

		initView();
	}

	private boolean packageSupportTheme(String packageName) {
		try {
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(packageName, 0);
			if (null == packageInfo || !versionMap.containsKey(packageName)) {
				return true;
			}

			if (packageInfo.versionCode < versionMap.get(packageName)) {
				return false;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return true;
	}

	private void parseData() {
		if (mWidgetInfo == null || mWidgetInfo.mPackage == null) {
			return;
		}

		mWidgetDatas.clear();
		// 主题包xml文件的名字
		String themeFileName = null;
		try {
			int typeId = mWidgetInfo.mType;

			// 独立apk形势的widget
			String packageName = null;
			final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
			if (mWidgetInfo.mPrototype == GoWidgetBaseInfo.PROTOTYPE_NORMAL) {
				packageName = mWidgetInfo.mPackage;
				themeFileName = doParseData(packageName, typeId);
			} else // 内置的widget
			{
				InnerWidgetInfo innerWidgetInfo = widgetManager
						.getInnerWidgetInfo(mWidgetInfo.mPrototype);
				if (innerWidgetInfo != null) {
					if (innerWidgetInfo.mBuildin == InnerWidgetInfo.BUILDIN_CODE_ONLY) {
						packageName = innerWidgetInfo.mWidgetPkg;
						themeFileName = doParseData(packageName, typeId);
					} else if (innerWidgetInfo.mBuildin == InnerWidgetInfo.BUILDIN_ALL) {
						packageName = innerWidgetInfo.mWidgetPkg;
						Resources resources = mContext.getResources();
						// 根据typeid获取当前widget的style(?*?)，取的是widget程序包
						// 获取行数
						int[] rowLists = resources.getIntArray(innerWidgetInfo.mRowList);
						int[] colLists = resources.getIntArray(innerWidgetInfo.mColumnList);
						int[] styleTypeList = resources.getIntArray(innerWidgetInfo.mTypeList);

						int typePosition = -1;
						for (int i = 0; i < styleTypeList.length; i++) {
							if (typeId == styleTypeList[i]) {
								typePosition = i;
							}
						}

						// 没找到对应的typeId;typePosition是指当前type在widget包里的位置
						if (-1 == typePosition) {
							return;
						}

						mCol = colLists[typePosition];
						mRow = rowLists[typePosition];

						// GOSTORE widget 标题需要根据渠道号显示
						if (mWidgetInfo.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE) {
							innerWidgetInfo.mTitle = GoStoreChannelControl
									.getChannelCheckName(mContext);
						}

						String lastTitle = String.format(innerWidgetInfo.mTitle + "(%dx%d)", mCol,
								mRow);
						mTitle.setText(lastTitle);
						themeFileName = innerWidgetInfo.mThemeConfig;
					}
				}
			}

			if (typeId == -1 || themeFileName == null) {
				return;
			}

			// 如果widget版本还不支持主题，提示更新
			if (!packageSupportTheme(packageName)) {
				WidgetInfo item = new WidgetInfo();
				item.resouceId = R.drawable.widget_upgrade;
				item.resouces = mChooseView.getResources();
				item.title = mContext.getString(R.string.widget_choose_defaulstyle);

				mWidgetDatas.add(item);
				mToDeskButton.setText(mContext.getString(R.string.widget_theme_upgradenow));
				// mNeedToUpgrade = true;
				mClickAction = ClickAction.UpgradeWidget;

				mProgressBar.setIsShow(false);
				// mChooseView基本设置
				mChooseView.removeAllViews();
				int count = mWidgetDatas.size();
				mChooseView.resetScroll(count);
				mChooseView.mShowView.add(0);
				addUpgradeView();
			} else {// 扫出所有当前style的主题
				String styleString = String.valueOf(typeId);
				if (null == styleString) {
					Log.e("WidgetThemeChooseFrame", "styleString is null");
					return;
				}

				SetWidgetItemInfo(packageName, styleString, themeFileName);
				// 异步解析主题信息
				if (mParseTask != null && mParseTask.getStatus() == Status.RUNNING) {
					mParseTask.cancel(true);
				}
				mParseTask = new ThemeParseTask();
				mParseTask.execute((Void) null);

				// ///////////////////////////////////////
				int themeSize = 0;
				synchronized (mWidgetDatas) {
					themeSize = mWidgetDatas.size();
				}

				// 有一个"更多主题"界面
				if (themeSize == 0) {
					mProgressBar.setIsShow(false);
				}
				refreshData();
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();

			// 退出widget选择界面
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					getId(), null, null);
		}

		refreshIndicator();
	}

	private String doParseData(String packageName, int typeId) throws NameNotFoundException {
		Resources resources = mContext.getPackageManager().getResourcesForApplication(packageName);

		// 根据typeid获取当前widget的style(?*?)，取的是widget程序包
		// 获取行数
		int[] rowLists = null;
		int[] colLists = null;
		int[] styleTypeList = null;

		int rowList = resources.getIdentifier(GoWidgetConstant.ROW_LIST, "array", packageName);
		if (rowList > 0) {
			rowLists = resources.getIntArray(rowList);
		} else {
			return null;
		}

		// 获取列数
		int colList = resources.getIdentifier(GoWidgetConstant.COL_LIST, "array", packageName);
		if (colList > 0) {
			colLists = resources.getIntArray(colList);
		}

		// 获取当前typeId对应widget的行列数
		// 如果typeId不存在，<0，则返回
		int styleTypeId = resources.getIdentifier(GoWidgetConstant.TYPE_LIST, "array", packageName);
		if (styleTypeId > 0) {
			styleTypeList = resources.getIntArray(styleTypeId);
		} else {
			return null;
		}

		int typePosition = -1;
		for (int i = 0; i < styleTypeList.length; i++) {
			if (typeId == styleTypeList[i]) {
				typePosition = i;
			}
		}

		// 没找到对应的typeId;typePosition是指当前type在widget包里的位置
		if (-1 == typePosition) {
			return null;
		}

		mCol = colLists[typePosition];
		mRow = rowLists[typePosition];
		// 获取widget主题选择界面的title,取的是widget程序包
		int resId = resources.getIdentifier(GoWidgetConstant.WIDGET_TITLE, "string", packageName);
		String title = resources.getString(resId);
		String lastTitle = String.format(title + "(%dx%d)", mCol, mRow);
		mTitle.setText(lastTitle);

		String widgetName = "";
		final int prefixLength = WIDGET_PACKAGE_PREFIX.length();
		if (packageName.length() > prefixLength) {
			widgetName = packageName.substring(prefixLength);
		}
		if (packageName.equals(LauncherEnv.GOSMS_PACKAGE)) {
			widgetName = "gosms";
		}
		return "widget_" + widgetName + ".xml";
	}

	// 增加一个“更多主题”界面
	private void addMoreThemeView() {
		FrameLayout viewFrame = (FrameLayout) mInflater.inflate(R.layout.widgetchoosesubview, null);

		mImgDownloadView = (ImageView) viewFrame.findViewById(R.id.widgetsubviewimage);
		if (mImgDownloadView != null) {
			mImgDownloadView.setImageDrawable(mChooseView.getResources().getDrawable(
					R.drawable.widget_themes));
			mImgDownloadView.setClickable(true);
			mImgDownloadView.setFocusable(true);
			mImgDownloadView.setOnClickListener(this);
		}

		DeskTextView texView = (DeskTextView) viewFrame.findViewById(R.id.widgetstyletitle);
		texView.setText(mContext.getString(R.string.widget_theme_moretheme));

		ImageView downloadMarkView = (ImageView) viewFrame.findViewById(R.id.down_mark_view);
		Drawable downloadMark = mChooseView.getResources().getDrawable(R.drawable.download_mark);
		downloadMarkView.setImageDrawable(downloadMark);
		DeskTextView downLoad = (DeskTextView) viewFrame.findViewById(R.id.download);
		downLoad.setText(R.string.widget_theme_download);

		DeskTextView downLoadInfo = (DeskTextView) viewFrame.findViewById(R.id.downlaodinfo);
		// downLoadInfo.setText(R.string.widget_theme_downinfo);

		GridView gridview = (GridView) viewFrame.findViewById(R.id.widgetgridview);

		// 这个view的内容有可能改变
		ViewHolder holder = new ViewHolder();

		holder.viewType = VIEW_TYPE_MORESKIN;

		holder.imagePreview = mImgDownloadView;
		holder.name = texView;
		holder.gridView = gridview;

		holder.downloadTxt = downLoad;
		holder.downloadInfo = downLoadInfo;

		viewFrame.setTag(holder);

		// 显示几×几的格子
		ImageAdapter adpter = new ImageAdapter(mContext);
		adpter.changeResouce(mRow, mCol);
		gridview.setAdapter(adpter);

		mChooseView.addView(viewFrame);
	}

	// widget版本太低，增加升级view
	private void addUpgradeView() {

		WidgetInfo info = mWidgetDatas.get(0);

		FrameLayout viewFrame = (FrameLayout) mInflater.inflate(R.layout.widgetchoosesubview, null);

		mImgDownloadView = (ImageView) viewFrame.findViewById(R.id.widgetsubviewimage);
		if (mImgDownloadView != null) {
			mImgDownloadView.setImageDrawable(info.resouces.getDrawable(info.resouceId));
			mImgDownloadView.setClickable(true);
			mImgDownloadView.setFocusable(true);

			mImgDownloadView.setOnClickListener(this);
		}

		DeskTextView texView = (DeskTextView) viewFrame.findViewById(R.id.widgetstyletitle);
		texView.setText(info.title);

		DeskTextView downLoad = (DeskTextView) viewFrame.findViewById(R.id.download);
		downLoad.setText(R.string.widget_theme_upgradenow);

		DeskTextView downLoadInfo = (DeskTextView) viewFrame.findViewById(R.id.downlaodinfo);
		downLoadInfo.setText(R.string.widget_theme_update);

		GridView gridview = (GridView) viewFrame.findViewById(R.id.widgetgridview);

		ImageAdapter adpter = new ImageAdapter(mContext);
		adpter.changeResouce(mRow, mCol);
		gridview.setAdapter(adpter);

		mChooseView.addView(viewFrame);
	}

	/**
	 * 通过主题包名和style扫出所有匹配的
	 * 
	 * @param packageName
	 * @param styleString
	 * @param col
	 * @param row
	 */
	private void SetWidgetItemInfo(String packageName, String styleString, String themeFileName) {
		// 扫描所有主题包
		// 桌面主题包
		// Intent intent=new Intent(ThemeManager.MAIN_THEME_PACKAGE);
		// intent.addCategory(THEME_CATEGORY);
		PackageManager pm = mContext.getPackageManager();
		// List<ResolveInfo> themes=pm.queryIntentActivities(intent, 0);

		// widget主题包
		Intent intent = new Intent(ICustomAction.ACTION_WIDGET_THEME_PACKAGE);
		intent.addCategory(THEME_CATEGORY);
		List<ResolveInfo> themes = pm.queryIntentActivities(intent, 0);

		// themes.addAll(widgetThemes);

		ThemeBean themeBean = null;
		String appPackageName = null;
		String loadingThemeName = mContext.getString(R.string.loading);

		int widgetSize = 0;
		int size = themes.size();
		for (int i = 0; i <= size; i++) {
			if (i != size) {
				appPackageName = themes.get(i).activityInfo.packageName.toString();
			} else {
				// 加上widget默认的主题
				appPackageName = packageName;
			}

			// for gosms
			if (packageName.equals(LauncherEnv.GOSMS_PACKAGE)) {
				if (!appPackageName.equals(LauncherEnv.GOSMS_PACKAGE)) {
					themeFileName = "widget_smswidget.xml";
				} else {
					themeFileName = "widget_gosms.xml";
				}
			}

			// 查看包名的输出
			Log.v("System.out.print", "包名" + (i + 1) + ":" + appPackageName);

			if (appPackageName.equals(LauncherEnv.Plugin.RECOMMAND_GOTASKMANAGER_PACKAGE)) {
				themeFileName = "widget_taskmanagerex.xml";
			}

			InputStream inputStream = XmlParserFactory.createInputStream(mContext, appPackageName,
					themeFileName);

			// for 任务管理器EX add by chenguanyu 2012.6.29
			if (inputStream == null
					&& packageName.equals(LauncherEnv.Plugin.RECOMMAND_GOTASKMANAGER_PACKAGE)) {
				themeFileName = "widget_taskmanager.xml";
				inputStream = XmlParserFactory.createInputStream(mContext, appPackageName,
						themeFileName);
			}

			if (null == inputStream) {
				Log.i("WidgetThemeChooseFrame", "no file:" + themeFileName + " in package:"
						+ appPackageName);
				continue;
			}

			WidgetInfo widgetItem = new WidgetInfo();
			// widgetItem.col = col;
			// widgetItem.row = row;
			widgetItem.title = loadingThemeName;

			themeBean = new PreviewSpecficThemeBean();
			if (themeBean != null) {
				themeBean.setPackageName(appPackageName);

				((PreviewSpecficThemeBean) themeBean).setWidgetStyle(styleString);

				// 通过widgetStyle来解析对应的某一种widget样式的信息
				// ((PreviewSpecficThemeBean)themeBean).setThemePosition(themePosition.get(j));
				// 将主题themeBean与parser, xmlPullParser和stream绑定起来,因为parser,
				// xmlPullParser和stream后面解析还会用到
				// ((PreviewSpecficThemeBean)themeBean).setParser(parser);
				((PreviewSpecficThemeBean) themeBean).setInputStream(inputStream);
				// 将PreviewSpecficThemeBean的位置写进去
				// ((PreviewSpecficThemeBean)themeBean).setPosition(widgetSize);
				// 注册observer
				// ((PreviewSpecficThemeBean)themeBean).registerObserver(this);

				widgetItem.packageName = appPackageName;
				// widgetItem.type = styleType;
				widgetItem.themeBean = (PreviewSpecficThemeBean) themeBean;

				mWidgetDatas.add(widgetItem);

				widgetSize++;
			}
		}
	}

	/**
	 * 解析各个主题包中widget预览信息
	 * 
	 * @author penglong
	 * @param themePackage
	 *            主题包名 styleString 当前widget的style themeBean 需要填充的theme信息
	 */
	private int parserThemeInfo(WidgetInfo widgetItem, int position) {
		if (null == widgetItem || null == widgetItem.themeBean || null == widgetItem.packageName) {
			return position;
		}

		PreviewSpecficThemeBean themeBean = widgetItem.themeBean;
		String themePackage = widgetItem.packageName;

		InputStream inputStream = themeBean.getInputStream();

		// SystemClock.sleep(1000); //test

		// 扫描主题包内有几个主题
		XmlPullParser xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
		ParseSpecficWidgetTheme parser = new ParseSpecficWidgetTheme();
		parser.parseXml(xmlPullParser, themeBean);
		parser = null;

		// 关闭inputStream
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Resources resTheme = null;
		try {
			resTheme = mContext.getPackageManager().getResourcesForApplication(themePackage);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		if (null == resTheme) {
			return position;
		}

		ArrayList<String> widgetPreview = themeBean
				.getWidgetAttrib(ParseSpecficWidgetTheme.WIDGET_PREVIEW);
		ArrayList<String> widgetTitle = themeBean
				.getWidgetAttrib(ParseSpecficWidgetTheme.WIDGET_TITLE);
		ArrayList<String> widgetThemeType = themeBean
				.getWidgetAttrib(ParseSpecficWidgetTheme.WIDGET_THEME_TYPE);
		ArrayList<Integer> themePositionList = themeBean.getThemePositionList();

		WidgetInfo newWidgetInfo = null;
		if (null == themePositionList || 0 == themePositionList.size()) {
			synchronized (mWidgetDatas) {
				mWidgetDatas.remove(position);
			}
			sendMessageToView(EVENT_ADD_REMOVE_SKIN, position, null, null);
		} else {
			for (int i = 0; i < themePositionList.size(); i++) {
				if (0 == i) {
					newWidgetInfo = widgetItem;
				} else {
					newWidgetInfo = new WidgetInfo();
					newWidgetInfo.packageName = widgetItem.packageName;
				}

				try {
					newWidgetInfo.themeId = Integer.parseInt(widgetThemeType.get(i));
				} catch (Exception e) {
					newWidgetInfo.themeId = -1;
				}

				if (widgetTitle != null && i < widgetTitle.size()) {
					int resId = resTheme.getIdentifier(widgetTitle.get(i), "string", themePackage);
					if (resId != 0) {
						newWidgetInfo.title = resTheme.getString(resId);
					}
				}

				if (widgetPreview != null && i < widgetPreview.size()) {
					int res = resTheme
							.getIdentifier(widgetPreview.get(i), "drawable", themePackage);
					if (res != 0) {
						newWidgetInfo.resouceId = res;
						newWidgetInfo.resouces = resTheme;
					}
				}

				if (i > 0) {
					synchronized (mWidgetDatas) {
						mWidgetDatas.add(position, newWidgetInfo);
					}

					sendMessageToView(EVENT_ADD_REMOVE_SKIN, position, null, null);
				}

				sendMessageToView(EVENT_THEME_PARSE_OK, position, null, null);
				position++;
			}
		}

		return position;
	}

	@Override
	public void removeView(int position) {
		int themeSize = 0;
		synchronized (mWidgetDatas) {
			themeSize = mWidgetDatas.size();
		}

		if (position < 0 || mChooseView.mShowView.size() < 0) {
			return;
		}

		int index = mChooseView.mShowView.indexOf(position);

		if (index == -1) {
			return;
		}

		View view = mChooseView.getChildAt(index);

		if (view != null) {
			mChooseView.removeViewInLayout(view);
			mChooseView.mShowView.remove(index);

			// 最后一个是“更多主题”界面,不能recycle
			if (position != themeSize) {
				mRecycleView.addView(view);
			}
		}
	}

	public void refreshData() {
		// 样式总数
		int count = 0;
		synchronized (mWidgetDatas) {
			count = mWidgetDatas.size() + 1;
		}

		mChooseView.removeAllViews();
		mChooseView.resetScroll(count);
		mRecycleView.clear();
		if (count > 0) {
			addView(0);
		}
	}

	// widget theme存在，增加主题view
	@Override
	public void addView(int position) {
		int themeCount = 0;
		WidgetInfo widgetInfo = null;
		synchronized (mWidgetDatas) {
			themeCount = mWidgetDatas.size();
			if (position < 0 || position > themeCount) {
				return;
			}

			if (position != themeCount) {
				widgetInfo = mWidgetDatas.get(position);
				if (null == widgetInfo) {
					return;
				}
			}
		}

		mChooseView.mShowView.add(position);

		// 最后一个view是“更多主题”界面
		if (position == themeCount) {
			addMoreThemeView();
			return;
		}

		View view = mRecycleView.getView();
		ViewHolder viewHolder = (ViewHolder) view.getTag();
		if (widgetInfo.title != null) {
			viewHolder.name.setText(widgetInfo.title);
		} else {
			viewHolder.name.setText("");
		}

		if (widgetInfo.resouces != null && widgetInfo.resouceId != 0) {
			viewHolder.imagePreview.setImageDrawable(widgetInfo.resouces
					.getDrawable(widgetInfo.resouceId));
		}

		// 显示几×几的格子
		ImageAdapter adpter = new ImageAdapter(mContext);
		adpter.changeResouce(mRow, mCol);
		viewHolder.gridView.setAdapter(adpter);
		mChooseView.addView(view);
	}

	/*
	 * view 的缓冲器,用来产生或是回收view
	 */
	class RecycleView {
		private ArrayList<View> mScrapViews;

		public RecycleView() {
			mScrapViews = new ArrayList<View>();
		}

		public void addView(View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			holder.imagePreview.setImageBitmap(null);
			holder.name.setText("");
			this.mScrapViews.add(view);
		}

		public View getView() {
			int i = mScrapViews.size();
			int j;

			if (i > 0) {
				j = i - 1;
				// Log.i(TAG_Wiget, "return a exit view");
				return mScrapViews.remove(j);
			}

			View viewFrame;

			viewFrame = mInflater.inflate(R.layout.widgetchoosesubview, null, false);

			ViewHolder holder = new ViewHolder();

			holder.viewType = VIEW_TYPE_NORMSKIN;

			holder.imagePreview = (ImageView) viewFrame.findViewById(R.id.widgetsubviewimage);

			holder.name = (DeskTextView) viewFrame.findViewById(R.id.widgetstyletitle);

			holder.gridView = (GridView) viewFrame.findViewById(R.id.widgetgridview);

			viewFrame.setTag(holder);

			return viewFrame;
		}

		public void clear() {
			if (mScrapViews != null) {
				mScrapViews.clear();
			}
		}
	}

	/*
	 * viewholder 用来显示并传递数据的view
	 */
	private static class ViewHolder {
		/*
		 * view在列表的位置
		 */
		// int position;

		/*
		 * view的类型：皮肤预览的view, 下载更多皮肤的view
		 */
		int viewType;

		/*
		 * 预览图片
		 */
		ImageView imagePreview;

		/*
		 * 样式的名字
		 */
		DeskTextView name;

		/*
		 * 样式的正方形显示
		 */
		GridView gridView;

		/*
		 * 更多主题界面的下载文字
		 */
		DeskTextView downloadTxt;

		/*
		 * 更多主题界面的下载信息
		 */
		DeskTextView downloadInfo;
	}

	// 处理预览界面体现样式的正方形
	private static class ImageAdapter extends BaseAdapter {

		private Context mContext;
		static final int GREEN = R.drawable.widgetchoosestyle_green;
		static final int GRAY = R.drawable.widgetchoosestyle_gray;
		static final int COL = 4;

		private Integer[] mThumbIds = { GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY,
				GRAY, GRAY, GRAY, GRAY, GRAY, GRAY };

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public void changeResouce(int row, int col) {

			if (row > COL || col > COL) {

				return;
			}

			for (int i = 0; i < mThumbIds.length; i++) {
				if (i % COL < col) {

					if (i >= COL * row) {
						break;
					}
					mThumbIds[i] = GREEN;
				}
			}
		}

		@Override
		public int getCount() {
			return mThumbIds.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;

			if (convertView == null) {
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				// 设置ImageView宽高
				// imageView.setAdjustViewBounds(false);
				// imageView.setScaleType(ImageView.ScaleType.FIT_XY);

				// imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}
			imageView.setImageResource(mThumbIds[position]);
			return imageView;
		}
	}

	public void sendMessageToView(int msgId, int param, Object object, List objects) {
		Message msg = new Message();
		msg.what = msgId;
		msg.arg1 = param;
		if (mHandler != null) {
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public void updateCurrentView(int newScreen, int oldScreen) {
		int themeCount = 0;
		synchronized (mWidgetDatas) {
			themeCount = mWidgetDatas.size();
		}

		if (newScreen < 0 || newScreen > themeCount) {
			return;
		}

		if (newScreen == themeCount && mToDeskButton != null) {
			mToDeskButton.setText(mContext.getString(R.string.widget_theme_apply_download));
			mClickAction = ClickAction.DownloadTheme;
		}

		if (newScreen == themeCount - 1 && mToDeskButton != null) {
			mToDeskButton.setText(mContext.getString(R.string.apply_widget_theme));
			mClickAction = ClickAction.ApplyWidgetTheme;
		}
	}

	private class ThemeParseTask extends AsyncTask<Void, Void, Integer> {
		@SuppressWarnings("unchecked")
		@Override
		protected Integer doInBackground(Void... params) {
			synchronized (mWidgetDatas) {
				if (mWidgetDatas != null) {
					int position = 0;
					LinkedList<WidgetInfo> tempWidgetInfos = (LinkedList<WidgetInfo>) mWidgetDatas
							.clone();
					for (int i = 0; i < tempWidgetInfos.size(); i++) {
						WidgetInfo item = tempWidgetInfos.get(i);
						position = parserThemeInfo(item, position);
					}
					tempWidgetInfos.clear();
					return new Integer(position);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result != null) {
				int position = result.intValue();
				if (position != mWidgetDatas.size()) {
					return;
				}

				int index = mChooseView.mShowView.indexOf(position);
				if (index == -1) {
					return;
				}

				removeView(position);
				mChooseView.mShowView.add(position);
				addMoreThemeView();

				mToDeskButton.setText(mContext.getString(R.string.widget_theme_apply_download));
				mClickAction = ClickAction.DownloadTheme;
			}
		}
	}
}

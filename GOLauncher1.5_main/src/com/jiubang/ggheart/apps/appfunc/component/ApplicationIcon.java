package com.jiubang.ggheart.apps.appfunc.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.ConvertUtils;
import com.go.util.Utilities;
import com.go.util.graphics.DrawUtils;
import com.go.util.log.LogUnit;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.MImage;
import com.jiubang.core.mars.XAEngine;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XAnimator;
import com.jiubang.core.mars.XMElastic;
import com.jiubang.core.mars.XMotion;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.appfunc.timer.Scheduler;
import com.jiubang.ggheart.apps.appmanagement.component.AppsManageView;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingVisualIconTabView;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncExceptionHandler;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.OrientationInvoker;
import com.jiubang.ggheart.apps.desks.appfunc.XViewFrame;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager.AnimationInfo;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.MonitorSver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * “所有程序的图标
 * 
 * @author tanshu
 * 
 */
//CHECKSTYLE:OFF
public class ApplicationIcon extends BaseAppIcon {

	public static final int DELFINISH = 1;
	private static final String FOLDER_BG_PRIMARY_KEY = "application_icon_folder_bg_key";
	/**
	 * 归位动画
	 */
	private XMotion mbackMotion;
	/**
	 * 长按时的缩放比例因子
	 */
	private float mScaleFactor;
	/**
	 * 长按过程是否发生位置改变
	 */
	private boolean mNoPlaceChanges;
	/**
	 * 是否被放大
	 */
	public boolean mIsEnlarged;
	/**
	 * 是否准备生成文件夹
	 */
	private volatile boolean mIsFolderReady;
	protected static ConcurrentHashMap<String, ConcurrentHashMap<String, BitmapDrawable>> sFolderBgMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, BitmapDrawable>>();
	public static boolean sIsReloaded = false;
	/**
	 * 文件夹底图背景（成员引用）
	 */
	protected BitmapDrawable mFolderBg;
	protected BitmapDrawable mFolderBgTop;
	protected BitmapDrawable mFolderBgTopOpen;
	/**
	 * 是否触发卸载
	 */
	public static boolean sIsUninstall = false;
	/**
	 * 是否打开应用程序
	 */
	public static boolean sIsStartApp = false;
	/**
	 * 应用程序信息: 不可修改
	 */
	public FunItemInfo mInfo;
	/**
	 * 是否需要重绘：TODO 后期去掉，框架统一管理
	 */
	private volatile boolean mRePaint;
	/**
	 * 小图标列表
	 */
	protected ArrayList<FolderItem> mImageList;

	private float mDensity;
	private Canvas mCanvas;
	private Paint mPaint;
	/**
	 * 是否收到IconChange消息：用于文件夹缩略图的重绘
	 */
	private boolean mGetIconChange;
	/**
	 * 文件夹背景的图片
	 */
	private BitmapDrawable mFolderDrawable;
	/**
	 * 文件夹盖子的图片
	 */
	private BitmapDrawable mFolderTopDrawable;
	/**
	 * 是否有文件夹背景被画过
	 */
	private boolean mFolderBgHasDrawed;
	/**
	 * 文件夹打开时盖子的图片
	 */
	private BitmapDrawable mFolderOpenTopDrawable;
	private BroadCasterObserver mAppItemInfoListener;
	private BroadCasterObserver mFunFolderItemInfoListener;
	// private BitmapDrawable mEditFolderDrawable = null;
	// private BitmapDrawable mEditHighlightFolderDrawable = null;
//	private BitmapDrawable mDeletAppDrawable = null;
//	private BitmapDrawable mDeletHighlightAppDrawable = null;
	// ///////////////图标抓起的动画/////////////////////////////
	/**
	 * 图标抓起的动画
	 */
	private XALinear mDragUpMotion;
	/**
	 * 图标抓起的透明效果渐变因子
	 */
	private volatile int mDragUpMotionAlphaFactor;
	/**
	 * 图标抓起的的原透明度
	 */
	private int mDragSrcAlpha;

	// ////////////////////////////////////////////////////////

	// /////////////////////////用于重叠图标出现文件夹时的动画的成员和重叠到不重叠的成员////////////////////
	/**
	 * 重叠图标出现文件夹时的动画
	 */
	private XALinear mShowFolderMotion;
	/**
	 * 重叠到不重叠图标出现文件夹时的动画
	 */
	// private XALinear mCloesFolderMotion;
	/**
	 * 用于记录显示文件夹的缩放因子值
	 */
	private volatile int mShowFolderMotionScaleFactor;
	/**
	 * 用于记录绘图前原来画笔的透明值
	 */
	private int mFolderSrcAlpha;
	/**
	 * 用于记录绘图画笔的透明值
	 */
	private volatile int mShowFolderMotionAlphaFactor;
	/**
	 * 是否重叠图标出现文件夹时的动画完成
	 */
	private volatile boolean mIsShowFolderMotionFinish;
	/**
	 * 是否关闭文件夹的动画完成
	 */
	private volatile boolean mIsCloseFolderMotionFinish = true;

	// //////////////////////////////////////////////////////////////////////////////

	// ///////////////删除图标的动画/////////////////////////////
	/**
	 * 图标删除的动画
	 */
	private XALinear mDeleteMotion;

	/**
	 * 文件兲删除的动画
	 */
	// private XALinear mFolderDeleteMotion;

	/**
	 * 图标由小放大放大的动画
	 */
	// private XALinear mOpenMotion;

	/**
	 * 图标抓起的透明效果渐变因子
	 */
	private volatile float mDeleteMotionScaleFactor;

	// //////////////////////////////////////////////////////////////////////////////

	// ///////////////图标诞生的动画/////////////////////////////
	/**
	 * 图标删除的动画
	 */
	private XALinear mNewMotion;
	/**
	 * 图标抓起的透明效果渐变因子
	 */
	private volatile float mNewMotionScaleFactor = 1.0f;

	// //////////////////////////////////////////////////////////////////////////////

	// ///////////////图标缩小动画/////////////////////////////
	/**
	 * 图标缩小的动画
	 */
	private XALinear mShrinkMotion;
	/**
	 * 图标缩小时的动画变量
	 */
	protected volatile float mShrinkFactor;
	protected volatile float mShrinkFactorY;

	protected boolean mIsShrink;

	public boolean mIsInMid;

	private Handler mHandler;

	private static final String DUMMYTITLE = "Unknown App";

	// 内部消息
	private static final int MSG_SHOWTIPS = 0; // 弹出对话框

	private static final int MSG_CHANGE_TITLE = 1;

	private FunControler mFunControler = null;

	private DialogConfirm mDialog = null;

	private boolean mIsDragging;

	private static final int ANIMATION_TYPE_STRINK = 0;

	// ////////////////////////////////////////////////////////
	public ApplicationIcon(Activity activity, int tickCount, int x, int y, int width, int height,
			FunItemInfo info, BitmapDrawable appPic, BitmapDrawable editPic,
			BitmapDrawable editLightPic, String title, boolean isDrawText) {
		super(activity, tickCount, x, y, width, height, appPic, null, editPic, editLightPic, title,
				isDrawText);
		initHandler();
		registerHandler();

		mFunControler = AppFuncFrame.getFunControler();

		mInfo = info;
		setAppItemInfoListener();
		setFunFolderItemInfoListener();
		mDensity = activity.getResources().getDisplayMetrics().density;
		// ApplicationIcon并非初始化功能表时就被构造，因此需要在被构造时加载主题资源
		loadResource();
		if (FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
			mShowEditPic = true;
			readyData();
		} else {
			mShowEditPic = !((FunAppItemInfo) mInfo).isSysApp();
		}
		sIsUninstall = false;
		mIsFolderReady = false;
		mPaint = new Paint();
		mPaint.setFilterBitmap(true);
		mPaint.setAntiAlias(true);

		mIsShrink = false;
		mIsInMid = false;

	}

	protected void registerHandler() {
		// 注册加载资源事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
		// 注册主题改变事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.THEME_CHANGE,
				this);
		// 注册重新刷folder事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(
				AppFuncConstants.RELOAD_FOLDER_THEMES, this);
	}

	@Override
	public BaseAppIcon clone() {
		// ApplicationIcon newIcon = new ApplicationIcon(this.mActivity,
		// this.mTickCount, this.mX,
		// this.mY, this.mWidth, this.mHeight, this.mInfo,
		// new BitmapDrawable(this.mAppPic.getBitmap()),
		// new BitmapDrawable(this.mEditPic.getBitmap()),
		// new BitmapDrawable(this.mEditLightPic.getBitmap()),
		// this.mTitle, this.mIsDrawText);
		// if (mIsEnlarged) {
		// newIcon.setDragEffect();
		// }
		BaseAppIcon newIcon = null;
		try {
			newIcon = (BaseAppIcon) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return newIcon;
	}

	private void initHandler() {
		/**
		 * @edit by huangshaotao
		 * @date 2012-4-26 使用主线程的looper来初始化handler。因为在处理sd加载事件时是在子线程中进行的，
		 *       这时候如果需要创建applicationIcon对象，
		 *       而子线程又没有looper就会报java.lang.RuntimeException: Can't create
		 *       handler inside thread that has not called Looper.prepare() 异常
		 *       所以这里给handler指定用主线程的looper
		 */
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_SHOWTIPS : {
						showTips();
						break;
					}
					case MSG_CHANGE_TITLE : {
						if (mAppText != null) {
							mAppText.setNameTxt((String) msg.obj);
						}
						if (getAbsX() >= 0 && getAbsX() <= DrawUtils.sWidthPixels && getAbsY() >= 0
								&& getAbsY() <= DrawUtils.sHeightPixels) {
							mRePaint = true;
						} else {
							mRePaint = false;
						}
						break;
					}
					default :
						break;
				}
			}
		};
	}

	protected void setAppItemInfoListener() {
		if (mAppItemInfoListener == null) {
			LogUnit.d("ApplicationIcon new 196");
			mAppItemInfoListener = new BroadCasterObserver() {
				@Override
				public void onBCChange(int msgId, int param, Object object,
						@SuppressWarnings("rawtypes") List objects) {
					switch (msgId) {
						case FunAppItemInfo.RESETBEAN : {
							try {
								// 可能在MImage内部发生异常
								if (mAppPic == null) {
									LogUnit.d("ApplicationIcon new 206");
									mAppPic = new MImage(((AppItemInfo) object).mIcon);
								} else {
									mAppPic.setDrawable(((AppItemInfo) object).mIcon);
								}
								if (getAbsX() >= 0 && getAbsX() <= DrawUtils.sWidthPixels
										&& getAbsY() >= 0 && getAbsY() <= DrawUtils.sHeightPixels) {
									mRePaint = true;
								} else {
									mRePaint = false;
								}
							} catch (Exception e) {
								Log.d("XViewFrame", "Hoops!");
								e.printStackTrace();
							}
							break;
						}
						case FunAppItemInfo.INCONCHANGE : {
							try {
								// 可能在MImage内部发生异常
								if (mAppPic == null) {
									LogUnit.d("ApplicationIcon new 223");
									mAppPic = new MImage((BitmapDrawable) object);
									resetLayoutParams(mX, mY, mWidth, mHeight);
								} else {
									boolean isNeedLayout = false;
									BitmapDrawable temp = (BitmapDrawable) object;

									if (temp.getIntrinsicHeight() != mAppPic.getHeight()
											|| temp.getIntrinsicWidth() != mAppPic.getWidth()) {
										isNeedLayout = true;
									}
									mAppPic.setDrawable(temp);

									if (isNeedLayout) {
										resetLayoutParams(mX, mY, mWidth, mHeight);
									}
									temp = null;
								}
								if (getAbsX() >= 0 && getAbsX() <= DrawUtils.sWidthPixels
										&& getAbsY() >= 0 && getAbsY() <= DrawUtils.sHeightPixels) {
									mRePaint = true;
								} else {
									mRePaint = false;
								}
							} catch (Exception e) {
								Log.d("XViewFrame", "Hoops!");
								e.printStackTrace();
							}
							break;
						}
						case FunAppItemInfo.TITLECHANGED : {
							// if (mAppText != null) {
							// mAppText.setNameTxt(( String ) object);
							// }
							// if (getAbsX() >= 0 && getAbsX() <=
							// DrawUtils.sWidthPixels
							// && getAbsY() >= 0 && getAbsY() <=
							// DrawUtils.sHeightPixels) {
							// mRePaint = true;
							// }
							// // 有搜索页面的话所有图标的位置都往后面挪了一个屏幕，所以第一页图标取值范围是480-960
							// else if (getAbsX() >= DrawUtils.sWidthPixels
							// && getAbsX() <= (DrawUtils.sWidthPixels * 2) &&
							// getAbsY() >= 0
							// && getAbsY() <= DrawUtils.sHeightPixels) {
							// mRePaint = true;
							// } else {
							// mRePaint = false;
							// }
							if (mHandler != null) {
								Message msg = mHandler.obtainMessage();
								msg.what = MSG_CHANGE_TITLE;
								msg.obj = object;
								mHandler.sendMessage(msg);
							}
							break;
						}

						case FunAppItemInfo.UNREADCHANGED : {
							mUnreadCount = String.valueOf(param);
							mRePaint = true;
							break;
						}
						
						case FunAppItemInfo.IS_RECOMMEND_APP_CHANGE : {
							mRePaint = true; // 申请重画即可
							break;
						}
						default :
							break;
					}
				}
			};
		}
		if (mInfo.getType() == FunItemInfo.TYPE_APP) {
			mInfo.registerObserver(mAppItemInfoListener);
			// LogUnit.e("name :" + );
		}
	}

	protected void setFunFolderItemInfoListener() {
		if (mFunFolderItemInfoListener == null) {
			LogUnit.d("ApplicationIcon new 271");
			mFunFolderItemInfoListener = new BroadCasterObserver() {
				@Override
				public void onBCChange(int msgId, int param, Object object,
						@SuppressWarnings("rawtypes") List objects) {
					switch (msgId) {
						case FunFolderItemInfo.ADDITEM : {
							// 目前暂不支持
							break;
						}
						case FunFolderItemInfo.REMOVEITEM : {
							if ((object != null) && (object instanceof FunAppItemInfo)) {
								if ((mInfo != null) && (mInfo instanceof FunFolderItemInfo)) {
									if ((param >= 0) && (param < 4)) {
										// 重绘缩略图
										readyData();
									}
								}
							}
							break;
						}
						case FunFolderItemInfo.TITLECHANGED : {
							// if (mAppText != null) {
							// mAppText.setNameTxt(( String ) object);
							// }
							// if (getAbsX() >= 0 && getAbsX() <=
							// DrawUtils.sWidthPixels
							// && getAbsY() >= 0 && getAbsY() <=
							// DrawUtils.sHeightPixels) {
							// mRePaint = true;
							// } else {
							// mRePaint = false;
							// }
							if (mHandler != null) {
								Message msg = mHandler.obtainMessage();
								msg.what = MSG_CHANGE_TITLE;
								msg.obj = object;
								mHandler.sendMessage(msg);
							}
							break;
						}
						default :
							break;
					}
				};
			};
		}
		if (mInfo.getType() == FunItemInfo.TYPE_FOLDER) {
			mInfo.registerObserver(mFunFolderItemInfoListener);
		}
	}

	/**
	 * 处理Click事件
	 * 
	 * @return
	 */
	@Override
	protected boolean onClickEvent(Object event, int arg, Object object) {
		if (mInfo == null) {
			return false;
		}
		if (event != null) {
			// 屏幕触摸事件
			int offsetX = arg;
			int offsetY = (Integer) object;

			MotionEvent motionEvent = (MotionEvent) event;
			if (mEditMode == true) {
				// 系统应用不能卸载

				if (FunItemInfo.TYPE_APP == mInfo.getType()) {
					if (((FunAppItemInfo) mInfo).isSysApp()) {
						return false;
					}
				}

				if (isInEditPicComponent((int) motionEvent.getX() + offsetX,
						(int) motionEvent.getY() + offsetY)) {
					// 点击在编辑图标范围内, 若为程序图标
					mIsEditPress = false;
					if (FunItemInfo.TYPE_APP == mInfo.getType()) {
						// 卸载程序
						// try {
						if (mInfo instanceof FunAppItemInfo) {
							// 终止不必要的schedular task
							Scheduler.getInstance().terminateAll();
							sIsUninstall = true;
							if (mDeleteMotion != null) {
								mAnimManager.cancelAnimation(this, mDeleteMotion);
//								detachAnimator(mDeleteMotion);
								mDeleteMotion = null;
							}
							// 是否能在AppDataEngine中找到
							if (GOLauncherApp.getAppDataEngine().isAppExist(mInfo.getIntent())) {
								mDeleteMotionScaleFactor = 1.0f;
								mDeleteMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, 100, 0,
										0, 0, 10, 1, 1);
								// mDeleteMotion
								// .setAnimateListener(new IAnimateListener() {
								//
								// @Override
								// public void onStart(
								// XAnimator animator) {
								// }
								//
								// @Override
								// public void onProgress(
								// XAnimator animator,
								// int progress) {
								// }
								//
								// @Override
								// public void onFinish(
								// XAnimator animator) {
								// try {
								// final Uri pkgUri = ((FunAppItemInfo) mInfo)
								// .getAppItemInfo().mUri;
								// AppUtils.uninstallApp(
								// mActivity, pkgUri);
								// } catch (Exception e) {
								// Message message = mHandler
								// .obtainMessage();
								// message.what = MSG_SHOWTIPS;
								// mHandler.sendMessage(message);
								// }
								// }
								// });
								// attachAnimator(mDeleteMotion);

								AnimationInfo animInfo = new AnimationInfo(
										AnimationInfo.TYPE_SIMPLE, this, mDeleteMotion,
										new IAnimateListener() {

											@Override
											public void onStart(XAnimator animator) {
											}

											@Override
											public void onProgress(XAnimator animator, int progress) {
											}

											@Override
											public void onFinish(XAnimator animator) {
												try {
													final Uri pkgUri = ((FunAppItemInfo) mInfo)
															.getAppItemInfo().mUri;
													AppUtils.uninstallApp(mActivity, pkgUri);
													//用户行为统计。
													StatisticsData
															.countUserActionData(
																	StatisticsData.FUNC_ACTION_ID_APPLICATION,
																	StatisticsData.USER_ACTION_TWO,
																	IPreferencesIds.APP_FUNC_ACTION_DATA);
												} catch (Exception e) {
													if (mHandler != null) {
														Message message = mHandler.obtainMessage();
														message.what = MSG_SHOWTIPS;
														mHandler.sendMessage(message);
													}
												}
											}
										});
								mAnimManager.attachAnimation(animInfo, null);
							} else {
								// 应用程序信息不在AppDataEngine, 提示是否删除
								showTips();
							}

						}
						// }
						// catch (ActivityNotFoundException e) {
						// } finally {
						return true;
						// }
					} else if (FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
						// // 进入一个新的Activity编辑文件夹内容
						// AppFuncMainView.sOpenFuncSetting = true;
						// LogUnit.d("ApplicationIcon new 390");
						// Intent intent = new Intent(mActivity,
						// AppFuncModifyFolderActivity.class);
						// intent.putExtra(AppFuncConstants.FOLDER_ID,
						// ((FunFolderItemInfo) mInfo).getFolderId());
						// mActivity.startActivityForResult(intent, 0);
						// return true;
						// 直接删除文件夹
						mDialog = new DialogConfirm(mActivity);
						mDialog.show();
						mDialog.setTitle(R.string.dlg_deleteFolder);
						mDialog.setMessage(R.string.dlg_deleteFolderContent);
						mDialog.setPositiveButton(null, new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								if ((mInfo != null) && (mInfo instanceof FunFolderItemInfo)) {
									FunFolderItemInfo mdeleteInfo = (FunFolderItemInfo) mInfo;
									removeFolder(mdeleteInfo);
								}
							}
						});
						mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								mDialog = null;
							}
						});
						return true;
					}
				}
				// 如果没有点中删除按钮
				if (inIconRange((int) motionEvent.getX(), (int) motionEvent.getY(), offsetX,
						offsetY)) {
					if (mIsEditPress == true) {
						mIsEditPress = false;
						return true;
					} else if (FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
						// 打开文件夹
						AppFuncHandler.getInstance().showFolder((FunFolderItemInfo) mInfo);
						return true;
					}
				}

			} else {
				if (mShowUpdate) {
					// 如果有更新，且点在了更新按钮上面(目前跟删除按钮位置相同)
					if (isInEditPicComponent((int) motionEvent.getX() + offsetX,
							(int) motionEvent.getY() + offsetY)) {
						// 如果点击的是GOStore图标 并且数字为0时不作反应
						if (null != getIntent()
								&& null != getIntent().getAction()
								&& getIntent().getAction().equals(
										ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE)
								&& AppFuncFrame.getFunControler().getmBeancount() == 0) {
							return false;
						}
						AppsBean beans = AppFuncFrame.getFunControler().getCurrentAppUpdateBeans();
						// AppFuncFrame.getFunControler().clearAppUpdateInfo(getInfo());
						// 统计：默认值是保存点击GoStore图标更新标志进入
						AppManagementStatisticsUtil.getInstance().saveCurrentEnter(mContext,
								AppManagementStatisticsUtil.ENTRY_TYPE_GOSTORE_ICON);
						Bundle bd = new Bundle();
						if (!(null != getIntent() && null != getIntent().getAction() && getIntent()
								.getAction().equals(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE))) {
							// 如果点击的图标不是GOStore刚排序
							// 被点中的排第一个
							sortAppUpdateBeans(beans);
							bd.putSerializable(AppsBean.KEY, beans);
							// 统计：如果不是GoStore图标更新标志进入，更改入口标识
							AppManagementStatisticsUtil.getInstance().saveCurrentEnter(mContext,
									AppManagementStatisticsUtil.ENTRY_TYPE_APP_ICON);
						}
						// 点击应用和store图标的更新标识，进入更新列表，退出功能表——清除标识；没有点击更新标识，退出功能表——不清除标识。
						if (null != AppFuncFrame.getDataHandler()) {
							AppFuncFrame.getDataHandler().setmClickAppupdate(true);
						}
						// String bundleData =
						// GoStorePublicDefine.URI_SECHEMA_APPS_MANAGER;
						// Uri uri = Uri.parse("gostorewidget://" + bundleData);
						// Intent intent = new Intent(Intent.ACTION_MAIN, uri);
						// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						// //跳转到GO Store时把Widget的标识带上,用于GO精品入口统计
						// intent.putExtra(GoStorePublicDefine.APP_ID_KEY,GoStorePublicDefine.GO_STORE_WIDGET_ID);
						// intent.putExtras(bd);
						//
						// GoLauncher.sendMessage(this,
						// IDiyFrameIds.SCHEDULE_FRAME,
						// IDiyMsgIds.START_ACTIVITY, -1,
						// intent, null);
						GoStoreStatisticsUtil.setCurrentEntry(
								GoStoreStatisticsUtil.ENTRY_TYPE_APP_UPDATE, mContext);
						// ApplicationManager.showAppCenter(mContext,
						// MainViewGroup.ACCESS_FOR_UPDATE);
						// 要根据渠道配置信息，确定升级小图标点击后的动作
						// Add by wangzhuobin 2012.07.28
						ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
						if (channelConfig != null && channelConfig.isNeedAppCenter()) {
							// 有应用中心跳应用中心
							// update by zhoujun 应用中心图标上面的更新数字，跳转到应用中心的首页
							//							AppsManagementActivity.startAppCenter(mContext,
							//									MainViewGroup.ACCESS_FOR_UPDATE);
							//如果是点击应用中心上面的数字，传入口值ACCESS_FOR_APPCENTER_UPATE，否则传入口值ACCESS_FOR_UPDATE
							//add by xiedezhi 2012.11.14
							if(null != getIntent()
									&& null != getIntent().getAction()
									&& getIntent().getAction().equals(
											ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER)){
								AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
										AppRecommendedStatisticsUtil.ENTRY_TYPE_APPFUNC_UPDATE);
								AppsManagementActivity.startAppCenter(mContext,
										MainViewGroup.ACCESS_FOR_APPCENTER_UPATE, false);
							} else {
								AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
										AppRecommendedStatisticsUtil.ENTRY_TYPE_APP_ICON);
								AppsManagementActivity.startAppCenter(mContext,
										MainViewGroup.ACCESS_FOR_UPDATE, false);
							}
							//update by zhoujun 2012-10-08 end
						} else {
							// 没有的话跳应用管理模块
							AppManagementStatisticsUtil.getInstance().saveCurrentEnter(mContext,
									AppManagementStatisticsUtil.ENTRY_TYPE_APPFUNC_UPDATE);
							AppCore.getInstance()
									.getApplicationManager()
									.show(IDiyFrameIds.APPFUNC_FRAME,
											AppsManageView.APPS_UPDATE_VIEW_ID);
						}
						if (!isVirtualApp(getIntent())) {
							sIsStartApp = true;
						}
						return true;
					}
				}
				if (inIconRange((int) motionEvent.getX(), (int) motionEvent.getY(), offsetX,
						offsetY)) {
					// 点击在应用程序图标范围内
					if (FunItemInfo.TYPE_APP == mInfo.getType()) {
						// 移除文件夹
						AppFuncHandler.getInstance().removeFolder();
						// 终止剩余的schedular task
						Scheduler.getInstance().terminateAll();
						// 启动程序
						AppItemInfo itemInfo = ((FunAppItemInfo) mInfo).getAppItemInfo();
						// 假如是主题设置图标或者go精品图标则特别处理 go精品图标也返回到功能表
						if (itemInfo != null) {
							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IDiyMsgIds.START_ACTIVITY, -1, itemInfo.mIntent, null);
							if (getIntent().getAction().equals(
									ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE)) {
								StatisticsData.countStatData(mActivity,
										StatisticsData.ENTRY_KEY_APPFUNC);
//								GoStoreStatisticsUtil.setCurrentEntry(
//										GoStoreStatisticsUtil.ENTRY_TYPE_FUNTAB_ICON, mContext);
								AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext, 
										AppRecommendedStatisticsUtil.ENTRY_TYPE_APPFUNC_ICO_GOSTORE);
							}
							if (!isVirtualApp(itemInfo.mIntent)) {
								sIsStartApp = true;
							} else {
								sIsStartApp = false;
							}
						}
						return true;
					} else {
						// 打开文件夹
						if (AppDataEngine.getInstance(mContext).isLoadedCompletedData()) {
							AppFuncHandler.getInstance().showFolder((FunFolderItemInfo) mInfo);
						} else {
							DeskToast.makeText(mContext, R.string.app_fun_strat_loading,
									Toast.LENGTH_SHORT).show();
						}
						return true;
					}
				}
			}
		} else {
			// 回车键触发启动应用程序
			if (FunItemInfo.TYPE_APP == mInfo.getType()) {
				if (mEditMode == false) {
					// 移除文件夹
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.TABCOMPONENT,
							AppFuncConstants.REMOVEFOLDER, null);

					AppItemInfo itemInfo = ((FunAppItemInfo) mInfo).getAppItemInfo();
					if (itemInfo != null) {
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IDiyMsgIds.START_ACTIVITY, -1, itemInfo.mIntent, null);
						sIsStartApp = true;
					}
					return true;
				}
			} else {
				// 打开文件夹
				AppFuncHandler.getInstance().showFolder((FunFolderItemInfo) mInfo);
				return true;
			}
		}
		return false;
	}

	/**
	 * 处理Up&Down事件
	 */
	@Override
	protected boolean onUpDownEvent(Object event, int offsetX, int offsetY) {
		if (event != null) {
			MotionEvent motionEvent = (MotionEvent) event;
			if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
				// 无论是否在编辑模式，只要点击范围在图标范围内，图标就变成半透明效果
				if (inIconRange((int) motionEvent.getX(), (int) motionEvent.getY(), offsetX,
						offsetY)) {

					if (mEditMode == true) {
						if (isInEditPicComponent((int) motionEvent.getX() + offsetX,
								(int) motionEvent.getY() + offsetY)) {
							mIsEditPress = true;
							return true;
						}
					} else {
						setAlpha(128);
						mTouchDown = true;
						return true;
					}
				}
			} else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
				if (mTouchDown == true && mEditMode == false) {
					setAlpha(255);
					mTouchDown = false;
					return true;
				}
				mIsEditPress = false;
			}
		}
		return false;
	}

	@Override
	protected boolean onLongClickEvent(Object event, int offsetX, int offsetY) {
		// 放大图标和文字，并且产生半透明效果
		if (event != null) {
			MotionEvent motionEvent = (MotionEvent) event;

			if (mEditMode && mShowEditPic) {
				if (isInEditPicComponent((int) motionEvent.getX() + offsetX,
						(int) motionEvent.getY() + offsetY)) {
					mIsEditPress = true;
					return false;
				}
			}

			if (inIconRange((int) motionEvent.getX(), (int) motionEvent.getY(), offsetX, offsetY)) {
				mIsDragging = true;
				setAlpha(255);
				mScaleFactor = 1.2f * 85 * 0.01f;
				mAnimManager.cancelAnimation(this, mDragUpMotion);
//				detachAnimator(mDragUpMotion);
				mDragUpMotion = null;
				mDragUpMotionAlphaFactor = 100;
				mDragUpMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, 100, 85, 50, 100, 6, 1,
						1);
				AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this,
						mDragUpMotion, null);
				mAnimManager.attachAnimation(animInfo, this);
				mIsEnlarged = true;
				return true;
			}
		}
		return false;
	}

	/**
	 * 设置被抓起的放大和透明效果(取消透明效果)
	 */
	@Override
	public void setDragEffect() {
		setAlpha(128);
		mScaleFactor = 1.2f;
		mDragUpMotionAlphaFactor = 100;
		mIsEnlarged = true;
		mIsDragging = true;
	}

	/**
	 * 长按后的弹起事件
	 */
	@Override
	public void onLongClickUp(int x, int y, IAnimateListener listener,
			OrientationInvoker orientationInvoker) {
		// 如果没有发生位置偏移，则不产生移动动画
		// mIsFocused = true;
		mIsDragging = false;
		mIsEditPress = false;
		if ((mX != x) || (mY != y)) {
			if (null != mbackMotion) {
				mAnimManager.cancelAnimation(this, mbackMotion);
//				detachAnimator(mbackMotion);
				mbackMotion = null;
			}
			mNoPlaceChanges = false;
			LogUnit.d("ApplicationIcon new 557");
			mbackMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mX, mY, x, y, 6, 0, 0);
			// attachAnimator(mbackMotion);
			// mbackMotion.setAnimateListener(listener);
			AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this,
					mbackMotion, listener);
			mAnimManager.attachAnimation(animInfo, orientationInvoker);
		} else {
			setAlpha(255);
			mNoPlaceChanges = true;
			LogUnit.d("ApplicationIcon new 565");
			mbackMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mX, mY, mX + 2, mY + 2, 4, 0,
					0);
			// attachAnimator(mbackMotion);
			AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this,
					mbackMotion, null);
			mAnimManager.attachAnimation(animInfo, orientationInvoker);
		}
		mAnimManager.cancelAnimation(this, mDragUpMotion);
//		detachAnimator(mDragUpMotion);
		mDragUpMotion = null;
	}

	/**
	 * 清楚动画效果
	 */
	public void clearMotion() {
		if (null != mbackMotion) {
			setAlpha(255);
			mAnimManager.cancelAnimation(this, mbackMotion);
//			detachAnimator(mbackMotion);
			mbackMotion = null;
			mIsEnlarged = false;
			mIsDragging = false;
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		super.drawCurrentFrame(canvas);
		// mBgPaint.setColor(AppFuncConstants.ICON_BG_FOCUSED);
		// canvas.drawRoundRect(new RectF(0,0,mWidth,mHeight), 10, 10,
		// mBgPaint);
		// 先绘制背景图
		// if(mIsPressed){
		// //优先绘制按键背景
		// mBgPaint.setColor(AppFuncConstants.ICON_BG_PRESSED);
		// canvas.drawRoundRect(mBgRect, 10, 10, mBgPaint);
		// }
		// 绘制文件夹背景

		if (mIsShrink) {

			canvas.save();

			if (mShrinkFactor < 0 || mShrinkFactorY < 0) {
				mShrinkFactor = 0;
				mShrinkFactorY = 0;
			}

			if (mAppPic != null) {
				mAppPic.setScale(mShrinkFactor, mShrinkFactorY);
				mAppPic.draw(canvas, 0.0f, 0.0f);
			}

			canvas.restore();
			return;
		}
		if (mIsFolderReady) {
			if (FunItemInfo.TYPE_FOLDER == mInfo.getType() && mAppPic != null) {
				canvas.save();
				canvas.scale(0.013f * mShowFolderMotionScaleFactor,
						0.013f * mShowFolderMotionScaleFactor, mWidth / 2, mHeight / 2);
				mFolderSrcAlpha = mPaint.getAlpha();
				mPaint.setAlpha((int) (255 * mShowFolderMotionAlphaFactor * 0.01f));
				canvas.drawBitmap(mAppPic.getBitmap(), (mWidth - mAppPic.getWidth()) / 2,
						(mHeight - mAppPic.getHeight()) / 2, mPaint);
				if (mIsShowFolderMotionFinish && (mFolderOpenTopDrawable != null)
						&& (mFolderOpenTopDrawable.getBitmap().isRecycled() == false)) {
					canvas.drawBitmap(mFolderOpenTopDrawable.getBitmap(),
							(mWidth - mAppPic.getWidth()) / 2, (mHeight - mAppPic.getHeight()) / 2,
							mPaint);
				}
				mPaint.setAlpha(mFolderSrcAlpha);
				canvas.restore();
				mFolderBgHasDrawed = true;
				return;
			} else {
				if ((mFolderBg != null) && (mFolderBg.getBitmap().isRecycled() == false)) {
					int width = mFolderBg.getBitmap().getWidth();
					int height = mFolderBg.getBitmap().getHeight();
					canvas.save();
					canvas.scale(0.013f * mShowFolderMotionScaleFactor,
							0.013f * mShowFolderMotionScaleFactor, mWidth / 2, mHeight / 2);
					mFolderSrcAlpha = mPaint.getAlpha();
					mPaint.setAlpha((int) (255 * mShowFolderMotionAlphaFactor * 0.01f));
					// LogUnit.i("Alpha : " + mPaint.getAlpha());
					canvas.drawBitmap(mFolderBg.getBitmap(), (mWidth - width) / 2,
							(mHeight - height) / 2, mPaint);
					mPaint.setAlpha(mFolderSrcAlpha);
					canvas.restore();
				}
			}
			mFolderBgHasDrawed = true;
			// return;
		}
		// 长按产生放大和半透明效果
		if (mIsEnlarged) {
			canvas.save();
			canvas.scale(mScaleFactor, mScaleFactor, mWidth / 2, mHeight / 2);
			mDragSrcAlpha = getAlpha();
			if (!mIsInEdge) {
				setAlpha((int) (2.55f * mDragUpMotionAlphaFactor));
			}
		}

//		if (mIsFocused && mBgPaint != null) {
//			// 绘制聚焦时的背景
//			mBgPaint.setColor(sFocusedBgColor);
//			canvas.drawRoundRect(mBgRect, 5, 5, mBgPaint);
//		}
		if (mIsFolderReady && FunItemInfo.TYPE_APP == mInfo.getType()) {
			mFolderSrcAlpha = getAlpha();
			setAlpha(128);
			drawAllChildComponents(canvas);
			setAlpha(mFolderSrcAlpha);
		} else if (mDeleteMotion != null) {
			canvas.save();
			canvas.scale(mDeleteMotionScaleFactor, mDeleteMotionScaleFactor, mWidth / 2,
					mHeight / 2);
			drawAllChildComponents(canvas);
			canvas.restore();
		}
		// else if (mOpenMotion != null) {
		// canvas.save();
		// canvas.scale(mDeleteMotionScaleFactor, mDeleteMotionScaleFactor,
		// mWidth / 2, mHeight / 2);
		// drawAllChildComponents(canvas);
		// canvas.restore();
		// }
		// else if (mFolderDeleteMotion != null) {
		// canvas.save();
		// canvas.scale(mDeleteMotionScaleFactor, mDeleteMotionScaleFactor,
		// mWidth / 2, mHeight / 2);
		// drawAllChildComponents(canvas);
		// canvas.restore();
		// }
		else if (mNewMotion != null) {
			canvas.save();
			canvas.scale(mNewMotionScaleFactor, mNewMotionScaleFactor, mWidth / 2, mHeight / 2);
			drawAllChildComponents(canvas);
			canvas.restore();
		} else if (mAlphaMotion != null) {
			setAlpha(mAlphaMotionFactor);
			drawAllChildComponents(canvas);
		} else if (mScaleMotion != null) {
			canvas.save();
			canvas.scale(mScaleMotionHFactor, mScaleMotionVFactor, mWidth / 2, mHeight / 2);
			drawAllChildComponents(canvas);
			canvas.restore();
		} else {
			drawAllChildComponents(canvas);
		}

		if (mIsEnlarged) {
			setAlpha(mDragSrcAlpha);
			canvas.restore();
		}
		if (!mIsFolderReady) {
			mFolderBgHasDrawed = false;
		}
	}

	@Override
	protected boolean animate() {
		boolean isAnimate = false;
		if (mShrinkMotion != null) {
			if (mShrinkMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mShrinkMotion);
//				detachAnimator(mShrinkMotion);
				mShrinkMotion = null;
			} else {
				mShrinkFactor = mShrinkMotion.GetCurX();
				mShrinkFactorY = mShrinkMotion.GetCurY();
			}
			isAnimate = true;
		}

		if (null != mbackMotion) {
			if (mbackMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mbackMotion);
//				detachAnimator(mbackMotion);
				mbackMotion = null;
				mIsEnlarged = false;
				if (mIsDrawText) {
					setAppTextDrawingCacheEnable(true);
				}
				// setAlpha(255);
			} else {
				int currentX = mbackMotion.GetCurX();
				int currentY = mbackMotion.GetCurY();
				if (mNoPlaceChanges == false) {
					mX = mbackMotion.GetCurX();
					mY = mbackMotion.GetCurY();
				}
				int factor;
				if (mbackMotion.GetEndX() != mbackMotion.GetStartX()) {
					factor = Math.abs((currentX - mbackMotion.GetStartX())
							/ (mbackMotion.GetEndX() - mbackMotion.GetStartX()));
				} else {
					factor = Math.abs((currentY - mbackMotion.GetStartY())
							/ (mbackMotion.GetEndY() - mbackMotion.GetStartY()));
				}
				mScaleFactor = 1.2f - 0.2f * factor;
				int alpha = 128 + factor * 127;
				if (alpha > 255) {
					alpha = 255;
				}
				setAlpha(alpha);
			}
			isAnimate = true;
		}
		if (mDragUpMotion != null) {
			if (mDragUpMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mDragUpMotion);
//				detachAnimator(mDragUpMotion);
				mDragUpMotion = null;
			} else {
				// 3.25取消长按半透明动画
//				mDragUpMotionAlphaFactor = mDragUpMotion.GetCurX();
				mScaleFactor = 1.2f * mDragUpMotion.GetCurY() * 0.01f;
			}
			isAnimate = true;
		}
		if (mShowFolderMotion != null) {
			if (mShowFolderMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mShowFolderMotion);
//				detachAnimator(mShowFolderMotion);
				mShowFolderMotion = null;
			} else {
				mShowFolderMotionScaleFactor = mShowFolderMotion.GetCurX();
				mShowFolderMotionAlphaFactor = mShowFolderMotion.GetCurY();
			}
			isAnimate = true;
		}
		// if (mCloesFolderMotion != null) {
		// if (mCloesFolderMotion.isFinished()) {
		// detachAnimator(mCloesFolderMotion);
		// mCloesFolderMotion = null;
		// } else {
		// mShowFolderMotionScaleFactor = mCloesFolderMotion.GetCurX();
		// mShowFolderMotionAlphaFactor = mCloesFolderMotion.GetCurY();
		// }
		// isAnimate = true;
		// }
		if (mDeleteMotion != null) {
			if (mDeleteMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mDeleteMotion);
//				detachAnimator(mDeleteMotion);
				mDeleteMotion = null;
			} else {
				mDeleteMotionScaleFactor = mDeleteMotion.GetCurX() * 0.01f;
			}
			isAnimate = true;
		}
		// if (mOpenMotion != null) {
		// if (mOpenMotion.isFinished()) {
		// detachAnimator(mOpenMotion);
		// mOpenMotion = null;
		// } else {
		// mDeleteMotionScaleFactor = mOpenMotion.GetCurX() * 0.01f;
		// }
		// isAnimate = true;
		// }
		// if (mFolderDeleteMotion != null) {
		// if (mFolderDeleteMotion.isFinished()) {
		// detachAnimator(mFolderDeleteMotion);
		// mFolderDeleteMotion = null;
		// } else {
		// mDeleteMotionScaleFactor = mFolderDeleteMotion.GetCurX() * 0.01f;
		// }
		// isAnimate = true;
		// }
		if (mNewMotion != null) {
			if (mNewMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mNewMotion);
//				detachAnimator(mNewMotion);
				mNewMotion = null;
			} else {
				mNewMotionScaleFactor = mNewMotion.GetCurX() * 0.01f;
			}
			isAnimate = true;
		}
		if (mAlphaMotion != null) {
			mIsEnlarged = false;
			if (mAlphaMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mAlphaMotion);
//				detachAnimator(mAlphaMotion);
				mAlphaMotion = null;
			} else {
				mAlphaMotionFactor = mAlphaMotion.GetCurX();
			}
			isAnimate = true;
		}
		if (mScaleMotion != null) {
			if (mScaleMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mScaleMotion);
//				detachAnimator(mScaleMotion);
				mScaleMotion = null;
			} else {
				mScaleMotionHFactor = mScaleMotion.GetCurX() * 0.01f;
				mScaleMotionVFactor = mScaleMotion.GetCurY() * 0.01f;
			}
			isAnimate = true;
		}
		if (mMoveMotion != null) {
			if (mMoveMotion.isFinished()) {
				mAnimManager.cancelAnimation(this, mMoveMotion);
//				detachAnimator(mMoveMotion);
				mMoveMotion = null;
			} else {
				mX = mMoveMotion.GetCurX();
				mY = mMoveMotion.GetCurY();
			}
			isAnimate = true;
		}
		if (mRePaint) {
			mRePaint = false;
			isAnimate = true;
		}
		return isAnimate;
	}

	@Override
	public void setEditMode(boolean editModeEnabled) {
		if (mEditMode != editModeEnabled) {
			mEditMode = editModeEnabled;
			if (mIconImage != null) {
				if (mEditMode) {
					if (AppSettingDefault.APPFUNC_OPEN_EFFECT) {
						startMotion();
					}
				} else {
					if (mElastic != null) {
						mIconImage.detachAnimator(mElastic);
						mElastic = null;
					}
					mIconImage.exitEditMode();
				}
			}
		}
	}

	private void startMotion() {
		// mMotion = new XMElastic(1, XMElastic.XMElastic_ESimpleHarm,
		// (mWidth - mIconWidth)/2, mMargin_v + mIconToBgTop_v,
		// mMargin_h, 0, 30, 15, 1.0f);
		// mMotion.SetRepeatMode(true);
		// mIconImage.setMotionFilter(mMotion);
		LogUnit.d("ApplicationIcon new 748");
		mElastic = new XMElastic(1, XMElastic.XMElastic_EBackForth, -4, 0, 4, 0, 24); // -3,0,3,0,6);//
		mElastic.SetRepeatMode(true);

		// 不同抖动频率时使用以下代码
//		mElastic.delay(XAEngine.ANIMATE_TYPE_FRAME, (long) (24 * Math.random()));

		mIconImage.setRotateAnimator(mElastic);
	}

	public void setAppInfo(FunItemInfo info) {
		mIsFolderReady = false;
		mFolderBgHasDrawed = false;
		if (mInfo != null) {
			if (mInfo.getType() == FunItemInfo.TYPE_APP) {
				if (mAppItemInfoListener != null) {
					mInfo.unRegisterObserver(mAppItemInfoListener);
				}
			} else if (mInfo.getType() == FunItemInfo.TYPE_FOLDER) {
				if (mFunFolderItemInfoListener != null) {
					mInfo.unRegisterObserver(mFunFolderItemInfoListener);
				}
			}
		}
		mInfo = info;
		if (mInfo != null) {
			if (mInfo.getType() == FunItemInfo.TYPE_APP) {
				setAppItemInfoListener();
			} else if (mInfo.getType() == FunItemInfo.TYPE_FOLDER) {
				setFunFolderItemInfoListener();
			}
			loadEditPicResource();
			readyData();
		}
	}

	/**
	 * 准备图片和数据
	 */
	@SuppressWarnings("unchecked")
	public synchronized void readyData() {
		if (FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
			mShowEditPic = true;
			mTitle = ((FunFolderItemInfo) mInfo).getTitle();
			// ///////////
			if (mImageList == null) {
				try {
					if (mFolderDrawable == null) {
						mFolderDrawable = mFolderBg;
					} else {
						if ((mFolderDrawable.getBitmap() != null)
								&& (mFolderDrawable.getBitmap().isRecycled())) {
							mFolderDrawable = mFolderBg;
						}
					}
					if ((mFolderDrawable == null) || (mFolderDrawable.getBitmap() == null)
							|| (mFolderDrawable.getBitmap().isRecycled())) {
						return;
					}
					Bitmap copy = mFolderDrawable.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
					if (copy == null) {
						return;
					}
					if (mAppPic == null) {
						LogUnit.d("ApplicationIcon new 817");
						mAppPic = new MImage(copy);
					} else {
						mAppPic.setBitmap(copy);
					}
					LogUnit.d("ApplicationIcon new 822");
					mImageList = new ArrayList<FolderItem>();
					for (int i = 0; i < AppFuncConstants.SHOW_ICON_SIZE; i++) {
						LogUnit.d("ApplicationIcon new 825");
						mImageList.add(new FolderItem(this));
					}
				} catch (OutOfMemoryError e) {
					if (mImageList != null) {
						for (FolderItem item : mImageList) {
							if (item.mBitmap != null) {
								item.mBitmap = null;
							}
							item.clearItemInfo();
						}
						mImageList.clear();
						mImageList = null;
						mCanvas = null;
						mFolderDrawable = null;
						mFolderTopDrawable = null;
						mFolderOpenTopDrawable = null;
					}
					e.printStackTrace();
					OutOfMemoryHandler.handle();
					return;
				}
			}
			ArrayList<FunAppItemInfo> list = null;
			if(((FunFolderItemInfo)mInfo).getFolderType() == FunFolderItemInfo.TYPE_NORMAL){
				list = (ArrayList<FunAppItemInfo>) ((FunFolderItemInfo) mInfo)
						.getFunAppItemInfos().clone();
			}else{
				list = new ArrayList<FunAppItemInfo>(1);
			}
			Iterator<FunAppItemInfo> iterator = list.iterator();
			// int iConsize = GoLauncher.isLargeIcon() ? (int) (20 * mDensity) :
			// (int) (17 * mDensity);
			int count = 0;
			while (iterator.hasNext() && count < AppFuncConstants.SHOW_ICON_SIZE) {
				FunAppItemInfo next = iterator.next();
				if (!next.isHide()) {
					BitmapDrawable icon;
					if (next.getAppItemInfo() == null) {
						// 使用系统默认图标
						icon = GOLauncherApp.getAppDataEngine().getSysBitmapDrawable();;
					} else {
						icon = next.getAppItemInfo().getIcon();
					}
					FolderItem showItem = mImageList.get(count);
					FunAppItemInfo showItemInfo = showItem.mItemInfo;
					if (!next.equals(showItemInfo) || mGetIconChange) {
						// 原来位置的图标发生改变
						FolderItem folderItem = showItem;
						if (!next.equals(showItemInfo)) {
							folderItem.clearItemInfo();
							folderItem.setItemInfo(next);
						}
						folderItem.mBitmap = null;
						folderItem.mBitmap = icon;
						// 注册后台数据以便图标发生改变时能收到消息
					} else {
						// 增加新图标
						BitmapDrawable mIcon = icon;
						FolderItem folderItem = showItem;
						folderItem.setItemInfo(next);
						folderItem.mBitmap = mIcon;
					}
					count++;
				}
			}
			mGetIconChange = false;
			if (count < AppFuncConstants.SHOW_ICON_SIZE) {
				for (int i = count; i < AppFuncConstants.SHOW_ICON_SIZE; i++) {
					FolderItem folderItem = mImageList.get(i);
					folderItem.clearItemInfo();
					if (folderItem.mBitmap != null) {
						folderItem.mBitmap = null;
					}
				}
			}
			if (mCanvas == null) {
				if (mAppPic != null && mAppPic.getBitmap() != null
						&& mAppPic.getBitmap().isMutable() && !mAppPic.getBitmap().isRecycled()) {
					mCanvas = mAppPic.getCanvas();
				} else {
					return;
				}
			}
			mCanvas.drawColor(0x00FFFFFF, Mode.CLEAR);
			if (mFolderDrawable == null) {
				mFolderDrawable = mFolderBg;
			} else {
				if ((mFolderDrawable.getBitmap() != null)
						&& (mFolderDrawable.getBitmap().isRecycled())) {
					mFolderDrawable = mFolderBg;
				}
			}
			if ((mFolderDrawable != null) && (mFolderDrawable.getBitmap().isRecycled() == false)) {
				mCanvas.drawBitmap(mFolderDrawable.getBitmap(), 0, 0, mPaint);
			}
			int width = mIntrinSize;
			int height = mIntrinSize;
			if (mAppPic != null) {
				width = mAppPic.getWidth();
				height = mAppPic.getHeight();
			}
			int colunm = 2;
			int curColunm = 0;
			int row = 0;
			for (FolderItem folderItem : mImageList) {
				if (folderItem.mItemInfo != null && folderItem.mBitmap != null) {
					Bitmap bitmap = folderItem.mBitmap.getBitmap();
					if (bitmap != null) {
						final float first = width * 0.12f;
						final float grap = width * 0.015f;
						final int innerIconSize = (int) (mIntrinSize * 0.365f);
						final float left = first + curColunm * (innerIconSize + grap * 2);
						final float top = first + row * (innerIconSize + grap * 2);
						final float scaleX = (float) innerIconSize
								/ (float) folderItem.mBitmap.getBitmap().getWidth();
						final float scaleY = (float) innerIconSize
								/ (float) folderItem.mBitmap.getBitmap().getHeight();

						folderItem.mMatrix.reset();
						folderItem.mMatrix.postScale(scaleX, scaleY);
						folderItem.mMatrix.postTranslate(left, top);
						mCanvas.drawBitmap(bitmap, folderItem.mMatrix, mPaint);
					}
					curColunm++;
					if (curColunm >= colunm) {
						curColunm = 0;
						row++;
					}
				}
			}
			if (mIsFolderReady) {
				if (mFolderOpenTopDrawable == null) {
					mFolderOpenTopDrawable = mFolderBgTopOpen;
				} else {
					if ((mFolderOpenTopDrawable.getBitmap() != null)
							&& (mFolderOpenTopDrawable.getBitmap().isRecycled())) {
						mFolderOpenTopDrawable = mFolderBgTopOpen;
					}
				}
				// if ((mFolderOpenTopDrawable != null)
				// && (mFolderOpenTopDrawable.getBitmap().isRecycled() ==
				// false)) {
				// mCanvas.drawBitmap(mFolderOpenTopDrawable.getBitmap(), 0,
				// 0, mPaint);
				// }
			} else {
				if (mFolderTopDrawable == null) {
					mFolderTopDrawable = mFolderBgTop;
				} else {
					if ((mFolderTopDrawable.getBitmap() != null)
							&& (mFolderTopDrawable.getBitmap().isRecycled())) {
						mFolderTopDrawable = mFolderBgTop;
					}
				}
				if ((mFolderTopDrawable != null) && (mFolderTopDrawable.getBitmap() != null)
						&& (!mFolderTopDrawable.getBitmap().isRecycled())) {
					XViewFrame viewFrame = XViewFrame.getInstance();
					if (viewFrame != null && !viewFrame.getAppFuncMainView().isFolderShow()) {
						mCanvas.drawBitmap(mFolderTopDrawable.getBitmap(), 0, 0, mPaint);
					}
				}
			}
			// ///////////
		} else {
			if (mImageList != null) {
				for (FolderItem item : mImageList) {
					if (item.mBitmap != null) {
						item.mBitmap = null;
					}
					item.clearItemInfo();
				}
				mImageList.clear();
				mImageList = null;
				mCanvas = null;
				if (mAppPic != null && mAppPic.getBitmap() != null) {
					mAppPic.getBitmap().recycle();
				}
				mFolderDrawable = null;
				mFolderTopDrawable = null;
				mFolderOpenTopDrawable = null;
			}
			mShowEditPic = !((FunAppItemInfo) mInfo).isSysApp();
			AppItemInfo item = ((FunAppItemInfo) mInfo).getAppItemInfo();
			BitmapDrawable icon = null;
			String title = null;
			// 如果内部的AppItemInfo已经被删除，则直接使用dummy的Item代替
			if (item == null) {
				icon = GOLauncherApp.getAppDataEngine().getSysBitmapDrawable();
				title = DUMMYTITLE;
			} else {
				icon = item.mIcon;
				title = item.mTitle;
			}
			if (mAppPic == null) {
				LogUnit.d("ApplicationIcon new 989");
				mAppPic = new MImage(icon);
			} else {
				mAppPic.setDrawable(icon);
			}
			mTitle = title;
		}
		if (mAppText != null) {
			mAppText.setNameTxt(mTitle);
		}
	}

	private void loadEditPicResource() {
		BitmapDrawable deletAppDrawable = null;
		BitmapDrawable deletHighlightAppDrawable = null;
		
		Drawable tmpDrawable = mThemeController
				.getDrawable(mThemeController.getThemeBean().mAppIconBean.mDeletApp);
		
		if (tmpDrawable instanceof BitmapDrawable) {
			deletAppDrawable = (BitmapDrawable) tmpDrawable;
		}

		tmpDrawable = mThemeController
				.getDrawable(mThemeController.getThemeBean().mAppIconBean.mDeletHighlightApp);
		if (tmpDrawable instanceof BitmapDrawable) {
			deletHighlightAppDrawable = (BitmapDrawable) tmpDrawable;
		}
		
		if (deletAppDrawable != null) {
			setEditPic(deletAppDrawable);
		}
		if (deletHighlightAppDrawable != null) {
			setEditLightPic(deletHighlightAppDrawable);
		}
	}

	public FunItemInfo getInfo() {
		return mInfo;
	}

	public Drawable getIcon() {
		if (mInfo == null) {
			return null;
		}

		if (FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
			LogUnit.d("ApplicationIcon new 1008");
			return new BitmapDrawable(mActivity.getResources(), mAppPic.getBitmap());
		} else {
			FunAppItemInfo itemInfo = (FunAppItemInfo) mInfo;
			if (itemInfo == null || itemInfo.getAppItemInfo() == null) {
				return null;
			} else {
				return itemInfo.getAppItemInfo().mIcon;
			}
		}
	}

	@Override
	public String getTitle() {
		if (FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
			return ((FunFolderItemInfo) mInfo).getTitle();
		} else {
			AppItemInfo itemInfo = ((FunAppItemInfo) mInfo).getAppItemInfo();
			if (itemInfo == null) {
				return DUMMYTITLE;
			} else {
				return itemInfo.mTitle;
			}
		}
	}

	public long getID() {
		if (FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
			return mInfo.getFolderId();
		} else {
			AppItemInfo itemInfo = ((FunAppItemInfo) mInfo).getAppItemInfo();
			if (itemInfo == null) {
				return -1;
			} else {
				return itemInfo.mID;
			}
		}
	}

	@Override
	protected void constructAppText() {
		int textWidth = (mIconWidth > mWidth) ? (mWidth - 4) : mIconWidth;
		int textX = (mWidth - textWidth) / 2;
		int textY = mMargin_v + mIconHeight + mIconTextDst;
		if (mAppText == null) {
			// 所有程序Tab的Icon可以分两行显示名称
			LogUnit.d("ApplicationIcon new 1054");
			mAppText = new AppText(mActivity, mTickCount, textX, textY, textWidth, mTextCtrlHeight,
					mTextHeight, mTitle, mShowTwoLines);
		} else {
			mAppText.setXY(textX, textY);
			mAppText.setSize(textWidth, mTextCtrlHeight);
			mAppText.setTextSize(mTextHeight);
			mAppText.setShowTwoLines(mShowTwoLines);
		}
	}

	@Override
	public boolean isFolder() {
		return FunItemInfo.TYPE_FOLDER == mInfo.getType();
	}

	public Intent getIntent() {
		if (FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
			return mInfo.getIntent();
		} else {
			AppItemInfo itemInfo = ((FunAppItemInfo) mInfo).getAppItemInfo();
			if (itemInfo == null) {
				return null;
			} else {
				return itemInfo.mIntent;
			}
		}
	}

	public int getItemType() {
		int type = IItemType.ITEM_TYPE_APPLICATION;
		if (FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
			type = IItemType.ITEM_TYPE_USER_FOLDER;
		}
		// else
		// {
		// type = ((FunAppItemInfo)mInfo).getAppItemInfo().mItemType;
		// }
		return type;
	}

	/**
	 * 是否准备生成文件夹
	 * 
	 * @param isFolderReady
	 */
	public void setIsFolderReady(boolean isFolderReady) {
		boolean equal = mIsFolderReady == isFolderReady;
		mIsFolderReady = isFolderReady;
		mFolderBgHasDrawed = false;
		mRePaint = true;
		if (equal == false) {
			// LogUnit.i("isFolderReady : " + isFolderReady);
			readyData();
			if (isFolderReady) {
				if (mAppText != null) {
					mAppText.setVisible(false);
				}

				if (mShowFolderMotion != null) {
					mAnimManager.cancelAnimation(this, mShowFolderMotion);
//					detachAnimator(mShowFolderMotion);
					mShowFolderMotion = null;
				}
				mShowFolderMotionScaleFactor = 60;
				mShowFolderMotionAlphaFactor = 50;
				mIsShowFolderMotionFinish = false;
				LogUnit.d("ApplicationIcon new 1110");
				mShowFolderMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, 60, 50, 100, 100,
						10, 1, 1);
				LogUnit.d("ApplicationIcon new 1113");
				// mShowFolderMotion.setAnimateListener(new IAnimateListener() {
				//
				// @Override
				// public void onStart(XAnimator animator) {
				// }
				//
				// @Override
				// public void onProgress(XAnimator animator, int progress) {
				// }
				//
				// @Override
				// public void onFinish(XAnimator animator) {
				// mIsShowFolderMotionFinish = true;
				// }
				// });
				// attachAnimator(mShowFolderMotion);
				AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this,
						mShowFolderMotion, new IAnimateListener() {

							@Override
							public void onStart(XAnimator animator) {
							}

							@Override
							public void onProgress(XAnimator animator, int progress) {
							}

							@Override
							public void onFinish(XAnimator animator) {
								mIsShowFolderMotionFinish = true;
							}
						});
				mAnimManager.attachAnimation(animInfo, this);
			} else {
				if (mAppText != null) {
					mAppText.setVisible(true);
				}

				if (mShowFolderMotion != null) {
					mAnimManager.cancelAnimation(this, mShowFolderMotion);
//					detachAnimator(mShowFolderMotion);
					mShowFolderMotion = null;
				}
			}
		}
	}

	public void setIsIntoFolderReady(boolean isReady) {
		if (isReady) {
			if (mIsEnlarged) {
				mIsEnlarged = false;
			}
			setAlpha((int) (2.55f * mDragUpMotionAlphaFactor));
			if (mAppText != null) {
				mAppText.setVisible(false);
			}

		} else {
			if (!mIsEnlarged && mIsDragging) {
				mIsEnlarged = true;
			}
			if (mAppText != null) {
				mAppText.setVisible(true);
			}

		}
	}

	public boolean isFolderReady() {
		return mFolderBgHasDrawed && mIsFolderReady;
	}

	/**
	 * 找不到应用程序的提示，让用户选择是否删除
	 */
	private void showTips() {
		mDialog = new DialogConfirm(mActivity);
		mDialog.show();
		mDialog.setTitle(R.string.dlg_promanageTitle);
		mDialog.setMessage(R.string.dlg_activityNotFound);
		mDialog.setPositiveButton(null, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					removeSelf();
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
			}
		});
		mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mDialog = null;
			}
		});
	}

	private void handleDeletedGoStoreAndGoThemeIcon() {
		if (mInfo.getIntent() != null) {
			if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE.equals(mInfo.getIntent().getAction())) {
				// 如果是gostore或者go精品图标则将AppDataEngine中的数据也一起删掉
				AppDataEngine.getInstance(mContext).onBCChange(MonitorSver.APPCHANGE,
						MonitorSver.FLAG_UNINSTALL, mInfo.getIntent(), null);
				// 保存删除状态
				FunAppSetting funAppSetting = GoSettingControler.getInstance(mContext)
						.getFunAppSetting();
				if (funAppSetting != null) {
					funAppSetting.setShowGoStoreAndGoTheme(false,
							FunAppSetting.FUNC_APP_TYPE_GOSTORE);
				}
			} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME.equals(mInfo.getIntent()
					.getAction())) {
				// 如果是gostore或者go精品图标则将AppDataEngine中的数据也一起删掉
				AppDataEngine.getInstance(mContext).onBCChange(MonitorSver.APPCHANGE,
						MonitorSver.FLAG_UNINSTALL, mInfo.getIntent(), null);
				// 保存删除状态
				FunAppSetting funAppSetting = GoSettingControler.getInstance(mContext)
						.getFunAppSetting();
				if (funAppSetting != null) {
					funAppSetting.setShowGoStoreAndGoTheme(false,
							FunAppSetting.FUNC_APP_TYPE_GOTHEME);
				}
			} else if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET.equals(mInfo.getIntent()
					.getAction())) {
				// 如果是gowidget图标则将AppDataEngine中的数据也一起删掉
				AppDataEngine.getInstance(mContext).onBCChange(MonitorSver.APPCHANGE,
						MonitorSver.FLAG_UNINSTALL, mInfo.getIntent(), null);
				// 保存删除状态
				FunAppSetting funAppSetting = GoSettingControler.getInstance(mContext)
						.getFunAppSetting();
				if (funAppSetting != null) {
					funAppSetting.setShowGoStoreAndGoTheme(false,
							FunAppSetting.FUNC_APP_TYPE_GOWIDGET);
				}
			}
		}
	}

	private void removeSelf() throws DatabaseException {
		if (AppFuncFrame.getFunControler().removeFunAppItemInfo(mInfo) != null) {
			// 检测如果是应用中心/游戏中心，则记录标志，避免下次程序启动时再次加入该组图标
			// add by songzhaochun, 2012.06.19
			if (mContext != null && mInfo != null) {
				Intent i = mInfo.getIntent();
				if (i != null) {
					boolean specialRecord = false;
					// 应用中心
					if (!specialRecord) {
						if (ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER.equals(i.getAction())) {
							specialRecord = true;
						}
					}
					// 游戏中心
					if (!specialRecord) {
						if (ICustomAction.ACTION_FUNC_SHOW_GAMECENTER.equals(i.getAction())) {
							specialRecord = true;
						}
					}
					// 记录
					if (specialRecord) {
						PreferencesManager sp = new PreferencesManager(mContext,
								IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
						if (sp != null) {
								sp.putBoolean(i.getAction(), true);
								sp.commit();
						}
					}
				}
			}
			//
			handleDeletedGoStoreAndGoThemeIcon();
			// 删除成功，刷屏
			AppFuncHandler.getInstance().refreshAllAppGrid();
		}
	}

	/**
	 * 文件夹JavaBean
	 * @author yangguanxiang
	 *
	 */
	protected class FolderItem implements BroadCasterObserver {

		FunAppItemInfo mItemInfo;
		BitmapDrawable mBitmap;
		Matrix mMatrix;
		ApplicationIcon mAppIcon;

		public FolderItem(ApplicationIcon icon) {
			mAppIcon = icon;
			LogUnit.d("ApplicationIcon new 1152");
			mMatrix = new Matrix();
		}

		public void setItemInfo(FunAppItemInfo itemInfo) {
			if (mItemInfo == null) {
				mItemInfo = itemInfo;
				mItemInfo.registerObserver(this);
			} else {
				if (ConvertUtils.intentCompare(mItemInfo.getIntent(), itemInfo.getIntent())) {
					mItemInfo.unRegisterObserver(this);
					mItemInfo = itemInfo;
					mItemInfo.registerObserver(this);
				}
			}
		}

		public void clearItemInfo() {
			if (mItemInfo != null) {
				mItemInfo.unRegisterObserver(this);
				mItemInfo = null;
			}
		}

		@Override
		public void onBCChange(int msgId, int param, Object object,
				@SuppressWarnings("rawtypes") List objects) {
			switch (msgId) {
				case FunAppItemInfo.RESETBEAN :
				case FunAppItemInfo.INCONCHANGE : {
					mGetIconChange = true;
					mAppIcon.readyData();
					if (getAbsX() >= 0 && getAbsX() <= DrawUtils.sWidthPixels && getAbsY() >= 0
							&& getAbsY() <= DrawUtils.sHeightPixels) {
						mRePaint = true;
					} else {
						mRePaint = false;
					}
					break;
				}
				default :
					break;
			}
		}
	}

	/*
	 * 开始缩放以某个位置缩放
	 */
	public void startShrink(float x, float y, int offsetX, int offsetY, boolean isMid,
			ApplicationIcon targetIcon) {

		IAnimateListener listener = new IAnimateListener() {

			@Override
			public void onStart(XAnimator animator) {
			}

			@Override
			public void onProgress(XAnimator animator, int progress) {
			}

			@Override
			public void onFinish(XAnimator animator) {
				mIsShrink = false;
				AppFuncHandler.getInstance().mergerItemToFolder();
				//用户行为
				StatisticsData.countUserActionData(StatisticsData.FUNC_ACTION_ID_APPLICATION,
						StatisticsData.USER_ACTION_FOUR, IPreferencesIds.APP_FUNC_ACTION_DATA);
			}
		};
		startShrink(x, y, offsetX, offsetY, isMid, targetIcon, listener);
	}

	public void startShrink(float x, float y, int offsetX, int offsetY, boolean isMid,
			ApplicationIcon targetIcon, IAnimateListener listener) {
		if (mInfo.getType() == FunItemInfo.TYPE_FOLDER) {
			return;
		}

		int iConsize = targetIcon.getFitInnerSize();

		FunAppItemInfo info = (FunAppItemInfo) mInfo;
		float w = info.getAppItemInfo().getIcon().getBitmap().getWidth();
		float h = info.getAppItemInfo().getIcon().getBitmap().getHeight();
		setEditMode(false);

		mShrinkFactor = mScaleFactor;
		mShrinkFactorY = mScaleFactor;

		if (isMid) {
			x += iConsize / 4;
			y += iConsize / 4;
		}

		mIsShrink = true;

		clearMotion();

		// 终止不必要的schedular task
		Scheduler.getInstance().terminateAll();

		// 清除上次的动作
		if (mShrinkMotion != null) {
//			detachAnimator(mShrinkMotion);
			mAnimManager.cancelAnimation(this, mShrinkMotion);
			mShrinkMotion = null;
		}

		XMotion motion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mX + offsetX, mY + offsetY,
				(int) x + offsetX, (int) y + offsetY, 9, 1.0f, 1.0f);
		AnimationInfo animInfo1 = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION, this,
				motion, null);
		if (!isMid) {
			mShrinkMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, (int) w, (int) h, iConsize
					- (int) ((w - iConsize) / 10), iConsize - (int) ((h - iConsize) / 10), 10, 1, 1);
		} else {
			mShrinkMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, (int) w, (int) h,
					0 - (int) (w / 10), 0 - (int) (h / 10), 10, 1, 1);
		}

		AnimationInfo animInfo2 = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this, mShrinkMotion,
				listener);
		ArrayList<AnimationInfo> infoList = new ArrayList<AnimationInfo>(2);
		infoList.add(animInfo1);
		infoList.add(animInfo2);
		mAnimManager.attachBatchAnimations(ANIMATION_TYPE_STRINK, infoList, null, this);
	}
	public void stratNewIcon() {
		if (mNewMotion != null) {
			mAnimManager.cancelAnimation(this, mNewMotion);
//			detachAnimator(mNewMotion);
			mNewMotion = null;
		}
		mNewMotionScaleFactor = 0.0f;
		mNewMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, 0, 0, 100, 0, 10, 1, 1);
		mNewMotion.delay(XAEngine.ANIMATE_TYPE_FRAME, 10);
		// attachAnimator(mNewMotion);
		AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this, mNewMotion,
				null);
		mAnimManager.attachAnimation(animInfo, null);
	}

	public void startShrink(float x, float y, int offsetX, int offsetY, ApplicationIcon targetIcon) {
		IAnimateListener listener = new IAnimateListener() {

			@Override
			public void onStart(XAnimator animator) {
			}

			@Override
			public void onProgress(XAnimator animator, int progress) {
			}

			@Override
			public void onFinish(XAnimator animator) {
				mIsShrink = false;
				AppFuncHandler.getInstance().mergerItemToFolder();
				//用户行为
				StatisticsData.countUserActionData(StatisticsData.FUNC_ACTION_ID_APPLICATION,
						StatisticsData.USER_ACTION_THREE, IPreferencesIds.APP_FUNC_ACTION_DATA);
			}
		};
		startShrink(x, y, offsetX, offsetY, targetIcon, listener);
	}

	public void startShrink(float x, float y, int offsetX, int offsetY, ApplicationIcon targetIcon,
			IAnimateListener listener) {
		if (mInfo.getType() == FunItemInfo.TYPE_FOLDER) {
			return;
		}

		int iConsize = targetIcon.getFitInnerSize();

		FunAppItemInfo info = (FunAppItemInfo) mInfo;
		float w = info.getAppItemInfo().getIcon().getBitmap().getWidth();
		float h = info.getAppItemInfo().getIcon().getBitmap().getHeight();
		setEditMode(false);

		mShrinkFactor = mScaleFactor;
		mShrinkFactorY = mScaleFactor;

		mIsShrink = true;

		clearMotion();

		// 终止不必要的schedular task
		Scheduler.getInstance().terminateAll();

		// 清除上次的动作
		if (mShrinkMotion != null) {
			mAnimManager.cancelAnimation(this, mShrinkMotion);
//			detachAnimator(mShrinkMotion);
			mShrinkMotion = null;
		}

		XMotion motion = new XALinear(1, XALinear.XALINEAR_ECSPEED, mX + offsetX, mY + offsetY,
				(int) x + offsetX, (int) y + offsetY, 9, 1.0f, 1.0f);
		AnimationInfo animInfo1 = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION, this,
				motion, null);
		mShrinkMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, (int) w, (int) h, iConsize
				- (int) ((w - iConsize) / 10), iConsize - (int) ((h - iConsize) / 10), 10, 1, 1);

		AnimationInfo animInfo2 = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this, mShrinkMotion,
				listener);
		ArrayList<AnimationInfo> infoList = new ArrayList<AnimationInfo>(2);
		infoList.add(animInfo1);
		infoList.add(animInfo2);
		mAnimManager.attachBatchAnimations(ANIMATION_TYPE_STRINK, infoList, null, this);
	}

	public PointF getNextFolderItemPoint() {

		PointF point = new PointF();
		boolean isAction = false;

		mIsInMid = true;
		// int iConsize = (int) (17 * mDensity);
		int iConsize = getFitInnerSize();
		int width = mIntrinSize;
		int height = mIntrinSize;
		if (mAppPic != null) {
			width = mAppPic.getWidth();
			height = mAppPic.getHeight();
		}
		if (mImageList != null) {
			int colunm = 2;
			int curColunm = 0;
			int row = 0;

			for (FolderItem folderItem : mImageList) {

				if (folderItem.mItemInfo != null && folderItem.mBitmap != null) {
					curColunm++;
					if (curColunm >= colunm) {
						curColunm = 0;
						row++;
					}
				} else {
					isAction = true;
					mIsInMid = false;
					point.x = (width - iConsize * 2 - width * 0.03f) / 2 + curColunm
							* (iConsize + width * 0.03f) + width * 0.194f + mIconImage.mX;
					point.y = height * 0.12f + row * (iConsize + width * 0.03f) + height * 0.194f
							+ mIconImage.mY;
				}
			}
		}

		if (!isAction) {
			point.x = (width - iConsize) / 2 + width * 0.194f + mIconImage.mX;;
			point.y = (height - iConsize) / 2 + height * 0.194f + mIconImage.mX;;
		}

		return point;
	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.THEME_CHANGE : {
				//				sIsResLoaded = false;
				if (sFolderBgMap.containsKey(FOLDER_BG_PRIMARY_KEY)) {
					sFolderBgMap.get(FOLDER_BG_PRIMARY_KEY).clear();
					sFolderBgMap.remove(FOLDER_BG_PRIMARY_KEY);
				}
//				mDeletAppDrawable = null;
//				mDeletHighlightAppDrawable = null;
				if (FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
					if (mImageList != null) {
						for (FolderItem item : mImageList) {
							if (item.mBitmap != null) {
								item.mBitmap = null;
							}
							item.clearItemInfo();
						}
						mImageList.clear();
						mImageList = null;
						mCanvas = null;
						mFolderDrawable = null;
						mFolderTopDrawable = null;
						mFolderOpenTopDrawable = null;
					}
					if (mAppPic != null) {
						mAppPic.setDrawable(null);
					}
					mFolderBg = null;
					mFolderBgTop = null;
					mFolderBgTopOpen = null;
				}
			}
				break;
			case AppFuncConstants.LOADTHEMERES : {
				super.notify(key, obj);
				loadResource();
			}
				break;
			case AppFuncConstants.RELOAD_FOLDER_THEMES : {
				if (!sIsReloaded) {
					reloadFolderResource();
				}
				resetFolderIcon();
			}
				break;
		}
	}

	public void setShowStyle(int showStyle) {
		mIconStyle = showStyle;
	}

	/**
	 * 加载主题资源
	 */
	protected void loadResource() {
		loadEditPicResource();
		//		if (!sIsResLoaded) {
		// 如果图标尺寸是默认模式就不用缩放
		final boolean isDefault = GoLauncher.getIconSizeStyle() == DeskSettingVisualIconTabView.DEFAULT_ICON_SIZE;
		String themePackage = GOLauncherApp.getSettingControler().getScreenStyleSettingInfo()
				.getFolderStyle();
		if (!sFolderBgMap.containsKey(FOLDER_BG_PRIMARY_KEY)) {
			ConcurrentHashMap<String, BitmapDrawable> map = new ConcurrentHashMap<String, BitmapDrawable>();
			BitmapDrawable drawable = (BitmapDrawable) mThemeController.getDrawable(
					mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath,
					themePackage, true);
			mFolderBg = getFitDrawable(drawable, isDefault);
			if (mFolderBg != null) {
				map.put(mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath,
						mFolderBg);
			}
			
			drawable = (BitmapDrawable) mThemeController.getDrawable(
					mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath,
					themePackage, true);
			mFolderBgTop = getFitDrawable(drawable, isDefault);
			if (mFolderBgTop != null) {
				map.put(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath,
						mFolderBgTop);
			}

			drawable = (BitmapDrawable) mThemeController.getDrawable(
					mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopOpenPath,
					themePackage, true);
			mFolderBgTopOpen = getFitDrawable(drawable, isDefault);
			if (mFolderBgTopOpen != null) {
				map.put(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopOpenPath,
						mFolderBgTopOpen);
			}

			sFolderBgMap.put(FOLDER_BG_PRIMARY_KEY, map);
		} else {
			ConcurrentHashMap<String, BitmapDrawable> map = sFolderBgMap.get(FOLDER_BG_PRIMARY_KEY);
			mFolderBg = map
					.get(mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath);
			mFolderBgTop = map
					.get(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath);
			mFolderBgTopOpen = map
					.get(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopOpenPath);
		}

		//			sIsResLoaded = true;
		//		}
		// 如果被拉到前台时收到加载资源的消息，证明是因为主题安装在SD卡，SD卡OK时发出来的，
		// 因此需要重新生成缩略图
		if (AppFuncFrame.sVisible) {
			readyData();
		}
	}

	protected void reloadFolderResource() {
		final boolean isDefault = GoLauncher.getIconSizeStyle() == DeskSettingVisualIconTabView.DEFAULT_ICON_SIZE;
		String themePackage = GOLauncherApp.getSettingControler().getScreenStyleSettingInfo()
				.getFolderStyle();
		//		BitmapDrawable drawable = (BitmapDrawable) mThemeController.getDrawable(
		//				mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath,
		//				themePackage, true);
		//		mFolderBg = getFitDrawable(drawable, isDefault);
		//
		//		drawable = (BitmapDrawable) mThemeController.getDrawable(
		//				mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath,
		//				themePackage, true);
		//		mFolderBgTop = getFitDrawable(drawable, isDefault);
		//
		//		drawable = (BitmapDrawable) mThemeController.getDrawable(
		//				mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopOpenPath,
		//				themePackage, true);
		//		mFolderBgTopOpen = getFitDrawable(drawable, isDefault);
		//

		//		if (!sFolderBgMap.containsKey(FOLDER_BG_PRIMARY_KEY)) {
		//			ConcurrentHashMap<String, BitmapDrawable> map = new ConcurrentHashMap<String, BitmapDrawable>();
		//			BitmapDrawable drawable = (BitmapDrawable) mThemeController.getDrawable(
		//					mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath,
		//					themePackage, true);
		//			mFolderBg = getFitDrawable(drawable, isDefault);
		//			map.put(mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath,
		//					mFolderBg);
		//
		//			drawable = (BitmapDrawable) mThemeController.getDrawable(
		//					mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath,
		//					themePackage, true);
		//			mFolderBgTop = getFitDrawable(drawable, isDefault);
		//			map.put(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath,
		//					mFolderBgTop);
		//
		//			drawable = (BitmapDrawable) mThemeController.getDrawable(
		//					mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopOpenPath,
		//					themePackage, true);
		//			mFolderBgTopOpen = getFitDrawable(drawable, isDefault);
		//			map.put(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopOpenPath,
		//					mFolderBgTopOpen);
		//
		//			sFolderBgMap.put(FOLDER_BG_PRIMARY_KEY, map);
		//		} else {
		//			ConcurrentHashMap<String, BitmapDrawable> map = sFolderBgMap.get(FOLDER_BG_PRIMARY_KEY);
		//			mFolderBg = map
		//					.get(mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath);
		//			mFolderBgTop = map
		//					.get(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath);
		//			mFolderBgTopOpen = map
		//					.get(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopOpenPath);
		//		}

		ConcurrentHashMap<String, BitmapDrawable> map = null;
		if (!sFolderBgMap.containsKey(FOLDER_BG_PRIMARY_KEY)) {
			map = new ConcurrentHashMap<String, BitmapDrawable>();
			sFolderBgMap.put(FOLDER_BG_PRIMARY_KEY, map);
		} else {
			map = sFolderBgMap.get(FOLDER_BG_PRIMARY_KEY);
			map.clear();
		}

		BitmapDrawable drawable = (BitmapDrawable) mThemeController.getDrawable(
				mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath,
				themePackage, true);
		mFolderBg = getFitDrawable(drawable, isDefault);
		if (mFolderBg != null) {
			map.put(mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath,
					mFolderBg);
		}

		drawable = (BitmapDrawable) mThemeController.getDrawable(
				mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath,
				themePackage, true);
		mFolderBgTop = getFitDrawable(drawable, isDefault);
		if (mFolderBgTop != null) {
			map.put(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath,
					mFolderBgTop);
		}

		drawable = (BitmapDrawable) mThemeController.getDrawable(
				mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopOpenPath,
				themePackage, true);
		mFolderBgTopOpen = getFitDrawable(drawable, isDefault);
		if (mFolderBgTopOpen != null) {
			map.put(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopOpenPath,
					mFolderBgTopOpen);
		}
		sIsReloaded = true;
	}

	@Override
	public void setAlpha(int alpha) {
		if (mAlpha == alpha) {
			return;
		}
		mAlpha = alpha;
		if (mAppPic != null) {
			mAppPic.setAlpha(alpha);
		}

		if (mGOStoreNumPic != null) {
			mGOStoreNumPic.setAlpha(alpha);
		}

		if (mUpdatePic != null) {
			mUpdatePic.setAlpha(alpha);
		}

		if (mCountPaint != null) {
			mCountPaint.setAlpha(alpha);
		}

		if (mNotificationNumPic != null) {
			mNotificationNumPic.setAlpha(alpha);
		}

		if (mNewAppPic != null) {
			mNewAppPic.setAlpha(alpha);
		}

//		if (mEditPic != null) {
//			if (alpha == 255) {
//				mEditPic.setAlpha(alpha);
//				if (mEditLightPic != null) {
//					mEditLightPic.setAlpha(alpha);
//				}
//				if (mDeletAppDrawable != null && mEditPic != null
//						&& mEditPic.getBitmap() != mDeletAppDrawable.getBitmap()) {
//					mEditPic.setDrawable(mDeletAppDrawable);
//				}
//				if (mDeletHighlightAppDrawable != null && mEditLightPic != null
//						&& mEditLightPic.getBitmap() != mDeletHighlightAppDrawable.getBitmap()) {
//					mEditLightPic.setDrawable(mDeletHighlightAppDrawable);
//				}
//			} else {
//				BitmapDrawable temp = AppFuncUtils.getInstance(mActivity).getKillIconCopy();
//				if (mEditPic != null && temp != null && mEditPic.getBitmap() != temp.getBitmap()) {
//					mEditPic.setDrawable(temp);
//				}
//
//				temp = AppFuncUtils.getInstance(mActivity).getKillLightIconCopy();
//				if (mEditLightPic != null && temp != null
//						&& mEditLightPic.getBitmap() != temp.getBitmap()) {
//					mEditLightPic.setDrawable(temp);
//				}
//				if (mEditPic != null) {
//					mEditPic.setAlpha(alpha);
//				}
//				if (mEditLightPic != null) {
//					mEditLightPic.setAlpha(alpha);
//				}
//			}
//		}
		if (mEditPic != null) {
			mEditPic.setAlpha(alpha);
		}
		if (mEditLightPic != null) {
			mEditLightPic.setAlpha(alpha);
		}
		if (mAppText != null) {
			if (isChildrenDrawnWithCacheEnabled() && mAppText.isDrawingCacheEnabled()) {
				if (!GoLauncher.getCustomTitleColor()) {
					mAppText.setPaintAlpha(255);
				}
				mAppText.setDrawingCacheAlpha(alpha);
			} else if (!GoLauncher.getCustomTitleColor()) {
				mAppText.setPaintAlpha(alpha);
			}
		}

	}

	// 判断是否为假图标
	private boolean isVirtualApp(Intent intent) {
		if (null == intent || null == intent.getAction()) {
			return false;
		}
		if (intent.getAction().equals(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME)) {
			return true;
		}
		return false;
	}

	protected BitmapDrawable getFitDrawable(BitmapDrawable drawable, boolean isDefault) {
		if (null != drawable && null != drawable.getBitmap()) {
			// 针对一些做得不好的主题（文件夹的图片没有按规格大小出图，所以要进行缩放）
			if (drawable.getIntrinsicWidth() != Utilities.getStandardIconSize(mContext)) {
				isDefault = false;
			}
			return isDefault ? drawable : new BitmapDrawable(mContext.getResources(),
					Utilities.createBitmapThumbnail(drawable.getBitmap(), mContext));
		}
		return null;
	}

	protected int getFitInnerSize() {
		return (int) (mIntrinSize * 0.365f); // 简化得来的
		// return (int) ((17 * DockConstant.getScreenIconSize() / 72) *
		// mDensity);
	}

	private void removeFolder(final FunFolderItemInfo mdeleteInfo) {
		// 显示提示框
		showProgressDialog(false);
		/**
		 * 很奇怪的问题，这里mHandler.sendMessage(message);发出去的消息有时候不会执行handlemessage方法。
		 * 先改成用asynctask来实现；以后需要追查原因。
		 * 
		 * @edit by huangshaotao
		 * @date 2011-12-6
		 */
		// new Thread(ThreadName.FUNC_REMOVE_FOLDER) {
		// @Override
		// public void run(){
		// Log.e("......mFunControler.removeFolder((FunFolderItemInfo)mInfo);",
		// "start");
		// ArrayList<AppItemInfo> removeList = mFunControler
		// .removeFolder((FunFolderItemInfo)mInfo);
		// Log.e("......mFunControler.removeFolder((FunFolderItemInfo)mInfo);",
		// "end");
		// Log.e("......mHandler.obtainMessage();", "start");
		// Message message = mHandler.obtainMessage();
		// Message message = new Message();
		// Log.e("......mHandler.obtainMessage();", "end");
		// message.what = DELFINISH;
		// message.obj = removeList;
		// Log.e("......mHandler.sendMessage(message);", "start");
		// mHandler.sendMessage(message);
		// Log.e("......mHandler.sendMessage(message);", "end");
		// }
		// }.start();

		new AsyncTask<Object, Object, Object>() {
			ArrayList<AppItemInfo> mRemoveList = null;

			@Override
			protected Object doInBackground(Object... params) {

				try {
					mRemoveList = mFunControler.removeFolder(mdeleteInfo);
					return "success";
				} catch (DatabaseException e) {
					e.printStackTrace();
					return e;
				}
			}

			@Override
			protected void onPostExecute(Object result) {
				if (result instanceof Exception) {
					AppFuncExceptionHandler.handle((Exception) result);
				} else {
					// 通知桌面文件夹同步
					GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
							IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, -1,
							mInfo.getFolderId(), mRemoveList);
					GoLauncher.sendHandler(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
							IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, 0,
							mInfo.getFolderId(), mRemoveList);
					// 主动刷屏
					if (AppFuncHandler.getInstance() != null) {
						AppFuncHandler.getInstance().refreshGrid();
					}
				}
				// 取消加载框
				dismissProgressDialog();
			}
		}.execute();
	}

	private void showProgressDialog(boolean isInit) {
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
				AppFuncConstants.PROGRESSBAR_SHOW, null);
	}

	private void dismissProgressDialog() {
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
				AppFuncConstants.PROGRESSBAR_HIDE, null);
	}

	private String getString(int id) {
		return mActivity.getResources().getString(id);
	}

	private synchronized void resetFolderIcon() {
		if (sFolderBgMap.containsKey(FOLDER_BG_PRIMARY_KEY)) {
			ConcurrentHashMap<String, BitmapDrawable> map = sFolderBgMap.get(FOLDER_BG_PRIMARY_KEY);
			mFolderBg = map
					.get(mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath);
			mFolderTopDrawable = map
					.get(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath);
			mFolderOpenTopDrawable = map
					.get(mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopOpenPath);
		} else {
			loadResource();
		}
		mFolderDrawable = null;
		mFolderTopDrawable = null;
		mFolderOpenTopDrawable = null;
		mCanvas = null;
//		mFolderDrawable = mFolderBg;
//		Bitmap copy = mFolderBg.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
//		if (copy == null) {
//			return;
//		}
//		if (mAppPic == null) {
//			mAppPic = new MImage(copy);
//		} else {
//			mAppPic.setBitmap(copy);
//		}
		readyData();
	}

	private void sortAppUpdateBeans(AppsBean beans) {
		if (beans != null) {
			ArrayList<AppBean> list = beans.mListBeans;
			if (list != null) {
				String pkName = mInfo.getIntent().getComponent().getPackageName();
				AppBean temp = null;

				Iterator<AppBean> iterator = list.iterator();
				while (iterator.hasNext()) {
					AppBean bean = iterator.next();
					if (pkName.equals(bean.mPkgName)) {
						temp = bean;
						iterator.remove();
						break;
					}
				}
				if (temp != null) {
					list.add(0, temp);
				}
			}
		}
	}

	// public void startDeleteFolderMotion(final FunFolderItemInfo
	// funFolderItemInfo,final long folderId){
	// mDeleteMotionScaleFactor = 1.0f;
	// if(mFolderDeleteMotion != null){
	// detachAnimator(mFolderDeleteMotion);
	// mFolderDeleteMotion = null;
	// }
	// mFolderDeleteMotion = new XALinear(1,XALinear.XALINEAR_ECSPEED, 100, 0,
	// 0,0, 30, 1, 1);
	// mFolderDeleteMotion.setAnimateListener(new IAnimateListener() {
	// @Override
	// public void onStart(XAnimator xanimator) {
	// }
	//
	// @Override
	// public void onProgress(XAnimator xanimator, int i) {
	// }
	//
	// @Override
	// public void onFinish(XAnimator xanimator) {
	// // 删除文件夹
	// ArrayList<AppItemInfo> removeList;
	// try {
	// removeList = AppFuncFrame.getFunControler().removeFolder(
	// funFolderItemInfo);
	// GoLauncher.sendMessage(
	// GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
	// IDiyFrameIds.SCREEN_FRAME,
	// IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, -1, folderId,
	// removeList);
	// GoLauncher.sendHandler(
	// GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
	// IDiyFrameIds.DOCK_FRAME,
	// IDiyMsgIds.SCREEN_FOLDER_REMOVEITEMS, -1, folderId,
	// removeList);
	// // 主动刷屏
	// AppFuncHandler.getInstance().refreshGrid();
	// } catch (DatabaseException e) {
	// AppFuncExceptionHandler.handle(e);
	// }
	// }
	// });
	// attachAnimator(mFolderDeleteMotion);
	// }

	// public void startOpenMotion(){
	// mDeleteMotionScaleFactor = 1.0f;
	// if(mOpenMotion != null){
	// detachAnimator(mOpenMotion);
	// mOpenMotion = null;
	// }
	// mOpenMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, 30,30, 100, 100,
	// 15, 1, 1);
	// mOpenMotion.setAnimateListener(new IAnimateListener() {
	// @Override
	// public void onStart(XAnimator xanimator) {
	// }
	//
	// @Override
	// public void onProgress(XAnimator xanimator, int i) {
	// }
	//
	// @Override
	// public void onFinish(XAnimator xanimator) {
	// AppFuncHandler.getInstance().refreshGrid();
	// }
	// });
	// attachAnimator(mOpenMotion);
	// }
	/**
	 * 获取不包括文字的图标位置的高度
	 * 
	 * @return
	 */
	public int getIconHeight() {
		return mMargin_v + mIconHeight + mIconTextDst * 2;
	}

	private XMotion mAlphaMotion;
	private int mAlphaMotionFactor;

	@Override
	public AnimationInfo changeAlpha(int sourceAlpha, int targetAlpha, int totalStep,
			IAnimateListener listener, OrientationInvoker orientationInvoker, boolean batchMode) {
		if (sourceAlpha < 0) {
			sourceAlpha = 0;
		} else if (sourceAlpha > 255) {
			sourceAlpha = 255;
		}
		setAlpha(sourceAlpha);
		if (mAlphaMotion != null) {
			mAnimManager.cancelAnimation(this, mAlphaMotion);
//			detachAnimator(mAlphaMotion);
			mAlphaMotion = null;
		}
		mAlphaMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, sourceAlpha, sourceAlpha,
				targetAlpha, targetAlpha, totalStep, 1, 1);
		// if (listener != null) {
		// mAlphaMotion.setAnimateListener(listener);
		// }
		// attachAnimator(mAlphaMotion);
		AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this, mAlphaMotion,
				listener);
		if (!batchMode) {
			mAnimManager.attachAnimation(animInfo, orientationInvoker);
		}
		return animInfo;
	}

	private XMotion mScaleMotion;
	private float mScaleMotionHFactor;
	private float mScaleMotionVFactor;

	@Override
	public void changeScale(float sourceHScale, float sourceVScale, float targetHScale,
			float targetVScale, IAnimateListener listener) {
		if (mScaleMotion != null) {
			mAnimManager.cancelAnimation(this, mScaleMotion);
//			detachAnimator(mScaleMotion);
			mScaleMotion = null;
		}
		int orgX = 100;
		int orgY = 100;
		if (sourceHScale > 0) {
			orgX = (int) (sourceHScale * 100);
		} else {
			if (mIsEnlarged) {
				orgX = (int) (mScaleFactor * 100);
			}
		}
		if (sourceHScale > 0) {
			orgY = (int) (sourceVScale * 100);
		} else {
			if (mIsEnlarged) {
				orgY = (int) (mScaleFactor * 100);
			}
		}

		mScaleMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, orgX, orgY,
				(int) (targetHScale * 100), (int) (targetVScale * 100), 40, 1, 1);
		// if (listener != null) {
		// mScaleMotion.setAnimateListener(listener);
		// }
		// attachAnimator(mScaleMotion);
		AnimationInfo animInfo = new AnimationInfo(AnimationInfo.TYPE_SIMPLE, this, mScaleMotion,
				listener);
		mAnimManager.attachAnimation(animInfo, this);
	}

	// private XMotion mMoveMotion;

	// @Override
	// public void move(int sourceX, int sourceY, int targetX, int targetY,
	// IAnimateListener listener) {
	// if ((targetX != sourceX) || (targetY != sourceY)) {
	// if (mMoveMotion != null) {
	// detachAnimator(mMoveMotion);
	// mMoveMotion = null;
	// }
	// mMoveMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, sourceX,
	// sourceY, targetX, targetY, 40, 1, 1);
	// if(listener != null){
	// mMoveMotion.setAnimateListener(listener);
	// }
	// attachAnimator(mMoveMotion);
	//
	// }
	// }

	@Override
	public void unRegister() {
		super.unRegister();
		// 注册主题改变事件
		DeliverMsgManager.getInstance().unRegisterDispenseMsgHandler(AppFuncConstants.THEME_CHANGE,
				this);
		// 注册重新刷folder事件
		DeliverMsgManager.getInstance().unRegisterDispenseMsgHandler(
				AppFuncConstants.RELOAD_FOLDER_THEMES, this);
		if (mInfo != null) {
			if (mInfo.getType() == FunItemInfo.TYPE_APP) {
				mInfo.unRegisterObserver(mAppItemInfoListener);
			} else if (mInfo.getType() == FunItemInfo.TYPE_FOLDER) {
				mInfo.unRegisterObserver(mFunFolderItemInfoListener);
			}
		}
		mFolderBg = null;
		mFolderBgTop = null;
		mFolderBgTopOpen = null;
	}

	@Override
	protected void onHide() {
		if (mDialog != null) {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			mDialog = null;
		}
		// 有些机型，例如milestone 2在安装一个程序的时候系统会自动出发onPause和onResume
		// 在onPause的时候会调用组件的onHide，但是因为此时并不是推出功能表，因此不需要将
		// 是否是新安装应用程序的标志位重置
		if (!AppFuncFrame.sVisible && mInfo instanceof FunAppItemInfo) {
			FunAppItemInfo funAppItemInfo = (FunAppItemInfo) mInfo;
			if (!funAppItemInfo.getAppItemInfo().mIsNewRecommendApp) {
				funAppItemInfo.setIsNew(false);
			}
		}
		super.onHide();

	}

	@Override
	public void keepCurrentOrientation() {
		// do nothing
	}

	@Override
	public void resetOrientation() {
		// do nothing
	}

	public static void setStartApp(boolean isStartApp) {
		sIsStartApp = isStartApp;
	}

	public void setIsShrink(boolean shrink) {
		mIsShrink = shrink;
	}
}
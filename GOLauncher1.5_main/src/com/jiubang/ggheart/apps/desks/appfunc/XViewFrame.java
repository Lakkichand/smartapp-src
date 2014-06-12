package com.jiubang.ggheart.apps.desks.appfunc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.gau.go.launcherex.R;
import com.go.util.graphics.FadePainter;
import com.go.util.graphics.ImageFilter;
import com.go.util.graphics.ImageUtil;
import com.go.util.graphics.effector.united.CoupleScreenEffector;
import com.jiubang.core.mars.ITicker;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.appfunc.component.ProManageIcon.ProManageIconInfo;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.appfunc.timer.Scheduler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AppFuncAllAppMenu;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AppFuncProManageMenu;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseMenu;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IBackgroundInfoChangedObserver;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.appfunc.model.MsgEntity;
import com.jiubang.ggheart.apps.desks.appfunc.search.AppFuncSearchFrame;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.ggmenu.GGMenu;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.components.QuickActionMenu;
import com.jiubang.ggheart.components.QuickActionMenu.onActionListener;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IDisplayedDialogHandler;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaManager;

/**
 * 该类为功能表视图组件最底层，是唯一一个也是最后一个使用系统ui组件的视图组件，继承自ViewGroup
 * 负责绘制和排版功能表背景及所有ui组件
 * @author wenjiaming
 * 
 */
public class XViewFrame extends ViewGroup
		implements
			OnTouchListener,
			ITicker,
			IBackgroundInfoChangedObserver,
			IMsgHandler,
			onActionListener,
			IDisplayedDialogHandler {
	private final static int FORCE_PAINT = 2;

	private List<XComponent> mStoreComponents = new ArrayList<XComponent>();

	/** 背景色 */
	private int mBgColor = AppFuncConstants.DEFAULT_BG_COLOR;

	/** 合成的背景图 */
	private Bitmap mMergeBg = null;
	private Canvas mMergeBgCanvas = null;
	private boolean mIsDrawMergeBg = false;
	private Matrix mMergeBgMatrix = null;
	/** 原生的背景图 */
	private BitmapDrawable mOrigBg = null;
	/**
	 * 是否需要重新合成背景图
	 */
	private boolean mIsReMergeBg = false;
	/**
	 * 是否与桌面背景混合
	 */
	private boolean mMergeWidthDesktop = false;

	// private AppFuncFrame mAppFuncFrame;

	private AppFuncMainView mAppFuncMainView;

	private Activity mActivity;

	private AppFuncUtils mUtils;

	private int mPaintCount;

	private volatile static ConcurrentLinkedQueue<MsgEntity> sQueue;

	private DeliverMsgManager msgManager;
	/**
	 * 是否为强制重新布局
	 */
	private boolean mIsForceLayout;
	/**
	 * 是否更新背景图
	 */
	private boolean mIsUpdateBgImg = true;

	private ProgressDialog mProgressDialog;
	/**
	 * 弹出菜单：进程管理Tab使用
	 */
	private QuickActionMenu mQuickActionMenu;

	private AppFuncThemeController mThemeCtrl;
	private AppFuncAllAppMenu mAllAppMenu;
	private AppFuncProManageMenu mProManageMenu;
	// private GGMenu mManageProgramMenu;
	private boolean mNeedOpenGGMenu;

	private Drawable mDesktopBg;

	private BitmapShader mShader = null;
	private FadePainter mFadePainter = null;
	private Matrix mBgMatrix = null;
	private int mStatusBarHeight;
	private Matrix mShaderMatrix = null; // mShader当前使用的矩阵

	private boolean mHighBgQuality = false; // 高质量背景，应该跟桌面的窗口颜色质量保持一致
	private boolean mBlurBg = false; // 背景模糊，可以考虑作为设置选项

	private boolean mEnableBg = true; // 是否绘制背景

	private static XViewFrame sInstance;

	private WindowManager mWindowManager;
	private View mMenuGuideView;
	// private InputConnection mInputConnection = null;

	private View mGoProgressBar;

	private boolean mBgVertical;

	private Handler mHandler;
	private XViewFrame(Activity activity, AttributeSet attrs) {
		super(activity, attrs);
		initHandler();
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.XVIEW, this);
		sQueue = new ConcurrentLinkedQueue<MsgEntity>();
		this.mActivity = activity;
		mUtils = AppFuncUtils.getInstance(mActivity);
		mThemeCtrl = AppFuncFrame.getThemeController();
		setOnTouchListener(this);
		mAppFuncMainView = new AppFuncMainView(activity, 1, 0, 0, 0, 0);

		addComponent(mAppFuncMainView);
		mAppFuncMainView.setDrawingCacheEnabled(true);

		setClickable(true);
		setFocusableInTouchMode(true);
		// 注册后台事件监听
		AppFuncFrame.getDataHandler().registerBgInfoChangeObserver(this);
		// 注册主题改变事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.THEME_CHANGE,
				this);
		// 注册获取主题图片事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
		mPaintCount = 0;
		msgManager = DeliverMsgManager.getInstance();

		mFadePainter = new FadePainter();
		mBgMatrix = new Matrix();
		mBlurBg = 1 == AppFuncFrame.getDataHandler().getBlurBackground();
		mWindowManager = mActivity.getWindowManager(); // (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
		// 建立竖屏的背景合成图
		// initMergeBg();
		// setBackgroundColor(mBgColor);
		setAnimationCacheEnabled(true);
		
		MediaPluginFactory.buildSwitchMenuControler(mActivity, this);
	}

	private void initHandler() {
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case AppFuncConstants.PROGRESSBAR_SHOW :

//						XAEngine.stop();
						if (mGoProgressBar == null) {
							WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
									LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,
									WindowManager.LayoutParams.TYPE_APPLICATION,
									WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
									PixelFormat.TRANSLUCENT);
							mGoProgressBar = mActivity.getLayoutInflater().inflate(
									R.layout.progressbar, null);
							mWindowManager.addView(mGoProgressBar, lp);
						} else if (!mGoProgressBar.isShown()) {
							mGoProgressBar.setVisibility(View.VISIBLE);
						}
						break;
					case AppFuncConstants.PROGRESSBAR_HIDE :
						if (mGoProgressBar != null
								&& mGoProgressBar.isShown()) {
							mGoProgressBar.setVisibility(View.GONE);
						}
//						XAEngine.resume();
						break;
					default :
						break;
				}
			}
		};
	}

	/**
	 * 创建实例：将创建实例与获取实例分开的原因是某些机型在桌面重新加载后sInstance还是 指向以前的实例，即时已经显示将sIntance设置为空.
	 * Weird!!!!
	 * 
	 * @param activity
	 * @return
	 */
	public static void createInstance(Activity activity) {
		sInstance = new XViewFrame(activity, null);
	}

	public static XViewFrame getInstance() {
		return sInstance;
	}

	/**
	 * 创建合成背景图
	 * 
	 * @return
	 */
	private boolean initMergeBg() {
		int width, height;
		if (GoLauncher.isPortait()) {
			width = mUtils.getScreenWidth();
			height = mUtils.getScreenHeight();
		} else {
			width = mUtils.getScreenHeight();
			height = mUtils.getScreenWidth();
		}
		try {
			mMergeBg = Bitmap.createBitmap(width, height, mHighBgQuality
					? Config.ARGB_8888
					: Config.RGB_565);
			if (width == mUtils.getScreenWidth()) {
				mBgVertical = true;
			} else {
				mBgVertical = false;
			}
			if (mMergeBg != null) {
				mMergeBgCanvas = new Canvas(mMergeBg);
				mMergeBgMatrix = new Matrix();
				mMergeBgMatrix.postRotate(90.0f);
				mMergeBgMatrix.postTranslate(mMergeBg.getHeight(), 0);
				mShaderMatrix = mMergeBgMatrix;
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
		}
		return false;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		onDraw(canvas);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		if (AppFuncFrame.sVisible == false) {
			return;
		}

		if (mEnableBg) {
			drawBackground(canvas);
		} else {
			canvas.drawColor(0xFF000000);
		}

		// Bitmap bitmap = mBgImg == null ? null : mBgImg.getBitmap();
		// if (bitmap != null && !bitmap.isRecycled()) {
		// // 有自定义的背景图片
		// if(bitmap.hasAlpha()){
		// // 如果功能表自定义背景图片是半透明的，则通知桌面绘制背景
		// AppFuncFrame.getInstance().sendMessage(IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.DRAW_BACKGROUND, 0, canvas, null);
		// }
		// // boolean isRotate = false;
		// int startY = mUtils.getStatusBarHeight();
		// // if (mUtils.isVertical()) {
		// // if (mBgImg.getIntrinsicWidth() > mBgImg.getIntrinsicHeight()) {
		// // isRotate = true;
		// //
		// // } else {
		// // isRotate = false;
		// // }
		// // } else {
		// // if (mBgImg.getIntrinsicWidth() < mBgImg.getIntrinsicHeight()) {
		// // isRotate = true;
		// // } else {
		// // isRotate = false;
		// // }
		// // }
		// // if (isRotate) {
		// // canvas.save();
		// // canvas.rotate(-90.0f);
		// // canvas.translate(-mUtils.getScreenHeight() + startY, 0);
		// // ImageUtil.drawImage(canvas, mBgImg, ImageUtil.STRETCHMODE, 0,
		// // 0, mUtils.getScreenHeight(), mUtils.getScreenWidth(),
		// // mPaint);
		// // canvas.restore();
		// // } else {
		// ImageUtil.drawImage(canvas, mBgImg, ImageUtil.STRETCHMODE, 0,
		// -startY, mUtils.getScreenWidth(),
		// mUtils.getScreenHeight() - startY, mPaint);
		// // }
		// } else {
		// if (mAppFuncMainView != null && mAppFuncMainView.isFolderShow()) {
		// // 当文件夹存在时，只有背景色为不透明时才绘制
		// if ((mBgColor & 0xFF000000) == 0xFF000000) {
		// canvas.drawColor(mBgColor);
		// }
		// } else {
		// // 通知桌面绘制背景和半透明颜色
		// AppFuncFrame.getInstance().sendMessage(IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.DRAW_BACKGROUND, mBgColor, canvas, null);
		// }
		//
		// }
		if (AppFuncSearchFrame.sIsSearchVisable == true) {
			return;
		}
		// 重绘
		int size = mStoreComponents.size();
		for (int i = 0; i < size; i++) {
			XComponent component = mStoreComponents.get(i);
			if (component.isVisible()) {
				component.checkIsShowed();
				component.paintCurrentFrame(canvas, component.mX, component.mY);
			}
		}
	}

	@Override
	public synchronized void tick() {
		boolean b = isRepaint();
		Scheduler.getInstance().executeFrame();
		handleMsgQueue();
		if (!b && mPaintCount == 0) { // 如果不需要重绘，不去拿(锁定)画布
			return;
		}
		if (mPaintCount > 0) {
			mPaintCount--;
		}
		try {
			// if (mIsPostResponse)
			// {
			// 在做进出动画时不需要刷新
			if (getVisibility() == View.VISIBLE) {
				postInvalidate();
			}
			// mIsPostResponse = false;
			// }
		} catch (Exception e) {
			Log.e("XViewFrame", "System error. Ignore the tick this time.");
		}
	}

	private void handleMsgQueue() {
		while (sQueue != null && !sQueue.isEmpty()) {
			MsgEntity entity = sQueue.poll();
			if (entity != null) {
				if (entity.mMsgId == AppFuncConstants.POST_REPANIT) {
					mPaintCount = FORCE_PAINT;
				} else {
					msgManager.onChange(entity.mHandlerID, entity.mMsgId, entity.mObj);
				}
			}
		}
	}

	protected boolean isRepaint() {
//		int size = mStoreComponents.size();
		boolean isRepaint = false;
		for (XComponent component : mStoreComponents) {
//			XComponent component = mStoreComponents.get(i);
			if (component != null && component.isVisible() && component.tick()) {
				isRepaint = true;
			}
		}
		return isRepaint;
	}

	public synchronized void addComponent(XComponent component) {
		if (component == null) {
			// TODO 异常
		} else if (mStoreComponents.indexOf(component) < 0) {
			mStoreComponents.add(component);
			component.start();
		}
	}

	public synchronized void removeComponent(XComponent component) {
		if (component != null) {
			mStoreComponents.remove(component);
			component.close();
		}
	}

	public synchronized void onHide() {
		for (XComponent component : mStoreComponents) {
			component.close();
		}
	}

	public int getBgColor() {
		return mBgColor;
	}

	public void setBgColor(int bgColor) {
		mBgColor = bgColor;
	}

	public synchronized boolean onKey(int keyCode, KeyEvent event) {
		if (AppFuncFrame.sCurrentState != AppFuncFrame.STATE_NORMAL) {
			return false;
		}
		boolean isAction = false;
		try {
			mPaintCount = FORCE_PAINT;
			int size = mStoreComponents.size();
			for (int i = size - 1; i >= 0; i--) {
				XComponent component = mStoreComponents.get(i);
				if (component.onKey(event)) {
					isAction = true;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!isAction) {
			// 自定义菜单处理
			if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
				if (!mAppFuncMainView.isFolderShow()) {
					if (KeyEvent.ACTION_DOWN == event.getAction()) {
						mNeedOpenGGMenu = true;
					} else if (KeyEvent.ACTION_UP == event.getAction() && mNeedOpenGGMenu) {
						mNeedOpenGGMenu = false;
						if (AppFuncConstants.ALLAPPS == getSeletedTab()) {
							if (AppFuncContentTypes.sType == AppFuncContentTypes.APP) {
								if (mAllAppMenu != null && mAllAppMenu.isShowing()) {
									mAllAppMenu.dismiss();
								} else {
									if (!(getAppFuncMainView().getCurrentContent().getXGrid() != null && getAppFuncMainView()
											.getCurrentContent().getXGrid().isInDragStatus())) {
										if (mAllAppMenu == null) {
											mAllAppMenu = new AppFuncAllAppMenu(mActivity);
										}
										mAllAppMenu.show(this);
									}
								}
							} else if (AppFuncContentTypes.sType != AppFuncContentTypes.SEARCH) {
//								MediamanagementTabBasicContent content = mAppFuncMainView
//										.getMediaManagementContent();
//								if (content != null) {
//									content.showMenu(true);
//								}
								IMediaManager mediaManager = MediaPluginFactory.getMediaManager();
								if (mediaManager != null) {
									mediaManager.showMenu(true);
								}
							}

						} else if (AppFuncConstants.PROCESSMANAGEMENT == getSeletedTab()) {
							if (mProManageMenu != null && mProManageMenu.isShowing()) {
								mProManageMenu.dismiss();
							} else {
								// if (!getAppFuncMainView().getCurrentContent()
								// .getmGrid().isInDragStatus()) {
								if (mProManageMenu == null) {
									mProManageMenu = new AppFuncProManageMenu(mActivity);
								}
								mProManageMenu.show(this);
								// }
							}
						}
						isAction = true;
					}
				}
			}
		}
		return isAction;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mAllAppMenu != null && mAllAppMenu.isShowing()) {
			return true;
		}
		return super.onInterceptTouchEvent(ev);
	}

	public boolean optionsItemSelected(MenuItem item) {
		// return mTabComponent.optionsItemSelected(item);
		return mAppFuncMainView.optionsItemSelected(item);
	}

	@Override
	public synchronized boolean onTouch(View v, MotionEvent event) {

		if (mAllAppMenu != null && mAllAppMenu.isShowing()) {
			return true;
		}
		try {
			int size = mStoreComponents.size();
			mPaintCount = FORCE_PAINT;
			for (int i = size - 1; i >= 0; i--) {
				XComponent component = mStoreComponents.get(i);
				if (component.onTouch(event)) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	// TODO 重构
	public int getSeletedTab() {
		return mAppFuncMainView.getSeletedTab();
	}

	public AppFuncMainView getAppFuncMainView() {
		return mAppFuncMainView;
	}

	/**
	 * 功能表被添加和横竖屏切换时调用
	 */
	@Override
	protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (AppFuncFrame.sVisible == false) {
			return;
		}

		if ((changed == false) && (mIsForceLayout == false)) {
			return;
		}
		mIsForceLayout = false;
		AppFuncUtils.getInstance(mActivity).setKeyCode();
		// super.onLayout(changed, left, top, right, bottom);
		if (mQuickActionMenu != null) {
			mQuickActionMenu.cancel();
			mQuickActionMenu = null;
		}
		// TODO 重构
		if (mAppFuncMainView != null) {
			mPaintCount = FORCE_PAINT;
			layoutTabComponent(left, top, right, bottom);
		}
	}

	private void layoutTabComponent(int left, int top, int right, int bottom) {
		// Tab栏高度
		if (mUtils.isVertical() == false) {
			// 横屏
			mAppFuncMainView.layout(left, top, right, bottom);
		} else {
			mAppFuncMainView.layout(left, top, right, bottom);
		}
	}

	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		switch (msgId) {
			case BG_SHOWED :
				mIsUpdateBgImg = true;
				mIsReMergeBg = true;
				return true;
			case BG_CHANGED : {
				mIsUpdateBgImg = true;
				mIsReMergeBg = true;
				return true;
			}
			case ICONEFFECT_CHANGED : {
				int type = (obj1 instanceof Integer) ? ((Integer) obj1).intValue() : 0;
				if (type == CoupleScreenEffector.EFFECTOR_TYPE_RANDOM_CUSTOM) {
					GoSettingControler controler = GOLauncherApp.getSettingControler();
					FunAppSetting setting = null;
					if (controler != null && (setting = controler.getFunAppSetting()) != null) {
						int[] effects = setting.getAppIconCustomRandomEffect();
						mAppFuncMainView.setCustomRandomEffects(effects);
					}
				}
				mAppFuncMainView.setGridEffector(type);
				mAppFuncMainView.mediaManagementPluginSettingChange(AppFuncMainView.GRID_EFFECTOR_CHANGE, obj1);
				return true;
			}
			case SCROLL_LOOP_CHANGED : {
				int value = (obj1 instanceof Integer) ? ((Integer) obj1).intValue() : 0;
				mAppFuncMainView.setCycleMode(value == 1);
				mAppFuncMainView.mediaManagementPluginSettingChange(AppFuncMainView.SCROLL_LOOP_CHANGE, obj1);
				return true;
			}
			case BLUR_BACKGROUND_CHANGED : {
				int value = (obj1 instanceof Integer) ? ((Integer) obj1).intValue() : 0;
				setBlurBackground(value == 1);
				return true;
			}
			case SHOW_TAB_ROW_CHANGED : {
				requestLayout();
				return true;
			}
			case VERTICAL_SCROLL_EFFECT_CHANGED : {
				requestLayout();
				mAppFuncMainView.mediaManagementPluginSettingChange(AppFuncMainView.VERTICAL_SCROLL_EFFECTOR_CHANGE, obj1);
				return true;
			}
			case SHOW_SEARCH : {
				requestLayout();
				return true;
			}
			default :
				return false;
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		mIsReMergeBg = true;
		dismissAllAppMenu();
		dismissProgressDialog();
		mStatusBarHeight = mUtils.getStatusBarHeight(); // 这样使得下次绘制时在onDraw重新计算矩阵

	}

	public void destroyComponentsDrawingCache() {
		int size = mStoreComponents.size();
		for (int i = 0; i < size; i++) {
			XComponent component = mStoreComponents.get(i);
			component.destroyDrawingCache();
		}
	}

	/**
	 * 根据背景颜色和桌面背景图创建功能表背景图
	 */
	private void createBgImgWithBgColor() {
		// Log.d("XViewFrame", "createBgImgWithBgColor");
		mIsDrawMergeBg = false;
		mMergeWidthDesktop = false;
		final int startY = mUtils.getStatusBarHeight();
		// 获取桌面壁纸
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.GET_BACKGROUND,
				IDiyFrameIds.APPFUNC_FRAME, null, null);
		if (mDesktopBg == null) {
			// 桌面使用的是动态壁纸
			return;
		}
		int valueWithMask = mBgColor & 0xFF000000;
		boolean hasAlpha = false;
		if (mOrigBg != null && mOrigBg.getBitmap().hasAlpha()) {
			hasAlpha = true;
		}
		// 当桌面颜色包含透明度或背景图也存在Alpha时才合成
		if (valueWithMask != 0xFF000000 && (hasAlpha || mOrigBg == null)) {
			if (mMergeBg == null) {
				if (initMergeBg() == false) {
					// 创建图片不成功则直接返回
					return;
				}
			}
			// 画桌面背景和功能表颜色
			mMergeBgCanvas.drawColor(Color.BLACK);
			// if (mUtils.isVertical() == false) {
			// mMergeBgCanvas.save();
			// mMergeBgCanvas.rotate(-90.0f);
			// mMergeBgCanvas.translate(-mUtils.getScreenWidth(), 0);
			// }
			if (isVertical() == false) {
				mMergeBgCanvas.save();
				mMergeBgCanvas.rotate(-90.0f);
				mMergeBgCanvas.translate(-mUtils.getScreenWidth(), 0);
			}
			GoLauncher.sendMessage(GoLauncher.getFrame(IDiyFrameIds.APPFUNC_FRAME),
					IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DRAW_BACKGROUND, 0, mMergeBgCanvas, null);

			// mDesktopBg.setBounds(0, 0, getMeasuredWidth(),
			// getMeasuredHeight());
			// mDesktopBg.draw(mMergeBgCanvas);

			mMergeWidthDesktop = true;
			if (mBlurBg) {
				// 把桌面壁纸模糊
				blurBackground(false);
			}
			mMergeBgCanvas.drawColor(mBgColor);

			// 画功能表背景
			if (mOrigBg != null && mOrigBg.getBitmap() != null
					&& mOrigBg.getBitmap().isRecycled() == false) {
				ImageUtil.drawStretchImage(mMergeBgCanvas, mOrigBg.getBitmap(), 0, 0,
						mUtils.getScreenWidth(), mUtils.getScreenHeight() - startY, null);
			}
			// if (mUtils.isVertical() == false) {
			// mMergeBgCanvas.restore();
			// }
			if (isVertical() == false) {
				mMergeBgCanvas.restore();
			}
			mIsDrawMergeBg = true;
		} else {
			if (mOrigBg != null) {

				// 合并功能表背景与颜色
				if (mMergeBg == null) {
					if (initMergeBg() == false) {
						// 创建图片不成功则直接返回
						return;
					}
				}
				if (mOrigBg.getBitmap() == null || mOrigBg.getBitmap().isRecycled() == true) {
					return;
				}
				if (isVertical() == false) {
					mMergeBgCanvas.save();
					mMergeBgCanvas.rotate(-90.0f);
					mMergeBgCanvas.translate(-mUtils.getScreenWidth(), 0);
				}
				if (mBlurBg) {
					mMergeBgCanvas.drawColor(Color.BLACK);
					ImageUtil.drawStretchImage(mMergeBgCanvas, mOrigBg.getBitmap(), 0, 0,
							mUtils.getScreenWidth(), mUtils.getScreenHeight() - startY, null);
					mMergeWidthDesktop = true;
					// 把壁纸模糊(功能表背景为非透明时的应用)
					blurBackground(true);
				} else {
					// mMergeBgCanvas.drawBitmap(mOrigBg.getBitmap(), 0, 0,
					// null);
					ImageUtil.drawStretchImage(mMergeBgCanvas, mOrigBg.getBitmap(), 0, 0,
							mUtils.getScreenWidth(), mUtils.getScreenHeight() - startY, null);
				}
				if (isVertical() == false) {
					mMergeBgCanvas.restore();
				}
				mIsDrawMergeBg = true;
			}
		}

	}

	/**
	 * 功能表背景图是否带透明度
	 * 
	 * @return
	 */
	public boolean isAlphaBg() {
		boolean hasAlpha = false;
		if (mOrigBg != null && mOrigBg.getBitmap().hasAlpha()) {
			hasAlpha = true;
		}
		if ((mBgColor & 0xFF000000) != 0xFF000000 && (hasAlpha || mOrigBg == null)) {
			return true;
		}
		return false;
	}

	private void setShader() {
		Bitmap bitmap = null;
		if (mIsDrawMergeBg && mMergeBg != null && mMergeBg.isRecycled() == false) {
			bitmap = mMergeBg;
		} else if (mOrigBg != null && mOrigBg.getBitmap() != null
				&& mOrigBg.getBitmap().isRecycled() == false) {
			bitmap = mOrigBg.getBitmap();
		}

		mFadePainter.recycle();
		if (bitmap != null) {
			mShader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
			computeBgMatrix(mStatusBarHeight, bitmap);
		} else {
			mShader = null;
		}
	}

	public BitmapShader getShader() {
		return mShader;
	}

	public void setDeskTopBg(Drawable bg) {
		mDesktopBg = bg;
	}

	private void validateBG() {
		// 回收以前的
		// Log.d("XViewFrame", "validateBG");

		mOrigBg = null;
		mBgColor = mThemeCtrl.getThemeBean().mWallpaperBean.mBackgroudColor;

		switch (AppFuncFrame.getDataHandler().getShowBg()) {
			case FunAppSetting.BG_NON : {
				// 显示默认颜色
				mBgColor = AppFuncConstants.DEFAULT_BG_COLOR;
				break;
			}
			case FunAppSetting.BG_DEFAULT : {
				if (mThemeCtrl.isDefaultTheme() == false) {
					try {
						mOrigBg = (BitmapDrawable) mThemeCtrl
								.getDrawable(mThemeCtrl.getThemeBean().mWallpaperBean.mImagePath);
					} catch (Exception e) {
						// 在某些主题使用了9切图作为背景，导致程序崩溃
					}
				}
				break;
			}
			case FunAppSetting.BG_GO_THEME :
			case FunAppSetting.BG_CUSTOM : {
				mOrigBg = AppFuncFrame.getDataHandler().getBg();
				if ((null == mOrigBg) || (mOrigBg.getBitmap() == null)
						|| (mOrigBg.getBitmap().isRecycled())) {
					// 如果设置了显示背景图却又没有选择图片，则显示默认颜色
					mBgColor = AppFuncConstants.DEFAULT_BG_COLOR;
				} else {
					// 如果有图片，则不使用颜色
					mBgColor = 0;
				}
				break;
			}
			default :
				break;
		}
	}

	public static void addQueue(MsgEntity entity) {
		sQueue.add(entity);
	}

	@Override
	public synchronized void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.START_REFRESH_GRID_LIST : {
				boolean open = (Boolean) (obj != null ? obj : false);
				if (open) {
					CharSequence title = mActivity.getResources().getText(
							R.string.dlg_promanageTitle);
					CharSequence msg = mActivity.getResources().getText(R.string.sd_card_app_load);
					mProgressDialog = ProgressDialog.show(mActivity, title, msg);
				} else {
					boolean shown = isShown();
					// LogUnit.i("---------------show the load sd card app dialog");
					if (shown) {
						// LogUnit.i("show the load sd card app dialog");
						CharSequence title = mActivity.getResources().getText(
								R.string.dlg_promanageTitle);
						CharSequence msg = mActivity.getResources().getText(
								R.string.sd_card_app_load);
						mProgressDialog = ProgressDialog.show(mActivity, title, msg);
					}
				}
			}
				break;
			case AppFuncConstants.FINISH_REFRESH_GRID_LIST : {
				if (mProgressDialog != null) {
					// LogUnit.i("close the load sd card app dialog");
					mProgressDialog.dismiss();
				}
			}
				break;
			case AppFuncConstants.THEME_CHANGE : {
				// Log.d("XViewFrame", "THEME_CHANGE");
				mIsUpdateBgImg = true;
				mIsReMergeBg = true;
				if ((mOrigBg != null) && (mOrigBg.getBitmap().isRecycled() == false)) {
					mOrigBg.getBitmap().recycle();
					mOrigBg = null;
				}
			}
				break;
			case AppFuncConstants.SHOWQUICKACTIONMENU : {
				if (mQuickActionMenu != null) {
					mQuickActionMenu.cancel();
				}
				ProManageIconInfo info = (ProManageIconInfo) obj;
				if (info != null && info.mTaskInfo != null) {
					mQuickActionMenu = new QuickActionMenu(mActivity, info.mTaskInfo, info.mRect,
							this, this);

					// mQuickActionMenu.addItem(IQuickActionId.CLOSE,
					// R.drawable.qa_close, R.string.closeicontext);
					mQuickActionMenu.addItem(IQuickActionId.INFO, R.drawable.info,
							R.string.infotext);
					if (info.mTaskInfo.isInWhiteList()) {
						mQuickActionMenu.addItem(IQuickActionId.UNLOCK, R.drawable.lock,
								R.string.unlock2text);
					} else {
						mQuickActionMenu.addItem(IQuickActionId.LOCK, R.drawable.unlock,
								R.string.lock2text);
					}
					// mQuickActionMenu.addItem(IQuickActionId.GOTO,
					// R.drawable.qa_launch, R.string.gototext);
					mQuickActionMenu.show();
				}
				break;
			}
			case AppFuncConstants.TUTORIAL_HIDE_APP_MODE : {
				// 因所有程序页面菜单已经调整，所以暂时隐藏新手指引画面。
				// SharedPreferences sharedPreferences =
				// mActivity.getSharedPreferences(IPreferencesIds.USERTUTORIALCONFIG,Context.MODE_PRIVATE);
				// Editor editor = sharedPreferences.edit();
				// editor.putBoolean(LauncherEnv.SHOULD_SHOW_APPFUNC_MENU_GUIDE,
				// true);
				// editor.commit();
				// GGMenu menu = onPrepareGGMenu();
				// if (null != menu)
				// {
				// menu.show(false);
				// showMenuGuideView();
				// }
			}
				break;
			default :
				break;
		}

	}

	public static void destroyInstance() {
		if (sInstance != null) {
			if (sInstance.mAllAppMenu != null) {
				sInstance.mAllAppMenu.recyle();
			}
			if (sInstance.mProManageMenu != null) {
				sInstance.mProManageMenu.recyle();
			}
		}
		sInstance = null;
	}

	public boolean getIsForceLayout() {
		return mIsForceLayout;
	}

	public void setIsForceLayout(boolean isForceLayout) {
		mIsForceLayout = isForceLayout;
	}

	public void setReMergeBg() {
		mIsReMergeBg = true;
		// 设置功能表是否为高质量绘图
		setHighQualityBackground(GoLauncher.isHighQualityDrawing());
	}

	public void setReMergeBg(boolean merge) {
		mIsReMergeBg = merge;
	}

	// private GGMenu createGGMenu(Context context, View parentView,
	// OnMenuItemSelectedListener listener, int rows, int columns,
	// TabData[] tabs, int textColor, int selecttabColor, int unselecttabColor,
	// Drawable background, Drawable itembackground, Drawable itemline,
	// Drawable unselecttabline, Drawable selecttabline,Drawable newMsg) {
	// GGMenu menu = new GGMenu(context, parentView, R.layout.ggmenu_default,
	// textColor,
	// selecttabColor, unselecttabColor, background, itembackground, itemline,
	// unselecttabline, selecttabline,newMsg);
	// menu.setmMenuItemSelectedListener(listener);
	// menu.setMenuData(tabs, columns, R.layout.ggmenu_default,
	// R.layout.ggmenu_item_default);
	// menu.setmDismissListener(this);
	//
	// return menu;
	//
	// }

	// private GGMenu createGGMenu(Context context, View parentView,
	// OnMenuItemSelectedListener listener, int rows, int columns,
	// TabData[] tabs, int textColor,Drawable itembackground,Drawable
	// itemline,Drawable newMsg)
	// {
	// return mAllProgramMenu;
	//
	// }
	public GGMenu onPrepareGGMenu() {
		GGMenu menu = null;

		int index = getSeletedTab();
		switch (index) {
			case AppFuncConstants.RECENTAPPS :
				break;

			default :
				break;
		}

		return menu;
	}

	public void onDismissGGMenu() {
		if (mMenuGuideView != null) {
			mWindowManager.removeView(mMenuGuideView);
			mMenuGuideView = null;
		}
		// if (null != mManageProgramMenu && mManageProgramMenu.isShowing()) {
		// mManageProgramMenu.dismiss();
		// }
	}

	public void removeMenuGuideView() {
		if (mMenuGuideView != null) {
			mWindowManager.removeView(mMenuGuideView);
			mMenuGuideView = null;
		}
	}

	public void recycleMergeBg() {
		if (mMergeBg != null) {
			mMergeBg.recycle();
			mMergeBg = null;
		}
	}

	/**
	 * 绘制图标网格的淡化边缘，其实是将背景渐变地绘制图标上
	 * 
	 * @param canvas
	 * @param rect
	 * @param dir
	 */
	public void drawFadeRect(Canvas canvas, Rect rect, int dir) {
		final BitmapShader shader = getShader();
		if (shader != null) {
			mFadePainter.drawFadeBitmap(canvas, rect, dir, shader);
		}
		// else if((mBgColor & 0xFF000000) != 0){
		// 桌面使用动态壁纸的时候，功能表只绘制半透明颜色，这时候再画淡化边缘会变慢吗？还没测试。
		// mFadePainter.drawFadeColor(canvas, rect, dir, mBgColor);
		// }
	}

	/**
	 * 绘制图标网格的渐变高亮边缘
	 * 
	 * @param canvas
	 * @param rect
	 * @param dir
	 * @param color
	 */
	public void drawHighlightRect(Canvas canvas, Rect rect, int dir, int color) {
		mFadePainter.drawFadeColor(canvas, rect, dir, color);
	}

	public void computeBgMatrix(int startY, Bitmap bitmap) {
		if (bitmap == null) {
			mShaderMatrix = null;
		} else {
			ImageUtil.computeStretchMatrix(mBgMatrix, bitmap.getWidth(), bitmap.getHeight(), 0,
					-startY, mUtils.getScreenWidth(), mUtils.getScreenHeight() - startY);
			mShaderMatrix = mBgMatrix;
		}
	}

	private void setBlurBackground(boolean blur) {
		if (mBlurBg != blur) {
			mBlurBg = blur;
			// 只有当与桌面合成背景时才执行
			if (mMergeWidthDesktop) {
				mIsUpdateBgImg = true;
				mIsReMergeBg = true;
			}
		}
	}

	public void setHighQualityBackground(boolean high) {
		if (mHighBgQuality != high) {
			mHighBgQuality = high;
			if (mMergeBg != null) {
				mMergeBg.recycle();
				mMergeBg = null;
			}
			// // 只要有合成背景就执行
			// if(mIsDrawMergeBg){
			// mIsUpdateBgImg = true;
			// mIsReMergeBg = true;
			// }
		}
	}

	@Override
	public void onActionClick(int action, Object target) {
		// TODO Auto-generated method stub
		mQuickActionMenu = null;
		if (target == null || !(target instanceof FunTaskItemInfo)) {
			return;
		}
		FunTaskItemInfo taskInfo = (FunTaskItemInfo) target;
		switch (action) {
			case IQuickActionId.CLOSE : {
				AppFuncFrame.getDataHandler().terminateApp(taskInfo.getPid());
				break;
			}
			case IQuickActionId.LOCK : {
				AppCore.getInstance().getTaskMgrControler()
						.addIgnoreAppItem(taskInfo.getAppItemInfo().mIntent);
				break;
			}
			case IQuickActionId.UNLOCK : {
				AppCore.getInstance().getTaskMgrControler()
						.delIgnoreAppItem(taskInfo.getAppItemInfo().mIntent);
				break;
			}
			case IQuickActionId.GOTO : {
				try {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.START_ACTIVITY, -1, taskInfo.getAppItemInfo().mIntent, null);
					ApplicationIcon.setStartApp(true);
				} catch (NullPointerException e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				break;
			}
			case IQuickActionId.INFO : {
				ApplicationIcon.setStartApp(true);
				DeliverMsgManager.getInstance().onChange(AppFuncConstants.PROCESS_GRID,
						AppFuncConstants.IS_GO_TO_INFO, null);
				AppCore.getInstance().getTaskMgrControler()
						.skipAppInfobyIntent(taskInfo.getAppItemInfo().mIntent);
				break;
			}
			default :
				break;
		}
	}

	public void drawBackground(Canvas canvas) {
		if (mIsUpdateBgImg) {
			// 重新获取背景图片
			validateBG();
			mIsUpdateBgImg = false;
		}
		if (mIsReMergeBg) {
			recycleMergeBg();
			// 合成背景图片
			createBgImgWithBgColor();

			// 初始化Shader
			setShader();
			mIsReMergeBg = false;
		} else {
			if (checkBg()) {
				recycleMergeBg();
				createBgImgWithBgColor();
				setShader();
			}
		}

		// 优先画合成图片
		if (mIsDrawMergeBg && mMergeBg != null && !mMergeBg.isRecycled()) {
			// if (mUtils.isVertical() == false) {
			// // canvas.save();
			// // canvas.rotate(90.0f);
			// // canvas.translate(0, -mUtils.getScreenWidth());
			// // canvas.drawBitmap(mMergeBg, 0, 0, null);
			// // canvas.restore();
			// canvas.drawBitmap(mMergeBg, mMergeBgMatrix, null);
			// mShaderMatrix = mMergeBgMatrix;
			// } else {
			// canvas.drawBitmap(mMergeBg, 0, 0, null);
			// mShaderMatrix = null;
			// }
			if (!GoLauncher.isPortait()) {
				// canvas.save();
				// canvas.rotate(90.0f);
				// canvas.translate(0, -mUtils.getScreenWidth());
				// canvas.drawBitmap(mMergeBg, 0, 0, null);
				// canvas.restore();
				canvas.drawBitmap(mMergeBg, mMergeBgMatrix, null);
				mShaderMatrix = mMergeBgMatrix;
			} else {
				canvas.drawBitmap(mMergeBg, 0, 0, null);
				mShaderMatrix = null;
			}
		} else {
			// 画功能表原生的背景图
			Bitmap bitmap = mOrigBg == null ? null : mOrigBg.getBitmap();
			if (bitmap != null && !bitmap.isRecycled()) {
				final int startY = mUtils.getStatusBarHeight();
				// ImageUtil.drawImage(canvas, mOrigBg, ImageUtil.STRETCHMODE,
				// 0,
				// -startY, mUtils.getScreenWidth(),
				// mUtils.getScreenHeight() - startY, null);
				if (mStatusBarHeight != startY || mShaderMatrix != mBgMatrix) {
					mStatusBarHeight = startY;
					computeBgMatrix(startY, bitmap);
				}
				canvas.drawBitmap(bitmap, mBgMatrix, null);
			}
			// 画功能表背景颜色(非全透时才绘制)
			if ((mBgColor & 0xFF000000) != 0) {
				canvas.drawColor(mBgColor);
			}
		}
		if (mShader != null) {
			mShader.setLocalMatrix(mShaderMatrix);
		}
	}

	void setEnableBg(boolean enabled) {
		mEnableBg = enabled;
	}

	/**
	 * 根据高宽判断横竖屏
	 * 
	 * @return true为竖屏，false为
	 */
	public boolean isVertical() {
		if (getMeasuredWidth() < getMeasuredHeight()) {
			return true;
		}
		return false;
	}

	public boolean isInputMethodTarget() {
		// InputMethodManager imm = InputMethodManager.peekInstance();
		InputMethodManager imm = (InputMethodManager) mActivity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		return imm != null && imm.isActive(this);
	}

	public void setInputType(int type) {
		// InputMethodManager imm = InputMethodManager.peekInstance();
		InputMethodManager imm = (InputMethodManager) mActivity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.restartInput(this);
		}
	}

	// 模糊背景
	private void blurBackground(boolean drawColor) {
		Bitmap bitmap = ImageFilter.convertToARGB8888(mMergeBg);
		if (bitmap != null) {
			boolean res = ImageFilter.trickyBlur(bitmap, 2, 4);
			if (res) {
				Canvas canvas = new Canvas(bitmap);
				if (bitmap != mMergeBg) {
					canvas.setBitmap(mMergeBg);
					canvas.drawBitmap(bitmap, 0, 0, null);
				}
			}
			if (bitmap != mMergeBg) {
				bitmap.recycle();
			}
		}
		if (drawColor) {
			final int color = mBgColor == 0 ? 0x56000000 : mBgColor;
			mMergeBgCanvas.drawColor(color);
		}
	}

	private void showMenuGuideView() {
		PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		boolean needStarMenuTutorial = sharedPreferences.getBoolean(
				IPreferencesIds.SHOULD_SHOW_APPFUNC_MENU_GUIDE, true);
		if (needStarMenuTutorial) {
			sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_APPFUNC_MENU_GUIDE, false);
			sharedPreferences.commit();
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,
					WindowManager.LayoutParams.TYPE_APPLICATION,
					WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
			mMenuGuideView = mActivity.getLayoutInflater().inflate(R.layout.appfuncmenuguide, null);
			mWindowManager.addView(mMenuGuideView, lp);
		}
	}

	public boolean isGGmenuTutorialShowing() {
		return mMenuGuideView != null;
	}

	// @Override
	// public void onDismiss()
	// {
	// if (null != mManageProgramMenu)
	// {
	// mManageProgramMenu.cleanup();
	// mManageProgramMenu = null;
	// }
	// }
	/**
	 * 获取当前屏幕截图
	 * 
	 * @return
	 */
	public Bitmap buildCache() {

		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		Bitmap mCache = null;
		if (mCache == null || mCache.getWidth() != width || mCache.getHeight() != height) {
			Bitmap.Config quality = Bitmap.Config.RGB_565;
			try {
				mCache = Bitmap.createBitmap(width, height, quality);
				if (mCache != null && getContext() != null) {
					mCache.setDensity(getContext().getResources().getDisplayMetrics().densityDpi);
				}
			} catch (OutOfMemoryError e) {
				mCache = null;
				return null;
			}

		}
		if (mCache != null) {
			Canvas canvas = new Canvas(mCache);
			final int restoreCount = canvas.save();
			onDraw(canvas);
			canvas.restoreToCount(restoreCount);
		}
		return mCache;
	}

	public void dismissAllAppMenu() {
		if (null != mAllAppMenu) {
			mAllAppMenu.dismiss();
		}
		if (null != mProManageMenu) {
			mProManageMenu.dismiss();
		}
		if (mDisplayedMenu != null) {
			mDisplayedMenu.dismiss();
		}
		if (mDisplayedDialog != null) {
			mDisplayedDialog.dismiss();
			mDisplayedDialog = null;
		}
		
	}

	private BaseMenu mDisplayedMenu;

	public void setDisplayedMenu(BaseMenu menu) {
		mDisplayedMenu = menu;
	}

	private Dialog mDisplayedDialog;

	public void setDisplayedDialog(Dialog dialog) {
		mDisplayedDialog = dialog;
	}

	public boolean isDrawMergeBg() {
		return mIsDrawMergeBg;
	}

	public void showProgressDialog() {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(AppFuncConstants.PROGRESSBAR_SHOW);
		}
	}

	public void dismissProgressDialog() {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(AppFuncConstants.PROGRESSBAR_HIDE);
		}
	}

	private boolean checkBg() {
		boolean reMergeBg = false;
		if (mMergeBg != null && !mMergeBg.isRecycled()) {
			if (mBgVertical && GoLauncher.isPortait()) {
				if (mMergeBg.getWidth() < mUtils.getScreenWidth()
						|| mMergeBg.getHeight() < mUtils.getScreenHeight()) {
					reMergeBg = true;
				}
			} else if (!mBgVertical && !GoLauncher.isPortait()) {
				if (mMergeBg.getWidth() < mUtils.getScreenHeight()
						|| mMergeBg.getHeight() < mUtils.getScreenWidth()) {
					reMergeBg = true;
				}
			} else {
				reMergeBg = true;
			}
		} else {
			reMergeBg = true;
		}
		return reMergeBg;
	}

	public void handleConfigChanged() {
		XBaseGrid curGrid = mAppFuncMainView.getCurrentContent().getXGrid();
		if (curGrid != null) {
			curGrid.fixScoller();
		}
	}
}

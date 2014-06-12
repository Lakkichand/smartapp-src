package com.jiubang.ggheart.apps.desks.diy.frames.cover;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.opengl.GLSurfaceView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.gau.go.launcherex.theme.cover.ui.MaskViewChrismas2;
import com.go.util.LoadDexUtil;
import com.go.util.device.Machine;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.cover.CoverMonitor.ICoverCallback;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;

/**
 * 桌面最上面的罩子层
 * 
 * @author jiangxuwen
 * 
 */
public class CoverFrame extends AbstractFrame {
	public static final String CHRISTMAS2_PACKAGE_NAME = "com.gau.go.launcherex.theme.christmas2";
	/**
	 * 主题类型
	 */
	public static final int VIEWTYPE_APP = 1;
	public static final int VIEWTYPE_THEME = 2;
	public static final int VIEWTYPE_APP_3D = 3;

	public static final String VERSION = "1.0";
	// for method
	public static final String METHOD_ON_CREATE = "onCreate";
	public static final String METHOD_ON_STOP = "onStop";
	public static final String METHOD_ON_PAUSE = "onPause";
	public static final String METHOD_ON_RESUME = "onResume";
	public static final String METHOD_ON_DESTROY = "onDestroy";
	public static final String METHOD_ON_WAKEUP = "onWakeUp";
	public static final String METHOD_ON_READ_VERSION = "onReadVersion";
	public static final String METHOD_ON_STATUSBAR_CHANGE = "onStatusBarChange";
    public static final String METHOD_DODRAW = "doDraw";
    
	public static final int COVER_VIEW_THEME = 0x1; // 主题2.0的view
	public static final int COVER_VIEW_MESSAGECENTER = 0x2; // 消息中心的view
	public static final int COVER_VIEW_WIDGET = 0x3; // 全屏widget的view（未来可能会用到）
	public static final int COVER_VIEW_EXTRA = 0x4; // 为了解决surfaceView引起下层View无法正常刷新而添加的额外view（不参与逻辑操作）
	public static final int COVER_VIEW_HOLIDAY = 0x5; // 节日版的view
	public static final int COVER_VIEW_SCREEN_GUIDE = 0x6; // 功能提示的view
	
	private int mViewType = VIEWTYPE_APP;
	private View mMainView; // 主界面，由外界传进来
	private View mForegroundView; // 前景界面，消息中心进行主题等推送的界面显示
	private int mPaddingTop; // TODO:拖拽图标或者widget时要做出UI调整
	private int mPaddingLeft;

	private CoverMonitor mMonitor;
	private CoverFrameControl mControl;

	private static final String LOG_TAG = "CoverFrame";
	private static final String MASK_MATCH_CODE = "Hello_this_is_CoverFrame_welcome_you";
	private String mLastPackageName = "lastPackageName";

	private LoadDexUtil mLoadDexUtil = null;
	
	public CoverFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);
		mFrameManager.registKey(this);
		init();
		// mFrameManager.registDispatchEvent(this);
	}

	private void resetLastPackageName() {
		mLastPackageName = "lastPackageName";
	}

	private void init() {
		mMonitor = new CoverMonitor(mActivity);
		mControl = new CoverFrameControl();
	}

	public void registerCoverCallback(ICoverCallback callback) {
		if (mMonitor != null) {
			mMonitor.registerCoverCallback(callback);
		}
	}

	public void setContainer(ViewGroup parent) {
		mControl.setContainer(parent);
	}

	@Override
	public View getContentView() {
		return null;
	}

	public boolean addCoverView(View view, int viewId) {
		boolean ret = false;
		int visibility = View.VISIBLE;
		AbstractFrame appFuncFrame = GoLauncher
				.getFrame(IDiyFrameIds.APPFUNC_FRAME);
		if (appFuncFrame != null
				&& appFuncFrame.getVisibility() == View.VISIBLE) {
			visibility = View.INVISIBLE;
		}
		if (view != null && viewId != COVER_VIEW_EXTRA) {
			view.setVisibility(visibility);
		}
		CoverView coverView = new CoverView(view, viewId);
		// 添加到罩子层管理器
		ret = mControl.addCoverView(coverView, visibility);
		return ret;
	}

	public void handleMessages(int msgId, int viewID, Object object) {
		switch (msgId) {
		case IDiyMsgIds.COVER_FRAME_ADD_VIEW:
			switch (viewID) {
			case COVER_VIEW_THEME:
				if (object != null && object instanceof String) {
					String packName = (String) object;
					if (!mLastPackageName.equals(packName)) {
						mControl.removeCoverView(COVER_VIEW_HOLIDAY);
						mLastPackageName = packName;
						createAppView(packName);
					}
				}
				break;

			case COVER_VIEW_MESSAGECENTER:
				if (object != null && object instanceof View) {
					addCoverView((View) object, COVER_VIEW_MESSAGECENTER);
				}
				break;
				
			case COVER_VIEW_SCREEN_GUIDE:
				if (object != null && object instanceof View) {
					addCoverView((View) object, COVER_VIEW_SCREEN_GUIDE);
				}
				break;	
				
			case COVER_VIEW_HOLIDAY:
			{
				CoverView oldView = mControl.getCoverView(COVER_VIEW_HOLIDAY);
				// 如果节日版的罩子层已经存在，则不再进行加载，直接返回
				if (oldView != null) {
					return;
				}
				// 预防内存不足出现OOM问题，所以try-catch
				try {
//					View holidayView = new MaskViewChristmas(mActivity);
//					// 添加节日版的view
//					if (holidayView != null) {
//						mControl.hideCoverView(COVER_VIEW_THEME);
//						if (holidayView instanceof SurfaceView) {
//							// 如果为GLSurfaceView，则为3d主题
//							if (holidayView instanceof GLSurfaceView) {
//								mViewType = VIEWTYPE_APP_3D;
//							}
//							((SurfaceView) holidayView).setZOrderOnTop(true);
//						}
//						addCoverView(holidayView, COVER_VIEW_HOLIDAY);
//						View view = new View(mActivity);
//						addCoverView(view, COVER_VIEW_EXTRA);
//					} // end if holidayView
				} catch (Throwable e) {
				}
				
			}
			    break;
			    
			default:
				break;
			}
			break;

		case IDiyMsgIds.COVER_FRAME_REMOVE_VIEW:
			mControl.removeCoverView(viewID);
			if (viewID == COVER_VIEW_THEME) {
				resetLastPackageName();
				doThemeViewMethod(METHOD_ON_DESTROY, null);
			} else if (viewID == COVER_VIEW_HOLIDAY) {
				if (object != null && object instanceof Boolean) {
					if (!(Boolean) object) {
						// 当罩子层元素为0时，移除整个罩子层
						if (mControl.getCoverSize() <= 0) {
							mMonitor.handleRemoveCover();
						}
						return ; // 不需要唤醒罩子层，直接返回
					}
				} // end if object
				mControl.showCoverView(COVER_VIEW_THEME);
				doThemeViewMethod(METHOD_ON_RESUME, null);
			}
			// 当罩子层元素为0时，移除整个罩子层
			if (mControl.getCoverSize() <= 0) {
				mMonitor.handleRemoveCover();
			}
			break;

		case IDiyMsgIds.COVER_FRAME_SHOW_ALL:
			mControl.showAllCoverViews();
			doThemeViewMethod(METHOD_ON_RESUME, null);
			break;

		case IDiyMsgIds.COVER_FRAME_HIDE_ALL:
			// 不能立刻隐藏全部，主题界面需要进行渐变动画
			// mControl.hideAllCoverViews();
			mControl.hideCoverView(COVER_VIEW_MESSAGECENTER);
			mControl.hideCoverView(COVER_VIEW_SCREEN_GUIDE);
			doThemeViewMethod(METHOD_ON_STOP, null);
			// resetLastPackageName();
			break;

		case IDiyMsgIds.COVER_FRAME_REMOVE_ALL:
			mControl.removeAllCoverViews();
			doThemeViewMethod(METHOD_ON_STOP, null);
			resetLastPackageName();
			mMonitor.handleRemoveCover();
			break;

		default:
			break;
		}
	} // end handleMessages

	/**
	 * 只能在UI线程调用
	 * 
	 * @param packName
	 * @return
	 */
	public boolean createAppView(String packName) {
		try {
			View mainView = null;
			Context remoteContext = mActivity.createPackageContext(packName,
					Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			if (packName.equals(CHRISTMAS2_PACKAGE_NAME)) {
				mainView = createSuperThemeChristmas(remoteContext);
			} else {
				final ThemeInfoBean themeInfoBean = ThemeManager.getInstance(mActivity).getCurThemeInfoBean();
				final String[] classDexNames = themeInfoBean.getClassDexNames();
				// 收费主题的处理
				if (Machine.IS_ICS && classDexNames != null && classDexNames.length > 0) {
					Resources resources = remoteContext.getResources();
					final int length = classDexNames.length;
					int[] dexIds = new int[length];
					for (int i = 0; i < length; i ++) {
						dexIds[i] = resources.getIdentifier(classDexNames[i], "raw", packName);
					}
					final int versionCode = themeInfoBean.getVerId();
					final String viewPath = themeInfoBean.getMaskViewPath();
					mLoadDexUtil = LoadDexUtil.getInstance(mActivity);
					if (mLoadDexUtil != null) {
						mainView = mLoadDexUtil.createDexAppView(packName, dexIds, versionCode, viewPath);
					}
				} 
				if (mainView == null) { // 免费主题的处理
					LayoutInflater inflater = LayoutInflater.from(remoteContext);
					Resources resources = remoteContext.getResources();
					final int resourceId = resources.getIdentifier("cover_root_view", "layout",
							packName);
					mainView = inflater.inflate(resourceId, mFrameManager.getRootView(), false);
				}
			}
			if (mainView != null && mainView instanceof SurfaceView) {
				// 如果为GLSurfaceView，则为3d主题
				if (mainView instanceof GLSurfaceView) {
					mViewType = VIEWTYPE_APP_3D;
				}
				((SurfaceView) mainView).setZOrderOnTop(true);
				// 添加到罩子层管理器
				addCoverView(mainView, COVER_VIEW_THEME);
				View view = new View(mActivity);
				addCoverView(view, COVER_VIEW_EXTRA);
			}
		} catch (OutOfMemoryError e) {
			// e.printStackTrace();
			return false;
		} catch (Exception e) {
			// e.printStackTrace();
			return false;
		}
		return true;
	}

	private View createSuperThemeChristmas(Context context) {
		View christmasView = null;
		try {
			christmasView = new MaskViewChrismas2(context);
		} catch (Throwable e) {
		}
		return christmasView;
	}
	
	@Override
	public void onVisiable(int visibility) {
		super.onVisiable(visibility);
		if (visibility == View.VISIBLE) {
			mFrameManager.registKey(this);
		} else {
			mFrameManager.unRegistKey(this);
		}
	}

	public void cleanup() {
		onDestroy();
		resetLastPackageName();
		mFrameManager.unRegistKey(this);
		mMonitor.cleanup();
		mControl.cleanup();
		if (mLoadDexUtil != null) {
			mLoadDexUtil.cleanUp();
		}

	}

	/**
	 * 反射回去rootView的方法
	 * 
	 * @param methodName
	 * @return
	 */
	private void doThemeViewMethod(String methodName, Object value,
			Class... params) {
//		if (reflectFilter(methodName, value)) {
//			return ;
//		}
		Method method = null;
		CoverView coverView = mControl.getCoverView(COVER_VIEW_THEME);
		if (coverView != null && coverView.getView() != null) {
			View themeView = coverView.getView();
			try {
				Class tempClass = themeView.getClass();
				method = tempClass.getMethod(methodName, params);
				if (value != null) {
					method.invoke(themeView, value);
				} else {
					method.invoke(themeView);
				}
			} catch (Exception e) {
			}
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return mControl.dispatchTouchEvent(event);
	}

	public void onPause() {
		doThemeViewMethod(METHOD_ON_PAUSE, null);
	}

	public void onCreate() {
		doThemeViewMethod(METHOD_ON_CREATE, null);
	}

	public void onResume() {
		doThemeViewMethod(METHOD_ON_RESUME, null);
	}

	public void onStop() {
		doThemeViewMethod(METHOD_ON_STOP, null);
	}

	public void onDestroy() {
		doThemeViewMethod(METHOD_ON_DESTROY, null);
	}

	public void onWakeUp() {
		doThemeViewMethod(METHOD_ON_WAKEUP, MASK_MATCH_CODE, Object.class);
	}

	public void onStatusBarChange(int height) {
		doThemeViewMethod(METHOD_ON_STATUSBAR_CHANGE, height, Integer.TYPE);
	}

	public void onReadVersion() {
		doThemeViewMethod(METHOD_ON_READ_VERSION, VERSION, Integer.TYPE);
	}

	public void hideMaskView() {
		if (mControl != null) {
			mControl.hideCoverView(COVER_VIEW_THEME);
		}
	}

	public void showMaskView() {
		onWakeUp();
	}

	public View getCoverView(int viewId) {
		return mControl.getCoverView(viewId).getView();
	}
	// public void hideMaskView() {
	// onWakeUp();
	// }

	/**
	 * 罩子层内显示的view
	 * 
	 * @author jiangxuwen
	 * 
	 */
	public class CoverView {
		private View mContentView;
		private int mViewId;

		public CoverView(View view, int id) {
			mContentView = view;
			mViewId = id;
		}

		public int getId() {
			return mViewId;
		}

		public View getView() {
			return mContentView;
		}
	}
	
	/**
	 * 过滤节日版的罩子层
	 * @param srcView
	 * @return
	 */
	private boolean reflectFilter(String methodName, Object value) {
		CoverView holidayView = mControl.getCoverView(COVER_VIEW_HOLIDAY);
		if (holidayView != null && holidayView.getView() != null) {
			if (holidayView.getView().getVisibility() == View.VISIBLE) {
				// 进行节日版的操作
//				((MaskViewChristmas) holidayView.getView()).doMethod(methodName, value);
				return true;
			}
		}
		return false;
	} // end reflectFilter
	
	/**
	 * 绘制当前所有的surfaceView到指定画布
	 * @param canvas
	 */
	public void drawSurfaceView(Canvas canvas) {
		if (mControl != null) {
			CoverView coverView = mControl.getCoverView(CoverFrame.COVER_VIEW_THEME);
			doSurfaceDraw(coverView, canvas);
//			coverView = mControl.getCoverView(CoverFrame.COVER_VIEW_HOLIDAY);
//			if (coverView != null && coverView.getView() != null) {
//				View view = coverView.getView();
//				if (view instanceof MaskViewChristmas) {
//					((MaskViewChristmas) view).doDraw(canvas);
//				}
//			} // end coverView
		}
	}

	private void doSurfaceDraw(CoverView coverView, Canvas canvas) {
		if (coverView == null || coverView.getView() == null) {
			return;
		}
		View view = coverView.getView();
		if (view.getVisibility() != View.VISIBLE) {
			return;
		}
		Class tempClass = view.getClass();
		try {
			Method method = tempClass.getMethod(METHOD_DODRAW, Canvas.class);
			try {
				method.invoke(view, canvas);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	
}

package com.jiubang.ggheart.apps.desks.appfunc.help;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.core.mars.MImage;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;

/**
 * 功能表模块工具类
 * @author yangguanxiang
 *
 */
public class AppFuncUtils {
	public static final int KILL_ICON_COPY = -1;
	private Context mContext;
	private DisplayMetrics mMetrics;
	private PicManager mPicManager;
	/**
	 * 横竖屏时对应的方向键
	 */
	private int[] mKeyIndex;
	/**
	 * 底部竖屏时的背景图片
	 */
	private MImage mBottomBg_v;
	/**
	 * 底部横屏时的背景图片
	 */
	private MImage mBottomBg_h;
	/**
	 * 竖屏时移动到桌面控件的背景
	 */
	private MImage mMoveToDesk_v;
	/**
	 * 横屏时移动到桌面控件的背景
	 */
	private MImage mMoveToDesk_h;
	/**
	 * 存放与主题无关的资源Id
	 */
	private HashMap<Integer, Object> mPublicResources;

	private BitmapDrawable mKillImg;
	private BitmapDrawable mEditImg;
	private BitmapDrawable mKillLightIcon;
	private BitmapDrawable mEditLightIcon;
	private BitmapDrawable mCloseImg;
	private BitmapDrawable mCloseLightIcon;
	private Vibrator mVibrator;

	private AppFuncThemeController mThemeController;

	private static AppFuncUtils sInstance;

	private Object mLock = new Object();
	private Thread mUpdateGridSettingThread;

	private int mMediaPluginCompatible = -1;
	private Handler mHandler;

	public static AppFuncUtils getInstance(Context context) {
		if (context == null) {
			context = GOLauncherApp.getContext();
		}
		if (sInstance == null) {
			sInstance = new AppFuncUtils(context);
		}
		return sInstance;
	}

	public static void destroyInstance() {
		if (sInstance != null) {
			sInstance.unregisterDispenseMsgHandler();
			sInstance = null;
		}
	}

	protected AppFuncUtils(Context context) {
		mContext = context;
		mMetrics = mContext.getResources().getDisplayMetrics();
		mPicManager = new PicManager();
		setKeyCode();
		initPublicResources();
		mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
//		mThemeController = AppFuncFrame.getThemeController();
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.THEME_CHANGE,
				new IMsgHandler() {

					@Override
					public void notify(int key, Object obj) {
						mKillImg = null;
						mEditImg = null;
						mKillLightIcon = null;
						mEditLightIcon = null;
						mCloseImg = null;
						mCloseLightIcon = null;
					}
				});
		initHandler();
	}

	private void unregisterDispenseMsgHandler() {
		DeliverMsgManager.getInstance().unRegisterDispenseMsgHandler(AppFuncConstants.THEME_CHANGE);
	}

	/**
	 * 初始化公共资源数组(与主题资源无关)
	 */
	private void initPublicResources() {
		mPublicResources = new HashMap<Integer, Object>();
		// TODO: 删除符号主题可配，从里面去掉
		// publicResources.put(R.drawable.kill, null);
		// publicResources.put(R.drawable.kill_light, null);
		// 横向超过十屏时的滚动条
		mPublicResources.put(R.drawable.scrollh, null);
		// 竖向滚动条
		mPublicResources.put(R.drawable.scrollv, null);
		// 锁
		mPublicResources.put(R.drawable.minus, null);
		// 新安装的程序提示图标
		// publicResources.put(R.drawable.new_install_app, null);
		// publicResources.put(R.drawable.promanage_lock_icon, null);
		// 正在运行tab编辑状态，程序右上角关闭图标
		// publicResources.put(R.drawable.promanage_close_normal, null);
		// publicResources.put(R.drawable.promanage_close_light, null);
		// 编辑文件夹
		// publicResources.put(R.drawable.eidt_folder, null);
		// publicResources.put(R.drawable.eidt_folder_light, null);
	}

	/**
	 * 将BitmapDrawable缩放为MImage
	 * 
	 * @param bitmap
	 * @param width
	 * @param height
	 * @return
	 */
	public MImage convertImage(BitmapDrawable bitmap, int width, int height) {
		MImage origImage = new MImage(bitmap);
		if ((origImage.getWidth() != width) || (origImage.getHeight() != height)) {
			origImage.setScale(width, height);
		}
		return origImage;
	}

	/**
	 * 横竖屏
	 * 
	 * @return
	 */
	public boolean isVertical() {
		if (mMetrics.widthPixels <= mMetrics.heightPixels) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 屏幕密度
	 * 
	 * @return
	 */
	public float getDensity() {
		return mMetrics.density;
	}

	/**
	 * 获得屏幕较短的边的长度
	 * 
	 * @return
	 */
	public int getSmallerBound() {
		return (mMetrics.widthPixels <= mMetrics.heightPixels)
				? mMetrics.widthPixels
				: mMetrics.heightPixels;
	}

	/**
	 * 获得屏幕较长的边的长度
	 * 
	 * @return
	 */
	public int getLongerBound() {
		return (mMetrics.widthPixels > mMetrics.heightPixels)
				? mMetrics.widthPixels
				: mMetrics.heightPixels;
	}

	/**
	 * 获得屏幕宽度
	 * 
	 * @return
	 */
	public int getScreenWidth() {
		if (Machine.isTablet(mContext)) {
			return DrawUtils.getTabletScreenWidth(mContext);
		}
		return mMetrics.widthPixels;
	}

	/**
	 * 获得当横屏时屏幕可显示区域的高度
	 * 
	 * @return
	 */
	public int getScreenHeightForHorizon() {
		// // Get the height of the status bar
		// if (WindowControl.getIsFullScreen(mActivity) == false) {
		// return getScreenHeight() - getStatusBarHeight();
		// } else {
		// return getScreenHeight();
		// }
		return getScreenHeight() - getStatusBarHeight();
	}

	public int getStatusBarHeight() {
		// mActivity.getWindow().getDecorView()
		// .getWindowVisibleDisplayFrame(mRect);
		// return Math.max(mRect.top, 0);
		return GoLauncher.getStatusbarHeight();
	}

	/**
	 * 获得屏幕高度
	 * 
	 * @return
	 */
	public int getScreenHeight() {
		if (Machine.isTablet(mContext)) {
			return DrawUtils.getTabletScreenHeight(mContext);
		}
		return mMetrics.heightPixels;
	}

	/**
	 * 根据屏幕方向设定Tab栏方向键
	 */
	public void setKeyCode() {
		if (mKeyIndex == null) {
			mKeyIndex = new int[4];
		}
		if (isVertical()) {
			mKeyIndex[0] = KeyEvent.KEYCODE_DPAD_UP;
			mKeyIndex[1] = KeyEvent.KEYCODE_DPAD_DOWN;
			mKeyIndex[2] = KeyEvent.KEYCODE_DPAD_LEFT;
			mKeyIndex[3] = KeyEvent.KEYCODE_DPAD_RIGHT;
		} else {
			mKeyIndex[0] = KeyEvent.KEYCODE_DPAD_LEFT;
			mKeyIndex[1] = KeyEvent.KEYCODE_DPAD_RIGHT;
			mKeyIndex[2] = KeyEvent.KEYCODE_DPAD_DOWN;
			mKeyIndex[3] = KeyEvent.KEYCODE_DPAD_UP;
		}
	}

	/**
	 * 根据屏幕方向返回对应的方向键
	 * 
	 * @return
	 */
	public int[] getKeyCode() {
		return mKeyIndex;
	}

	/**
	 * 获取卸载图标的副本
	 * 
	 * @return
	 */
	public BitmapDrawable getKillIconCopy() {
		try {
			if (mKillImg == null) {
				// BitmapDrawable origImg = (BitmapDrawable) mActivity
				// .getResources().getDrawable(R.drawable.kill);
				if (mThemeController == null) {
					mThemeController = AppFuncFrame.getThemeController();
				}
				BitmapDrawable origImg = (BitmapDrawable) mThemeController
						.getDrawable(mThemeController.getThemeBean().mAppIconBean.mDeletApp);
				if (origImg != null) {
					Bitmap copy = origImg.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
					if (copy != null) {
						mKillImg = new BitmapDrawable(mContext.getResources(), copy);
					}
				}
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mKillImg;
	}

	/**
	 * 获取关闭图标的副本
	 * 
	 * @return
	 */
	public BitmapDrawable getColseIconCopy() {
		try {
			if (mCloseImg == null) {
				if (mThemeController == null) {
					mThemeController = AppFuncFrame.getThemeController();
				}
				BitmapDrawable origImg = (BitmapDrawable) mThemeController
						.getDrawable(mThemeController.getThemeBean().mAppIconBean.mKillApp);
				if (origImg != null) {
					Bitmap copy = origImg.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
					if (copy != null) {
						mCloseImg = new BitmapDrawable(mContext.getResources(), copy);
					}
				}
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mCloseImg;
	}

	/**
	 * 获取卸载高光图标的副本
	 * 
	 * @return
	 */
	public BitmapDrawable getKillLightIconCopy() {
		try {
			if (mKillLightIcon == null) {
				// BitmapDrawable origImg = (BitmapDrawable) mActivity
				// .getResources().getDrawable(
				// R.drawable.kill_light);
				if (mThemeController == null) {
					mThemeController = AppFuncFrame.getThemeController();
				}
				BitmapDrawable origImg = (BitmapDrawable) mThemeController
						.getDrawable(mThemeController.getThemeBean().mAppIconBean.mDeletHighlightApp);
				if (origImg != null) {
					Bitmap copy = origImg.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
					if (copy != null) {
						mKillLightIcon = new BitmapDrawable(mContext.getResources(), copy);
					}
				}
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mKillLightIcon;
	}

	/**
	 * 获取关闭高光图标的副本
	 * 
	 * @return
	 */
	public BitmapDrawable getColseLightIconCopy() {
		try {
			if (mCloseLightIcon == null) {
				if (mThemeController == null) {
					mThemeController = AppFuncFrame.getThemeController();
				}
				BitmapDrawable origImg = (BitmapDrawable) mThemeController
						.getDrawable(mThemeController.getThemeBean().mAppIconBean.mKillAppLight);
				if (origImg != null) {
					Bitmap copy = origImg.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
					if (copy != null) {
						mCloseLightIcon = new BitmapDrawable(mContext.getResources(), copy);
					}
				}
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mCloseLightIcon;
	}

	/**
	 * 获取编辑文件夹图标的副本
	 * 
	 * @return
	 */
	public BitmapDrawable getEditIconCopy() {
		try {
			if (mEditImg == null) {
				// BitmapDrawable origImg = (BitmapDrawable) mActivity
				// .getResources().getDrawable(
				// R.drawable.eidt_folder);
				if (mThemeController == null) {
					mThemeController = AppFuncFrame.getThemeController();
				}
				BitmapDrawable origImg = (BitmapDrawable) mThemeController
						.getDrawable(mThemeController.getThemeBean().mAppIconBean.mEditFolder);
				if (origImg != null) {
					Bitmap copy = origImg.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
					if (copy != null) {
						mEditImg = new BitmapDrawable(mContext.getResources(), copy);
					}
				}
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mEditImg;
	}

	/**
	 * 获取编辑文件夹高光图标的副本
	 * 
	 * @return
	 */
	public BitmapDrawable getEditLightIconCopy() {
		try {
			if (mEditLightIcon == null) {
				// BitmapDrawable origImg = (BitmapDrawable) mActivity
				// .getResources().getDrawable(
				// R.drawable.eidt_folder_light);
				if (mThemeController == null) {
					mThemeController = AppFuncFrame.getThemeController();
				}
				BitmapDrawable origImg = (BitmapDrawable) mThemeController
						.getDrawable(mThemeController.getThemeBean().mAppIconBean.mEditHighlightFolder);
				if (origImg != null) {
					Bitmap copy = origImg.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
					if (copy != null) {
						mEditLightIcon = new BitmapDrawable(mContext.getResources(), copy);
					}
				}
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mEditLightIcon;
	}

	/**
	 * 从主题资源管理器查询并获取图片
	 * 
	 * @param drawableId
	 * @return
	 */
	public Drawable getDrawableFromPicManager(int drawableId) {
		return mPicManager.getDrawable(drawableId);
	}

	/**
	 * 获得格式为Drawable的图片
	 * 
	 * @param drawableId
	 * @param addToHashMap
	 * @return
	 */
	public Drawable getDrawable(int drawableId, boolean addToHashMap) {
		Drawable img = null;
		try {
			if (mPublicResources.containsKey(drawableId)) {
				// 与主题无关的资源不保存在主题资源管理器中
				img = getDrawableFromMainPkg(drawableId);
			} else {
				img = mPicManager.getViewDrawable(drawableId, addToHashMap);
			}
		} catch (OutOfMemoryError e) {
			Log.d("XViewFrame", "Hoops! Out of Memeory");
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		}
		return img;
	}

	/**
	 * 获得格式为Drawable的图片
	 * 
	 * @param drawableId
	 * @return
	 */
	public Drawable getDrawable(int drawableId) {
		Drawable img = null;
		try {
			if (mPublicResources.containsKey(drawableId)) {
				// 与主题无关的资源不保存在主题资源管理器中
				img = getDrawableFromMainPkg(drawableId);
			} else {
				img = mPicManager.getViewDrawable(drawableId);
			}
		} catch (OutOfMemoryError e) {
			Log.d("XViewFrame", "Hoops! Out of Memeory");
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		}
		return img;
	}

	// 从主程序包中取图片
	// public Drawable getDrawableFromMainPkg(int id){
	// return mActivity.getResources().getDrawable(id);
	// }

	public Drawable getDrawableFromMainPkg(int id) {
		Drawable drA = null;
		try {
			drA = ImageExplorer.getInstance(mContext).getDefaultDrawable(id);
			if (null == drA) {
				drA = mContext.getResources().getDrawable(id);
			}
		} catch (NotFoundException e) {
			Log.e("NotFoundException", Integer.toHexString(id));
		}
		return drA;
	}

	/**
	 * 将图片资源加入资源管理器
	 * 
	 * @param pic
	 */
	public void addToPicManager(int drawableId, Drawable drawable) {
		mPicManager.putViewDrawable(drawableId, drawable);
	}

	/**
	 * 获得功能表底部背景图片: 底层为BitmapDrawable格式
	 */
	public MImage getBottomBg() {
		if (isVertical()) {
			if (mBottomBg_v == null) {
				try {
					mBottomBg_v = generateMImage(R.drawable.shorcut_slaver, getScreenWidth(),
							getRealNum(85), 0);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}
			return mBottomBg_v;
		} else {
			if (mBottomBg_h == null) {
				try {
					mBottomBg_h = generateMImage(R.drawable.shorcut_slaver, getScreenHeight(),
							getRealNum(85), -90.0f);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}
			return mBottomBg_h;
		}
	}

	/**
	 * 获得功能表底部背景图片：当拖动图标至桌面时的背景效果图
	 */
	public MImage getMoveToDeskBg() {
		if (isVertical()) {
			if (mMoveToDesk_v == null) {
				try {
					mMoveToDesk_v = generateMImage(R.drawable.appfunc_movetodesk, getScreenWidth(),
							getRealNum(85), 0);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}
			return mMoveToDesk_v;
		} else {
			if (mMoveToDesk_h == null) {
				try {
					mMoveToDesk_h = generateMImage(R.drawable.appfunc_movetodesk,
							getScreenHeight(), getRealNum(85), -90.0f);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}
			return mMoveToDesk_h;
		}
	}

	/**
	 * 根据Drawable Id生成对应的MImage
	 * 
	 * @param drawableId
	 * @param width
	 * @param height
	 * @param angle
	 * @return
	 */
	private MImage generateMImage(int drawableId, int width, int height, float angle) {
		BitmapDrawable drawable = (BitmapDrawable) mPicManager.getViewDrawable(drawableId);
		MImage img = new MImage(drawable);
		scaleImage(img, width, height);
		img.setRotation(angle);
		return img.getDrawingCache();
	}

	/**
	 * 根据行列数设置Grid的规格 当规格发生改变时返回true
	 */
	public boolean setGridStandard(int standard, XBaseGrid grid, long gridId) {
		try {
			final boolean isVertical = isVertical();
			int row = 0;
			int column = 0;
			int smallerBound = getSmallerBound();
			FunAppSetting setting = GOLauncherApp.getSettingControler().getFunAppSetting();
			switch (standard) {
				case FunAppSetting.LINECOLUMNNUMXY_SPARSE : {
					if (isVertical) {
						if (smallerBound <= 240) {
							row += 3;
							column += 4;
						} else {
							row += 4;
							column += 4;
						}
						if (null != grid) {
							if ((grid.getColunmNums() != column) || (grid.getRowNums() != row)) {
								grid.setColunmNums(column);
								grid.setRowNums(row);
								setRCNum(column, row, gridId);
								return true;
							}
						}
					} else {
						if (smallerBound <= 240) {
							row += 3;
							column += 4;
						} else {
							row += 3;
							column += 5;
						}
						if (null != grid) {
							if ((grid.getColunmNums() != row) || (grid.getRowNums() != column)) {
								grid.setColunmNums(row);
								grid.setRowNums(column);
								setRCNum(column, row, gridId);
								return true;
							}
						}
					}
					setRCNum(column, row, gridId);
					break;
				}
				case FunAppSetting.LINECOLUMNNUMXY_MIDDLE : {
					if (isVertical) {
						if (smallerBound <= 240) {
							if (gridId == AppFuncConstants.ALLAPPS_GRID) {
								row += 4;
							} else {
								row += 3;
							}
							column += 4;
						} else {
							row += 4;
							column += 5;
						}
						if (null != grid) {
							if ((grid.getColunmNums() != column) || (grid.getRowNums() != row)) {
								grid.setColunmNums(column);
								grid.setRowNums(row);
								setRCNum(column, row, gridId);
								return true;
							}
						}
					} else {
						row += 3;
						if (smallerBound <= 240) {
							if (gridId == AppFuncConstants.ALLAPPS_GRID) {
								column += 5;
							} else {
								column += 4;
							}
						} else {
							if (gridId == AppFuncConstants.ALLAPPS_GRID) {
								column += 6;
							} else {
								column += 5;
							}
						}
						if (null != grid) {
							if ((grid.getColunmNums() != row) || (grid.getRowNums() != column)) {
								grid.setColunmNums(row);
								grid.setRowNums(column);
								setRCNum(column, row, gridId);
								return true;
							}
						}
					}
					setRCNum(column, row, gridId);
					break;
				}
				case FunAppSetting.LINECOLUMNNUMXY_MIDDLE_2 : {
					if (isVertical) {
						if (smallerBound <= 240) {
							if (gridId == AppFuncConstants.ALLAPPS_GRID) {
								row += 4;
							} else {
								row += 3;
							}
							column += 4;
						} else {
							row += 5;
							column += 4;
						}
						if (null != grid) {
							if ((grid.getColunmNums() != column) || (grid.getRowNums() != row)) {
								grid.setColunmNums(column);
								grid.setRowNums(row);
								setRCNum(column, row, gridId);
								return true;
							}
						}
					} else {
						row += 3;
						if (smallerBound <= 240) {
							if (gridId == AppFuncConstants.ALLAPPS_GRID) {
								column += 5;
							} else {
								column += 4;
							}
						} else {
							if (gridId == AppFuncConstants.ALLAPPS_GRID) {
								column += 6;
							} else {
								column += 5;
							}
						}
						if (null != grid) {
							if ((grid.getColunmNums() != row) || (grid.getRowNums() != column)) {
								grid.setColunmNums(row);
								grid.setRowNums(column);
								setRCNum(column, row, gridId);
								return true;
							}
						}
					}
					setRCNum(column, row, gridId);
					break;
				}
				case FunAppSetting.LINECOLUMNNUMXY_THICK : {
					if (isVertical) {
						if (smallerBound <= 240) {
							column += 5;
							if (gridId == AppFuncConstants.ALLAPPS_GRID) {
								row += 4;
							} else {
								row += 3;
							}
						} else {
							column += 5;
							if (gridId == AppFuncConstants.ALLAPPS_GRID) {
								row += 5;
							} else {
								row += 4;
							}
						}
						if (null != grid) {
							if ((grid.getColunmNums() != column) || (grid.getRowNums() != row)) {
								grid.setColunmNums(column);
								grid.setRowNums(row);
								setRCNum(column, row, gridId);
								return true;
							}
						}
					} else {
						if (smallerBound <= 240) {
							row += 4;
							if (gridId == AppFuncConstants.ALLAPPS_GRID) {
								column += 5;
							} else {
								column += 4;
							}
						} else {
							row += 4;
							if (gridId == AppFuncConstants.ALLAPPS_GRID) {
								column += 6;
							} else {
								column += 5;
							}
						}
						if (null != grid) {
							if ((grid.getColunmNums() != row) || (grid.getRowNums() != column)) {
								// 横屏时列代表实际行，行代表实际列
								grid.setColunmNums(row);
								grid.setRowNums(column);
								setRCNum(column, row, gridId);
								return true;
							}
						}
					}
					setRCNum(column, row, gridId);
					break;
				}
				case FunAppSetting.LINECOLUMNNUMXY_DIY : {
					// 功能表设置数据
					if (grid != null) {
						if (isVertical) {
							grid.setColunmNums(setting.getColNum());
							grid.setRowNums(setting.getRowNum());
						} else {
							grid.setColunmNums(setting.getRowNum());
							grid.setRowNums(setting.getColNum());
						}
					}
					return true;
				}
				case FunAppSetting.LINECOLUMNNUMXY_AUTO_FIT : {
					AppFuncAutoFitManager autoFitMgr = AppFuncAutoFitManager.getInstance(mContext);
					if (grid != null) {
						if (isVertical) {
							grid.setColunmNums(autoFitMgr.getAppDrawerColumnsV());
							grid.setRowNums(autoFitMgr.getAppDrawerRowsV());
						} else {
							grid.setRowNums(autoFitMgr.getAppDrawerColumnsH());
							grid.setColunmNums(autoFitMgr.getAppDrawerRowsH());
						}
					}
					break;
				}
				default : {
					if (isVertical()) {
						if (smallerBound <= 240) {
							row += 3;
							column += 4;
						} else {
							row += 4;
							column += 4;
						}
						if (null != grid) {
							if ((grid.getColunmNums() != column) || (grid.getRowNums() != row)) {
								grid.setColunmNums(column);
								grid.setRowNums(row);
								setRCNum(column, row, gridId);
								return true;
							}
						}

					} else {
						if (smallerBound <= 240) {
							row += 3;
							column += 4;
						} else {
							row += 3;
							column += 5;
						}
						if (null != grid) {
							if ((grid.getColunmNums() != row) || (grid.getRowNums() != column)) {
								grid.setColunmNums(row);
								grid.setRowNums(column);
								setRCNum(column, row, gridId);
								return true;
							}
						}
					}
					break;
				}
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取功能表文件夹列数
	 * 
	 * @param standard
	 *            竖屏时是设置项参数，横屏时是图标宽度
	 * @return
	 */
	public int getFolderColumn(int standard) {
		int column = 0;
		try {
			final boolean isVertical = isVertical();
			column = 0;
			int smallerBound = getSmallerBound();

			if (isVertical) {
				switch (standard) {
					case FunAppSetting.LINECOLUMNNUMXY_SPARSE : {
						if (smallerBound <= 240) {
							column += 4;
						} else {
							column += 4;
						}
						break;
					}
					case FunAppSetting.LINECOLUMNNUMXY_MIDDLE : {
						if (smallerBound <= 240) {
							column += 4;
						} else {
							column += 5;
						}
						break;
					}
					case FunAppSetting.LINECOLUMNNUMXY_MIDDLE_2 : {
						if (smallerBound <= 240) {
							column += 4;
						} else {
							column += 4;
						}
						break;
					}
					case FunAppSetting.LINECOLUMNNUMXY_THICK : {
						if (smallerBound <= 240) {
							column += 5;
						} else {
							column += 5;

						}
						break;
					}
					case FunAppSetting.LINECOLUMNNUMXY_DIY : {
						// 功能表设置数据
						FunAppSetting setting = GOLauncherApp.getSettingControler()
								.getFunAppSetting();
						column = setting.getColNum();

						break;
					}
					default : {
						if (smallerBound <= 240) {
							column += 4;
						} else {
							column += 4;
						}
						break;
					}
				}
			} else {
				column = (getLongerBound() - getStatusBarHeight()) / standard;
			}
		} catch (NullPointerException e) {
			column = 4;
			e.printStackTrace();
		}
		return column;
	}

	/**
	 * 根据字符串和画笔大小以及最大长度截取字符串
	 * 
	 * @param origStr
	 * @param maxLength
	 * @param paint
	 * @return
	 */
	public String cutString(String origStr, int maxLength, Paint paint) {
		if (origStr != null) {
			if (paint.measureText(origStr) > maxLength) {
				for (int i = 1; i <= origStr.length(); i++) {
					String temp = origStr.substring(0, i);
					if (paint.measureText(temp) > maxLength) {
						return (String) origStr.subSequence(0, i - 1);
					}
				}
			}
		}
		return origStr;
	}

	public static Point setMode(MImage image, int width, int height, int mode) {
		Point point = new Point();
		if (mode == 1) {
			image.setScale(width, height);
		} else if (mode == 2) {
			int w = image.getWidth();
			int h = image.getHeight();
			int x = (width - w) / 2;
			int y = (height - h) / 2;
			point.x = x;
			point.y = y;
		}
		return point;
	}

	private int getRealNum(int actualSize) {
		if (getSmallerBound() * getDensity() < 480) {
			return 320 * actualSize / 480;
		}
		return actualSize;
	}

	/**
	 * 根据屏幕大小对图标进行缩放 width, height都是以480*800为标准
	 */
	private void scaleImage(MImage origPic, int width, int height) {
		if ((origPic.getWidth() != width) || (origPic.getHeight() != height)) {
			origPic.setScale(width, height);
		}
	}

	public void clearResources() {
		mPicManager.clearDrawable();
		System.gc();
	}

	/**
	 * 图片资源管理器
	 * 
	 * @author tanshu
	 * 
	 */
	private class PicManager {
		private final HashMap<Integer, Drawable> mDrawableMap = new HashMap<Integer, Drawable>();

		/**
		 * 从Map中查找图片
		 * 
		 * @param drawbleId
		 * @return
		 */
		public Drawable getDrawable(int drawableId) {
			/**
			 * @edit by huangshaotao
			 * @date 2012-3-15
			 *       当手机内存不足而方法返回数据过大时有可能会出现StackOverflowError异常，这里加个保护
			 */
			try {
				return mDrawableMap.get(new Integer(drawableId));
			} catch (StackOverflowError e) {
				return null;
			}

		}

		/**
		 * 获取Drawable图片
		 * 
		 * @param drawableId
		 * @return
		 */
		public Drawable getViewDrawable(int drawableId) {
			return getViewDrawable(drawableId, true);
		}

		/**
		 * 获取Drawable图片
		 * 
		 * @param drawableId
		 * @param addHashMap
		 *            是否添加进hashMap的标识
		 * @return
		 */
		public Drawable getViewDrawable(int drawableId, boolean addHashMap) {
			Drawable image = getDrawable(drawableId);
			// 在此过滤自定义的图片资源ID
			if (drawableId != KILL_ICON_COPY) {
				if (image == null) {
					try {
						if (Machine.isTablet(mContext)) {
							image = ImageExplorer.getInstance(mContext).getDrawableForDensity(
									mContext.getResources(), drawableId);
							if (addHashMap) {
								putViewDrawable(drawableId, image);
							}
						} else {
							image = mContext.getResources().getDrawable(drawableId);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return image;
		}

		public void putViewDrawable(int drawableId, Drawable drawable) {
			mDrawableMap.put(drawableId, drawable);
		}

		public void clearDrawable() {
			Iterator<Integer> iterator = mDrawableMap.keySet().iterator();
			while (iterator.hasNext()) {
				Drawable drawable = mDrawableMap.get(iterator.next());
				if ((drawable != null) && (drawable instanceof BitmapDrawable)) {
					BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
					Bitmap bitmap = bitmapDrawable.getBitmap();
					if ((bitmap != null) && (bitmap.isRecycled() == false)) {
						bitmap.recycle();
						bitmap = null;
					}
				}
			}
			mDrawableMap.clear();
		}
	}

	public void vibrate(long[] pattern, int repeat) {
		try {
			mVibrator.vibrate(pattern, repeat);
		} catch (Exception e) {
			// Do nothing
		}
	}

	// public int getHomeComponentHeight() {
	// return getStandardSize(AppFuncTabBasicContent.sBottomHeight);
	// }

	public int getDimensionPixelSize(int resId) {
		return mContext.getResources().getDimensionPixelSize(resId);
	}

	public int getScaledSize(int origSize) {
		float scaleFactor = (getSmallerBound()) / 480f;
		return Math.round(origSize * scaleFactor);
	}

	// 根据字符大小以及运行的最大宽度判断是否将一个字符串分割成不超过最大长度的字符串数组，用于自动换行绘制文字
	public ArrayList<String> splitStringByWidth(String str, float maxWidth, Paint paint) {
		if (str == null || "".equals(str)) {
			return null;
		}
		ArrayList<String> list = new ArrayList<String>();
		int index = 0;
		int length = str.length();
		for (int i = 0; i < length; i++) {
			String substr = str.substring(index, i + 1);
			float w = paint.measureText(substr);
			if (w > maxWidth) {
				if (i > 1) {
					list.add(str.substring(index, i));
				} else {
					list.add(str.substring(0, 1));
				}
				index = i;
			}
		}
		// 剩余的字符串
		if (index < length) {
			list.add(str.substring(index));
		}
		return list;
	}

	public int getStandardSize(float orgSize) {
		return Math.round(orgSize * getDensity() / 1.5f);
	}

	/**
	 * 检查网络状态
	 * 
	 * @return 1表示仅有 移动网络 2表示仅有wifi 3表示移动wifi都开通 4表示移动wifi都不通
	 */
	public int checkNetwork() {
		if (Machine.isTablet(mContext)) {
			return 0;
		}
		ConnectivityManager conMan = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (conMan == null) {
			return 0;
		}
		android.net.NetworkInfo.State mobilestate = conMan.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).getState();
		android.net.NetworkInfo.State wifistate = conMan.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState();
		if (wifistate != android.net.NetworkInfo.State.CONNECTED
				&& mobilestate == android.net.NetworkInfo.State.CONNECTED) {
			return 1;
		}
		if (mobilestate != android.net.NetworkInfo.State.CONNECTED
				&& wifistate == android.net.NetworkInfo.State.CONNECTED) {
			return 2;
		}
		if (mobilestate == android.net.NetworkInfo.State.CONNECTED
				&& wifistate == android.net.NetworkInfo.State.CONNECTED) {
			return 3;
		}
		if (mobilestate != android.net.NetworkInfo.State.CONNECTED
				&& wifistate != android.net.NetworkInfo.State.CONNECTED) {
			return 4;
		}
		return 0;
	}

	public void setRCNum(final int column, final int row, long gridId) {
		if (gridId == AppFuncConstants.ALLAPPS_GRID) {
			if (mUpdateGridSettingThread == null) {
				mUpdateGridSettingThread = new Thread(new Runnable() {
					@Override
					public void run() {
						android.os.Process
								.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
						synchronized (mLock) {
							FunControler controler = AppFuncFrame.getFunControler();
							if (controler != null) {
								if (!controler.isInited()) {
									try {
										mLock.wait();
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}
							}
							FunAppSetting setting = GOLauncherApp.getSettingControler()
									.getFunAppSetting();
							setting.setColNum(column);
							setting.setRowNum(row);
							mUpdateGridSettingThread = null;
						}
					}
				});
				mUpdateGridSettingThread.start();
			}
		}
	}

	public Object getLock() {
		return mLock;
	}

	public String getLanguage() {
		Locale l = Locale.getDefault();
		return String.format("%s-%s", l.getLanguage(), l.getCountry());
	}

	public boolean isTabletDevice() {
		if (android.os.Build.VERSION.SDK_INT >= 11) { // honeycomb
			// test screen size, use reflection because isLayoutSizeAtLeast is
			// only available since 11
			Configuration con = mContext.getResources().getConfiguration();
			try {
				Method mIsLayoutSizeAtLeast = con.getClass().getMethod("isLayoutSizeAtLeast",
						int.class);
				Boolean r = (Boolean) mIsLayoutSizeAtLeast.invoke(con, 0x00000004); // Configuration.SCREENLAYOUT_SIZE_XLARGE
				return r;
			} catch (Exception x) {
				x.printStackTrace();
				return false;
			}
		}
		return false;
	}

	/**
	 * 判断SDCard是否可以读写的方法 如果没有SDCard则返回false
	 * 
	 * @return 如果可以读写，则返回true,否则返回false
	 */
	public static boolean isSDCardAccess() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * 检查当前桌面版本与插件包版本是否互相兼容
	 * @return
	 */
	public boolean isMediaPluginCompatible() {
		float mediaPluginCompatibleVersion = -1;
		float golauncherCompatibleVersion = -1;

		float mediaPluginCurrentVersion = -1;
		float golauncherCurrentVersion = -1;
		if (mMediaPluginCompatible == -1) {
			try {
				InputStream is = mContext.getResources().openRawResource(
						R.raw.mediamanagement_plugin);
				Properties properties = new Properties();
				properties.load(is);
				String version = properties.getProperty("compatible_version");
//				mediaPluginCompatibleVersion = Float.parseFloat(version);
				mediaPluginCompatibleVersion = AppUtils.changeVersionNameToFloat(version);
				String versionName = AppUtils.getVersionNameByPkgName(mContext,
						LauncherEnv.PACKAGE_NAME);
//				if (versionName.contains(beta)) {
//					versionName = versionName.replace(beta, "");
//				}
//				golauncherCurrentVersion = Float.parseFloat(versionName);
				golauncherCurrentVersion = AppUtils.changeVersionNameToFloat(versionName);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Resources res = mContext.getPackageManager().getResourcesForApplication(
						PackageName.MEDIA_PLUGIN);
				int resId = res.getIdentifier("go_launcher_ex", "raw", PackageName.MEDIA_PLUGIN);
				InputStream is = res.openRawResource(resId);
				Properties properties = new Properties();
				properties.load(is);
				String version = properties.getProperty("compatible_version");
//				golauncherCompatibleVersion = Float.parseFloat(version);
				golauncherCompatibleVersion = AppUtils.changeVersionNameToFloat(version);

				String versionName = AppUtils.getVersionNameByPkgName(mContext,
						PackageName.MEDIA_PLUGIN);
//				if (versionName.contains(beta)) {
//					versionName = versionName.replace(beta, "");
//				}
//				mediaPluginCurrentVersion = Float.parseFloat(versionName);
				mediaPluginCurrentVersion = AppUtils.changeVersionNameToFloat(versionName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (mMediaPluginCompatible == -1
				&& (mediaPluginCompatibleVersion == -1 || golauncherCompatibleVersion == -1)) {
			if (mediaPluginCompatibleVersion == -1) {
				mMediaPluginCompatible = 0;
				//show download and update GoLauncher dialog
				mHandler.sendEmptyMessage(SHOW_UPDATE_GO_LAUNCHER_DIALOG);
			} else if (golauncherCompatibleVersion == -1) {
				mMediaPluginCompatible = 1;
				//show download and update plugin dialog
				mHandler.sendEmptyMessage(SHOW_UPDATE_MEDIA_PLUGIN_DIALOG);
			}
			return false;
		} else {
			if (mMediaPluginCompatible == -1) {
				if (golauncherCurrentVersion >= golauncherCompatibleVersion
						&& mediaPluginCurrentVersion >= mediaPluginCompatibleVersion) {
					mMediaPluginCompatible = 2;
					return true;
				} else {
					if (golauncherCurrentVersion < golauncherCompatibleVersion) {
						mMediaPluginCompatible = 0;
						//show download and update GoLauncher dialog
						mHandler.sendEmptyMessage(SHOW_UPDATE_GO_LAUNCHER_DIALOG);
					} else if (mediaPluginCurrentVersion < mediaPluginCompatibleVersion) {
						mMediaPluginCompatible = 1;
						//show download and update plugin dialog
						mHandler.sendEmptyMessage(SHOW_UPDATE_MEDIA_PLUGIN_DIALOG);
					}
					mMediaPluginCompatible = 0;
					return false;
				}
			} else {
				if (mMediaPluginCompatible == 0) {
					mHandler.sendEmptyMessage(SHOW_UPDATE_GO_LAUNCHER_DIALOG);
				} else if (mMediaPluginCompatible == 1) {
					mHandler.sendEmptyMessage(SHOW_UPDATE_MEDIA_PLUGIN_DIALOG);
				}
				return mMediaPluginCompatible == 2 ? true : false;
			}
		}
	}

	private final static int SHOW_UPDATE_GO_LAUNCHER_DIALOG = 1;
	private final static int SHOW_UPDATE_MEDIA_PLUGIN_DIALOG = 2;

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				final Context context = GoLauncher.getContext();
				switch (msg.what) {
					case SHOW_UPDATE_GO_LAUNCHER_DIALOG :
						DialogConfirm launcherDialog = new DialogConfirm(context);
						launcherDialog.show();
						launcherDialog.setTitle(R.string.update_go_launcher_dialog_title);
						launcherDialog.setMessage(R.string.update_go_launcher_dialog_text);
						launcherDialog.setPositiveButton(R.string.yes, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// 跳转到GoStore更新桌面
//								GoStoreOperatorUtil.gotoStoreDetailDirectly(mContext,
//										mContext.getPackageName());
								AppsDetail.gotoDetailDirectly(context, AppsDetail.START_TYPE_APPMANAGEMENT, context.getPackageName());
							}
						});
						launcherDialog.setNegativeButton(R.string.no, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
							}
						});
						break;
					case SHOW_UPDATE_MEDIA_PLUGIN_DIALOG :
						DialogConfirm pluginDialog = new DialogConfirm(context);
						pluginDialog.show();
						pluginDialog.setTitle(R.string.update_go_launcher_dialog_title);
						pluginDialog.setMessage(R.string.update_media_plugin_dialog_text);
						pluginDialog.setPositiveButton(R.string.yes, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// 跳转进行下载
								String packageName = PackageName.MEDIA_PLUGIN;
								String url = LauncherEnv.Url.MEDIA_PLUGIN_FTP_URL; // 插件包ftp地址
								String linkArray[] = { packageName, url };
								String title = context
										.getString(R.string.mediamanagement_plugin_download_title);
								boolean isCnUser = Machine.isCnUser(context);

								CheckApplication.downloadAppFromMarketFTPGostore(context, "",
										linkArray, LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK,
										title, System.currentTimeMillis(), isCnUser,
										CheckApplication.FROM_MEDIA_DOWNLOAD_DIGLOG);
							}
						});
						pluginDialog.setNegativeButton(R.string.no, new View.OnClickListener() {
							@Override
							public void onClick(View v) {
							}
						});
						break;
					default :
						break;
				}
			}
		};
	}
}

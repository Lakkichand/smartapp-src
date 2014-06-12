package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.ConvertUtils;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.effector.united.CoupleScreenEffector;
import com.go.util.scroller.ScreenScroller;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderGridView.FolderAdapter;
import com.jiubang.ggheart.apps.desks.imagepreview.ChangeIconPreviewActivity;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.components.QuickActionMenu;
import com.jiubang.ggheart.components.QuickActionMenu.onActionListener;
import com.jiubang.ggheart.components.renamewindow.RenameActivity;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.info.FeatureItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenFolderInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.bean.AppFuncBaseThemeBean.AbsFolderBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * UserFolder的展示类
 * 
 * @author jiangxuwen
 * 
 */
public class DeskUserFolderFrame extends AbstractFrame
		implements
			OnItemLongClickListener,
			OnClickListener,
			OnItemClickListener,
			OnTouchListener,
			IIndicatorUpdateListner,
			IFolderAnimationListner,
			onActionListener {
	// 刷新文件名编辑框的标识
	public static final int UPDATE_FOLDER_NAME = 1;
	
	private static final long CLICK_LIMIT_TIME = 500; // 编辑按钮的点击间隔
	private long mLastTime; // 上次编辑按钮的点击时间
	
	// 文件夹指示器
	private DesktopIndicator mFolderIndicator;
	// 重命名按钮
	private DeskTextView mRenameEdit;
	// 编辑按钮
	private ImageButton mEditBtn;
	
	// 编辑按钮
	private ImageButton mSortBtn;
	// 图标展示区域
	private FolderGridView mFolderGridView;
	// 整-个层
	// private RelativeLayout mMainLayout;
	// 当前的显示层
	private FolderRelativeLayout mLayout;
	//
	private IMessageHandler mMessageHandler;
	// 文件夹信息
	private UserFolderInfo mInfo;
	// 主题控制器
	private AppFuncThemeController mThemeController;
	// 编辑按钮的未选中图片
	// private Drawable mEditBtnDrawable;
	// 编辑按钮的选中图片
	// private Drawable mEditBtnLightDrawable;
	// 字体颜色
	private int mTextColor;
	// 效果设置
	private EffectSettingInfo mEffectSettingInfo = null;
	// 编辑按钮的宽度
	private int mEditBtnWidth;
	// 文件夹图标
//	private FolderIcon mFolderIcon = null;
	//Diaglog add by: zzf
	DialogSingleChoice mDialog = null;

	public static final int TYPE_SCREEN = 1;
	public static final int TYPE_DOCK = 2;
	private int mType = TYPE_SCREEN;

	// 文件夹长按图标拖动换位
	private long mOutOfBoundStartTime = 0; // 刚超出文件夹边界的时间
	private static final int DRAG_OUT_OF_BOUND_TO_REMOVE_TIME = 300; // 超出边界经过这段时间后，退出folder层
	private static final int SCROLL_TO_NEXT_PAGE_SPACE = DrawUtils.dip2px(30); // 在这个范围内，响应换页

	// Handler消息
	private static final int HANDLER_UPDATE_SCREENSHOT = 1; // 更新桌面截图
	private static final int HANDLER_UPDATE_FOLDERICON = 2; // 更新桌面截图

	// folder3.0
	private boolean mDockFrameShow = true;

	// 长按图标
	private QuickActionMenu mQuickActionMenu; // 菜单
	private BubbleTextView mDragView; // 长按图标
//	private AlertDialog mDialog = null;
	/**
	 * 排序风格
	 */
	public static final int SORTTYPE_LETTER = 0;
	public static final int SORTTYPE_TIMENEAR = 1;
	public static final int SORTTYPE_TIMEREMOTE = 2;

	private boolean mSortFlag; // 使用了排序的标识
	private int mSortTpye; // 使用了排序的标识

	public static final int KEEP_SORT_TYPE = 0; // 保持原排序方式
	public static final int UPDATE_SORT_TYPE = 1; // 刷新排序方式

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HANDLER_UPDATE_SCREENSHOT :
					try {
						if (null != mLayout
								&& mLayout.getmStatus() == FolderRelativeLayout.DRAW_STATUS_NORMAL) {
							getScreenShotBmpsFromOtherFrames();
							mLayout.postInvalidate();
							Message mymsg = new Message();
							mymsg.what = HANDLER_UPDATE_FOLDERICON;
							handleMessage(mymsg);
						}
					} catch (Exception e) {
						// 此更新是异步，容易出现异常，若出现异常，则不更新桌面截图
						e.printStackTrace();
					}
					break;

				case HANDLER_UPDATE_FOLDERICON : {
					try {
						if (mType == TYPE_DOCK) {
							Rect rect = new Rect(-1, -1, -1, -1);
							ArrayList<Bitmap> list = new ArrayList<Bitmap>();
							GoLauncher
									.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
											IDiyMsgIds.GET_DOCK_OPEN_FOLDER_ICON_LAYOUTDATA, -1,
											rect, list);
							if (rect.left >= 0) {
								mLayout.setmFolderIconRect(rect);
							}
							if (!list.isEmpty() && null != list.get(0)
									&& list.get(0) instanceof Bitmap) {
								mLayout.setmFolderIconBmp(list.get(0));
							}
							list.clear();
							list = null;
						} else {
							ArrayList<Bitmap> list = new ArrayList<Bitmap>();
							GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
									IDiyMsgIds.GET_DOCK_OPEN_FOLDER_ICON_LAYOUTDATA, -1,
									mInfo.mInScreenId, list);
							if (!list.isEmpty() && null != list.get(0)
									&& list.get(0) instanceof Bitmap) {
								mLayout.setmFolderIconBmp(list.get(0));
							}
							list.clear();
							list = null;
						}
					} catch (Exception e) {
						// 此更新是异步，容易出现异常，若出现异常，则不更新桌面截图
						e.printStackTrace();
					}
				}
					break;

				default :
					break;
			}
		};
	};

	public DeskUserFolderFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);
		LayoutInflater inflater = LayoutInflater.from(activity);
		mLayout = (FolderRelativeLayout) inflater.inflate(R.layout.desk_user_folder,
				mFrameManager.getRootView(), false);
		initFrame();
	}

	// 初始化
	private void initFrame() {
		findViews();
		mThemeController = AppFuncFrame.getThemeController();
		loadResouce();
		getScreenShotBmpsFromOtherFrames();
	}

	/**
	 * 向其他层获取打开背景各view底图
	 */
	private void getScreenShotBmpsFromOtherFrames() {
		mLayout.removeAllCacheBmps();
		// 1:ScreenFrame底图
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.GET_CURRENT_VIEW_CACHE_BMP, -1, mLayout, null);
		mDockFrameShow = ShortCutSettingInfo.sEnable;
		// 2:DockFrame底图
		if (GoLauncher.isPortait() && mDockFrameShow) {
			GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
					IDiyMsgIds.GET_CURRENT_VIEW_CACHE_BMP, -1, mLayout, null);
		}
	}

	// 宽度设置
	private void setWidth() {
		// 指示器的宽度设置，使到与viewGroup保持居中
		RelativeLayout.LayoutParams gridLayoutParams = (RelativeLayout.LayoutParams) mFolderGridView
				.getLayoutParams();
		RelativeLayout.LayoutParams indicatorParams = (RelativeLayout.LayoutParams) mFolderIndicator
				.getLayoutParams();
		indicatorParams.width = gridLayoutParams.width;
		// mLayout的宽度设置
		LayoutParams layoutParams = (LayoutParams) mLayout.getLayoutParams();
		layoutParams.width = GoLauncher.getScreenWidth();
		// 重命名按钮的长度要自适应
		RelativeLayout.LayoutParams renameBtnParams = (RelativeLayout.LayoutParams) mRenameEdit
				.getLayoutParams();
		final int marginWidth = mActivity.getResources().getDimensionPixelSize(
				R.dimen.user_folder_closeBtn_margin);
		renameBtnParams.width = layoutParams.width - 2 * mEditBtnWidth - marginWidth;
	}

	// 初始化指示器
	private void initIndicator(int num) {
		mFolderIndicator.applyTheme();
		mFolderIndicator.setTotal(num);
		mFolderIndicator.setIndicatorListner(this);
		setWidth();
	}

	// 初始化ViewGroup
	private void initFolderGridView() {
		mFolderGridView.setAdapters(mInfo);
		mFolderGridView.setmMessageHandler(this);
		mFolderGridView.setListeners(this, this);
		handleEffectSettingChange(mEffectSettingInfo);
		mFolderGridView.show();
	}

	// 根据id获取组件
	private void findViews() {
		mLayout.setSelected(false);

		mLayout.setOnTouchListener(this);
		mLayout.setmFolderAnimationListner(this);

		mRenameEdit = (DeskTextView) mLayout.findViewById(R.id.rename_folder_edittext);
		mRenameEdit.setOnClickListener(this);
		boolean editable = !GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen;
		mRenameEdit.setEnabled(editable);
		// DrawUtils.px2dip(5)是为了与功能表文件夹字号统一
		mRenameEdit.setTextSize(GoLauncher.getAppFontSize() + DrawUtils.px2dip(5));

		mEditBtn = (ImageButton) mLayout.findViewById(R.id.edit_folder_btn);
		mEditBtn.setOnClickListener(this);
		mEditBtn.setEnabled(editable);

		mSortBtn = (ImageButton) mLayout.findViewById(R.id.sort_folder_btn);
		mSortBtn.setOnClickListener(this);
		mSortBtn.setEnabled(editable);

		mFolderIndicator = (DesktopIndicator) mLayout.findViewById(R.id.folder_indicator);
		mFolderIndicator.setIndicatorHeight(mActivity.getResources().getDimensionPixelSize(
				R.dimen.folder_indicator_height));

		mFolderGridView = (FolderGridView) mLayout.findViewById(R.id.folder_gridView);
		mFolderGridView.setmIndicatorUpdateListner(this);
	}

	@Override
	public View getContentView() {
		return mLayout;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			// 隐藏 文件夹
			if (null != mLayout && mLayout.getmStatus() == FolderRelativeLayout.DRAW_STATUS_NORMAL) {
				close();
			} else {
				// 动画过程中，不响应
			}
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	private void bind(UserFolderInfo info, IMessageHandler iHandler) {

		if (info != null && info instanceof UserFolderInfo) {
			this.mInfo = info;
			// mInfo.mOpened = true;
			if (info.mTitle != null) {
				mRenameEdit.setText(info.mTitle.toString());
			}
		}// end if
		if (iHandler != null) {
			mMessageHandler = iHandler;
		}
	}

	private void close() {
		if (mLayout.getmStatus() == FolderRelativeLayout.DRAW_STATUS_FOLDER_CLOSE) {
			return;
		}
		mHandler.removeMessages(HANDLER_UPDATE_SCREENSHOT);
		if (null != mDragView) {
			GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
					IDiyMsgIds.SHOW_TRASH_DURING_DRAGING, -1, null, null);
		}

		// TODO:在这里开始各层的退出动画
		mLayout.setmStatus(FolderRelativeLayout.DRAW_STATUS_FOLDER_CLOSE);
		// 关闭文件夹的时候，需要prepareIcon，把罩子恢复
//		if (mFolderIcon != null) {
//		    mFolderIcon.prepareIcon(mFolderIcon, mInfo);
//        }

		if (!GoLauncher.isPortait() && mDockFrameShow) {
			// 横屏退出dock动画
			GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.FOLDER_CLOSED, -1,
					null, null);
		}
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		boolean ret = false;
		switch (msgId) {
			case IDiyMsgIds.USER_FOLDER_ADD_INFO : {
				bind((UserFolderInfo) object, (IMessageHandler) who);
				// 先初始化viewGroup再加载指示器
				mFolderGridView.removeAllViews();
				initFolderGridView();
				initIndicator(mFolderGridView.getmTotalScreenNum());
				break;
			}

			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED : {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DESK_USER_FOLDER_FRAME, null,
						null);
				// 竖屏为1，横屏为2
				// if (param != -1)
				// {
				// changeOrientation(param);
				// }
			 	closeSelectSortDialog();
				break;
			}

			case IDiyMsgIds.USER_FOLDER_UPDATE_INDICATOR : {
				if (object != null && object instanceof Bundle) {
					updateIndicator(param, (Bundle) object);
				}
				break;
			}

			case IFrameworkMsgId.SYSTEM_HOME_CLICK : {
				if (mInfo.mOpened) {
					close();
				}
				break;
			}

			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : {
				close();
			}
				break;

			case IDiyMsgIds.FOLDER_TYPE : {
				mType = param;
			}
				break;

			case IDiyMsgIds.FOLDER_CHECK_POSITION : {
				ScreenScroller scroller = mFolderGridView.getScreenScroller();
				Point point = (Point) object;
				if (null == point
						|| !scroller.isFinished()
						|| (mFolderGridView.getCurrentView() != null && mFolderGridView
								.getCurrentView().getStatus() == CellGridView.DRAW_REPLACE)) {
					// 还在滚动或在换位过程中，不响应其他位置操作

					ret = true;
				} else if (mLayout.getmFolderTop() > point.y
						|| mLayout.getmFolderBottom() < point.y) {
					// NOTE:出边界到桌面操作标记
					if (mOutOfBoundStartTime == 0) {
						mOutOfBoundStartTime = SystemClock.uptimeMillis();
					} else if (SystemClock.uptimeMillis() - mOutOfBoundStartTime > DRAG_OUT_OF_BOUND_TO_REMOVE_TIME) {
						close();
						// ADT-6739 隐藏状态栏后，将文件夹中的图标移到桌面时，垃圾箱和指示器同时显示了
						// 原因从文件夹拖出没有发消息通知屏幕隐藏指示器
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.HIDDEN_INDICATOR, -1, null, null);
						// ADT-6740 将文件夹中的图标拖到屏幕预览，再拖回到桌面后，没有显示垃圾箱
						// 原因是在文件夹里面开始拖动图标会发消息调用MyDragFrame的setTrashGone(),并且在拖出文件夹后没有设回来
						// 只是简单的将TrashAreaLayout设成可见，典型的数据与表现不一致，而在屏幕预览时进入桌面时是按照MyDragFrame
						// 里面的状态来控制垃圾桶是否显示，所以造成垃圾桶没有显示
						GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME,
								IDiyMsgIds.TRASH_VISIBLE, -1, null, null);
					}
					mFolderGridView.setmOutOfBound(true);

					ret = false;
				} else if (0 <= point.x && point.x <= SCROLL_TO_NEXT_PAGE_SPACE) {
					// 上一页
					int currentscreen = mFolderGridView.getCurrentViewIndex();
					if (currentscreen > 0) {
						mFolderGridView.snapToScreen(currentscreen - 1, false, -1);
					}

					mOutOfBoundStartTime = 0;
					ret = true;
				} else if (mLayout.getRight() - SCROLL_TO_NEXT_PAGE_SPACE <= point.x
						&& point.x <= mLayout.getRight()) {
					// 下一页
					int currentscreen = mFolderGridView.getCurrentViewIndex();
					if (currentscreen < mFolderGridView.getChildCount() - 1) {
						CellGridView nextCellGridView = (CellGridView) mFolderGridView
								.getChildAt(currentscreen + 1);
						if (nextCellGridView.getChildCount() > 1) {
							// 下一页只有一个+号就不跳转
							mFolderGridView.snapToScreen(currentscreen + 1, false, -1);
						}
					}

					mOutOfBoundStartTime = 0;
					ret = true;
				} else {
					// 换位响应范围中,传过来的point是layout的，而不是window
					CellGridView cellGridView = mFolderGridView.getCurrentView();
					// TODO：以下这句会报NullPointerException，暂时找不到确切原因，先做保护   by Ryan 2012.09.25
					// 如果为空，则不进行位置替换操作
					if (cellGridView != null) {
						cellGridView.checkReplacePosition(point);
					}

					mOutOfBoundStartTime = 0;
					mFolderGridView.setmOutOfBound(false);
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.DELETE_CACHE_INFO_IN_FOLDER : {
				if (null != object && object instanceof ItemInfo) {
					try {
						mFolderGridView.deleteItem((ItemInfo) object);
					} catch (Exception e) {
						Toast.makeText(mActivity, "ERROR:delete folder item during replacing",
								Toast.LENGTH_LONG).show();
					}
					mHandler.removeMessages(HANDLER_UPDATE_SCREENSHOT);
					mHandler.sendEmptyMessageDelayed(HANDLER_UPDATE_SCREENSHOT, 300);
				}
			}
				break;

			case IDiyMsgIds.EXCHANGE_CACHE_INFO_IN_FOLDER : {
				if (null != object && object instanceof ShortCutInfo) {
					mFolderGridView.exchangeItem((ShortCutInfo) object);
				}
			}
				break;

			case IDiyMsgIds.REFRASH_CACHE_FOLDER_CONTENT : {
				mFolderGridView.removeAllViews();
				initFolderGridView();
				mFolderIndicator.setTotal(mFolderGridView.getChildCount());

				int current = mFolderGridView.getCurrentViewIndex() < mFolderGridView
						.getChildCount() ? mFolderGridView.getCurrentViewIndex() : 0;
				mFolderIndicator.setCurrent(current);
				mFolderGridView.setmCurrentScreen(current);
			}
				break;

			case IDiyMsgIds.FOLDER_LAYOUT_DATA : {
				// 获取桌面壁纸
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.GET_BACKGROUND,
						IDiyFrameIds.DESK_USER_FOLDER_FRAME, null, null);
				if (null != objects && objects instanceof ArrayList<?>) {
					mLayout.setmArrowDirection(param);
					int width = GoLauncher.getDisplayWidth();
					int height = GoLauncher.getDisplayHeight();
					mLayout.measure(width, height);
					// mMainLayout.layout(0, 0, width, height);

					Bitmap bmp = (Bitmap) object;
					Rect folderData = (Rect) objects.get(0);
					// 桌面文件夹关闭时需要再拿到folderIcon进行一次prepareIcon()
//					if (objects.size() > 1) {
//					    mFolderIcon = (FolderIcon) objects.get(1);
//                    }
					
					mLayout.startOpenAnimation(bmp, folderData);
					mLayout.setPadding(0, mLayout.getmFolderTop(), 0, GoLauncher.getDisplayHeight()
							- mLayout.getmFolderBottom());
				}
			}
				break;
			case IDiyMsgIds.SCREEN_FOLDER_RENAME : {
				if (object instanceof Long) {
					// 0 现在名
					// 1 原有名
					@SuppressWarnings("unchecked")
					ArrayList<String> names = (ArrayList<String>) objects;
					if (null != names && names.size() > 0) {
						mRenameEdit.setText(names.get(0));
					}
				}
			}
				break;

			case IDiyMsgIds.BACK_FROM_EDIT : {
				// mEditBtn.setBackgroundDrawable(mEditBtnDrawable);
			}
				break;

			case IDiyMsgIds.SEND_BACKGROUND : {
				if (null != object && object instanceof Drawable && null != objects
						&& !objects.isEmpty()) {
					Drawable drawable = (Drawable) object;
					int[] offset = (int[]) objects.get(0);
					mLayout.setmWallpaper(drawable);
					mLayout.setmWallpaperOffset(offset);
				}
			}
				break;

			case IDiyMsgIds.GET_FOLDER_LAYOUT_DATA : {
				if (null != object && object instanceof int[] && null != mLayout
						&& mLayout.getmStatus() == FolderRelativeLayout.DRAW_STATUS_NORMAL) {
					int[] data = (int[]) object;
					data[0] = mLayout.getmFolderTop();
					data[1] = mLayout.getmFolderBottom();
					data[2] = mLayout.getmFolderClipLine();
				}
			}
				break;

			case IFrameworkMsgId.SYSTEM_FULL_SCREEN_CHANGE : {
				// TODO:收到收起垃圾箱的消息
			}
				break;

			case IDiyMsgIds.REMOVE_ACTION_MENU : {
				if (mQuickActionMenu != null && mQuickActionMenu.isShowing()) {
					hideQuickActionMenu(false);
					ret = true;
				}
			}
				break;

			case IDiyMsgIds.EVENT_UNINSTALL_APP : {
				@SuppressWarnings("unchecked")
				ArrayList<AppItemInfo> uninstallapps = (ArrayList<AppItemInfo>) objects;

				int uninstallsize = uninstallapps.size();
				for (int i = 0; i < uninstallsize; i++) {
					AppItemInfo appItemInfo = uninstallapps.get(i);

					// 动态删除，for循环边界每次取getChildCount()
					for (int j = 0; j < mFolderGridView.getChildCount(); j++) {
						CellGridView cellGridView = (CellGridView) mFolderGridView.getChildAt(j);
						for (int k = 0; k < cellGridView.getChildCount(); k++) {
							View view = cellGridView.getChildAt(k);
							ShortCutInfo shortCutInfo = (ShortCutInfo) view.getTag();
							if (null != shortCutInfo
									&& ConvertUtils.intentCompare(appItemInfo.mIntent,
											shortCutInfo.mIntent)) {
								onActionClick(IQuickActionId.DELETE, view);
							}
						}
					}
				}
			}
				break;

			case IDiyMsgIds.QUICKACTION_EVENT : {
				if (param == IQuickActionId.CHANGE_ICON && null != object
						&& object instanceof Bundle) {
					actionChangeIcon((Bundle) object);
				}
			}
				break;

			case IDiyMsgIds.RESET_DEFAULT_ICON : {
				resetDefaultIcon();
			}
				break;

			case IDiyMsgIds.RENAME : {
				if (null != object && object instanceof Long && null != objects
						&& !objects.isEmpty()) {
					long itemid = (Long) object;
					String name = (String) objects.get(0);
					if (itemid == mInfo.mInScreenId) {
						// 文件夹重命名
						mRenameEdit.setText(name);
					} else {
						// 文件夹内图标重命名
						renameItem(name);
					}
				}
			}
				break;

			default :
				break;
		}
		return ret;
	}
	
	@Override
	public void onClick(View v) // 文件夹按钮的点击处理事件
	{

		if (System.currentTimeMillis() - mLastTime < CLICK_LIMIT_TIME) {
			return;
		}
		if (mLayout.getmStatus() != FolderRelativeLayout.DRAW_STATUS_NORMAL) {
			return;
		}
		mLastTime = System.currentTimeMillis();
		if (v == mEditBtn) // 文件夹内容编辑
		{
			// mEditBtn.setBackgroundDrawable(mEditBtnLightDrawable);
			startEditFolderActivity();
			//用户行为统计---文件夹编辑
			StatisticsData.countUserActionData(
					StatisticsData.DESK_ACTION_ID_FLODER_EDIT,
					StatisticsData.USER_ACTION_DEFAULT, IPreferencesIds.DESK_ACTION_DATA);
		} else if (v == mRenameEdit) { // 文件夹重命名
			Intent intent = new Intent(mActivity, RenameActivity.class);
			CharSequence name = mRenameEdit.getText();
			intent.putExtra(RenameActivity.NAME, name);
			intent.putExtra(RenameActivity.HANDLERID, IDiyFrameIds.DESK_USER_FOLDER_FRAME);
			intent.putExtra(RenameActivity.ITEMID, mInfo.mInScreenId);
			intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, true);
			intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, true);
			mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_RENAME);
		} else if (v == mSortBtn) { // 文件夹内容排序
			showSelectSortDialog();
			//用户行为统计---文件夹排序
			StatisticsData.countUserActionData(
					StatisticsData.DESK_ACTION_ID_FLODER_SORT,
					StatisticsData.USER_ACTION_DEFAULT, IPreferencesIds.DESK_ACTION_DATA);
		}
	}

	private void startEditFolderActivity() {
		Intent intent = new Intent(mActivity, ScreenModifyFolderActivity.class);
		intent.putExtra(ScreenModifyFolderActivity.FOLDER_ID, mInfo.mInScreenId);
		intent.putExtra(ScreenModifyFolderActivity.FOLDER_TITLE, mInfo.mTitle);
		intent.putExtra(ScreenModifyFolderActivity.FOLDER_CREATE, false);
		intent.putExtra(ScreenModifyFolderActivity.FOLDER_DOCK, mType == TYPE_DOCK);
		mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_DESKTOP_FOLDER_EDIT);
	}

	// 刷新指示器
	private void updateIndicator(int type, Bundle bundle) {
		mFolderIndicator.updateIndicator(type, bundle);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		if (mLayout.getmStatus() != FolderRelativeLayout.DRAW_STATUS_NORMAL) {
			return;
		}
		if (mMessageHandler != null) {
			// 启动程序
			mMessageHandler.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_FOLDER_EVENT,
					IScreenFolder.START_ACTIVITY, view, null);
		}

		mInfo.mOpened = false;
		GoLauncher.postMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
				IDiyFrameIds.DESK_USER_FOLDER_FRAME, null, null);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		// 判断当前是否锁屏
		if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(mActivity);
			return true;
		}

		// 添加震动
		view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
				HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);

		if (!view.isInTouchMode()) {
			return false;
		}

		// 开始拖动图标
		ArrayList<ScreenFolderInfo> folderInfo = new ArrayList<ScreenFolderInfo>();
		folderInfo.add(mInfo);

		view.clearAnimation();
		boolean bool = view.isDrawingCacheEnabled();
		// 这里先清缓存，在换位后，4.0上的view还保留着没换位前的view的缓存
		view.setDrawingCacheEnabled(false);
		if (mMessageHandler != null) {
			mMessageHandler.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_FOLDER_EVENT,
					IScreenFolder.START_DRAG, view, folderInfo);
		}
		folderInfo.clear();
		folderInfo = null;
		// 关闭文件夹

		mFolderGridView.startDragItem(view);
		view.setDrawingCacheEnabled(bool);

		showQuickActionMenu(view);
		mDragView = (BubbleTextView) view;

		return true;
	}

	// 加载图片资源
	private void loadResouce() {
		String themePackage = GOLauncherApp.getSettingControler().getScreenStyleSettingInfo()
				.getFolderStyle();

		// 获得文件夹的bean
		AbsFolderBean folderBean = mThemeController.getThemeBean().mFolderBean;
		// 获取文件夹背景图片
		Drawable bgDrawable = mThemeController.getDrawable(folderBean.mFolderBgPath, themePackage,
				false);
		// 如果不为空
		if (bgDrawable != null) {
			if (bgDrawable instanceof NinePatchDrawable) {
				mLayout.setmBgDrawable(bgDrawable);
			} else {
				// bgDrawable = generatorBgDrawable(bgDrawable);
				mLayout.setmBgDrawable(bgDrawable);
			}
		} else {
			// 添加内存爆掉的保护
			try {
				mLayout.setmBgDrawable(mActivity.getResources().getDrawable(
						R.drawable.appfunc_folder_frame));
			} catch (OutOfMemoryError e) {
				OutOfMemoryHandler.handle();
			} catch (Exception e) {
			}
		}

		Drawable renameDrawable = mThemeController.getDrawable(folderBean.mFolderEditBgPath,
				themePackage, false);

		if (renameDrawable != null) {
			mRenameEdit.setBackgroundDrawable(renameDrawable);
		}
		mTextColor = folderBean.mFolderEditTextColor;

		mFolderGridView.setmImageBottomH(folderBean.mImageBottomH);

		// 设置字体颜色
		mRenameEdit.setTextColor(mTextColor);
		// 设置gridview里面的字体颜色为功能表图标的字体颜色
		if (GoLauncher.getCustomTitleColor()) {
			int color = GoLauncher.getAppTitleColor();
			if (color != 0) {
				mFolderGridView.setmTextColor(color);
			} else {
				mFolderGridView.setmTextColor(Color.WHITE);
			}
		} else {
			mFolderGridView.setmTextColor(mThemeController.getThemeBean().mAppIconBean.mTextColor);
		}

		// TODO:editBtn底图主题解析
		// mEditBtnDrawable =
		// mThemeController.getDrawable(folderBean.mFolderAddButton,
		// themePackage,false);
		// if(mEditBtnDrawable == null){
		// mEditBtnDrawable =
		// mActivity.getResources().getDrawable(R.drawable.appfunc_up);
		// }
		//
		// if (mEditBtnDrawable != null)
		// {
		// // 未点击时的按钮图片
		// mEditBtn.setBackgroundDrawable(mEditBtnDrawable);
		// }
		StateListDrawable drawable = new StateListDrawable();
		Drawable normalDrawable = mThemeController.getDrawable(folderBean.mFolderAddButton,
				themePackage, false);
		Drawable pressDrawable = mThemeController.getDrawable(folderBean.mFolderAddButtonLight,
				themePackage, false);
		drawable.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled },
				pressDrawable);
		drawable.addState(new int[] { android.R.attr.state_enabled }, normalDrawable);
		mEditBtn.setBackgroundDrawable(drawable);
		mEditBtnWidth = (mEditBtn.getBackground()).getIntrinsicWidth();

		// mEditBtnLightDrawable =
		// mThemeController.getDrawable(folderBean.mFolderAddButtonLight,themePackage,
		// false);
		// if(mEditBtnLightDrawable == null){
		// mEditBtnLightDrawable =
		// mActivity.getResources().getDrawable(R.drawable.appfunc_up_light);
		// }
		drawable = new StateListDrawable();
		normalDrawable = mThemeController.getDrawable(folderBean.mFolderSortButton, themePackage,
				false);
		pressDrawable = mThemeController.getDrawable(folderBean.mFolderSortButtonLight,
				themePackage, false);
		drawable.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled },
				pressDrawable);
		drawable.addState(new int[] { android.R.attr.state_enabled }, normalDrawable);
		mSortBtn.setBackgroundDrawable(drawable);
		/*---------------动画设置------------------*/
		GoSettingControler settingControler = GOLauncherApp.getSettingControler();
		mEffectSettingInfo = settingControler.getEffectSettingInfo();
	}

	// 设置动画效果的属性
	private boolean handleEffectSettingChange(final EffectSettingInfo settingInfo) {
		if (settingInfo != null) {
			mEffectSettingInfo = settingInfo;
		} else {
			mEffectSettingInfo = GOLauncherApp.getSettingControler().getEffectSettingInfo();
		}

		boolean ret = false;
		if (mEffectSettingInfo != null) {
			mFolderGridView.setScrollDuration(mEffectSettingInfo.getDuration());
			if (mEffectSettingInfo.mEffectorType == CoupleScreenEffector.EFFECTOR_TYPE_RANDOM_CUSTOM) {
				mFolderGridView
						.setCustomRandomEffectorEffects(mEffectSettingInfo.mEffectCustomRandomEffects);
			}
			mFolderGridView.setEffector(mEffectSettingInfo.mEffectorType); // 先设置效果器，限制当前使用的弹力
			mFolderGridView.setOvershootAmount(mEffectSettingInfo.getOvershootAmount());
			mFolderGridView.setAutoTweakElasicity(mEffectSettingInfo.mAutoTweakElasticity);
			ret = true;
		}

		// 设置是否循环滚动
		GoSettingControler settingControler = GOLauncherApp.getSettingControler();
		ScreenSettingInfo screenSettingInfo = settingControler.getScreenSettingInfo();
		if (screenSettingInfo != null) {
			mFolderGridView.setCycleMode(screenSettingInfo.mScreenLooping);
		}

		return ret;
	}

	/*-------------------------end. 进出动画监听 .end -----------------------------*/

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {

			if (v == mLayout
					&& mLayout.getmStatus() == FolderRelativeLayout.DRAW_STATUS_NORMAL
					&& (event.getRawY() < mLayout.getmFolderTop() + GoLauncher.getStatusbarHeight() || event
							.getRawY() > mLayout.getmFolderBottom())) {
				close();
			}
		}
		return true;
	}

	@Override
	public void onRemove() {
		super.onRemove();
		// 改名字
		saveRename();

		// 释放资源
		clear();

		// 使用Handler来处理更新数据库是防止出现退出动画会卡
		handleUpdateDB();

		mInfo.mOpened = false;
		// 通知各层正常显示
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.FOLDER_CLOSED, -1,
				mDragView, null);
		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.FOLDER_CLOSED, -1, null,
				null);
	}

	@Override
	public boolean isOpaque() {
		// 如果有桌面截图，就不透明；否则就是透明
		// return mLayout.getmScreenshotBmp() != null;
		return true;
	}

	/**
	 * 释放资源
	 */
	private void clear() {
		// 释放资源反注册mRenameEdit，deskIcon里包含的TextFont
		try {
			if (mRenameEdit != null) {
				mRenameEdit.selfDestruct();
			}
			int mcount = mFolderGridView.getChildCount();
			for (int i = 0; i < mcount; i++) {
				CellGridView cellGridView = (CellGridView) mFolderGridView.getChildAt(i);
				int cellCount = cellGridView.getChildCount();
				for (int j = 0; j < cellCount; j++) {
					BubbleTextView icon = (BubbleTextView) cellGridView.getChildAt(j);
					icon.selfDestruct();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		mFolderGridView.removeAllViewsInLayout();

		OutOfMemoryHandler.handle();

		if (mInfo != null && mInfo instanceof UserFolderInfo) {
			mInfo.mOpened = false;
			UserFolderInfo folderInfo = mInfo;
			// TODO:浩均测一下有没泄露问题
			// final int count = folderInfo.getChildCount();
			// for (int i=0; i<count; i++)
			// {
			// ShortCutInfo itemInfo = folderInfo.getChildInfo(i);
			// if (itemInfo != null)
			// {
			// itemInfo.selfDestruct();
			// }
			// }
			folderInfo.selfDestruct();
		}
		mLayout.clear();
		mFolderGridView.destroyChildrenDrawingCache();
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SET_WORKSPACE_DRAWING_CACHE, 0, null, null);
		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.EVENT_FOLDER_CLOSE, 0,
				null, null);
		mFolderGridView.getDragItemData().clear();
		mFolderGridView.getTargetPosition().clear();
	}

	/**
	 * 使用Handler来处理更新数据库是防止出现退出动画会卡
	 */
	private void handleUpdateDB() {
		Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				updateDB();
			}
		};
		Message msg = new Message();
		handler.sendMessage(msg);
	}

	private void saveRename() {
		String newName = mRenameEdit.getText().toString();
		String oldName = mInfo.mTitle.toString();
		if ((null == newName && null != oldName) || (null != newName && !newName.equals(oldName))) {
			// 通知存储rename
			ArrayList<String> nameList = new ArrayList<String>();
			nameList.add(newName);
			if (mType == TYPE_SCREEN) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.FOLDER_RENAME,
						-1, mInfo.mInScreenId, nameList);
			} else if (mType == TYPE_DOCK) {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.FOLDER_RENAME, -1,
						mInfo.mInScreenId, nameList);
			}
			nameList.clear();
			nameList = null;
		}
	}

	/**
	 * 更新数据库
	 */
	private void updateDB() {
		if (mFolderGridView.getReplaced() || mSortFlag) {
			// 发生过换位，要改位置信息
			ArrayList<ShortCutInfo> infos = mFolderGridView.getContents();
			final int updateSortType = mSortFlag ? UPDATE_SORT_TYPE : KEEP_SORT_TYPE;
			mInfo.mSortTpye = mSortTpye;
			if (mType == TYPE_SCREEN) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.FOLDER_REPLACE_INDEX, updateSortType, mInfo, infos);
			} else if (mType == TYPE_DOCK) {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
						IDiyMsgIds.FOLDER_REPLACE_INDEX, updateSortType, mInfo, infos);
			}
			infos.clear();
			infos = null;
		}
	}

	/*-----------------指示器的点击监听--------------------*/
	@Override
	public void clickIndicatorItem(int index) {
		if (mFolderGridView != null && index < mFolderGridView.getChildCount()) {
			mFolderGridView.snapToScreen(index, false, -1);
		}

	}

	@Override
	public void sliding(float percent) {
		if (0 <= percent && percent <= 100) {
			mFolderGridView.getScreenScroller().setScrollPercent(percent);
		}
	}

	@Override
	public void updateIndicator(int num, int current) {
		if (num >= 0 && current >= 0 && current < num) {
			mFolderIndicator.setTotal(num);
			mFolderIndicator.setCurrent(current);
		}
	}

	@Override
	public void onOpened() {
		// mSortTpye = mInfo.mSortTpye;
		mFolderGridView.setmOpened(true);
		// 把第一屏外的其他屏加入
		mFolderGridView.initGridViewDataAfterOpened(mInfo);
		mFolderGridView.setListeners(this, this);

		try {
			CellGridView cellGridView = (CellGridView) mFolderGridView.getChildAt(0);
			FolderAdapter folderAdapter = (FolderAdapter) cellGridView.getAdapter();
			folderAdapter.notifyDataSetInvalidated();
		} catch (Exception e) {
			// 不处理
		}

//		if (StaticTutorial.sCheckScreenFolder) {
//			StaticTutorial.sCheckScreenFolder = false;
//			// 桌面文件夹帮助提示
//			PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
//					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
//			boolean needToShow = sharedPreferences.getBoolean(
//					IPreferencesIds.SHOULD_SHOW_SCREENFOLDER_GUIDE, true);
//			if (needToShow) {
//				Editor editor = sharedPreferences.edit();
//				GuideForGlFrame.setmGuideType(GuideForGlFrame.GUIDE_TYPE_SCREENFOLDER);
//				// Rect r = mLayout.getmFolderIconRectOnOpened();
//				// GuideScreenFolderLightView.setFolderRect(r);
//				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
//				editor.putBoolean(IPreferencesIds.SHOULD_SHOW_SCREENFOLDER_GUIDE, false);
//				editor.commit();
//			}
//		}
	}

	/**
	 * 显示操作菜单
	 */
	private boolean showQuickActionMenu(View target) {
		// hideQuickActionMenu(false);
		if (target == null) {
			return false;
		}

		int[] xy = new int[2];
		target.getLocationInWindow(xy);
		Rect targetRect = new Rect(xy[0], xy[1], xy[0] + target.getWidth(), xy[1]
				+ target.getHeight());

		mQuickActionMenu = new QuickActionMenu(mActivity, target, targetRect, mLayout, this);

		ItemInfo itemInfo = (ItemInfo) target.getTag();
		if (itemInfo != null && itemInfo.mItemType != IItemType.ITEM_TYPE_FAVORITE) {
			int itemType = itemInfo.mItemType;

			switch (itemType) {
				case IItemType.ITEM_TYPE_APPLICATION :
					mQuickActionMenu.addItem(IQuickActionId.CHANGE_ICON, R.drawable.icon_change,
							R.string.changeicontext);
					mQuickActionMenu.addItem(IQuickActionId.RENAME, R.drawable.icon_rename,
							R.string.renametext);
					mQuickActionMenu.addItem(IQuickActionId.DELETE, R.drawable.icon_del,
							R.string.deltext);
					mQuickActionMenu.addItem(IQuickActionId.UNINSTALL, R.drawable.icon_uninstall,
							R.string.uninstalltext);
					break;

				case IItemType.ITEM_TYPE_SHORTCUT :
					mQuickActionMenu.addItem(IQuickActionId.CHANGE_ICON, R.drawable.icon_change,
							R.string.changeicontext);
					mQuickActionMenu.addItem(IQuickActionId.RENAME, R.drawable.icon_rename,
							R.string.renametext);
//					if (!DockUtil.isTheLastDockAppdrawer(target)) {
						mQuickActionMenu.addItem(IQuickActionId.DELETE, R.drawable.icon_del,
								R.string.deltext);
//					}
					break;

				default :
					break;
			}
		}

		mQuickActionMenu.show();
		return true;
	}

	@Override
	public void onActionClick(int action, Object target) {
		switch (action) {
			case IQuickActionId.CHANGE_ICON :
				if (null != ((View) target).getTag()
						&& ((View) target).getTag() instanceof ShortCutInfo) {
					String defaultNameString = "";
					Bitmap defaultBmp = null;
					Bundle bundle = new Bundle();

					if (mType == TYPE_SCREEN) {
						ChangeIconPreviewActivity
								.setFromWhatRequester(ChangeIconPreviewActivity.SCREEN_FOLDER_ITEM_STYLE);
					} else if (mType == TYPE_DOCK) {
						ChangeIconPreviewActivity
								.setFromWhatRequester(ChangeIconPreviewActivity.DOCK_FOLDER_ITEM_STYLE);
					}

					ShortCutInfo shortCutInfo = (ShortCutInfo) ((View) target).getTag();
					BitmapDrawable iconDrawable = null;
					if (null != shortCutInfo.getRelativeItemInfo()) {
						AppItemInfo appItemInfo = shortCutInfo.getRelativeItemInfo();
						iconDrawable = appItemInfo.getIcon();
						if (null != appItemInfo.mTitle) {
							defaultNameString = appItemInfo.mTitle.toString(); // 系统图标名称
						}
						if (null != iconDrawable) {
							defaultBmp = iconDrawable.getBitmap();
						}
					}
					if (defaultNameString.equals("")) {
						if (null != shortCutInfo.mTitle) {
							defaultNameString = shortCutInfo.mTitle.toString();
						} else {
							defaultNameString = shortCutInfo.getFeatureTitle();
						}
					}

					bundle.putString(ChangeIconPreviewActivity.DEFAULT_NAME, defaultNameString);
					if (defaultBmp != null) {
						bundle.putParcelable(ChangeIconPreviewActivity.DEFAULT_ICON_BITMAP,
								defaultBmp);
					}

					Intent intent = new Intent(mActivity, ChangeIconPreviewActivity.class);
					intent.putExtras(bundle);
					try {
						mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_THEME_FORICON);
					} catch (SecurityException e) {
						Toast.makeText(mActivity, "SecurityException, operation Fail!", Toast.LENGTH_SHORT).show();
					}
				}
				break;

			case IQuickActionId.RENAME :
				if (target instanceof BubbleTextView && null != ((BubbleTextView) target).getTag()) {
					ShortCutInfo shortCutInfo = (ShortCutInfo) ((View) target).getTag();

					Intent intent = new Intent(mActivity, RenameActivity.class);
					CharSequence title = ((BubbleTextView) target).getText();
					intent.putExtra(RenameActivity.NAME, title);
					intent.putExtra(RenameActivity.HANDLERID, IDiyFrameIds.DESK_USER_FOLDER_FRAME);
					intent.putExtra(RenameActivity.ITEMID, shortCutInfo.mInScreenId);
					intent.putExtra(RenameActivity.SHOW_RECOMMENDEDNAME, false);
					intent.putExtra(RenameActivity.FINISH_WHEN_CHANGE_ORIENTATION, true);
					mActivity.startActivityForResult(intent, IRequestCodeIds.REQUEST_RENAME);
				}
				break;

			case IQuickActionId.DELETE :
				View view = (View) target;

				if (mType == TYPE_SCREEN) {
					// 删除文件夹打开界面的图标（缓存）
					handleMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
							IDiyMsgIds.DELETE_CACHE_INFO_IN_FOLDER, -1, view.getTag(), null);
					// 删除数据库
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREEN_REMOVE_FOLDER_ITEM, -1, view, null);
				} else if (mType == TYPE_DOCK) {
					GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
							IDiyMsgIds.DOCK_DELETE_FOLDERITEM, -1, view, null);
				}
				break;

			case IQuickActionId.UNINSTALL :
				actionUninstall((View) target);
				break;

			default :
				break;
		}

	}

	private void resetDefaultIcon() {
		if (mDragView != null) {
			ShortCutInfo tagInfo = (ShortCutInfo) mDragView.getTag();
			tagInfo.resetFeature();
			// 通知外部修改数据
			ArrayList<ShortCutInfo> list = new ArrayList<ShortCutInfo>();
			list.add(tagInfo);
			if (mType == TYPE_SCREEN) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.UPDATE_FOLDER_ITEM_INFO, -1, mInfo.mInScreenId, list);
			} else if (mType == TYPE_DOCK) {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
						IDiyMsgIds.UPDATE_FOLDER_ITEM_INFO, -1, mInfo.mInScreenId, list);
			}
			list.clear();
			list = null;
			mDragView.setIcon(tagInfo.mIcon);
		}
	}

	/**
	 * 取消弹出菜单
	 * 
	 * @param dismissWithCallback
	 *            ， 是否回调， true仅取消菜单显示，false会回调到
	 *            {@link QuickActionMenu.onActionListener#onActionClick(int, View)}
	 *            并传回一个{@link IQuickActionId#CANCEL}事件
	 */
	private void hideQuickActionMenu(boolean dismissWithCallback) {
		if (mQuickActionMenu != null) {
			if (dismissWithCallback) {
				mQuickActionMenu.cancel();
			} else {
				mQuickActionMenu.dismiss();
			}
			mQuickActionMenu = null;
		}
	}

	private void actionUninstall(View editView) {
		if (editView == null) {
			return;
		}

		if ((editView instanceof BubbleTextView) || (editView instanceof TextView)) {
			if (editView.getTag() instanceof ShortCutInfo) {
				ShortCutInfo shortCutInfo = (ShortCutInfo) editView.getTag();
				if (shortCutInfo.mIntent != null) {
					final ComponentName componentName = shortCutInfo.mIntent.getComponent();
					if (componentName != null) {
						try {
							// go主题和go精品假图标提示用户不能删除
							if (ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE
									.equals(shortCutInfo.mIntent.getAction())
									|| ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME
											.equals(shortCutInfo.mIntent.getAction())
									|| ICustomAction.ACTION_FUNC_SPECIAL_APP_GOWIDGET
											.equals(shortCutInfo.mIntent.getAction())) {
								ScreenUtils.showToast(R.string.uninstall_fail, mActivity);
							} else {
								AppUtils.uninstallPackage(mActivity, componentName.getPackageName());
							}
						} catch (Exception e) {
							// 处理卸载异常
							ScreenUtils.showToast(R.string.uninstall_fail, mActivity);
						}
					}
				} else {
					// 卸载失败
					ScreenUtils.showToast(R.string.uninstall_fail, mActivity);
				}
			}
		}
	}

	private void renameItem(String name) {
		if (null != mDragView && null != mDragView.getTag()) {
			mDragView.setText(name);

			ShortCutInfo tagInfo = (ShortCutInfo) mDragView.getTag();
			tagInfo.setFeatureTitle(name);
			tagInfo.mTitle = name;
			tagInfo.mIsUserTitle = true;

			// 通知外部修改数据
			ArrayList<ShortCutInfo> list = new ArrayList<ShortCutInfo>();
			list.add(tagInfo);
			if (mType == TYPE_SCREEN) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.UPDATE_FOLDER_ITEM_INFO, -1, mInfo.mInScreenId, list);
			} else if (mType == TYPE_DOCK) {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
						IDiyMsgIds.UPDATE_FOLDER_ITEM_INFO, -1, mInfo.mInScreenId, list);
			}
			list.clear();
			list = null;
		}
	}

	private void actionChangeIcon(Bundle iconBundle) {
		if ((mDragView != null) && (iconBundle != null)) {
			ShortCutInfo tagInfo = (ShortCutInfo) mDragView.getTag();
			if (tagInfo == null) {
				Log.i("folder", "change icon fail tagInfo == null");
				return;
			}

			Drawable iconDrawable = null;
			boolean isDefaultIcon = false;
			int type = iconBundle.getInt(ImagePreviewResultType.TYPE_STRING);
			if (ImagePreviewResultType.TYPE_RESOURCE_ID == type) {
				int id = iconBundle.getInt(ImagePreviewResultType.IMAGE_ID_STRING);
				iconDrawable = mActivity.getResources().getDrawable(id);
				tagInfo.setFeatureIcon(iconDrawable, type, null, id, null);
			} else if (ImagePreviewResultType.TYPE_IMAGE_FILE == type) {
				String path = iconBundle.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
				tagInfo.setFeatureIcon(null, type, null, 0, path);
				if (tagInfo.prepareFeatureIcon()) {
					iconDrawable = ((FeatureItemInfo) tagInfo).getFeatureIcon();
				}
			} else if (ImagePreviewResultType.TYPE_IMAGE_URI == type) {
				String path = iconBundle.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
				tagInfo.setFeatureIcon(null, type, null, 0, path);
				if (tagInfo.prepareFeatureIcon()) {
					iconDrawable = ((FeatureItemInfo) tagInfo).getFeatureIcon();
				}
			} else if (ImagePreviewResultType.TYPE_PACKAGE_RESOURCE == type
					|| ImagePreviewResultType.TYPE_APP_ICON == type) {
				String packageStr = iconBundle.getString(ImagePreviewResultType.IMAGE_PACKAGE_NAME);
				String path = iconBundle.getString(ImagePreviewResultType.IMAGE_PATH_STRING);
				ImageExplorer imageExplorer = ImageExplorer.getInstance(mActivity);
				iconDrawable = imageExplorer.getDrawable(packageStr, path);

				if (null != iconDrawable) {
					tagInfo.setFeatureIcon(iconDrawable, type, packageStr, 0, path);
				}
			} else {
				BitmapDrawable bmp = null;
				bmp = (null != tagInfo.getRelativeItemInfo()) ? tagInfo.getRelativeItemInfo()
						.getIcon() : null;

				if (null != bmp) {
					bmp.setTargetDensity(mActivity.getResources().getDisplayMetrics());
					iconDrawable = bmp;
					isDefaultIcon = true;
				}

				tagInfo.resetFeature();
			}

			if (iconDrawable == null) {
				Toast.makeText(mActivity, R.string.save_image_error, Toast.LENGTH_LONG).show();
				return;
			}

			mDragView.setIcon(iconDrawable);
			tagInfo.mIsUserIcon = !isDefaultIcon;

			// 通知外部修改数据
			ArrayList<ShortCutInfo> list = new ArrayList<ShortCutInfo>();
			list.add(tagInfo);
			if (mType == TYPE_SCREEN) {
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.UPDATE_FOLDER_ITEM_INFO, -1, mInfo.mInScreenId, list);
			} else if (mType == TYPE_DOCK) {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
						IDiyMsgIds.UPDATE_FOLDER_ITEM_INFO, -1, mInfo.mInScreenId, list);
			}
			list.clear();
			list = null;
		}
	}

	/**
	 * 展示 图标排序对话框
	 */
	public void showSelectSortDialog() {
		mSortTpye = -1;
		mDialog = new DialogSingleChoice(mActivity);
		mDialog.show();
		mDialog.setTitle(R.string.dlg_sortChangeTitle);
		final CharSequence[] items = mActivity.getResources().getTextArray(R.array.folder_select_sort_style);
		mDialog.setItemData(items, mSortTpye, true);
		mDialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				mSortTpye = item;
				if (mSortTpye == SORTTYPE_LETTER) {
					// 进行排序
					Collections.sort(mFolderGridView.getContents(), new Comparator<Object>() {
						@Override
						public int compare(Object object1, Object object2) {
							int result = 0;
							CharSequence chars1 = ((ShortCutInfo) object1).mTitle;
							CharSequence chars2 = ((ShortCutInfo) object2).mTitle;
							if (chars1 == null || chars2 == null) {
								return result;
							}
							// 按字符串类型比较
							String str1 = chars1.toString();
							String str2 = chars2.toString();
							Collator collator = Collator.getInstance(Locale.CHINESE);
							if (collator == null) {
								collator = Collator.getInstance(Locale.getDefault());
							}
							result = collator.compare(str1.toUpperCase(), str2.toUpperCase());
							return result;
						}
					});
					//用户行为统计---文件夹排序
					StatisticsData.countUserActionData(
							StatisticsData.DESK_ACTION_ID_FLODER_SORT_BY_LETTER,
							StatisticsData.USER_ACTION_ONE, IPreferencesIds.DESK_ACTION_DATA);
				} else if (mSortTpye == SORTTYPE_TIMENEAR) {
					// 进行排序
					Collections.sort(mFolderGridView.getContents(), new Comparator<Object>() {
						@Override
						public int compare(Object object1, Object object2) {
							int result = 0;
							// 按int类型比较
							long value1 = ((ShortCutInfo) object1).mTimeInFolder;
							long value2 = ((ShortCutInfo) object2).mTimeInFolder;
							if (value1 == value2) {
								return result;
							}
							int temInt = value2 > value1 ? 1 : -1;
							result = temInt;
							return result;
						}
					});
					//用户行为统计---文件夹排序
					StatisticsData.countUserActionData(
							StatisticsData.DESK_ACTION_ID_FLODER_SORT_BY_TIME_ASC,
							StatisticsData.USER_ACTION_TWO, IPreferencesIds.DESK_ACTION_DATA);
				} else if (mSortTpye == SORTTYPE_TIMEREMOTE) {
					// 进行排序
					Collections.sort(mFolderGridView.getContents(), new Comparator<Object>() {
						@Override
						public int compare(Object object1, Object object2) {
							int result = 0;
							// 按int类型比较
							long value1 = ((ShortCutInfo) object1).mTimeInFolder;
							long value2 = ((ShortCutInfo) object2).mTimeInFolder;
							if (value1 == value2) {
								return result;
							}
							int temInt = value1 > value2 ? 1 : -1;
							result = temInt;
							return result;
						}
					});
					//用户行为统计---文件夹排序
					StatisticsData.countUserActionData(
							StatisticsData.DESK_ACTION_ID_FLODER_SORT_BY_TIME_DEC,
							StatisticsData.USER_ACTION_THREE, IPreferencesIds.DESK_ACTION_DATA);
				}
				mFolderGridView.sortIcon();
				// 需要数据写入
				mSortFlag = true;
			
			}
		});
	} // end showSelectSortDialog

	
	//关闭选择排序对话框 add by: zzf
	private void closeSelectSortDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}

	}
	
	// 在进行文件夹图标排序时，暂时不进行loading的转圈（看后期需求）
	private void beginSortIcon(final int itemCaused) {
		// showProgressDialog();

		GOLauncherApp.getSettingControler().getFunAppSetting().setSortType(itemCaused, false);

		// 通知桌面文件夹排序方式改变
		// GoLauncher.sendHandler(this, IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.EVENT_UPDATE_ALL_FOLDER_PREVIEW, -1, null, null);
		// 启动一个新线程去调用后台
		new Thread(ThreadName.FOLDER_SORT_ICON) {
			@Override
			public void run() {
				try {
					switch (itemCaused) {
						case SORTTYPE_LETTER : // "ASC"
							// 进行排序
							Collections.sort(mFolderGridView.getContents(),
									new Comparator<Object>() {
										@Override
										public int compare(Object object1, Object object2) {
											int result = 0;
											CharSequence chars1 = ((ShortCutInfo) object1).mTitle;
											CharSequence chars2 = ((ShortCutInfo) object2).mTitle;
											if (chars1 == null || chars2 == null) {
												return result;
											}
											// 按字符串类型比较
											String str1 = chars1.toString();
											String str2 = chars2.toString();
											Collator collator = Collator
													.getInstance(Locale.CHINESE);
											if (collator == null) {
												collator = Collator.getInstance(Locale.getDefault());
											}
											result = collator.compare(str1.toUpperCase(),
													str2.toUpperCase());
											return result;
										}
									});

							break;
						case SORTTYPE_TIMENEAR : // "DESC"
							// 进行排序
							Collections.sort(mFolderGridView.getContents(),
									new Comparator<Object>() {
										@Override
										public int compare(Object object1, Object object2) {
											int result = 0;
											// 按int类型比较
											long value1 = ((ShortCutInfo) object1).mTimeInFolder;
											long value2 = ((ShortCutInfo) object2).mTimeInFolder;
											if (value1 == value2) {
												return result;
											}
											int temInt = value2 > value1 ? 1 : -1;
											result = temInt;
											return result;
										}
									});

							break;
						case SORTTYPE_TIMEREMOTE : // "ASC"
							// 进行排序
							Collections.sort(mFolderGridView.getContents(),
									new Comparator<Object>() {
										@Override
										public int compare(Object object1, Object object2) {
											int result = 0;
											// 按int类型比较
											long value1 = ((ShortCutInfo) object1).mTimeInFolder;
											long value2 = ((ShortCutInfo) object2).mTimeInFolder;
											if (value1 == value2) {
												return result;
											}
											int temInt = value1 > value2 ? 1 : -1;
											result = temInt;
											return result;
										}
									});
							break;

						default :
							break;
					}
				} catch (Exception e) {
					// dismissProgressDialog();
				}
			}
		}.start();
	} // end beginSortIcon
}

package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditLargeTabView;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.MenuItemBean;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 添加go快捷方式
 */
public class AddGoShortCutTab extends BaseTab {

	private static long sCLICKTIME = 300;
	private long mLastTime; // 上次的点击时间
	private GoProgressBar mGoProgressBar;
	private final static int LIST_INIT_OK = 1000;

	public AddGoShortCutTab(Context context, String tag, int level) {
		super(context, tag, level);
		initListByLoading();
		mLastTime = System.currentTimeMillis();
		mIsNeedAsyncLoadData = true;
	}

	@Override
	public ArrayList<Object> getDtataList() {
		return (ArrayList) mList;
	}

	@Override
	public int getItemCount() {
		if (mList != null) {
			return mList.size();
		}
		return 0;
	}

	@Override
	public View getView(int position) {
		ShortCutInfo itemInfo = mList.get(position);
		View view = mInflater.inflate(R.layout.screen_edit_item_theme, null);
		ImageView image = (ImageView) view.findViewById(R.id.thumb);
		image.setImageDrawable(itemInfo.mIcon);
		TextView mText = (TextView) view.findViewById(R.id.title);
		mText.setText(itemInfo.mTitle);
		view.setTag(itemInfo);
		return view;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		long curTime = System.currentTimeMillis();
		if (curTime - mLastTime < sCLICKTIME) {
			return;
		}
		mLastTime = curTime;

		if (!resetTag(v)) {
			return;
		}
		if (!checkScreenVacant(1, 1)) {
			return;
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_AUTO_FLY,
				DragFrame.TYPE_ADD_APP_DRAG, v, null);
	}

	@Override
	public boolean onLongClick(View v) {
		// if(!resetTag(v)){
		// return false;
		// }
		//
		// ArrayList<Object> list = new ArrayList<Object>();
		// list.add(Workspace.getLayoutScale());
		// list.add(0);
		// list.add(0);
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.DRAG_START, DragFrame.TYPE_ADD_APP_DRAG, v, list);
		return false;
	}

	public static boolean resetTag(View v) {
		if (null == v || null == v.getTag()) {
			return false;
		}
		if (v.getTag() instanceof ShortCutInfo) {
			ShortCutInfo ret = new ShortCutInfo();
			ShortCutInfo info = (ShortCutInfo) v.getTag();
			ret.mIcon = info.mIcon;
			ret.mIntent = info.mIntent;
			ret.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
			ret.mSpanX = 1;
			ret.mSpanY = 1;
			ret.mTitle = info.mTitle;
			v.setTag(ret);
		} else {
			return false;
		}
		return true;
	}

	@Override
	public void clearData() {
		if (mList != null) {
			mList.clear();
			mList = null;
		}
		if (mGoProgressBar != null) {
			mGoProgressBar = null;
		}
		super.clearData();
	}

	@Override
	public void resetData() {
		// TODO Auto-generated method stub
	}

	private void initListByLoading() {
		// 显示提示框
		showProgressDialog();
		new Thread(ThreadName.SCREEN_EDIT_THEMETAB) {
			@Override
			public void run() {
				// 初始化数据
				initShortcutItem();
				// 对外通知
				Message msg = new Message();
				msg.what = LIST_INIT_OK;
				mHandler.sendMessage(msg);
			};
		}.start();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LIST_INIT_OK :
					// 刷新
					if (mTabActionListener != null) {
						mTabActionListener.onRefreshTab(BaseTab.TAB_ADDGOSHORTCUT, 0);
					}
					dismissProgressDialog();
					break;

				default :
					break;
			}
		};
	};

	private void showProgressDialog() {
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		if (screenEditBoxFrame != null) {
			ScreenEditLargeTabView mLayOutView = screenEditBoxFrame.getLargeTabView();
			mGoProgressBar = (GoProgressBar) mLayOutView.findViewById(R.id.edit_tab_progress);
			if (mGoProgressBar != null) {
				mGoProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void dismissProgressDialog() {
		if (mGoProgressBar != null) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	private String[] mIntentActions;
	private String[] mTitles;
	private int[] mDrawableIds;
	// add 初始化GO桌面快捷方式

	private List<ShortCutInfo> mList;

	public static final int MAIN_SCREEN = 0;
	public static final int MAIN_SCREEN_OR_PREVIEW = 1;
	public static final int FUNCMENU = 2;
	public static final int NOTIFICATION = 3;
	public static final int STATUS_BAR = 4;
	public static final int THEME_SETTING = 5;
	public static final int PREFERENCES = 6;
	public static final int GO_STORE = 7;
	public static final int PREVIEW = 8;
	public static final int LOCK_SCREEN = 9;
	public static final int DOCK_BAR = 10;
	public static final int MAIN_MENU = 11;
	public static final int DIY_GESTURE = 12;
	public static final int PHOTO = 13;
	public static final int MUSIC = 14;
	public static final int VIDEO = 15;

	private void initShortcutItem() {
		try {
			if (null == mList) {
				mList = new ArrayList<ShortCutInfo>();
			}
			initChoiceItem();
			final int count = mIntentActions.length;
			final String goComponentName = "com.gau.launcher.action";
			ShortCutInfo itemInfo = null;
			Intent intent = null;
			ComponentName cmpName = null;
			for (int i = 0; i < count; i++) {
				itemInfo = new ShortCutInfo();
				intent = new Intent(mIntentActions[i]);
				cmpName = new ComponentName(goComponentName, mIntentActions[i]);
				intent.setComponent(cmpName);
				itemInfo.mIntent = intent;
				itemInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
				itemInfo.mTitle = mTitles[i];
				itemInfo.mIcon = getItemImage(null, mDrawableIds[i], mContext, null);

				mList.add(itemInfo);

				itemInfo = null;
				intent = null;
				cmpName = null;
			}
			mIntentActions = null;
			mTitles = null;
			mDrawableIds = null;
		} catch (Exception e) {
		}

	}

	public static Drawable getItemImage(DeskThemeBean.MenuBean menuBean, int id, Context context,
			ImageExplorer imageExplorer) {
		Drawable ret = null;
		// 从主题获取(以后可能要用到)
		if (null != menuBean && null != menuBean.mItems) {

			int len = menuBean.mItems.size();
			for (int i = 0; i < len; i++) {
				MenuItemBean itemBean = menuBean.mItems.get(i);
				if (null == itemBean) {
					continue;
				}
				if (itemBean.mId == id) {
					if (null != itemBean.mImage) {
						ret = getDrawable(imageExplorer, itemBean.mImage.mResName);
					}
					break;
				}
			}
		}
		// 从主程序获取
		if (null == ret) {
			switch (id) {
				case MAIN_SCREEN :
					ret = getIcons(context, R.drawable.go_shortcut_mainscreen);
					break;

				case MAIN_SCREEN_OR_PREVIEW :
					ret = getIcons(context, R.drawable.go_shortcut_main_or_preview);
					break;

				case FUNCMENU :
					ret = getIcons(context, R.drawable.go_shortcut_appdrawer);
					break;

				case NOTIFICATION :
					ret = getIcons(context, R.drawable.go_shortcut_notification);
					break;

				case STATUS_BAR :
					ret = getIcons(context, R.drawable.go_shortcut_statusbar);
					break;

				case THEME_SETTING :
					ret = getIcons(context, R.drawable.go_shortcut_themes);
					break;

				case PREFERENCES :
					ret = getIcons(context, R.drawable.go_shortcut_preferences);
					break;

				case GO_STORE :
					ret = getIcons(context, R.drawable.go_shortcut_store);
					break;

				case PREVIEW :
					ret = getIcons(context, R.drawable.go_shortcut_preview);
					break;

				case LOCK_SCREEN :
					ret = getIcons(context, R.drawable.go_shortcut_lockscreen);
					break;
				case DOCK_BAR :
					ret = getIcons(context, R.drawable.go_shortcut_hide_dock);
					break;
				case MAIN_MENU : {
					ret = getIcons(context, R.drawable.go_shortcut_menu);
					break;
				}
				case DIY_GESTURE : {
					ret = getIcons(context, R.drawable.go_shortcut_diygesture);
					break;
				}
				case  PHOTO: {
					ret =  context.getResources().getDrawable(
							R.drawable.go_shortcut_photo);
					break;
				}
				case  MUSIC: {
					ret =  context.getResources().getDrawable(
							R.drawable.go_shortcut_music);
					break;
				}
				case VIDEO : {
					ret =  context.getResources().getDrawable(
							R.drawable.go_shortcut_video);
					break;
				}

				default :
					break;
			}
		}
		return ret;
	}

	/*private Drawable getDrawable(Context context, int resId) {
		Drawable ret = null;
		if (null == context) {
			return ret;
		}
		try {
			if (Machine.isTablet(context)) {
				ret = ImageExplorer.getInstance(context).getDrawableForDensity(mContext.getResources(), resId);
			}
			if (null == ret) {
				ret = context.getResources().getDrawable(resId);
			}
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}*/

	private static Drawable getDrawable(ImageExplorer imageExplorer, String resName) {
		Drawable ret = null;
		if (null == imageExplorer || null == resName) {
			return ret;
		}
		try {
			ret = imageExplorer.getDrawable(resName);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	// 初始化列表的选项
	private void initChoiceItem() {
		mIntentActions = new String[] { ICustomAction.ACTION_SHOW_MAIN_SCREEN,
				ICustomAction.ACTION_SHOW_MAIN_OR_PREVIEW,
				ICustomAction.ACTION_SHOW_FUNCMENU_FOR_LAUNCHER_ACITON,
				ICustomAction.ACTION_SHOW_EXPEND_BAR, ICustomAction.ACTION_SHOW_HIDE_STATUSBAR,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME,
				ICustomAction.ACTION_SHOW_PREFERENCES,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE, ICustomAction.ACTION_SHOW_PREVIEW,
				// ICustomAction.ACTION_SHOW_LOCKER_SETTING
				ICustomAction.ACTION_ENABLE_SCREEN_GUARD, ICustomAction.ACTION_SHOW_DOCK,
				ICustomAction.ACTION_SHOW_MENU, ICustomAction.ACTION_SHOW_DIYGESTURE
				, ICustomAction.ACTION_SHOW_PHOTO 
				, ICustomAction.ACTION_SHOW_MUSIC 
				, ICustomAction.ACTION_SHOW_VIDEO };

		mTitles = new String[] {
				mContext.getString(R.string.customname_mainscreen),
				mContext.getString(R.string.customname_mainscreen_or_preview),
				mContext.getString(R.string.customname_Appdrawer),
				mContext.getString(R.string.customname_notification),
				mContext.getString(R.string.customname_status_bar),
				mContext.getString(R.string.customname_themeSetting),
				mContext.getString(R.string.customname_preferences),
				mContext.getString(R.string.customname_gostore),
				mContext.getString(R.string.customname_preview),
				// getString(R.string.customname_golocker)
				mContext.getString(R.string.goshortcut_lockscreen),
				mContext.getString(R.string.goshortcut_showdockbar),
				mContext.getString(R.string.customname_mainmenu),
				mContext.getString(R.string.customname_diygesture),
				mContext.getString(R.string.customname_photo),
				mContext.getString(R.string.customname_music),
				mContext.getString(R.string.customname_video)};

		mDrawableIds = new int[] { MAIN_SCREEN, MAIN_SCREEN_OR_PREVIEW, FUNCMENU, NOTIFICATION,
				STATUS_BAR, THEME_SETTING, PREFERENCES, GO_STORE, PREVIEW// , GO_LOCK
				, LOCK_SCREEN, DOCK_BAR, MAIN_MENU, DIY_GESTURE, PHOTO, MUSIC, VIDEO };
	}

	/**
	 * <br>
	 * 功能简述:通过drawableId拿推荐图标图片 <br>
	 * 功能详细描述:可以过滤某些图标进行download tag标签合成图片（tag图片共享一张，减少图片资源） <br>
	 * @param drawableId
	 * @return　经过合成规则处理后的图片
	 */
	private static Drawable getIcons(Context context, int drawableId) {
		Drawable tag = context.getResources().getDrawable(drawableId);

		Drawable drawable = context.getResources().getDrawable(R.drawable.screenedit_icon_bg);
		try {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas cv = new Canvas(bmp);
			ImageUtil.drawImage(cv, drawable, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			ImageUtil.drawImage(cv, tag, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			BitmapDrawable bmd = new BitmapDrawable(bmp);
			bmd.setTargetDensity(context.getResources().getDisplayMetrics());
			drawable = bmd;
		} catch (Throwable e) {
			// 出错则不进行download Tag合成图
		}

		return drawable;
	}
}
package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.jiubang.ggheart.apps.desks.Preferences.info.GoShortCutInfo;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils;
import com.jiubang.ggheart.components.MutilCheckGridView;
import com.jiubang.ggheart.components.MutilCheckViewAdapter;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * <br>类描述:GO快捷方式对话框
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-11-8]
 */
public class DialogGoShortCut extends DialogBase implements OnItemClickListener {
	private boolean mIsSingleChoise = true;	//是否单选
	private MutilCheckGridView mGridView;
	private boolean[] mBooleanListCheck;	//勾选列表，数组存储是因为相比列表占较少空间，且个数已确定
	private int mMaxCheckSize;	//多选最大值
	private int mCurCheckSize = 0;	//当前选择的数量
	private ArrayList<GoShortCutInfo> mAddItems;	// 将要添加的go快捷方式

	// add 初始化GO桌面快捷方式
	List<GoShortCutInfo> mList;
	private String[] mShortCutIds;	//Go快捷方式 对应的value
	private String[] mIntentActions; //Go快捷方式 对应的intent
	private int[] mDrawableIds; //Go快捷方式 对应的图片
	private int[] mTitles;	//Go快捷方式 对应显示的内容
	
	public DialogGoShortCut(Context context) {
		super(context);
	}

	public DialogGoShortCut(Context context, int theme) {
		super(context, theme);
	}

	@Override
	public View getView() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View mView = inflater.inflate(R.layout.dialog_go_shortcut_view, null);
		mTitle = (TextView) mView.findViewById(R.id.name);
		mOkButton = (Button) mView.findViewById(R.id.finish_btn);
		mCancelButton = (Button) mView.findViewById(R.id.cancle_btn);
		mGridView = (MutilCheckGridView) mView.findViewById(R.id.gridview);
		return mView;
	}

	/**
	 * 初始化数据
	 * @param isSingleChoise	是否单选
	 * @param maxCheckSize	如果是多选。最大选择值。单选可以给1
	 * @param defaultCheck	单选时默认给选择的值 null：不选择任何
	 */
	public void setItemData(boolean isSingleChoise, int maxCheckSize, int[] defaultCheck) {
		mIsSingleChoise = isSingleChoise;
		mMaxCheckSize = maxCheckSize;
		
		if (defaultCheck != null) {
			mCurCheckSize = defaultCheck.length;
		}

		initShortcutItem();
		int listSize = mList.size();
		mBooleanListCheck = new boolean[listSize];
		if (defaultCheck != null) {
			int defaultCheckSize = defaultCheck.length;
			for (int i = 0; i < defaultCheckSize; i++) {
				int num = defaultCheck[i];
				mBooleanListCheck[num] = true;
			}
		}

		mGridView.initLayoutData(listSize);
		setAdapter();
	}

	/**
	 * 初始化数据(桌面设置-手势设置专用)
	 * @param isSingleChoise	是否单选
	 * @param maxCheckSize	如果是多选。最大选择值。单选可以给1
	 * @param defaultCheck	单选时默认给选择的值 -1：不选择任何
	 */
	public void setItemData(String shortCutId) {
		mIsSingleChoise = true;
		mMaxCheckSize = 1;
		mCurCheckSize = 1;

		initShortcutItem();
		int listSize = mList.size();
		mBooleanListCheck = new boolean[listSize];
		//设置已勾选的值
		for (int i = 0; i < listSize; i++) {
			if (mShortCutIds[i].equals(shortCutId)) {
				mBooleanListCheck[i] = true;
				break;
			}
		}

		mGridView.initLayoutData(listSize);
		setAdapter();
	}

	public int getSingleChoiseCheckValue() {
		if (mIsSingleChoise) {
			int size = mBooleanListCheck.length;
			for (int i = 0; i < size; i++) {
				if (mBooleanListCheck[i]) {
					return i;
				}
			}
			return -1;
		} else {
			return -1;
		}

	}

	/**
	 * 获取选择的列表
	 * @return
	 */
	public ArrayList<GoShortCutInfo> getCheckList() {
		int size = mList.size();
		mAddItems = new ArrayList<GoShortCutInfo>();
		GoShortCutInfo appItemInfo = null;
		for (int index = 0; index < size; index++) {
			if (mBooleanListCheck[index]) {
				appItemInfo = mList.get(index);
				mAddItems.add(appItemInfo);
			}
		}
		return mAddItems;
	}

	@Override
	protected void onStop() {
		super.onStop();

		if (mList != null) {
			mList.clear();
		}
		mBooleanListCheck = null;

		try {
			if (mGridView != null) {
				mGridView.recyle();
			}
			mGridView = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<GoShortCutInfo> getAllList() {
		return mList;
	}

	/**
	 * 
	 * @author jiangchao
	 * 
	 */
	private class MyAdapter extends MutilCheckViewAdapter {
		public Context mContext;

		public MyAdapter(ArrayList<Object> list, int screenIndex, Context context) {
			super(list, screenIndex);
			mContext = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Object info = null;
			try {
				info = getItem(position);
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}

			if (info == null) {
				return null;
			}

			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(mContext);
				convertView = inflater.inflate(R.layout.folder_grid_item, parent, false);
			}

			//内容
			TextView textView = (TextView) convertView.findViewById(R.id.name);
			if (info instanceof GoShortCutInfo) {
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
						((GoShortCutInfo) info).mIcon, null, null);
				textView.setText(((GoShortCutInfo) info).mTitle);
			}
			textView.setTextSize(GoLauncher.getAppFontSize());

			//图片
			ImageView img = (ImageView) convertView.findViewById(R.id.choice);
			if (getCheckStatus(mScreen, position)) {
				img.setVisibility(View.VISIBLE);
			} else {
				img.setVisibility(View.INVISIBLE);
			}

			return convertView;
		}
	}

	private boolean getCheckStatus(int screen, int position) {
		if (mBooleanListCheck == null) {
			return false;
		} else {
			return mBooleanListCheck[screen * mGridView.getCountPerPage() + position];
		}
	}

	private void setAdapter() {
		if (mList == null) {
			return;
		}
		final int count = mList.size();
		mGridView.removeAllViews();
		int screenCount = mGridView.getScreenCount();	//屏幕数
		int itemsCountPerScreen = mGridView.getCountPerPage(); //每屏item个数
		int culumns = mGridView.getCellCol();
		for (int i = 0; i < screenCount; i++) {
			GridView page = new GridView(mContext);
			ArrayList<Object> tempList = new ArrayList<Object>();
			for (int j = 0; j < itemsCountPerScreen && itemsCountPerScreen * i + j < count; j++) {
				Object obj = mList.get(itemsCountPerScreen * i + j);
				tempList.add(obj);
			}
			page.setAdapter(new MyAdapter(tempList, i, mContext));
			page.setNumColumns(culumns);
			page.setHorizontalSpacing(0);
			page.setVerticalSpacing(0);
			page.requestLayout();
			page.setSelector(android.R.color.transparent);
			page.setOnItemClickListener(this);
			mGridView.addView(page);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MyAdapter adapter = (MyAdapter) parent.getAdapter();
		int itemsCountPerScreen = mGridView.getCountPerPage();	// 每一屏图标的最大个数
		int screenIndex = adapter.mScreen;	//屏幕数量
		int p = position + screenIndex * itemsCountPerScreen;	//计算选择ITEM在哪个位置
		if (p > mBooleanListCheck.length) {
			return;
		}

		// 是否单选
		if (mIsSingleChoise) {
			if (mBooleanListCheck[p]) {
				mBooleanListCheck[p] = false;
				mCurCheckSize = 0;
			} else {
				mBooleanListCheck[p] = true;
				mCurCheckSize = 1;
			}

			// 把所有的选择置空
			int size = mBooleanListCheck.length;
			for (int i = 0; i < size; i++) {
				if (i != p) {
					mBooleanListCheck[i] = false;
				}
			}

			// 循环把每个适配器的数据刷新
			int gridViewSize = mGridView.getChildCount();
			for (int i = 0; i < gridViewSize; i++) {
				MyAdapter adapterTemp = (MyAdapter) ((GridView) mGridView.getChildAt(i))
						.getAdapter();
				if (adapterTemp != null) {
					adapterTemp.notifyDataSetChanged();
				}
			}
		} else {
			//多选
			if (mBooleanListCheck[p]) {
				mBooleanListCheck[p] = false;
				mCurCheckSize = mCurCheckSize - 1;
			} else {
				if (mCurCheckSize >= mMaxCheckSize) {
					ScreenUtils.showToast(R.string.tab_add_no_more_room, mContext);
					return;
				}

				mBooleanListCheck[p] = true;
				mCurCheckSize = mCurCheckSize + 1;
			}
			adapter.notifyDataSetChanged(); // 只刷新当前适配器的数据
		}

		if (mCurCheckSize <= 0) {
			setOkButtonEnabled(false);
		} else {
			setOkButtonEnabled(true);
		}

	}

	public void setOkButtonEnabled(boolean flag) {
		if (mOkButton != null) {
			if (flag) {
				mOkButton.setEnabled(true);
				mOkButton.setTextColor(mContext.getResources().getColor(
						R.color.desk_setting_item_title_color));
			} else {
				mOkButton.setEnabled(false);
				mOkButton.setTextColor(mContext.getResources().getColor(
						R.color.desk_setting_item_summary_color));
			}
		}
	}


	private void initShortcutItem() {
		if (null == mList) {
			mList = new ArrayList<GoShortCutInfo>();
		}
		initChoiceItem();
		final int count = mIntentActions.length;
		final String goComponentName = "com.gau.launcher.action";
		GoShortCutInfo itemInfo = null;
		Intent intent = null;
		ComponentName cmpName = null;
		for (int i = 0; i < count; i++) {
			itemInfo = new GoShortCutInfo();
			intent = new Intent(mIntentActions[i]);
			cmpName = new ComponentName(goComponentName, mIntentActions[i]);
			intent.setComponent(cmpName);
			itemInfo.mIntent = intent;
			itemInfo.mShortCutId = mShortCutIds[i];
			itemInfo.mTitle = mContext.getText(mTitles[i]);
			itemInfo.mIcon = getIcons(mDrawableIds[i]);

			mList.add(itemInfo);

			itemInfo = null;
			intent = null;
			cmpName = null;
		}
		mIntentActions = null;
		mTitles = null;
		mDrawableIds = null;

	}

	// 初始化列表的选项
	private void initChoiceItem() {
		//
		mShortCutIds = mContext.getResources().getStringArray(R.array.gesture_goshortcut_value);

		mIntentActions = new String[] {
				ICustomAction.ACTION_SHOW_MAIN_SCREEN,
				ICustomAction.ACTION_SHOW_MAIN_OR_PREVIEW,
				ICustomAction.ACTION_SHOW_PREVIEW,	//屏幕预览
				ICustomAction.ACTION_SHOW_FUNCMENU_FOR_LAUNCHER_ACITON,	//显示功能标
				ICustomAction.ACTION_SHOW_EXPEND_BAR, 
				ICustomAction.ACTION_SHOW_HIDE_STATUSBAR,
				ICustomAction.ACTION_SHOW_DOCK, 
				ICustomAction.ACTION_ENABLE_SCREEN_GUARD,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME,
				ICustomAction.ACTION_SHOW_PREFERENCES, 
				ICustomAction.ACTION_SHOW_MENU,
				ICustomAction.ACTION_SHOW_DIYGESTURE, 
				ICustomAction.ACTION_SHOW_PHOTO,
				ICustomAction.ACTION_SHOW_MUSIC, 
				ICustomAction.ACTION_SHOW_VIDEO };

		mTitles = new int[] {
				R.string.customname_mainscreen,
				R.string.customname_mainscreen_or_preview,
				R.string.customname_preview,
				R.string.customname_Appdrawer,
				R.string.customname_notification,
				R.string.customname_status_bar,
				R.string.goshortcut_showdockbar,
				R.string.goshortcut_lockscreen,
				R.string.customname_gostore,
				R.string.customname_themeSetting,
				R.string.customname_preferences,
				R.string.customname_mainmenu, 
				R.string.customname_diygesture,
				R.string.customname_photo, 
				R.string.customname_music,
				R.string.customname_video
		};
		
		mDrawableIds = new int[] { R.drawable.go_shortcut_mainscreen,
				R.drawable.go_shortcut_main_or_preview, R.drawable.go_shortcut_preview,
				R.drawable.go_shortcut_appdrawer, R.drawable.go_shortcut_notification,
				R.drawable.go_shortcut_statusbar, R.drawable.go_shortcut_hide_dock,
				R.drawable.go_shortcut_lockscreen, R.drawable.go_shortcut_store,
				R.drawable.go_shortcut_themes, R.drawable.go_shortcut_preferences,
				R.drawable.go_shortcut_menu, R.drawable.go_shortcut_diygesture,
				R.drawable.go_shortcut_photo, R.drawable.go_shortcut_music,
				R.drawable.go_shortcut_video };
	}

	/**
	 * <br>
	 * 功能简述:通过drawableId拿推荐图标图片 <br>
	 * 功能详细描述:可以过滤某些图标进行download tag标签合成图片（tag图片共享一张，减少图片资源） <br>
	 * @param drawableId
	 * @return　经过合成规则处理后的图片
	 */
	private Drawable getIcons(int drawableId) {
		Drawable tag = mContext.getResources().getDrawable(drawableId);
		Drawable drawable = mContext.getResources().getDrawable(R.drawable.screenedit_icon_bg);
		try {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas cv = new Canvas(bmp);
			ImageUtil.drawImage(cv, drawable, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			ImageUtil.drawImage(cv, tag, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			BitmapDrawable bmd = new BitmapDrawable(bmp);
			bmd.setTargetDensity(mContext.getResources().getDisplayMetrics());
			drawable = bmd;
		} catch (Throwable e) {
			// 出错则不进行download Tag合成图
		}

		return drawable;
	}

}

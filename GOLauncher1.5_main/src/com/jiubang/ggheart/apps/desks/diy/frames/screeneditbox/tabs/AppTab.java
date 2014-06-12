package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * 添加模块的初始界面
 */
public class AppTab extends BaseTab {

	private ArrayList<ItemInfo> mItems;
	public static final int APP_ADD_TAB_ADD = 1; // 应用程序tab
	public static final int APP_ADD_TAB_FOLDER = 2; // 文件夹tab
	private static final int APP_ADD_GO_WIDGET = 3; // go小部件
	private static final int APP_ADD_SYSTEM_WIDGET = 4; // 系统小部件
	private static final int APP_ADD_TAB_SHORTCUT = 5; // 快捷方式tab
	public static final int APP_ADD_TAB_GO_SHORTCUT = 6; // Go桌面快捷方式tab
	private static final long CLICK_TIME = 1000;
	private long mLastTime; // 上次的点击时间
	boolean mFirstClickFlag = false; //保证第一次点击不会被防止重复点击判断屏蔽掉 false为未点击
	public AppTab(Context context, String tag, int level) {
		super(context, tag, level);
		mItems = new ArrayList<ItemInfo>();
		initData(context);
		mLastTime = System.currentTimeMillis();
	}

	private void initData(Context contxt) {
		mItems.clear();
		Resources res = contxt.getResources();
		// 应用程序
		ItemInfo itemInfo = new ItemInfo();
		itemInfo.mId = APP_ADD_TAB_ADD;
		itemInfo.mTitle = res.getString(R.string.tab_add_app);
		itemInfo.mIcon = R.drawable.gesture_application;
		mItems.add(itemInfo);

		// 文件夹
		itemInfo = new ItemInfo();
		itemInfo.mId = APP_ADD_TAB_FOLDER;
		itemInfo.mTitle = res.getString(R.string.tab_add_app_folder);
		itemInfo.mIcon = R.drawable.tab_add_folder_icon;
		mItems.add(itemInfo);

		// go小部件
		itemInfo = new ItemInfo();
		itemInfo.mId = APP_ADD_GO_WIDGET;
		itemInfo.mTitle = res.getString(R.string.tab_add_widget);
		itemInfo.mIcon = R.drawable.tab_add_widget_icon;
		mItems.add(itemInfo);

		// 系统小部件
		itemInfo = new ItemInfo();
		itemInfo.mId = APP_ADD_SYSTEM_WIDGET;
		itemInfo.mTitle = res.getString(R.string.add_widget);
		itemInfo.mIcon = R.drawable.screen_edit_widget;
		mItems.add(itemInfo);

		// 快捷方式
		itemInfo = new ItemInfo();
		itemInfo.mId = APP_ADD_TAB_SHORTCUT;
		itemInfo.mTitle = res.getString(R.string.tab_add_app_shortcut);
		itemInfo.mIcon = R.drawable.tab_add_shortcut_icon;
		mItems.add(itemInfo);

		// Go桌面快捷方式
		itemInfo = new ItemInfo();
		itemInfo.mId = APP_ADD_TAB_GO_SHORTCUT;
		itemInfo.mTitle = res.getString(R.string.dialog_name_go_shortcut);
		itemInfo.mIcon = R.drawable.screen_edit_go_shortcut;
		mItems.add(itemInfo);

	}

	@Override
	public ArrayList<Object> getDtataList() {
		return null;
	}

	@Override
	public int getItemCount() {
		if (mItems != null) {
			return mItems.size();
		}
		return 0;
	}

	@Override
	public View getView(int position) {
		ItemInfo itemInfo = mItems.get(position);
		View view = mInflater.inflate(R.layout.screen_edit_item, null);
		ImageView image = (ImageView) view.findViewById(R.id.thumb);
		image.setImageResource(itemInfo.mIcon);
		TextView mText = (TextView) view.findViewById(R.id.title);
		mText.setText(itemInfo.mTitle);
		view.setTag(itemInfo);
		return view;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		long curTime = System.currentTimeMillis();
		if (curTime - mLastTime < CLICK_TIME && mFirstClickFlag) {
			return;
		}
		mFirstClickFlag = true;
		mLastTime = curTime;
		ItemInfo itemInfo = (ItemInfo) v.getTag();
		switch (itemInfo.mId) {
			case APP_ADD_TAB_ADD :
				// addFolder(mActivity, APP_ADD_TAB_ADD);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
						IDiyMsgIds.SCREEN_EDIT_ADD_APPS, 0, null, null);
				//				if (mTabActionListener != null) {
				//					mTabActionListener.onRefreshTopBack(BaseTab.TAB_ADDAPPS);
				//				}
				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_FIVE, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case APP_ADD_TAB_FOLDER :
				if (!checkScreenVacant(1, 1)) {
					return;
				}

				//				if (mTabActionListener != null) {
				//					mTabActionListener.onRefreshTopBack(BaseTab.TAB_ADDFOLDER);
				//				}
				addFolder(mContext, AppTab.APP_ADD_TAB_FOLDER);
				// 屏蔽workspace的触摸响应
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.IN_NEW_FOLDER_STATE, -1, null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
						IDiyMsgIds.SCREEN_EDIT_ADD_FORLDER, 0, null, null);
				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_SIX, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case APP_ADD_GO_WIDGET :
				if (mTabActionListener != null) {
					mTabActionListener.setCurrentTab(BaseTab.TAB_GOWIDGET);
					mTabActionListener.onRefreshTopBack(BaseTab.TAB_GOWIDGET);
				}
				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_SEVEN, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case APP_ADD_SYSTEM_WIDGET :
				if (checkScreenVacant(1, 1)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.PICK_WIDGET, 0, null, null);
					showToast();
				}
				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_EIGHT, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case APP_ADD_TAB_SHORTCUT :
				if (checkScreenVacant(1, 1)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SCREENEDIT_ADD_TAB_ADD_SHORTCUT, -1, null, null);
					showToast();
				}
				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_NINE, IPreferencesIds.DESK_ACTION_DATA);
				break;

			case APP_ADD_TAB_GO_SHORTCUT :
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
						IDiyMsgIds.SCREEN_EDIT_ADD_GOSHORTCUT, 0, null, null);
				//				if (mTabActionListener != null) {
				//					mTabActionListener.onRefreshTopBack(BaseTab.TAB_ADDGOSHORTCUT);
				//				}
				//用户行为统计
				StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
						StatisticsData.USER_ACTION_TEN, IPreferencesIds.DESK_ACTION_DATA);
				break;

			default :
				break;
		}
	}

	/**
	 * 添加文件夹
	 */
	private void addFolder(Context mContext, int createType) {
		UserFolderInfo folderInfo = new UserFolderInfo();
		folderInfo.mTitle = mContext.getText(R.string.folder_name);
		// 发送给屏幕层要求添加一个文件夹
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_ADD_USER_FOLDER,
				createType, folderInfo, null);
	}

	@Override
	public void clearData() {
		if (mItems != null) {
			mItems.clear();
			mItems = null;
		}
		super.clearData();
	}
	/**
	 * 
	 * <br>类描述:app item bean
	 */
	class ItemInfo {
		int mId;
		// 图标名称
		String mTitle;
		// 图标
		int mIcon;
	}

	// 显示系统小部件和快捷方式的时候弹出loading
	private void showToast() {
		if (mContext != null) {
			Toast.makeText(mContext, mContext.getString(R.string.loading), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void resetData() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}
}
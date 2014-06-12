package com.jiubang.ggheart.plugin.mediamanagement;

import java.util.ArrayList;

import android.app.Activity;
import android.view.View;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AppFuncSwitchHorizontalMenu;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AppFuncSwitchListMenu;
import com.jiubang.ggheart.apps.desks.appfunc.menu.AppFuncSwitchMenuItemInfo;
import com.jiubang.ggheart.apps.desks.appfunc.menu.BaseMenuItemInfo;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.plugin.mediamanagement.inf.ISwitchMenuControler;
import com.jiubang.ggheart.plugin.mediamanagement.inf.OnSwitchMenuItemClickListener;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-16]
 */
public class SwitchMenuControler implements ISwitchMenuControler {
	public final static int MENU_ITEM_COUNT = 4;
	private final static int[] MENU_ITEM_LABEL_LIST_APP = new int[] {
			R.drawable.switch_menu_image_selector, R.drawable.switch_menu_audio_selector,
			R.drawable.switch_menu_video_selector, R.drawable.switch_menu_search_selector };
	private final static int[] MENU_ITEM_LABEL_LIST_IMAGE = new int[] {
			R.drawable.switch_menu_app_selector, R.drawable.switch_menu_audio_selector,
			R.drawable.switch_menu_video_selector, R.drawable.switch_menu_search_selector };
	private final static int[] MENU_ITEM_LABEL_LIST_AUDIO = new int[] {
			R.drawable.switch_menu_app_selector, R.drawable.switch_menu_image_selector,
			R.drawable.switch_menu_video_selector, R.drawable.switch_menu_search_selector };
	private final static int[] MENU_ITEM_LABEL_LIST_VEDIO = new int[] {
			R.drawable.switch_menu_app_selector, R.drawable.switch_menu_image_selector,
			R.drawable.switch_menu_audio_selector, R.drawable.switch_menu_search_selector };
	private final static int[] MENU_ITEM_LABEL_LIST_SEARCH = new int[] {
			R.drawable.switch_menu_app_selector, R.drawable.switch_menu_image_selector,
			R.drawable.switch_menu_audio_selector, R.drawable.switch_menu_video_selector };

	private final static int[] MENU_ITEM_ACTION_LIST_APP = new int[] {
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_IMAGE,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_AUDIO,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_VIDEO,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_SEARCH };
	private final static int[] MENU_ITEM_ACTION_LIST_IMAGE = new int[] {
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_APP,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_AUDIO,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_VIDEO,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_SEARCH };
	private final static int[] MENU_ITEM_ACTION_LIST_AUDIO = new int[] {
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_APP,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_IMAGE,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_VIDEO,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_SEARCH };
	private final static int[] MENU_ITEM_ACTION_LIST_VEDIO = new int[] {
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_APP,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_IMAGE,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_AUDIO,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_SEARCH };
	private final static int[] MENU_ITEM_ACTION_LIST_SEARCH = new int[] {
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_APP,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_IMAGE,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_AUDIO,
			AppFuncSwitchMenuItemInfo.ACTION_GO_TO_VIDEO };

	private AppFuncSwitchListMenu mListMenu;
	private AppFuncSwitchHorizontalMenu mHorizontalMenu;
	private AppFuncThemeController mThemeController;
	private ImageExplorer mImageExplorer;
	private Activity mActivity;
	private View mRootView;
	
	public SwitchMenuControler(Activity activity, View rootView) {
		mActivity = activity;
		mThemeController = AppFuncFrame.getThemeController();
		mImageExplorer = AppCore.getInstance().getImageExplorer();
		mRootView = rootView;
	}

	private ArrayList<BaseMenuItemInfo> createMenuItemResouces(int type) {
		ArrayList<BaseMenuItemInfo> itemInfos = new ArrayList<BaseMenuItemInfo>(MENU_ITEM_COUNT);
		String packageName = mThemeController.getThemeBean().mSwitchMenuBean.mPackageName;
		switch (type) {
			case AppFuncContentTypes.APP :
				String[] selectors = mThemeController.getThemeBean().mSwitchMenuBean.mItemLabelAppSelectors;
				for (int i = 0; i < selectors.length; i++) {
					AppFuncSwitchMenuItemInfo itemInfo = new AppFuncSwitchMenuItemInfo();
					itemInfo.mDrawablePos = BaseMenuItemInfo.DRAWABLE_POS_TOP;
					int id = mImageExplorer.getResourceId(selectors[i], packageName);
					if (id == 0) {
						id = MENU_ITEM_LABEL_LIST_APP[i];
					}
					itemInfo.mDrawableId = id;
					itemInfo.mActionId = MENU_ITEM_ACTION_LIST_APP[i];
					itemInfos.add(itemInfo);
				}
				break;
			case AppFuncContentTypes.IMAGE :
				selectors = mThemeController.getThemeBean().mSwitchMenuBean.mItemLabelImageSelectors;
				for (int i = 0; i < selectors.length; i++) {
					AppFuncSwitchMenuItemInfo itemInfo = new AppFuncSwitchMenuItemInfo();
					itemInfo.mDrawablePos = BaseMenuItemInfo.DRAWABLE_POS_TOP;
					int id = mImageExplorer.getResourceId(selectors[i], packageName);
					if (id == 0) {
						id = MENU_ITEM_LABEL_LIST_IMAGE[i];
					}
					itemInfo.mDrawableId = id;
					itemInfo.mActionId = MENU_ITEM_ACTION_LIST_IMAGE[i];
					itemInfos.add(itemInfo);
				}
				break;
			case AppFuncContentTypes.MUSIC :
				selectors = mThemeController.getThemeBean().mSwitchMenuBean.mItemLabelAudioSelectors;
				for (int i = 0; i < selectors.length; i++) {
					AppFuncSwitchMenuItemInfo itemInfo = new AppFuncSwitchMenuItemInfo();
					itemInfo.mDrawablePos = BaseMenuItemInfo.DRAWABLE_POS_TOP;
					int id = mImageExplorer.getResourceId(selectors[i], packageName);
					if (id == 0) {
						id = MENU_ITEM_LABEL_LIST_AUDIO[i];
					}
					itemInfo.mDrawableId = id;
					itemInfo.mActionId = MENU_ITEM_ACTION_LIST_AUDIO[i];
					itemInfos.add(itemInfo);
				}
				break;
			case AppFuncContentTypes.VIDEO :
				selectors = mThemeController.getThemeBean().mSwitchMenuBean.mItemLabelVedioSelectors;
				for (int i = 0; i < selectors.length; i++) {
					AppFuncSwitchMenuItemInfo itemInfo = new AppFuncSwitchMenuItemInfo();
					itemInfo.mDrawablePos = BaseMenuItemInfo.DRAWABLE_POS_TOP;
					int id = mImageExplorer.getResourceId(selectors[i], packageName);
					if (id == 0) {
						id = MENU_ITEM_LABEL_LIST_VEDIO[i];
					}
					itemInfo.mDrawableId = id;
					itemInfo.mActionId = MENU_ITEM_ACTION_LIST_VEDIO[i];
					itemInfos.add(itemInfo);
				}
				break;
			case AppFuncContentTypes.SEARCH :
				selectors = mThemeController.getThemeBean().mSwitchMenuBean.mItemLabelSearchSelectors;
				for (int i = 0; i < selectors.length; i++) {
					AppFuncSwitchMenuItemInfo itemInfo = new AppFuncSwitchMenuItemInfo();
					itemInfo.mDrawablePos = BaseMenuItemInfo.DRAWABLE_POS_TOP;
					int id = mImageExplorer.getResourceId(selectors[i], packageName);
					if (id == 0) {
						id = MENU_ITEM_LABEL_LIST_SEARCH[i];
					}
					itemInfo.mDrawableId = id;
					itemInfo.mActionId = MENU_ITEM_ACTION_LIST_SEARCH[i];
					itemInfos.add(itemInfo);
				}
				break;
			default :
				break;
		}
		return itemInfos;
	}

	private void showMenu(int type, OnSwitchMenuItemClickListener listener) {
		if (GoLauncher.isPortait()) {
			if (mListMenu == null) {
				mListMenu = new AppFuncSwitchListMenu(mActivity);
			}
			mListMenu.setOnSwitchMenuItemClickListener(listener);
			mListMenu.setItemResources(createMenuItemResouces(type));
			mListMenu.show(mRootView);
		} else {
			if (mHorizontalMenu == null) {
				mHorizontalMenu = new AppFuncSwitchHorizontalMenu(mActivity);
			}
			mHorizontalMenu.setOnSwitchMenuItemClickListener(listener);
			mHorizontalMenu.setItemResources(createMenuItemResouces(type));
			mHorizontalMenu.show(mRootView);
		}
	}

	@Override
	public void popupAppMenu(OnSwitchMenuItemClickListener listener) {
		showMenu(AppFuncContentTypes.APP, listener);
	};
	
	@Override
	public void popupImageMenu(OnSwitchMenuItemClickListener listener) {
		showMenu(AppFuncContentTypes.IMAGE, listener);
	};
	
	@Override
	public void popupMusicMenu(OnSwitchMenuItemClickListener listener) {
		showMenu(AppFuncContentTypes.MUSIC, listener);
	};
	
	@Override
	public void popupVideoMenu(OnSwitchMenuItemClickListener listener) {
		showMenu(AppFuncContentTypes.VIDEO, listener);
	};
	
	@Override
	public void popupSearchMenu(OnSwitchMenuItemClickListener listener) {
		showMenu(AppFuncContentTypes.SEARCH, listener);
	};
	
	public void setMenuRootView(View view){
		mRootView = view;
	}
}

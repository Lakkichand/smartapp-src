package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AddAppTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AddFolderTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AddGoShortCutTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.AppTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.DesktopThemeTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.EffectTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.LockerThemeTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.ThemeTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WallpaperSubTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WallpaperTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WidgetSubTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WidgetTab;
  /**
   * 
   * <br>类描述:tab的数据管理类
   * <br>功能详细描述:创建和获取、销毁各类tab
   */
public class DataEdngine {
	private HashMap<String, BaseTab> mTabs;
	private Context mContext;

	public DataEdngine(Context context) {
		mContext = context;
		mTabs = new HashMap<String, BaseTab>();
		initTabs();
	}

	private void initTabs() {
		AppTab appTab = new AppTab(mContext, BaseTab.TAB_APP, BaseTab.TAB_LEVEL_1);
		mTabs.put(BaseTab.TAB_APP, appTab);

	}

	/**
	 * 每次换Tab的时候，都通过这个方法获取目标Tab
	 * 
	 * @param tag
	 * @return
	 */
	public BaseTab getTab(String tag) {
		BaseTab tab = mTabs.get(tag);
		if (tab == null) {
			try {
				tab = produceTab(tag);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return tab;
	}

	/**
	 * 通过tab名称获取具体tab对象
	 * 
	 * @param tag
	 * @return
	 */
	public BaseTab obtainTab(String tag) {
		BaseTab tab = mTabs.get(tag);
		return tab;
	}

	/**
	 * 刷新指定tab的内容，如果该tab没有创建则不处理
	 * 
	 * @param tag
	 */
	public void updateTab(String tag) {
		BaseTab tab = mTabs.get(tag);
		if (tab != null) {
			tab.resetData();
		}
	}

	/**
	 * 根据名称生成Tab
	 * 
	 * @param tag
	 * @return
	 */
	private BaseTab produceTab(String tag) {
		BaseTab tab = mTabs.get(tag);
		if (tab != null) {
			return tab;
		}
		if (tag.equals(BaseTab.TAB_APP)) {
			tab = new AppTab(mContext, BaseTab.TAB_APP, BaseTab.TAB_LEVEL_1);
		} else if (tag.equals(BaseTab.TAB_WALLPAPER)) {
			tab = new WallpaperTab(mContext, tag, BaseTab.TAB_LEVEL_1);
		} else if (tag.equals(BaseTab.TAB_THEMELOCKER)) {
			tab = new ThemeTab(mContext, tag, BaseTab.TAB_LEVEL_1);
		} else if (tag.equals(BaseTab.TAB_EFFECTS)) {
			tab = new EffectTab(mContext, tag, BaseTab.TAB_LEVEL_1);
		} else if (tag.equals(BaseTab.TAB_THEME)) {
			tab = new DesktopThemeTab(mContext, tag, BaseTab.TAB_LEVEL_2);
		} else if (tag.equals(BaseTab.TAB_LOCKER)) {
			tab = new LockerThemeTab(mContext, tag, BaseTab.TAB_LEVEL_2);
		} else if (tag.equals(BaseTab.TAB_GOWALLPAPER)) {
			tab = new WallpaperSubTab(mContext, tag, BaseTab.TAB_LEVEL_2);
		} else if (tag.equals(BaseTab.TAB_GOWIDGET)) {
			tab = new WidgetTab(mContext, tag, BaseTab.TAB_LEVEL_2);

		} else if (tag.equals(BaseTab.TAB_ADDAPPS)) {
			tab = new AddAppTab(mContext, tag, BaseTab.TAB_LEVEL_3);
		} else if (tag.equals(BaseTab.TAB_ADDFOLDER)) {
			tab = new AddFolderTab(mContext, tag, BaseTab.TAB_LEVEL_3);
		} else if (tag.equals(BaseTab.TAB_ADDGOSHORTCUT)) {
			tab = new AddGoShortCutTab(mContext, tag, BaseTab.TAB_LEVEL_3);
		} else if (tag.equals(BaseTab.TAB_ADDGOWIDGET)) {
			tab = new WidgetSubTab(mContext, tag, BaseTab.TAB_LEVEL_3);
		}

		mTabs.put(tag, tab);
		return tab;
	}

	// 清空指定tab的数据
	public void removeData(String tag) {
		BaseTab tab = mTabs.get(tag);
		if (tab != null && mTabs.get("" + tag) != null) {
			mTabs.remove("" + tag);
		}
	}

	public void clearData() {
		Iterator it = mTabs.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			BaseTab tab = (BaseTab) entry.getValue();
			if (tab != null) {
				tab.clearData();
				tab.setDataSetListener(null);
				tab.setTabActionListener(null);
				tab.mListener = null;
				tab = null;
			}
		}
		mTabs.clear();
		mContext = null;
	}

	/**
	 * 获取指定Tab的级别
	 * 
	 * @param tag
	 * @return
	 */
	public int getTabLevel(String tag) {
		if (mTabs != null) {
			BaseTab tab = mTabs.get(tag);
			if (tab != null) {
				return tab.mTabLevel;
			}
		}
		return BaseTab.TAB_LEVEL_1;
	}

	/**
	 * 返回该第二级对应的上一级
	 * 
	 * @param tag
	 * @return
	 */
	public BaseTab getBackTab(String tag) {
		String dstTag = BaseTab.TAB_APP;
		if (tag.equals(BaseTab.TAB_THEME)) {
			dstTag = BaseTab.TAB_THEMELOCKER;
		} else if (tag.equals(BaseTab.TAB_LOCKER)) {
			dstTag = BaseTab.TAB_THEMELOCKER;
		} else if (tag.equals(BaseTab.TAB_GOWIDGET)) {
			dstTag = BaseTab.TAB_APP;
		} else if (tag.equals(BaseTab.TAB_GOWALLPAPER)) {
			dstTag = BaseTab.TAB_WALLPAPER;
		}
		// new add
		else if (tag.equals(BaseTab.TAB_ADDAPPS)) {
			dstTag = BaseTab.TAB_APP;
		} else if (tag.equals(BaseTab.TAB_ADDFOLDER)) {
			dstTag = BaseTab.TAB_APP;
		} else if (tag.equals(BaseTab.TAB_ADDGOSHORTCUT)) {
			dstTag = BaseTab.TAB_APP;
		} else if (tag.equals(BaseTab.TAB_ADDGOWIDGET)) {
			dstTag = BaseTab.TAB_GOWIDGET;
		}
		return getTab(dstTag);
	}
}

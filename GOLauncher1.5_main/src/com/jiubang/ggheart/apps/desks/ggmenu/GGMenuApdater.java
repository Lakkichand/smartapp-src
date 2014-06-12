package com.jiubang.ggheart.apps.desks.ggmenu;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.go.util.AppUtils;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.MenuBean;

/**
 * 菜单项的适配器
 * 
 * @author ouyongqiang
 * 
 */
public class GGMenuApdater extends BaseAdapter implements ICleanable {

	/**
	 * 菜单项的显示文本
	 */
	private ArrayList<String> mTextArray;

	/**
	 * 菜单项的图片资源Id
	 */
	private ArrayList<Drawable> mImgArray;

	/**
	 * 菜单项的ID
	 */
	private ArrayList<Integer> mMenuItemIds;

	/**
	 * 菜单项
	 */
	private ArrayList<GGMenuItem> mGGMenuItems;

	/**
	 * 菜单项的布局文件ID
	 */
	private int mMenuItemLayout;

	/**
	 * 程序上下文
	 */
	private Context mContext;

	private MenuBean mMenuBean;
	/**
	 * 构造函数
	 * 
	 * @param context
	 *            程序上下文
	 * @param textArray
	 *            菜单项的显示文本
	 * @param imgResArray
	 *            菜单项的图片资源Id
	 * @param menuItemIds
	 *            菜单项的Id
	 * @param menuItemLayout
	 *            菜单项的布局文件ID
	 * @throws IllegalArgumentException
	 *             当textArray和imgResArray长度不一致时，会抛出异常
	 */
	public GGMenuApdater(Context context, String[] textArray, Drawable[] imgArray,
			int[] menuItemIds, int menuItemLayout) throws IllegalArgumentException {
		super();
		mContext = context;
		mMenuItemLayout = menuItemLayout;
		if (textArray.length != imgArray.length) {
			throw new IllegalArgumentException("textArray和imgResArray长度不一致");
		}
		mTextArray = new ArrayList<String>();
		mGGMenuItems = new ArrayList<GGMenuItem>();
		mImgArray = new ArrayList<Drawable>();
		mMenuItemIds = new ArrayList<Integer>();

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		int[] ignores = ignoreArray(menuItemIds);
		for (int i = 0; i < textArray.length; i++) {
			if (isIgnore(i, ignores)) {
				continue;
			}
			mTextArray.add(textArray[i]);
			mGGMenuItems.add((GGMenuItem) inflater.inflate(mMenuItemLayout, null, false));
		}
		for (int i = 0; i < imgArray.length; i++) {
			if (isIgnore(i, ignores)) {
				continue;
			}
			mImgArray.add(imgArray[i]);
		}
		for (int i = 0; i < menuItemIds.length; i++) {
			if (isIgnore(i, ignores)) {
				continue;
			}
			mMenuItemIds.add(new Integer(menuItemIds[i]));
		}
		String packageName = GoSettingControler.getInstance(mContext.getApplicationContext())
				.getScreenStyleSettingInfo().getGGmenuStyle();
		mMenuBean = ThemeManager.getInstance(mContext.getApplicationContext()).getGGmenuBean(
				packageName);

	}

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            程序上下文
	 * @param textArray
	 *            菜单项的显示文本Id
	 * @param imgResArray
	 *            菜单项的图片资源Id
	 * @param menuItemIds
	 *            菜单项的Id
	 * @param menuItemLayout
	 *            菜单项的布局文件ID
	 * @throws IllegalArgumentException
	 *             当textArray和imgResArray长度不一致时，会抛出异常
	 */
	public GGMenuApdater(Context context, int[] textArray, Drawable[] imgArray, int[] menuItemIds,
			int menuItemLayout) throws IllegalArgumentException {
		super();
		mContext = context;
		mMenuItemLayout = menuItemLayout;
		if (textArray.length != imgArray.length) {
			throw new IllegalArgumentException("textArray和imgResArray长度不一致");
		}
		mTextArray = new ArrayList<String>();
		mGGMenuItems = new ArrayList<GGMenuItem>();
		mImgArray = new ArrayList<Drawable>();
		mMenuItemIds = new ArrayList<Integer>();
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int[] ignores = ignoreArray(menuItemIds);
		for (int i = 0; i < textArray.length; i++) {
			if (isIgnore(i, ignores)) {
				continue;
			}
			mTextArray.add(mContext.getString(textArray[i]));
			mGGMenuItems.add((GGMenuItem) inflater.inflate(mMenuItemLayout, null, false));
		}
		for (int i = 0; i < imgArray.length; i++) {
			if (isIgnore(i, ignores)) {
				continue;
			}
			mImgArray.add(imgArray[i]);
		}
		for (int i = 0; i < menuItemIds.length; i++) {
			if (isIgnore(i, ignores)) {
				continue;
			}
			mMenuItemIds.add(new Integer(menuItemIds[i]));
		}

		mMenuItemLayout = menuItemLayout;
		String packageName = GoSettingControler.getInstance(mContext.getApplicationContext())
				.getScreenStyleSettingInfo().getGGmenuStyle();
		mMenuBean = ThemeManager.getInstance(mContext.getApplicationContext()).getGGmenuBean(
				packageName);
	}

	private int[] ignoreArray(int[] ids) {
		if (AppUtils.isGoLockerExist(mContext)) {
			return null;
		}

		int count = 0;
		// for (int i = 0; i < ids.length; i++)
		// {
		// if (GGMenuData.GGMENU_ID_LOCKER == ids[i])
		// {
		// count++;
		// }
		// }

		if (0 == count) {
			return null;
		}

		int[] ret = new int[count];
		// for (int i = 0, j = 0; i < ids.length; i++)
		// {
		// if (GGMenuData.GGMENU_ID_LOCKER == ids[i])
		// {
		// ret[j] = i;
		// j++;
		// }
		// }
		return ret;
	}

	private boolean isIgnore(int index, int[] ignores) {
		if (null != ignores) {
			int len = ignores.length;
			for (int i = 0; i < len; i++) {
				if (index == ignores[i]) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取菜单项的布局文件ID
	 * 
	 * @return int 菜单项的布局文件ID
	 */
	public int getMenuItemLayout() {
		return mMenuItemLayout;
	}

	@Override
	public int getCount() {
		return (null != mTextArray) ? mTextArray.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return (null != mGGMenuItems) ? mGGMenuItems.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return (null != mGGMenuItems) ? mMenuItemIds.get(position).intValue() : -1;
	}

	/**
	 * 插入菜单项到指定位置
	 * 
	 * @param text
	 *            菜单项的显示文本Id
	 * @param imgRes
	 *            菜单项的图片资源Id
	 * @param id
	 *            菜单项的Id
	 * @param menuItemLayout
	 *            菜单项的布局文件ID
	 * @param index
	 *            插入的位置，如果是菜单项的大小则插入到最后的位置
	 */
	public void addMenuItem(int text, Drawable imgRes, int id, int menuItemLayout, int index) {
		String tmpText = mContext.getString(text);
		mTextArray.add(index, tmpText);
		mImgArray.add(index, imgRes);
		mMenuItemIds.add(index, new Integer(id));

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mGGMenuItems.add(index, (GGMenuItem) inflater.inflate(mMenuItemLayout, null, false));
	}

	/**
	 * 插入菜单项到指定位置
	 * 
	 * @param text
	 *            菜单项的显示文本
	 * @param imgRes
	 *            菜单项的图片资源Id
	 * @param id
	 *            菜单项的Id
	 * @param menuItemLayout
	 *            菜单项的布局文件ID
	 * @param index
	 *            插入的位置，如果是菜单项的大小则插入到最后的位置
	 */
	public void addMenuItem(String text, Drawable imgRes, int id, int menuItemLayout, int index) {
		mTextArray.add(index, text);
		mImgArray.add(index, imgRes);
		mMenuItemIds.add(index, new Integer(id));

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mGGMenuItems.add(index, (GGMenuItem) inflater.inflate(mMenuItemLayout, null, false));
	}

	/**
	 * 删除菜单项
	 * 
	 * @param id
	 *            菜单项的Id
	 */
	public boolean removeMenuItem(int id) {
		int index = mMenuItemIds.indexOf(new Integer(id));
		if (index == -1) {
			return false;
		}
		mTextArray.remove(index);
		mImgArray.remove(index);
		mMenuItemIds.remove(index);
		mGGMenuItems.remove(index);
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (null == mGGMenuItems) {
			// 菜单已关闭，已释放
			return convertView;
		}
		GGMenuItem item = mGGMenuItems.get(position);
		if (item != null) {
			if (0 == GGMenu.sTextColor) {
				item.bind(mTextArray.get(position), mImgArray.get(position));
			} else {
				item.bind(mTextArray.get(position), GGMenu.sTextColor, mImgArray.get(position));
			}
		}

		int itemId = mMenuItemIds.get(position);
		int highTextColor = 0xff7ca500;
		if (mMenuBean != null) {
			highTextColor = mMenuBean.mHighLightTextColor;
		}
		if (GGMenuData.GLMENU_ID_MESSAGE == itemId) {
			item.generatorMessageCountImage();
		}

		else if (GGMenuData.GLMENU_ID_SCREENEDIT == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needDrawInfo = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_PRIVIEW_GUIDE, true);
			if (needDrawInfo) {
				item.bind(mTextArray.get(position), highTextColor, mImgArray.get(position));
			}
		} else if (GGMenuData.GLMENU_ID_SHARE == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needDrawInfo = sharedPreferences.getBoolean(IPreferencesIds.SHOULD_SHOW_SHARE,
					true);
			if (needDrawInfo) {
				item.bind(mTextArray.get(position), highTextColor, mImgArray.get(position));
			}
		}
		// add by jiang 第一次屏幕锁定时，进入GGmenu字体高亮
		else if (GGMenuData.GLMENU_ID_UNLOCKEDIT == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needShowMenu = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_SCREEN_LOCK_GGMENU, false);
			if (needShowMenu) {
				item.bind(mTextArray.get(position), highTextColor, mImgArray.get(position));
				sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_SCREEN_LOCK_GGMENU, false);
				sharedPreferences.commit();
			}
		} else if (GGMenuData.GLMENU_ID_ONE_X_GUIDE == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needDrawInfo = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_ONE_X_GUIDE, true);
			if (needDrawInfo) {
				item.bind(mTextArray.get(position), highTextColor, mImgArray.get(position));
			}
		} else if (GGMenuData.GLMENU_ID_LANGUAGE == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			boolean needDrawInfo = sharedPreferences.getBoolean(
					IPreferencesIds.SHOULD_SHOW_LANGUAGE_GUIDE, true);
			if (needDrawInfo) {
				item.bind(mTextArray.get(position), highTextColor, mImgArray.get(position));
			}
		}

		else if (GGMenuData.GLMENU_ID_THEME == itemId || GGMenuData.GLMENU_ID_GOLOCKER == itemId) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
			boolean bool = false;
			if (GGMenuData.GLMENU_ID_THEME == itemId) {
				bool = sharedPreferences.getBoolean(IPreferencesIds.HASNEWTHEME, false);
			} else if (GGMenuData.GLMENU_ID_GOLOCKER == itemId) {
				bool = sharedPreferences.getBoolean(IPreferencesIds.LOCKER_HASNEWTHEME, false);
			}
			if (bool) {
				item.addNewThemeLogo();
			} else {
				item.removeNewThemeLogo();
			}
		}
		return item;
	}

	/**
	 * 更新其中一项
	 * 
	 * @param index
	 *            更改项的索引
	 * @param drawable
	 * @param name
	 */
	public void updateItem(int index, Drawable drawable, String name) {
		if (null == drawable || null == name || index >= getCount()) {
			return;
		}

		mTextArray.remove(index);
		mTextArray.add(index, name);

		mImgArray.remove(index);
		mImgArray.add(index, drawable);

		notifyDataSetChanged();
	}

	/**
	 * 更新其中一项
	 * 
	 * @param index
	 *            更改项的索引
	 * @param drawable
	 * @param name
	 */
	public boolean updateItem(int oldid, int newid, Drawable drawable, String name) {
		boolean ret = false;
		if (null == drawable || null == name) {
			return false;
		}

		int count = mMenuItemIds.size();
		for (int i = 0; i < count; i++) {
			int id = mMenuItemIds.get(i);
			if (id == oldid) {
				mMenuItemIds.remove(i);
				mMenuItemIds.add(i, newid);

				mImgArray.remove(i);
				mImgArray.add(i, drawable);

				mTextArray.remove(i);
				mTextArray.add(i, name);
				ret = true;
				break;
			}
		}
		notifyDataSetChanged();
		return ret;
	}

	@Override
	public void cleanup() {
		if (null != mTextArray) {
			mTextArray.clear();
			mTextArray = null;
		}
		if (null != mImgArray) {
			mImgArray.clear();
			mImgArray = null;
		}
		if (null != mMenuItemIds) {
			mMenuItemIds.clear();
			mMenuItemIds = null;
		}
		if (null != mGGMenuItems) {
			int count = mGGMenuItems.size();
			for (int i = 0; i < count; i++) {
				GGMenuItem ggItem = mGGMenuItems.get(i);
				ggItem.cleanup();
			}
			mGGMenuItems.clear();
			mGGMenuItems = null;
		}
	}
}

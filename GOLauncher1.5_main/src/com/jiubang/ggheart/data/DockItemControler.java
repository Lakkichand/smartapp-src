package com.jiubang.ggheart.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.go.util.Utilities;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.desks.dock.DockChangeIconControler;
import com.jiubang.ggheart.apps.desks.dock.DockStylePkgInfo;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.DockAppItemInfo;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.DockBean;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.launcher.GOLauncherApp;


/**
 * 
 * <br>类描述:5个dock特殊图标管理器，用于换主题时控制这5个图标的图片获取
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-7-1]
 */
public class DockItemControler extends Controler implements ICleanable {

	private ArrayList<DockAppItemInfo> mDockAppItemInfos;
	private Context mContext;

	public DockItemControler(Context context) {
		super(context);

		mContext = context;
		mDockAppItemInfos = new ArrayList<DockAppItemInfo>();

		initData();
	}

	private void initData() {
		DockAppItemInfo dockAppItemInfo = null;
		Intent intent = null;
		// 拨号
		dockAppItemInfo = new DockAppItemInfo();
		intent = AppIdentifier.createSelfDialIntent(mContext);
		dockAppItemInfo.mIntent = intent;
		dockAppItemInfo.mIconPackage = ThemeManager.DEFAULT_THEME_PACKAGE;
		dockAppItemInfo.mIconResource = "shortcut_0_0_phone";
		dockAppItemInfo.mIcon = AppDataEngine.getInstance(mContext).getSysBitmapDrawable();
		dockAppItemInfo.mTitle = GOLauncherApp.getContext().getString(R.string.customname_dial);
		mDockAppItemInfos.add(dockAppItemInfo);

		// 联系人
		dockAppItemInfo = new DockAppItemInfo();
		intent = AppIdentifier.createSelfContactIntent(mContext);
		dockAppItemInfo.mIntent = intent;
		dockAppItemInfo.mIconPackage = ThemeManager.DEFAULT_THEME_PACKAGE;
		dockAppItemInfo.mIconResource = "shortcut_0_1_contacts";
		dockAppItemInfo.mIcon = AppDataEngine.getInstance(mContext).getSysBitmapDrawable();
		dockAppItemInfo.mTitle = GOLauncherApp.getContext().getString(R.string.customname_contacts);
		mDockAppItemInfos.add(dockAppItemInfo);

		// 功能表
		dockAppItemInfo = new DockAppItemInfo();
		intent = AppIdentifier.createAppdrawerIntent();
		dockAppItemInfo.mIntent = intent;
		dockAppItemInfo.mIconPackage = ThemeManager.DEFAULT_THEME_PACKAGE;
		dockAppItemInfo.mIconResource = "shortcut_0_2_funclist";
		dockAppItemInfo.mIcon = AppDataEngine.getInstance(mContext).getSysBitmapDrawable();
		dockAppItemInfo.mTitle = GOLauncherApp.getContext()
				.getString(R.string.customname_Appdrawer);
		mDockAppItemInfos.add(dockAppItemInfo);

		// 短信
		dockAppItemInfo = new DockAppItemInfo();
		intent = AppIdentifier.createSelfMessageIntent();
		dockAppItemInfo.mIntent = intent;
		dockAppItemInfo.mIconPackage = ThemeManager.DEFAULT_THEME_PACKAGE;
		dockAppItemInfo.mIconResource = "shortcut_0_3_sms";
		dockAppItemInfo.mIcon = AppDataEngine.getInstance(mContext).getSysBitmapDrawable();
		dockAppItemInfo.mTitle = GOLauncherApp.getContext().getString(R.string.customname_sms);
		mDockAppItemInfos.add(dockAppItemInfo);

		// 浏览器
		dockAppItemInfo = new DockAppItemInfo();
		intent = AppIdentifier.createSelfBrowseIntent(mContext.getPackageManager());
		dockAppItemInfo.mIntent = intent;
		dockAppItemInfo.mIconPackage = ThemeManager.DEFAULT_THEME_PACKAGE;
		dockAppItemInfo.mIconResource = "shortcut_0_4_browser";
		dockAppItemInfo.mIcon = AppDataEngine.getInstance(mContext).getSysBitmapDrawable();
		dockAppItemInfo.mTitle = GOLauncherApp.getContext().getString(R.string.customname_browser);
		mDockAppItemInfos.add(dockAppItemInfo);
	}

	/**
	 * <br>功能简述:使用指定主题
	 * <br>功能详细描述:
	 * <br>注意:调用时机：1:刚启动桌面；2:dock主题风格发生改变
	 * @param themePkg　主题包名
	 */
	public void useStyle(String themePkg) {
		if (null == themePkg) {
			return;
		}

		ImageExplorer imageExplorer = ImageExplorer.getInstance(mContext);
		for (int i = 0; i < 5; i++) {
			DockAppItemInfo dockAppItemInfo = mDockAppItemInfos.get(i);

			// 获取相关主题1---5
			DeskThemeBean.SystemDefualtItem dockThemeItem = getSystemDefualtItem(themePkg, i);

			Drawable drawable = null;
			BitmapDrawable bitmapDrawable = null;
			if (null != dockThemeItem && null != dockThemeItem.mIcon
					&& null != dockThemeItem.mIcon.mResName) {
				// 主题安装包
				drawable = imageExplorer.getDrawable(themePkg, dockThemeItem.mIcon.mResName);
			} else {
				// 风格安装包
				drawable = getStylePkgDrawable(mContext, themePkg, i);
			}
			if (null != drawable) {
				if (drawable instanceof BitmapDrawable) {
					bitmapDrawable = (BitmapDrawable) drawable;
				} else {
					bitmapDrawable = Utilities.createBitmapDrawableFromDrawable(drawable, mContext);
				}
			}

			if (null == bitmapDrawable) {
				bitmapDrawable = (BitmapDrawable) imageExplorer.getDrawable(
						dockAppItemInfo.mIconPackage, dockAppItemInfo.mIconResource);
			}

			dockAppItemInfo.setIcon(bitmapDrawable);
		}
	}
	
	/**
	 * <br>功能简述:使用指定主题
	 * <br>功能详细描述:
	 * <br>注意:调用时机：1:刚启动桌面；2:dock主题风格发生改变
	 * @param themePkg　主题包名
	 */
	public void use3dDefaultStyle(Drawable[] drawables) {
		if (drawables == null || drawables.length != 5) {
			return;
		}

		BitmapDrawable bitmapDrawable = null;
		for (int i = 0; i < 5; i++) {
			DockAppItemInfo dockAppItemInfo = mDockAppItemInfos.get(i);
			Drawable drawable = drawables[i];
			if (null != drawable) {
				if (drawable instanceof BitmapDrawable) {
					bitmapDrawable = (BitmapDrawable) drawable;
				} else {
					bitmapDrawable = Utilities.createBitmapDrawableFromDrawable(drawable, mContext);
				}
			}
			if (bitmapDrawable != null) {
				dockAppItemInfo.setIcon(bitmapDrawable);
			}
		}
	}

	// 从某一主题获取
	// 1---5 自己定义的5个
	// 后面默认+
	
	/**
	 * <br>功能简述:获取指定主题包指定索引的dock特殊图标信息
	 * <br>功能详细描述:
	 * <br>注意:“主题包”与“风格安装包”区别
	 * @param context
	 * @param themePkg
	 * @param index　1-5
	 * @return
	 */
	public static DeskThemeBean.SystemDefualtItem getSystemDefualtItem(String themePkg, int index) {
		DeskThemeBean.SystemDefualtItem retItem = null;
		if (index < DockUtil.ICON_COUNT_IN_A_ROW) {
			DockBean bean = DockChangeIconControler.getInstance(GOLauncherApp.getContext())
					.getDockBean(themePkg);
			if (null != bean) {
				List<DeskThemeBean.SystemDefualtItem> items = bean.mSymtemDefualt;
				if (null != items) {
					int sz = items.size();
					for (int i = 0; i < sz; i++) {
						DeskThemeBean.SystemDefualtItem item = items.get(i);
						if (null != item) {
							if (item.mIndex == index) {
								retItem = item;
								break;
							}
						}
					}
				}
			}
		}
		return retItem;
	}

	/**
	 * <br>功能简述:获取指定风格安装包指定图片
	 * <br>功能详细描述:
	 * <br>注意:是“风格安装包”，而不是“主题包”，例如：dock透明风格安装包
	 * @param context
	 * @param stylePkgName
	 * @param index
	 * @return
	 */
	public static Drawable getStylePkgDrawable(Context context, String stylePkgName, int index) {
		String picname = null;
		switch (index) {
			case 0 :
				picname = DockStylePkgInfo.PHONE_NAME;
				break;

			case 1 :
				picname = DockStylePkgInfo.CONTACTS_NAME;
				break;

			case 2 :
				picname = DockStylePkgInfo.APPDRAWER_NAME;
				break;

			case 3 :
				picname = DockStylePkgInfo.SMS_NAME;
				break;

			case 4 :
				picname = DockStylePkgInfo.BROWSER_NAME;
				break;

			case 5 :
				picname = DockStylePkgInfo.ADD_NAME;
				break;

			default :
				break;
		}
		Drawable drawable = ImageExplorer.getInstance(context).getDrawable(stylePkgName, picname);

		return drawable;
	}

	/**
	 * <br>功能简述:通过intent从5个dock特殊图标中查找相应的图标
	 * <br>功能详细描述:
	 * <br>注意:TODO:isDockDial　isDockBrowser可能存在用户手机程序更改而不同的bug
	 * @param intent
	 * @return
	 */
	public DockAppItemInfo getDockAppItemInfo(Intent intent) {
		if (null == intent) {
			return null;
		}

		DockAppItemInfo dockAppItemInfo = null;

		int index = -1;
		if (DockUtil.isDockDial(intent)) {
			index = 0;
		} else if (DockUtil.isDockContacts(intent)) {
			index = 1;
		} else if (DockUtil.isDockAppdrawer(intent)) {
			index = 2;
		} else if (DockUtil.isDockSms(intent)) {
			index = 3;
		} else if (DockUtil.isDockBrowser(intent)) {
			index = 4;
		}

		if (index >= 0 && index < mDockAppItemInfos.size()) {
			dockAppItemInfo = mDockAppItemInfos.get(index);
		}

		return dockAppItemInfo;
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

}

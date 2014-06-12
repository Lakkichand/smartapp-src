package com.jiubang.ggheart.apps.desks.imagepreview;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderIcon;
import com.jiubang.ggheart.data.CustomIconRes;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskFolderThemeBean;
import com.jiubang.ggheart.data.theme.bean.DrawResourceThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * @author ruxueqin
 * 
 */
public class ImagePreviewData {
	private Context mContext;

	private ArrayList<ThemeInfoBean> mThemeitems; // 除默认主题以外的所有主题

	private Drawable mDrawable;           //数据源：单独一张drawable
	private String mFloder;               //数据源：整个文件夹导入
	private String[] mStringsarray;       //数据源：资源名字数组
	private ArrayList<String> mResNames;  //数据源：资源名字列表

	private boolean isNeedLoadFolder; // 是否加载文件夹数据
	private ArrayList<String> mFolderPackageList; // 文件夹包的队列
	private ArrayList<String> mFolderResNameList; // 文件夹图标的名称队列

	private int mFolderIconPosition; // 文件夹图标的位置

	/**
	 * 
	 * @param context
	 * @param isNeedLoadFolder
	 *            是否加载文件夹数据
	 */
	public ImagePreviewData(Context context, boolean isNeedLoadFolder) {
		mContext = context;
		this.isNeedLoadFolder = isNeedLoadFolder;
		initmThemeitems();

		if (isNeedLoadFolder) {
			loadFolderIcon();
		}
	}

	private void initmThemeitems() {
		// 获取除默认主题以外的所有主题
		mThemeitems = GOLauncherApp.getThemeManager().getAllThemeInfosWithoutDefaultTheme();
		if (mThemeitems == null) {
			mThemeitems = new ArrayList<ThemeInfoBean>();
		}

		// 把默认主题添加进去
		ThemeInfoBean defualtItem = new ThemeInfoBean();
		defualtItem.setThemeName(mContext.getString(R.string.theme_title));
		defualtItem.setPackageName(ThemeManager.DEFAULT_THEME_PACKAGE);
		mThemeitems.add(0, defualtItem);
	}

	/**
	 * 初始化图标信息数据
	 * 
	 * @param themePkg
	 */
	public void initData(String themePkg) {
		if (null == themePkg) {
			return;
		}

		mFolderIconPosition = 0;

		if (ThemeManager.DEFAULT_THEME_PACKAGE.equals(themePkg)) {
			mResNames = CustomIconRes.getDefaultResList();
		} else {
			DrawResourceThemeBean themeBean = GOLauncherApp.getThemeManager().getThemeResrouceBean(
					themePkg);
			if (themeBean != null) {
				mResNames = themeBean.getDrawrResourceList();
			}
		}

		if (isNeedLoadFolder) {
			putFolderInList(themePkg); // 把文件夹图标添加到队列放第一位
		}
	}

	/**
	 * 加载所有主题包里面文件夹的的图标
	 */
	private void loadFolderIcon() {
		mFolderPackageList = new ArrayList<String>();
		mFolderResNameList = new ArrayList<String>();
		ArrayList<ThemeInfoBean> allThemeInfoBeans = GOLauncherApp.getThemeManager()
				.getAllThemeInfosWithoutDefaultTheme();
		int count = allThemeInfoBeans.size();
		mFolderPackageList.add(ThemeManager.DEFAULT_THEME_PACKAGE); // 添加默认的主题包名
		mFolderResNameList.add(FolderIcon.DEFAULT_RAWICON_RES); // 添加默认主题的文件名
		for (int i = 0; i < count; i++) {
			applyTheme(allThemeInfoBeans.get(i).getPackageName(), mFolderPackageList,
					mFolderResNameList);
		}
	}

	/**
	 * 测试解析文件夹
	 * 
	 * @param themePackage
	 * @param packages
	 * @param resNames
	 * @return
	 */
	private boolean applyTheme(String themePackage, ArrayList<String> packages,
			ArrayList<String> resNames) {
		boolean result = true;
		DeskFolderThemeBean themeBean = null;

		ThemeManager themeManager = GOLauncherApp.getThemeManager();
		final String curThemePackage = themeManager.getCurThemePackage();
		if (themePackage.equals(curThemePackage)) {
			themeBean = themeManager.parserDeskFolderTheme(curThemePackage);
		} else {
			themeBean = themeManager.parserDeskFolderTheme(themePackage);
		}

		if (themeBean != null && themeBean.mFolderStyle != null) {
			if (themeBean.mFolderStyle.mBackground != null) {
				if (themePackage.equals(curThemePackage)) {
					packages.add(0, themePackage);
					resNames.add(0, themeBean.mFolderStyle.mBackground.mResName);
				} else {
					packages.add(themePackage);
					resNames.add(themeBean.mFolderStyle.mBackground.mResName);
				}
			}
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 把文件夹图标添加到队列放第一位
	 * 
	 * @param themePkg
	 */
	public void putFolderInList(String themePkg) {
		if (mFolderPackageList == null || mFolderResNameList == null) {
			return;
		}

		int size = mFolderPackageList.size();
		for (int i = 0; i < size; i++) {
			if (mFolderPackageList.get(i).endsWith(themePkg)) {
				if (mResNames != null) {
					// 先去对应的主题包去一次drawble，不为空才加入mResNames中
					Drawable drawable = ImageExplorer.getInstance(mContext).getDrawable(themePkg,
							mFolderResNameList.get(i));
					if (drawable != null) {
						++mFolderIconPosition;
						mResNames.add(0, mFolderResNameList.get(i));
					}
					break;
				}
			}
		}
	}

	public ArrayList<String> getmFolderPackageList() {
		return mFolderPackageList;
	}

	public ArrayList<String> getmFolderResNameList() {
		return mFolderResNameList;
	}

	public void setmDrawable(Drawable drawable) {
		mDrawable = drawable;
	}

	public Drawable getmDrawable() {
		return mDrawable;
	}

	public void setFolder(String folder) {
		mFloder = folder;
	}

	public String getmFloder() {
		return mFloder;
	}

	public void setStringsarray(String[] array) {
		mStringsarray = array;
	}

	public String[] getmStringsarray() {
		return mStringsarray;
	}

	public ArrayList<String> getmResNameList() {
		return mResNames;
	}

	public ArrayList<ThemeInfoBean> getThemeInfoBeans() {
		return mThemeitems;
	}

	public void initmDrawable(Drawable drawable) {
		mDrawable = drawable;
	}

	public int getFolderIconPosition() {
		return mFolderIconPosition;
	}

	public void resetFolderIconPosition() {
		mFolderIconPosition = -1;
	}
}

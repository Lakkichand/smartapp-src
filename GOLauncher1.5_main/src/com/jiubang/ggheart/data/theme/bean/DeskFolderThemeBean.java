package com.jiubang.ggheart.data.theme.bean;

import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.FolderStyle;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.WallpaperBean;

/**
 * 桌面用户文件夹
 * 
 * @author jiangxuwen
 * 
 */
public class DeskFolderThemeBean extends ThemeBean {
	/**
	 * 文件夹
	 */
	public FolderStyle mFolderStyle;

	public DeskFolderThemeBean(String pkgName) {
		super(pkgName);
		// 不需要添加bean类型，因为没有添加进bean集合列表
		// mBeanType = THEMEBEAN_TYPE_DESK_FOLDER;
		DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
		DeskThemeBean themeBean = themeControler.getDeskThemeBean();
		mFolderStyle = themeBean.createFolderStyle();
		mFolderStyle.mPackageName = pkgName;

	}

	/**
	 * 图片层
	 * 
	 * @author jiangxuwen
	 */
	// public class WallpaperBean{
	// public String mResName;
	// public String mIdentity;
	//
	// public WallpaperBean(){
	// }
	// }

	public WallpaperBean createWallpaperBean() {
		DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
		DeskThemeBean themeBean = themeControler.getDeskThemeBean();
		return themeBean.createWallpaperBean();
	}

	/**
	 * 桌面文件夹样式
	 * 
	 * @author jiagnxuwen
	 * 
	 */
	// public class FolderStyle {
	// public WallpaperBean mBackground;
	// public WallpaperBean mOpendFolder;
	// public WallpaperBean mClosedFolder;
	// public FolderStyle(){
	// mBackground =new WallpaperBean();
	// mOpendFolder =new WallpaperBean();
	// mClosedFolder =new WallpaperBean();
	// }
	// }

	public FolderStyle createFolderStyle() {
		DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
		DeskThemeBean themeBean = themeControler.getDeskThemeBean();
		return themeBean.createFolderStyle();
	}

}

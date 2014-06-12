package com.jiubang.ggheart.data;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.SelfAppItemInfo;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.FolderStyle;

public class SelfAppItemInfoControler extends Controler implements ICleanable, IMessageHandler {
	private SelfAppItemInfo mUserFolder;
	private SelfAppItemInfo mDefaultApplication;

	public SelfAppItemInfoControler(Context context) {
		super(context);

		// default user folder
		mUserFolder = new SelfAppItemInfo();
		mUserFolder.mTitle = mContext.getString(R.string.folder_name);
		mUserFolder.mIcon = (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.folder_back);

		// default app
		mDefaultApplication = new SelfAppItemInfo();
		mDefaultApplication.mTitle = mContext.getString(R.string.loading);
		mDefaultApplication.mIcon = (BitmapDrawable) context.getResources().getDrawable(
				android.R.drawable.sym_def_app_icon);
		// 初始化go主题和go精品数据;
	}

	public AppItemInfo getUserFolder() {
		return mUserFolder;
	}

	public AppItemInfo getDefaultApplication() {
		return mDefaultApplication;
	}

	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		switch (msgId) {
			case IDiyMsgIds.EVENT_THEME_CHANGED :
				doThemeChanged();
				break;

			default :
				break;
		}
		return false;
	}

	private void themeChanged(SelfAppItemInfo appItemInfo) {

	}

	private void doThemeChanged() {
		if (null != mUserFolder) {
			FolderStyle folderStyle = null;
			DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
			if (themeControler != null && themeControler.isUesdTheme()) {
				DeskThemeBean themeBean = themeControler.getDeskThemeBean();
				if (themeBean != null && themeBean.mScreen != null) {
					folderStyle = themeBean.mScreen.mFolderStyle;
				}
			}

			BitmapDrawable icon = null;
			if (folderStyle != null && folderStyle.mBackground != null) {
				icon = (BitmapDrawable) (themeControler
						.getThemeResDrawable(folderStyle.mBackground.mResName));
			}

			if (null != icon) {
				mUserFolder.mIcon = icon;
			}
		}

		// TODO 默认应用

	}

	@Override
	public int getId() {
		return 0;
	}

	/**
	 * 初始化go精品和go主题数据
	 */

	@Override
	public void cleanup() {
		mUserFolder.clearAllObserver();
		mUserFolder = null;

		mDefaultApplication.clearAllObserver();
		mDefaultApplication = null;

	}
}

package com.jiubang.ggheart.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.apps.font.FontStyle;
import com.jiubang.ggheart.apps.font.FontTypeface;

public class FontSettingControl extends Controler implements ISelfObject {
	private static final String TAG = "FontSettingControl";

	public static final int MSG_FONT_CHANGED = 0;

	private DataProvider mDataProvider;
	private AppDataEngine mDataEngine;

	private FontBean mUsedFontBean;
	private Typeface mTypeface;
	private int mStyle;

	public FontSettingControl(Context context, DataProvider dataProvider, AppDataEngine engine) {
		super(context);
		mDataProvider = dataProvider;
		mDataEngine = engine;

		selfConstruct();
	}

	@Override
	public void selfConstruct() {
		// mDataEngine.registerObserver(this);
		mUsedFontBean = getUsedFontBean();
	}

	@Override
	public void selfDestruct() {
		mDataProvider = null;
		if (null != mDataEngine) {
			// mDataEngine.unRegisterObserver(this);
			mDataEngine = null;
		}

		mUsedFontBean = null;
		mTypeface = null;
	}

	public FontBean getUsedFontBean() {
		if (null != mUsedFontBean) {
			return mUsedFontBean;
		}

		mUsedFontBean = createUsedFontBean();
		initTypeface(mUsedFontBean);
		return mUsedFontBean;
	}

	public FontBean createUsedFontBean() {
		FontBean bean = mDataProvider.getUsedFont();
		if (null == bean) {
			bean = new FontBean();
			mDataProvider.insertUsedFont(bean);
		}
		return bean;
	}

	public void updateUsedFontBean(FontBean bean) {
		if (null == bean) {
			Log.i(TAG, "update used font param is null");
			return;
		}

		if (null != mUsedFontBean && mUsedFontBean.equals(bean)) {
			// 没有修改
			return;
		}

		mDataProvider.updateUsedFont(bean);
		mUsedFontBean = bean;
		initTypeface(mUsedFontBean);
		broadCast(MSG_FONT_CHANGED, mStyle, mTypeface, null);
	}

	public ArrayList<FontBean> createFontBeans() {
		ArrayList<FontBean> beans = mDataProvider.getAllFont();
		if (null == beans) {
			// 初始化系统字体
			beans = new ArrayList<FontBean>();
			FontBean bean = new FontBean();
			bean.mFileName = FontTypeface.DEFAULT;
			beans.add(bean);
			bean = new FontBean();
			bean.mFileName = FontTypeface.DEFAULT_BOLD;
			beans.add(bean);
			bean = new FontBean();
			bean.mFileName = FontTypeface.SANS_SERIF;
			beans.add(bean);
			bean = new FontBean();
			bean.mFileName = FontTypeface.SERIF;
			beans.add(bean);
			bean = new FontBean();
			bean.mFileName = FontTypeface.MONOSPACE;
			beans.add(bean);
			updateFontBeans(beans);
		}
		return beans;
	}

	public void updateFontBeans(ArrayList<FontBean> beans) {
		mDataProvider.updateAllFont(beans);
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		super.onBCChange(msgId, param, object, objects);

		// 监听SD卡（SD卡字体会导致重启）
		// 1. 磁盘模式 ： 设为系统字体
		// 2. 挂载模式 ： 设为使用字体
		switch (msgId) {
		// TODO 监听不及时
			case IDiyMsgIds.EVENT_SD_SHARED :
				FontBean bean = new FontBean();
				initTypeface(bean);
				broadCast(MSG_FONT_CHANGED, mStyle, mTypeface, null);
				break;

			// TODO MOUNT多余操作，不过会快很多现实
			case IDiyMsgIds.EVENT_SD_MOUNT :
			case IDiyMsgIds.EVENT_REFLUSH_TIME_IS_UP :
			case IDiyMsgIds.EVENT_REFLUSH_SDCARD_IS_OK :
				initTypeface(mUsedFontBean);
				broadCast(MSG_FONT_CHANGED, mStyle, mTypeface, null);
				break;

			default :
				break;
		}
	}

	public Typeface getTypeface() {
		return mTypeface;
	}

	public int getStyle() {
		return mStyle;
	}

	private void initTypeface(FontBean bean) {
		if (null == bean) {
			return;
		}
		if (FontBean.FONTFILETYPE_SYSTEM == bean.mFontFileType) {
			mTypeface = FontTypeface.typeface(bean.mFileName);
		} else if (FontBean.FONTFILETYPE_PACKAGE == bean.mFontFileType) {
			mTypeface = FontTypeface.typeface(mContext, bean.mPackageName, bean.mFileName);
		} else if (FontBean.FONTFILETYPE_FILE == bean.mFontFileType) {
			mTypeface = FontTypeface.typeface(new File(bean.mFileName));
		} else {

		}
		mStyle = FontStyle.style(bean.mStyle);
	}
}

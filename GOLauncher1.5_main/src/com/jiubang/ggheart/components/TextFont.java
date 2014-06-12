package com.jiubang.ggheart.components;

import java.lang.ref.WeakReference;
import java.util.List;

import android.graphics.Typeface;

import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class TextFont implements ISelfObject, BroadCasterObserver {
	private WeakReference<TextFontInterface> mTextFontInterfaceRef;

	/**
	 * @param textFontInterface
	 *            no null
	 */
	public TextFont(TextFontInterface textFontInterface) {
		mTextFontInterfaceRef = new WeakReference<TextFontInterface>(textFontInterface);

		selfConstruct();
	}

	@Override
	public void selfConstruct() {
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		if (null != controler) {
			controler.registerObserver(this);
			initTypeface(controler.getUsedFontBean().mFontTypeface,
					controler.getUsedFontBean().mFontStyle);
		}
	}

	@Override
	public void selfDestruct() {
		mTextFontInterfaceRef.clear();
		mTextFontInterfaceRef = null;

		GoSettingControler controler = GOLauncherApp.getSettingControler();
		controler.unRegisterObserver(this);
		controler = null;
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case IDiyMsgIds.APPCORE_DATACHANGE :
				if (DataType.DATATYPE_DESKFONTCHANGED == param) {
					if (object instanceof FontBean) {
						FontBean bean = (FontBean) object;
						initTypeface(bean.mFontTypeface, bean.mFontStyle);
					}
				}
				break;

			default :
				break;
		}
	}

	private void initTypeface(Typeface typeface, int style) {
		TextFontInterface textFontInterface = mTextFontInterfaceRef.get();
		if (null == textFontInterface) {
			return;
		}
		textFontInterface.onTextFontChanged(typeface, style);
	}
}

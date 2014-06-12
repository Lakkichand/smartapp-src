package com.jiubang.ggheart.components;

import java.lang.reflect.Field;
import java.util.ArrayList;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceManager.OnActivityDestroyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.launcher.PreferenceConfigurationHandler;

/**
 * 
 * @author 
 *
 */
public class DeskListPreference extends ListPreference implements ISelfObject, TextFontInterface {
	private TextFont mTextFont;
	private ArrayList<TextView> mTextViews = new ArrayList<TextView>();

	public DeskListPreference(Context context) {
		super(context);
		selfConstruct();
	}

	public DeskListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		selfConstruct();

		if (null != DeskResourcesConfiguration.getInstance()) {
			DeskResourcesConfiguration.getInstance().configurationPreference(this, attrs);
		}
	}

	@Override
	public void selfConstruct() {

	}

	@Override
	public void selfDestruct() {
		mTextViews.clear();
		mTextViews = null;

		onUninitTextFont();
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		mTextViews.clear();
		ViewFinder.findView(view, mTextViews);

		onUninitTextFont();
		onInitTextFont();

		PreferenceConfigurationHandler.handlePreferenceItem(view);
	}

	@Override
	public void onInitTextFont() {
		if (null == mTextFont) {
			mTextFont = new TextFont(this);
		}
	}

	@Override
	public void onUninitTextFont() {
		if (null != mTextFont) {
			mTextFont.selfDestruct();
			mTextFont = null;
		}
	}

	@Override
	public void onTextFontChanged(Typeface typeface, int style) {
		int sz = mTextViews.size();
		for (int i = 0; i < sz; i++) {
			TextView textView = mTextViews.get(i);
			if (null == textView) {
				continue;
			}
			textView.setTypeface(typeface, style);
		}
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		if (Machine.isLephone()) {
			super.onPrepareDialogBuilder(builder);
		} else {
			ListAdapter adapter = createAdapter();
			if (null != adapter) {
				builder.setAdapter(adapter, null);
			}
			super.onPrepareDialogBuilder(builder);
		}
	}

	protected ListAdapter createAdapter() {
		CharSequence[] entries = getEntries();
		if (null == entries) {
			return null;
		}
		return new ArrayAdapter<CharSequence>(getContext(),
				R.layout.desk_select_dialog_singlechoice, R.id.radio_textview, entries);
	}

	@Override
	protected void showDialog(Bundle state) {
		try {
			// CharSequence title message postext negtext
			Field titleField = DialogPreference.class.getDeclaredField("mDialogTitle");
			titleField.setAccessible(true);
			Object titleObject = titleField.get(this);
			Field messageField = DialogPreference.class.getDeclaredField("mDialogMessage");
			messageField.setAccessible(true);
			Object messageObject = messageField.get(this);
			Field posField = DialogPreference.class.getDeclaredField("mPositiveButtonText");
			posField.setAccessible(true);
			Object posObject = posField.get(this);
			Field negField = DialogPreference.class.getDeclaredField("mNegativeButtonText");
			negField.setAccessible(true);
			Object negObject = negField.get(this);
			// Drawable icon
			Field iconField = DialogPreference.class.getDeclaredField("mDialogIcon");
			iconField.setAccessible(true);
			Object iconObject = iconField.get(this);

			DeskBuilder builder = new DeskBuilder(getContext());
			builder.setTitle((CharSequence) titleObject);
			builder.setIcon((Drawable) iconObject);
			builder.setPositiveButton((CharSequence) posObject, this);
			builder.setNegativeButton((CharSequence) negObject, this);

			View contentView = onCreateDialogView();
			if (contentView != null) {
				onBindDialogView(contentView);
				builder.setView(contentView);
			} else {
				builder.setMessage((CharSequence) messageObject);
			}

			onPrepareDialogBuilder(builder);

			// Create the dialog
			final Dialog dialog = builder.create();
			if (state != null) {
				dialog.onRestoreInstanceState(state);
			}
			// TODO 软键盘先放放
			// if (needInputMethod()) {
			// requestInputMethod(dialog);
			// }
			dialog.setOnDismissListener(this);

			// 设置 mBuilder mDialog mWhichButtonClicked
			Field buildField = DialogPreference.class.getDeclaredField("mBuilder");
			buildField.setAccessible(true);
			buildField.set(this, builder);
			Field dialogField = DialogPreference.class.getDeclaredField("mDialog");
			dialogField.setAccessible(true);
			dialogField.set(this, dialog);
			Field clickField = DialogPreference.class.getDeclaredField("mWhichButtonClicked");
			clickField.setAccessible(true);
			clickField.set(this, DialogInterface.BUTTON_NEGATIVE);

			// 注册监听
			PreferenceManager manager = getPreferenceManager();
			// Method method =
			// manager.getClass().getMethod("registerOnActivityDestroyListener",
			// this.getClass());
			// method.invoke(manager, this);
			// TODO 非公有函数，不能获取，采取拿值直接搞方案
			// 如果不行就算了，看源码只是消除弹出框
			Field managerField = PreferenceManager.class
					.getDeclaredField("mActivityDestroyListeners");
			managerField.setAccessible(true);
			Object managerObject = managerField.get(manager);
			// if (managerObject instanceof List<OnActivityResultListener>)
			{
				ArrayList<OnActivityDestroyListener> list = (ArrayList<OnActivityDestroyListener>) managerObject;
				if (list == null) {
					list = new ArrayList<OnActivityDestroyListener>();
				}

				if (!list.contains(this)) {
					list.add(this);
				}
				managerField.set(manager, list);
			}

			dialog.show();
		} catch (Exception e) {
			Log.i("DeskListPreference", "showDialog() has exception = " + e.getMessage());
			super.showDialog(state);
		}
	}
}

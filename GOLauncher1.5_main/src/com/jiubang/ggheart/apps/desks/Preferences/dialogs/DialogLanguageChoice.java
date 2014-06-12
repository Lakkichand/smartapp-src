package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingAdvancedActivity;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingMainActivity;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 类描述:单选对话框的公共组件。 
 * 功能详细描述:
 * 
 * @author dingzijian
 * @date [2012-7-25]
 */
public class DialogLanguageChoice extends DialogBase {
	private CharSequence[] mData;
	private CharSequence[] mData2;
	private OnItemCilckListener mCilckListener;
	private int mCheckedItem;
	private String[] mLanguageName = { "ko", "zh_TW", "es", "ru", "en_US", "zh_CN"};
	private List<String> mSrcLanguageArrayList;

	public DialogLanguageChoice(Context context) {
		super(context);
	}

	public DialogLanguageChoice(Context context, int theme) {
		super(context, theme);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initItemView();
	}

	@Override
	public View getView() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dialog_language_select, null);
		mDialogLayout = (LinearLayout) view.findViewById(R.id.dialog_layout);
		mCancelButton = (Button) view.findViewById(R.id.dialog_cancel);
		return view;
	}

	public void initData() {
		String[] codesLanguages = null;
		String uid = Statistics.getUid(mContext);
		if ("200".equals(uid)) {
			codesLanguages = mContext.getResources().getStringArray(R.array.codes_language);
		} else {
			codesLanguages = mContext.getResources().getStringArray(R.array.codes_language_cn);
		}
		if (codesLanguages == null || codesLanguages.length <= 0) {
			return;
		}
		int codesLanguagesLenght = codesLanguages.length;
		String[] showLanguagesArray = new String[codesLanguagesLenght];
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		String currentlanguage = preferences.getString(IPreferencesIds.CURRENTSELETELANGUAGE, "");
		int checkedItem = -1;
		for (int i = 0; i < codesLanguagesLenght; i++) {
			String codesLanguage = codesLanguages[i];
			if (codesLanguage.equals(currentlanguage)) {
				checkedItem = i;
			}
			if ("default".equals(codesLanguage)) {
				showLanguagesArray[i] = codesLanguage;
				continue;
			}
			Locale locale = null;
			if (codesLanguage.length() == 5) {
				String language = codesLanguage.substring(0, 2);
				String country = codesLanguage.substring(3, 5);
				locale = new Locale(language, country);
			} else {
				locale = new Locale(codesLanguage);
			}
			String showLanguage = locale.getDisplayLanguage(locale);
			if (showLanguage != null) {
				if ("zh_CN".equals(codesLanguage)) {
					showLanguage = "简体中文";
				} else if ("zh_TW".equals(codesLanguage)) {
					showLanguage = "繁體中文（台灣）";
				} else if ("zh_HK".equals(codesLanguage)) {
					showLanguage = "繁體中文（香港）";
				} else if ("id".equals(codesLanguage)) {
					showLanguage = "Indonesia";
				} else if ("ir".equals(codesLanguage)) {
					showLanguage = "جمهوری اسلامی ایران‎";
				}
				showLanguagesArray[i] = showLanguage;
			}
		}
		// 获取当前语言包语言
		String language = Locale.getDefault().toString();
		if (language.contains(currentlanguage) || "".equals(currentlanguage)) {
			checkedItem = 0;
		}
		if (language.length() == 5) {
			language = language.substring(0, 2);
		}
		ArrayList<String> mApkLanguageArrayList = new ArrayList<String>();
		String[] supportLanguages = mContext.getResources()
				.getStringArray(R.array.support_language);
		for (String apkLanguage : supportLanguages) {
			mApkLanguageArrayList.add(apkLanguage);
		}
		if (mApkLanguageArrayList.contains(language)) {
			if (!AppUtils
					.isAppExist(mContext, LauncherEnv.Plugin.LANGUAGE_PACKAGE + "." + language)
					|| !AppUtils.isAppExist(mContext, LauncherEnv.Plugin.LANGUAGE_PACKAGE + "."
							+ currentlanguage)) {
				//如果安装语言不存在则显示为英文
				if (checkedItem == -1) {
					if ("200".equals(uid)) {
						checkedItem = 8;
					} else {
						checkedItem = 7;
					}
				}
			}
		}

		mData = showLanguagesArray;
		mData2 = codesLanguages;
		mCheckedItem = checkedItem;
	}

	public void initItemView() {
		mSrcLanguageArrayList = Arrays.asList(mLanguageName);

		final LinearLayout group = (LinearLayout) findViewById(R.id.single_choice_radio_group);
		final ArrayList<RadioButton> buttons = new ArrayList<RadioButton>();
		for (int i = 0; i < mData.length; i++) {
			final View itemView = View.inflate(mContext, R.layout.single_choice_dialog_select_item,
					null);
			TextView textView = (TextView) itemView.findViewById(R.id.single_choice_name);
			final RadioButton radioButton = (RadioButton) itemView
					.findViewById(R.id.single_choice_select);
			final ImageButton downButton = (ImageButton) itemView
					.findViewById(R.id.single_choice_btn);
			mCilckListener = new ViewOnItemClickListen();
			if (mData2[i].equals("default")) {
				radioButton.setVisibility(View.VISIBLE);
				downButton.setVisibility(View.INVISIBLE);
			} else if (AppUtils.isAppExist(mContext, LauncherEnv.Plugin.LANGUAGE_PACKAGE + "."
					+ mData2[i])
					|| mSrcLanguageArrayList.contains(mData2[i])) {
				radioButton.setVisibility(View.VISIBLE);
				downButton.setVisibility(View.INVISIBLE);
			} else {
				downButton.setVisibility(View.VISIBLE);
				radioButton.setVisibility(View.INVISIBLE);
			}
			buttons.add(radioButton);
			if (i == mCheckedItem) {
				radioButton.setChecked(true);
			}
			radioButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					radioButton.setChecked(true);
					if (mCilckListener != null) {
						mCilckListener.onItemCilckListener(v, group.indexOfChild(itemView));
					}
				}
			});
			downButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					radioButton.setChecked(true);
					if (mCilckListener != null) {
						mCilckListener.onItemCilckListener(v, group.indexOfChild(itemView));
					}
				}
			});
			radioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						for (RadioButton button : buttons) {
							if (button.isChecked() && button != buttonView) {
								button.setChecked(false);
							}
						}
					}
				}
			});
			if (i == 0) {
				textView.setText(mContext.getResources().getString(R.string.system_default));
			} else {
				textView.setText(mData[i].toString());
			}
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (radioButton.getVisibility() == View.VISIBLE) {
						radioButton.setChecked(true);
					}
					if (mCilckListener != null) {
						mCilckListener.onItemCilckListener(v, group.indexOfChild(itemView));
					}
				}
			});
			group.addView(itemView);
			setCanceledOnTouchOutside(true);
		}
	}

	/**
	 * item点击事件处理:
	 * 
	 * @author dingzijian
	 * @date [2012-7-25]
	 */
	public interface OnItemCilckListener {
		public void onItemCilckListener(View v, int item);
	};

	public void setOnItemCilckListener(OnItemCilckListener cilckListener) {
		mCilckListener = cilckListener;
	}

	/**
	 * 点击事件:
	 * 
	 * @author dingzijian
	 * @date [2012-7-25]
	 */
	class ViewOnItemClickListen implements OnItemCilckListener {
		String mPackageName = null;

		@Override
		public void onItemCilckListener(View v, int item) {
			switch (v.getId()) {
				case R.id.single_choice_select :
					showRestartDialog(mContext, mData2[item].toString());
					break;
				case R.id.single_choice_btn :
					mPackageName = LauncherEnv.Plugin.LANGUAGE_PACKAGE + "." + mData2[item];
					AppsDetail.gotoDetailDirectly(mContext, AppsDetail.START_TYPE_APPRECOMMENDED, mPackageName);
//					GoStoreOperatorUtil.gotoStoreDetailDirectly(mContext, mPackageName);
					dismiss();
					break;
				case R.id.single_choice_relativelayout :
					if (AppUtils.isAppExist(mContext, LauncherEnv.Plugin.LANGUAGE_PACKAGE + "."
							+ mData2[item])
							|| mSrcLanguageArrayList.contains(mData2[item])
							|| mData2[item].equals("default")) {
						showRestartDialog(mContext, mData2[item].toString());
					} else {
						mPackageName = LauncherEnv.Plugin.LANGUAGE_PACKAGE + "." + mData2[item];
						AppsDetail.gotoDetailDirectly(mContext, AppsDetail.START_TYPE_APPRECOMMENDED, mPackageName);
//						GoStoreOperatorUtil.gotoStoreDetailDirectly(mContext, mPackageName);
						dismiss();
					}
					break;
				default :
					break;
			}
		}

	}

	//保存值。提示重启桌面

	public void showRestartDialog(final Context context, String item) {
		final String restartItem;
		final PreferencesManager restartPreferences = new PreferencesManager(context,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		if (item.equals("default")) {
			restartItem = "";
		} else {
			restartItem = item;
		}

		DialogConfirm mNormalDialog = new DialogConfirm(context);
		mNormalDialog.show();
		mNormalDialog.setTitle(R.string.attention_title);
		mNormalDialog.setMessage(R.string.language_change_content);
		mNormalDialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				restartPreferences.putString(IPreferencesIds.CURRENTSELETELANGUAGE, restartItem);
				restartPreferences.commit();
				dismiss();
				if (mContext instanceof DeskSettingAdvancedActivity) {
					((Activity) mContext).setResult(
							DeskSettingMainActivity.RESULT_CODE_RESTART_GO_LAUNCHER, null);
					((Activity) mContext).finish();
				}
				// 重启桌面
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.RESTART_GOLAUNCHER, -1, null, null);

			}
		});
	}
}

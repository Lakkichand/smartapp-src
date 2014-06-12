package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
/**
 * 
 * @author 
 *
 */
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2012-12-4]
 */
public class NewThemeAdapter extends BaseAdapter {
	private static final String GOLAUNCHER_ACTION = "com.gau.go.launcherex.MAIN";
	private static final String GOWIDGET_ACTION = "com.gau.go.launcherex.gowidget";
	private static final String GOLOCK_ACTION = "com.jiubang.goscreenlock";
	private Context mContext;
	private ThemeInfoBean mInfoBean;
	private ArrayList<String> mNewThemeTips = null;
	private HashMap<String, Boolean> mCheckBoxState = null;
	// 大主题所需资源
	private String mGolauncher;
	private String mGowidget;
	private String mGolock;

	public NewThemeAdapter(Context context, ThemeInfoBean mInfoBean, String mGolauncher,
			String mGowidget, String mGolock) {
		this.mContext = context;
		this.mInfoBean = mInfoBean;
		this.mGolauncher = mGolauncher;
		this.mGowidget = mGowidget;
		this.mGolock = mGolock;
		mNewThemeTips = new ArrayList<String>();
		mCheckBoxState = new HashMap<String, Boolean>();
		mNewThemeTips.add(this.mGolauncher);
		mNewThemeTips.add(this.mGowidget);
		mNewThemeTips.add(this.mGolock);

		for (int i = 0; i < mNewThemeTips.size(); i++) {
			// 默认checkbox状态为选中
			mCheckBoxState.put(mNewThemeTips.get(i), true);
		}
	}

	public HashMap<String, Boolean> getmCheckBoxState() {
		return mCheckBoxState;
	}

	public void filterNotExistTheme() {
		System.out.println("");
		if (!mInfoBean.ismExistGolauncher()) {
			mNewThemeTips.remove(mGolauncher);
			mInfoBean.getNewThemeInfo().getNewThemePkg().remove(GOLAUNCHER_ACTION);
		}
		if (!mInfoBean.ismExistGolock()) {
			mNewThemeTips.remove(mGolock);
			mInfoBean.getNewThemeInfo().getNewThemePkg().remove(GOLOCK_ACTION);
		}
		if (mInfoBean.getGoWidgetPkgName() == null) {
			mNewThemeTips.remove(mGowidget);
			mInfoBean.getNewThemeInfo().getNewThemePkg().remove(GOWIDGET_ACTION);
		}
	}

	@Override
	public int getCount() {
		return mNewThemeTips.size();
	}

	@Override
	public Object getItem(int position) {
		return mNewThemeTips.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RelativeLayout relativelayout = null;
		TextView textView = null;
		CheckBox checkBox = null;
		Button downloadButton = null;
		if (convertView != null) {
			relativelayout = (RelativeLayout) convertView;
			textView = (TextView) relativelayout.findViewById(R.id.new_theme_tip);
			checkBox = (CheckBox) relativelayout.findViewById(R.id.new_theme_checkbox);
			downloadButton = (Button) relativelayout.findViewById(R.id.new_theme_download_button);
		} else {
			relativelayout = (RelativeLayout) View.inflate(mContext, R.layout.new_theme_tips_item,
					null);
			textView = (TextView) relativelayout.findViewById(R.id.new_theme_tip);
			checkBox = (CheckBox) relativelayout.findViewById(R.id.new_theme_checkbox);
			downloadButton = (Button) relativelayout.findViewById(R.id.new_theme_download_button);
		}

		textView.setText(mNewThemeTips.get(position));
		DeskSettingConstants.setTextViewTypeFace(textView);
		DeskSettingConstants.setTextViewTypeFace(downloadButton);
		final int pos = position;
		// final String newPkg =
		// mInfoBean.getNewThemeInfo().getNewThemePkg().get(position);
		final String newPkg = getNewPkg(position);

		if (newPkg != null && !newPkg.trim().equals("")) {
			Intent intent = new Intent(newPkg);
			if (!AppUtils.isAppExist(mContext, intent)) {
				checkBox.setVisibility(View.GONE);
				downloadButton.setVisibility(View.VISIBLE);
			} else {
				checkBox.setVisibility(View.VISIBLE);
				downloadButton.setVisibility(View.GONE);
			}
		}

		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCheckBoxState.put(mNewThemeTips.get(pos), isChecked);
			}
		});

		downloadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (newPkg != null && !newPkg.trim().equals("")) {
					if (newPkg.trim().equals(GOWIDGET_ACTION)) {
						// 修改为 跳转添加界面 go小部件
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
								IDiyMsgIds.SCREEN_EDIT_GOTO_GO_WIDGET, 0, null, null);
						// Intent toGoWidget = new
						// Intent(GoWidgetConstant.ACTION_GOTO_GOWIDGET_FRAME);
						// context.sendBroadcast(toGoWidget);
					} else if (newPkg.trim().equals(GOLOCK_ACTION)) {
						AppsDetail.gotoDetailDirectly(mContext,
								AppsDetail.START_TYPE_APPRECOMMENDED, newPkg);
//						GoStoreOperatorUtil.gotoStoreDetailDirectly(context, newPkg);
					}
				}
				// view.dimissDialog();
			}
		});

		return relativelayout;
	}

	private String getNewPkg(int position) {
		String newPkg = null;
		try {
			newPkg = mInfoBean.getNewThemeInfo().getNewThemePkg().get(position);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return newPkg;
	}

}

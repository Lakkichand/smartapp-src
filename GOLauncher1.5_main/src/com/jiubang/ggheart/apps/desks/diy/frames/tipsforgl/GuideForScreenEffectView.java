/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.frames.tipsforgl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * @author ruxueqin
 * 
 */
public class GuideForScreenEffectView extends RelativeLayout
		implements
			OnItemClickListener,
			OnClickListener {
	private ListView mListView;

	private ScreeneffectAdapter mAdapter;

	private Button mSelectButton;

	/**
	 * @param context
	 * @param attrs
	 */
	public GuideForScreenEffectView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initData();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mSelectButton = (Button) findViewById(R.id.tryit);
		mSelectButton.setOnClickListener(this);

		mListView = (ListView) findViewById(R.id.effects);
		initData();
		mListView.setAdapter(mAdapter);
		mAdapter.updateSelection(0); // 默认选择“波浪”特效
		mListView.setOnItemClickListener(this);
	}

	private void initData() {
		int[] values = new int[] { 3, 7, 13, 16, 1 };
		mAdapter = new ScreeneffectAdapter(getContext(), values);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		mAdapter.updateSelection(arg2);
	}

	@Override
	public void onClick(View v) {
		if (v == mSelectButton) {
			try {
				int selection = mAdapter.getSelectValue();
				GoSettingControler controler = GOLauncherApp.getSettingControler();
				EffectSettingInfo mEffectInfo = controler.getEffectSettingInfo();
				if (mEffectInfo.mEffectorType != selection) {
					mEffectInfo.mEffectorType = selection;
					controler.updateEffectSettingInfo(mEffectInfo);
				}

				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_NEED_CHECK_SHOW_SCREEN_EFFECT_GUIDE, -1, null, null);

				PreferencesManager sharedPreferences = new PreferencesManager(getContext(),
						IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
				sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_SCREEN_EFFECT_SECOND_TIP,
						true);
				sharedPreferences.commit();
			} catch (IllegalStateException e) {
				Toast.makeText(getContext(), R.string.guide_screen_effect_noselect_effect,
						Toast.LENGTH_SHORT).show();
				return;
			} catch (Exception e) {
			}
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
				IDiyFrameIds.GUIDE_GL_FRAME, null, null);
	}

	public void changeOritation(int oritation) {
		RelativeLayout.LayoutParams lp_listview = (RelativeLayout.LayoutParams) mListView
				.getLayoutParams();
		int listview_height = getContext().getResources().getDimensionPixelSize(
				R.dimen.guide_screeneffect_listview_height);
		int listview_margin_top = getContext().getResources().getDimensionPixelSize(
				R.dimen.guide_screeneffect_topline_listview_space);
		lp_listview.height = listview_height;
		lp_listview.topMargin = listview_margin_top;

		RelativeLayout.LayoutParams lp_button = (RelativeLayout.LayoutParams) mSelectButton
				.getLayoutParams();
		int button_margin_top = getContext().getResources().getDimensionPixelSize(
				R.dimen.guide_screeneffect_listview_button_space);
		lp_button.topMargin = button_margin_top;
	}
}

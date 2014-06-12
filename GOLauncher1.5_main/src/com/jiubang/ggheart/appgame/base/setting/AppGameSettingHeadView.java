/*
 * 文 件 名:  AppGameSettingHeadView.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-7-24
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.setting;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.gau.go.launcherex.R;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-24]
 */
public class AppGameSettingHeadView extends Preference {

	private OnClickListener mBackListener = null;

	public AppGameSettingHeadView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		setLayoutResource(R.layout.appgame_setting_topview);
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		ImageButton btn = (ImageButton) view.findViewById(R.id.appgame_setting_back);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mBackListener != null) {
					mBackListener.onClick(v);
				}
			}
		});
	}

	public void setOnClickListener(OnClickListener listener) {
		this.mBackListener = listener;
	}

}

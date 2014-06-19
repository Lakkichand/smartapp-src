package com.jiubang.go.backup.pro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.util.Util;

/**
 * Root权限介绍页面
 * 
 * @author maiyongshen
 */
public class RootIntroductionActivity extends BaseActivity {

	private TextView mRootState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		// PreferenceManager pm = PreferenceManager.getInstance();
		// pm.putBoolean(this, PreferenceManager.KEY_SHOULD_SHOW_ROOT_TAB_PROMT,
		// false);

		StatisticsDataManager.getInstance().updateStatisticBoolean(this,
				StatisticsKey.GOINTO_ROOT_INTRODUCTION, true);
	}

	private void initViews() {
		setContentView(R.layout.layout_root_introduction_page);

		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.root_introduction_title);

		findViewById(R.id.return_btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mRootState = (TextView) findViewById(R.id.root_state);
		if (mRootState != null) {
			if (possibleRootUser()) {
				mRootState.setTextColor(getResources().getColor(R.color.root_introduction_rooted_text_color));
				mRootState.setText(R.string.root_state_rooted);
			} else {
				mRootState.setTextColor(getResources().getColor(R.color.root_introduction_unroot_text_color));
				mRootState.setText(R.string.root_state_unrooted);
			}
		}

		findViewById(R.id.what_is_root).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://en.wikipedia.org/wiki/Rooting_(Android_OS)"));
				try {
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		findViewById(R.id.how_to_get_root).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://en.wikipedia.org/wiki/Rooting_(Android_OS)"));
				try {
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private boolean possibleRootUser() {
		return Util.isRootRom(this);
	}
}

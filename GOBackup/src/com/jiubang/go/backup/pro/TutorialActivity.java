package com.jiubang.go.backup.pro;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.selfdef.ui.TutorialView;
import com.jiubang.go.backup.pro.selfdef.ui.TutorialView.OnSpotlightClickListener;

/**
 * 整合备份引导页
 * 
 * @author maiyongshen
 */
public class TutorialActivity extends Activity {
	public static final String KEY_LAYOUT_ID = "key_layout_id";
	public static final String KEY_SPOTLIGHT_RECT = "key_spotlight_rect";
	public static final String KEY_SPOTLIGHTE_CLICK = "key_spotlight_click";
	public static final String KEY_SPOTLIGHT_DRAWABLE_ID = "spotlight_drawable_id";
	private boolean mIsTutorialShow = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		int layoutId = intent.getIntExtra(KEY_LAYOUT_ID, -1);
		Rect rect = intent.getParcelableExtra(KEY_SPOTLIGHT_RECT);
		int spotLightDrawableId = intent.getIntExtra(KEY_SPOTLIGHT_DRAWABLE_ID, R.drawable.tutorial_circle);
		if (layoutId == -1) {
			finish();
		}

		TutorialView view = new TutorialView(this, layoutId, spotLightDrawableId, rect);
		view.setOnSpotlightClickListener(new OnSpotlightClickListener() {
			@Override
			public void onSpotlightClick(int x, int y) {
				Intent it = new Intent();
				it.putExtra(KEY_SPOTLIGHTE_CLICK, true);
				setResult(RESULT_OK, it);
				finish();
			}
		});
		setContentView(view);
		getWindow().setFormat(PixelFormat.RGBA_8888);
		mIsTutorialShow = true;
		Button btn = (Button) findViewById(R.id.tutorial_ok);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (!mIsTutorialShow) {
			return;
		}
		finish();
	}
}

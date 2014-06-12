package com.jiubang.ggheart.screen.touchhelper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture.MultiTouchDetector;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture.MutilPointInfo;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.gesture.OnMultiTouchListener;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * @author dengdazhong
 *
 */
public class TouchHelperDownloadGuideActivity extends Activity implements OnMultiTouchListener {
	public static final String DOWNLOAD_200 = "com.gau.go.launcher.touchhelper"; // 200渠道的下載地址
	public static final String DOWNLOAD_UN_200 = "http://godfs.3g.cn/group2/M00/05/01/wKiiDFErHTWEAme_AAAAAIgpQHU758.apk"; // 非200渠道的下載地址
	// 多点触摸识别
	private MultiTouchDetector mTouchDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.touchhelper_download_guide_layout);
		Reflector.overridePendingTransition(this, R.anim.slide_self_top_in, R.anim.slide_top_out);
		mTouchDetector = new MultiTouchDetector(this, this);
		Button downloadBtn = (Button) findViewById(R.id.download);
		downloadBtn.setShadowLayer(1, 0, -1, Color.parseColor("#5b000000"));
		downloadBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				String url = DOWNLOAD_200;
//				if (!GoStorePhoneStateUtil.is200ChannelUid(TouchHelperDownloadGuideActivity.this)) {
//					url = DOWNLOAD_UN_200;
//				}
//				Uri uri = Uri.parse(url); 
//				Intent it = new Intent(Intent.ACTION_VIEW, uri);  
//				startActivity(it);
				String title = getText(R.string.guide_custom_full_screen_widget_title).toString();
				CheckApplication.downloadAppFromMarketFTPGostore(
						TouchHelperDownloadGuideActivity.this, null, new String[] { DOWNLOAD_200,
								DOWNLOAD_UN_200 }, LauncherEnv.GOLAUNCHER_FORTOUCHHELPER_GOOGLE_REFERRAL_LINK, title, Long.MIN_VALUE, false,
						CheckApplication.FROM_TOUCHHELPER_RECOMMAND);
			}
		});
		
		downloadBtn = (Button) findViewById(R.id.never_show);
		downloadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PreferencesManager sp = new PreferencesManager(
						TouchHelperDownloadGuideActivity.this, IPreferencesIds.USERTUTORIALCONFIG,
						Context.MODE_PRIVATE);
				sp.putBoolean(IPreferencesIds.NEVER_SHOW_TOUCHHELPER_RECOMMAND, true);
				sp.commit();
				slipOut();
			}
		});
	}

	private void slipOut() {
		// 上滑退出
		finish();
		Reflector.overridePendingTransition(this, R.anim.slide_top_in, R.anim.slide_self_top_out);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// 如果是多点触摸，直接截获事件
/*		if (mTouchDetector.onTouchEvent(ev)) {
			return true;
		}*/
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 如果是多点触摸，直接截获事件
		if (mTouchDetector.onTouchEvent(event)) {
			return true;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void onBackPressed() {
		slipOut();
	}

	@Override
	public boolean onSwipe(MutilPointInfo p, float dx, float dy, int direction) {
		if (p.getPointCount() == 2 && direction == OnMultiTouchListener.DIRECTION_UP) {
			// 双指下滑打开全屏插件
			slipOut();
			return true;
		}
		return false;
	}

	@Override
	public boolean onScale(MutilPointInfo p, float scale, float angle) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTap(MutilPointInfo p) {
		// TODO Auto-generated method stub
		return false;
	}
}
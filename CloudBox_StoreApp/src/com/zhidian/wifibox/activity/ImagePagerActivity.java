package com.zhidian.wifibox.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.TextView;

import com.desrcibe.bigimageview.HackyViewPager;
import com.desrcibe.bigimageview.ImageDetailFragment;
import com.ta.TAApplication;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;

/**
 * 应用详情大图界面
 * 
 * @author zhaoyl
 * 
 */
public class ImagePagerActivity extends FragmentActivity {
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String EXTRA_IMAGE_INDEX = "image_index";
	public static final String EXTRA_IMAGE_URLS = "image_urls";

	public static final String PARAMS = "params";
	public static final String POSTION = "postion";
	public static final String BUNLDER = "b";

	private HackyViewPager mPager;
	private int pagerPosition; // 当前位置
	private TextView indicator;
	private ArrayList<String> picturePath = null; // 图片地址

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail_pager);
		Bundle bundle = this.getIntent().getExtras().getBundle(BUNLDER);
		pagerPosition = bundle.getInt(POSTION);

		picturePath = new ArrayList<String>();
		picturePath = bundle.getStringArrayList(PARAMS);

		// String[] urls = getIntent().getStringArrayExtra(EXTRA_IMAGE_URLS);
		// pagerPosition = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);

		mPager = (HackyViewPager) findViewById(R.id.pager);
		ImagePagerAdapter mAdapter = new ImagePagerAdapter(
				getSupportFragmentManager(), picturePath);
		mPager.setAdapter(mAdapter);
		indicator = (TextView) findViewById(R.id.indicator);

		CharSequence text = getString(R.string.viewpager_indicator, 1, mPager
				.getAdapter().getCount());
		indicator.setText(text);
		// 更新下标
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				CharSequence text = getString(R.string.viewpager_indicator,
						arg0 + 1, mPager.getAdapter().getCount());
				indicator.setText(text);
			}

		});
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		mPager.setCurrentItem(pagerPosition);
	}

	public void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("应用详情大图界面");
			MobclickAgent.onResume(this);
		}
	}

	public void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("应用详情大图界面");
			MobclickAgent.onPause(this);
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
		// overridePendingTransition(R.anim.my_scale_action_out,
		// R.anim.my_alpha_action_out);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, mPager.getCurrentItem());
	}

	private class ImagePagerAdapter extends FragmentStatePagerAdapter {

		private ArrayList<String> picturePath = new ArrayList<String>();

		public ImagePagerAdapter(FragmentManager fm,
				ArrayList<String> picturePath) {
			super(fm);
			this.picturePath = picturePath;
		}

		@Override
		public int getCount() {
			return picturePath == null ? 0 : picturePath.size();
		}

		@Override
		public Fragment getItem(int position) {
			String url = picturePath.get(position);
			return ImageDetailFragment.newInstance(url);
		}

	}
}
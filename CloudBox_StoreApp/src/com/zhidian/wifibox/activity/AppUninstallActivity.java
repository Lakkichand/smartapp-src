package com.zhidian.wifibox.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiubang.core.message.IMessageHandler;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.AppUninstallAdapter;
import com.zhidian.wifibox.controller.AppUninstallController;
import com.zhidian.wifibox.data.AppUninstallBean;
import com.zhidian.wifibox.data.AppUninstallGroup;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.AppUninstaller;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.DrawUtil;

/**
 * 应用卸载
 * 
 * @author xiedezhi
 * 
 */
public class AppUninstallActivity extends FragmentActivity implements
		IMessageHandler {

	private ViewPager mViewPager;
	private AppUninstallAdapter mPagerAdapter;
	private TextView mTitle1;
	private TextView mTitle2;
	private Button mButton;
	private View mButtonFrame;
	private View mLoadingFrame;
	private View mContent;

	public List<AppUninstallGroup> mUserappGroup = new ArrayList<AppUninstallGroup>();
	public List<AppUninstallBean> mSystemappInfo = new ArrayList<AppUninstallBean>();

	private int mFreezeCount;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.appuninstall);

		TextView title = (TextView) findViewById(R.id.header_title_text);
		title.setText("应用卸载");
		ImageView btnBack = (ImageView) findViewById(R.id.header_title_back);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		mContent = findViewById(R.id.content_frame);
		mLoadingFrame = findViewById(R.id.loading_frame);
		mContent.setVisibility(View.GONE);
		mLoadingFrame.setVisibility(View.VISIBLE);

		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mPagerAdapter = new AppUninstallAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mTitle1 = (TextView) findViewById(R.id.title1);
		mTitle2 = (TextView) findViewById(R.id.title2);
		mTitle1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mViewPager.getCurrentItem() != 0) {
					try {
						mViewPager.setCurrentItem(0);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		mTitle2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mViewPager.getCurrentItem() != 1) {
					try {
						mViewPager.setCurrentItem(1);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		mTitle1.setTextColor(0xFFFFFFFF);
		mTitle2.setTextColor(0xFF666666);
		mTitle1.setBackgroundColor(0xFF32b27c);
		mTitle2.setBackgroundColor(0xFFFFFFFF);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == 0) {
					mTitle1.setTextColor(0xFFFFFFFF);
					mTitle2.setTextColor(0xFF666666);
					mTitle1.setBackgroundColor(0xFF32b27c);
					mTitle2.setBackgroundColor(0xFFFFFFFF);
					mButton.setBackgroundResource(R.drawable.appuninstall_btn_bg);
					mButton.setText("立即卸载");
				} else {
					mTitle2.setTextColor(0xFFFFFFFF);
					mTitle1.setTextColor(0xFF666666);
					mTitle2.setBackgroundColor(0xFF32b27c);
					mTitle1.setBackgroundColor(0xFFFFFFFF);
					mButton.setBackgroundResource(R.drawable.appuninstall_trashbtn_bg);
					String text = "回收站(有" + mFreezeCount + "款系统软件可还原)";
					SpannableString word = new SpannableString(text);
					word.setSpan(new ForegroundColorSpan(0xFFbcfde1), 3,
							word.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					word.setSpan(
							new AbsoluteSizeSpan(DrawUtil.dip2px(
									getApplicationContext(), 14)), 3, word
									.length(),
							Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					mButton.setText(word);
				}
				TAApplication.sendHandler(null, IDiyFrameIds.APPUNINSTALL,
						IDiyMsgIds.UPDATE_UNINSTALL_BTN, 0, null, null);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		mButtonFrame = findViewById(R.id.btn_frame);
		mButton = (Button) findViewById(R.id.btn);
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mViewPager.getCurrentItem() == 0) {
					Set<String> set = new HashSet<String>();
					for (AppUninstallGroup group : mUserappGroup) {
						if (group.mList != null) {
							for (AppUninstallBean bean : group.mList) {
								if (bean.isSelect
										&& AppUtils.isAppExist(
												getApplicationContext(),
												bean.packname)) {
									set.add(bean.packname);
								}
							}
						}
					}
					if (set.size() <= 0) {
						Toast.makeText(getApplicationContext(), "请选择需要卸载的应用",
								Toast.LENGTH_SHORT).show();
					} else {
						for (String pkg : set) {
							AppUninstaller.commonUninstall(
									AppUninstallActivity.this, pkg);
						}
					}
				} else {
					// 打开回收站
					Intent intent = new Intent();
					intent.setClass(AppUninstallActivity.this,
							FreezeAppActivity.class);
					startActivity(intent);
				}
			}
		});
		// 开始扫描
		TAApplication.getApplication().doCommand(
				TAApplication.getApplication().getString(
						R.string.appuninstallcontroller),
				new TARequest(AppUninstallController.SCAN, null),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						mLoadingFrame.setVisibility(View.GONE);
						mContent.setVisibility(View.VISIBLE);
						Object[] objs = (Object[]) response.getData();
						mUserappGroup.addAll((List<AppUninstallGroup>) objs[0]);
						mSystemappInfo.addAll((List<AppUninstallBean>) objs[1]);
						// 显示列表
						mPagerAdapter.update(2);
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(TAResponse response) {
					}
				}, true, false);
		Animation animation = new AlphaAnimation(1, 0.8f);
		animation.setFillAfter(true);
		findViewById(R.id.title_frame).startAnimation(animation);
		// 注册消息组件
		TAApplication.registMsgHandler(this);
		TAApplication.sendHandler(null, IDiyFrameIds.APPUNINSTALL,
				IDiyMsgIds.UPDATE_UNINSTALL_BTN, 0, null, null);
	}

	/**
	 * 更新冻结数
	 */
	public void updateFreezeCount() {
		int count = 0;
		PackageManager pManager = getPackageManager();
		List<PackageInfo> packlist = null;
		try {
			packlist = pManager.getInstalledPackages(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (packlist != null) {
			for (PackageInfo info : packlist) {
				// 系统应用
				if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
					int state = pManager
							.getApplicationEnabledSetting(info.packageName);
					// 被冻结
					if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
							|| state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER) {
						count++;
					}
				}
			}
		}
		mFreezeCount = count;
		if (mViewPager.getCurrentItem() == 1) {
			// 更新按钮
			String text = "回收站(有" + mFreezeCount + "款系统软件可还原)";
			SpannableString word = new SpannableString(text);
			word.setSpan(new ForegroundColorSpan(0xFFbcfde1), 3, word.length(),
					Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			word.setSpan(
					new AbsoluteSizeSpan(DrawUtil.dip2px(
							getApplicationContext(), 14)), 3, word.length(),
					Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			mButton.setText(word);
			if (mFreezeCount <= 0) {
				mButtonFrame.setVisibility(View.GONE);
			} else {
				mButtonFrame.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 页面统计
		StatService.trackBeginPage(this, "应用卸载");
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		if (click != null) {
			// TODO
		}
		// 更新冻结应用数
		updateFreezeCount();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 页面统计
		StatService.trackEndPage(this, "应用卸载");
		XGPushManager.onActivityStoped(this);
	}

	@Override
	protected void onDestroy() {
		TAApplication.unRegistMsgHandler(this);
		super.onDestroy();
	}

	@Override
	public int getId() {
		return IDiyFrameIds.APPUNINSTALL;
	}

	@Override
	public boolean handleMessage(Object who, int type, final int msgId,
			final int param, final Object object, final List objects) {
		switch (msgId) {
		case IDiyMsgIds.UPDATE_FREEZE: {
			updateFreezeCount();
			break;
		}
		case IDiyMsgIds.UPDATE_RESTORE: {
			try {
				Set<AppUninstallBean> successBean = (Set<AppUninstallBean>) object;
				if (successBean.size() > 0) {
					mSystemappInfo.addAll(successBean);
					// 系统应用排序
					Collections.sort(mSystemappInfo,
							new Comparator<AppUninstallBean>() {
								public int compare(AppUninstallBean arg0,
										AppUninstallBean arg1) {
									if (arg0.size == arg1.size) {
										return 0;
									}
									return (arg0.size - arg1.size > 0 ? -1 : 1);
								}
							});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		case IDiyMsgIds.UPDATE_UNINSTALL_BTN: {
			if (mViewPager.getCurrentItem() == 0) {
				try {
					boolean select = false;
					if (mUserappGroup != null) {
						for (AppUninstallGroup group : mUserappGroup) {
							for (AppUninstallBean bean : group.mList) {
								if (bean.isSelect) {
									select = true;
									break;
								}
							}
						}
					}
					if (select) {
						mButtonFrame.setVisibility(View.VISIBLE);
					} else {
						mButtonFrame.setVisibility(View.GONE);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				updateFreezeCount();
			}
			break;
		}
		}
		return false;
	}
}

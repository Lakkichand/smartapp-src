package com.jiubang.ggheart.apps.desks.share;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.ConfigurationInfo;
import com.go.util.device.Machine;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.snapshot.SnapShotManager;
import com.jiubang.ggheart.apps.gowidget.gostore.component.ThemeStoreProgressBar;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author maxiaojun
 * @date [2012-9-11]
 */
public class ShareLayout extends RelativeLayout {
	private ShareContainer mShareContainer;
	private Button mShareButton; // 分享按钮
	private ThemeStoreProgressBar mLoadView; // 进度条
	private boolean mIsSharing; // 避免分享创建图片过程中重复点击
	private TextView mTipText;
	private Activity mActivity;
	private long mCurrTime;

	public static final String DOWNLOAD_200 = "http://goo.gl/R6Vml"; // 200渠道的下載地址
	public static final String DOWNLOAD_UN_200 = "http://t.cn/zW0DwGX"; // 非200渠道的下載地址

	private int mMaxCount = MAX_COUNT_HIGHT;
	public static final int MAX_COUNT_HIGHT = 3; //高端机最多只能选三张
	public static final int MAX_COUNT_LOW = 1; //低端机最多只能选一张

	private int mType = ShareFrame.TYPE_SHARE;
	public ShareLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundResource(R.color.theme_bg);
		mIsSharing = false;
		if (ConfigurationInfo.getDeviceLevel() == ConfigurationInfo.LOW_DEVICE) {
			mMaxCount = MAX_COUNT_LOW; //低端机
		} else {
			mMaxCount = MAX_COUNT_HIGHT;
		}
	}

	/**
	 * 初始化
	 * @param activity
	 * @param type {@link ShareFrame.TYPE_SHARE} {@link ShareFrame.TYPE_CAPTURE}
	 */
	public void init(Activity activity, int type) {
		mActivity = activity;
		mType = type;
		int count = getImageCount(mType);
		mTipText = (TextView) findViewById(R.id.share_point);
		if (mMaxCount == MAX_COUNT_HIGHT) {
			mTipText.setText(getResources().getString(R.string.share_text_choose_tip));
		} else {
			mTipText.setText(getResources().getString(R.string.share_text_choose_tip_low));
		}
		mShareContainer = (ShareContainer) findViewById(R.id.shareContainer);
		mShareContainer.setMaxCount(mMaxCount);
		mShareButton = (Button) findViewById(R.id.immediately_share);
		mShareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long time = System.currentTimeMillis();
				if (time - mCurrTime < 3000) {
					return;
				} else {
					mCurrTime = time;
				}
				ArrayList<Integer> list = mShareContainer.getSelList();
				if (list == null || list.size() == 0) {
					// 还沒选择要分享的圖片
					mIsSharing = false;
				} else {
					if (mIsSharing) {
						return;
					}
					// 检查sd卡
					if (!Machine.isSDCardExist()) {
						Toast.makeText(
								getContext(),
								getContext().getResources().getString(
										R.string.import_export_sdcard_unmounted), 200).show();
						return;
					}
					mIsSharing = true;
					// 分享圖片
					showLoadView();
					Bitmap img = null;
					try {
						ShareImage shareImage = new ShareImage(getContext(), list, mType);
						img = shareImage.spellImage();
						shareImage.clear();
					} catch (OutOfMemoryError error) {
						OutOfMemoryHandler.handle();
					}
					// 点分享按钮的统计
					StatisticsData.countMenuData(mActivity, StatisticsData.SHARE_ID_SHARE);

					AsyncShareImage load = new AsyncShareImage(img);
					load.execute();
				}
			}
		});
		if (null != mShareContainer) {
			mShareContainer.setShareButton(mShareButton);
			mShareContainer.initData(count, mType);
			mShareButton.setCompoundDrawables(null, null, null, null);
			mShareButton.setTextColor(0xffaaaaaa);
			mShareButton.setClickable(false);
		}

		mLoadView = (ThemeStoreProgressBar) findViewById(R.id.share_loading);
		if (mLoadView != null) {
			mLoadView.setBackgroundColor(0x9e000000);
			mLoadView.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return true;
				}
			});
		}
	}

	/**
	 * 获取待分享的图片个数
	 * @param type {@link ShareFrame.TYPE_SHARE} {@link ShareFrame.TYPE_CAPTURE}
	 * @return
	 */
	public int getImageCount(int type) {
		int count = 0;
		switch (type) {
			case ShareFrame.TYPE_SHARE : {
				Bundle bundle = new Bundle();
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.GET_SHARE_IMAGE_NUM, -1, bundle, null);
				count = bundle.getInt("imagenum");
			}
				break;
			case ShareFrame.TYPE_CAPTURE : {
				count = SnapShotManager.getInstance(GOLauncherApp.getContext()).getSnapShotCount();
			}
		}
		return count;
	}

	private static final int MSG_SHOW_PROGRESS = 0;
	private static final int MSG_HIDE_PROGRESS = 1;
	private static final int MSG_OPEN_CHOOSER = 2;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_SHOW_PROGRESS :
					showLoadView();
					break;
				case MSG_HIDE_PROGRESS :
					hideLoadView();
					break;
				case MSG_OPEN_CHOOSER :
					if (!mIsSharing) {
						return;
					}
					mIsSharing = false;
					openChooser();
					hideLoadView();
					break;
			}
		}
	};

	private void openChooser() {
		String path = LauncherEnv.Path.SHARE_IMAGE_PATH + "shareImg.jpg";
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("image/jpeg");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				getResources().getString(R.string.share_title));

		String shareContent = getResources().getString(R.string.share_content_text);
		if (GoStorePhoneStateUtil.is200ChannelUid(getContext())) {
			shareContent += DOWNLOAD_200;
		} else {
			shareContent += DOWNLOAD_UN_200;
		}

		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareContent);
		shareIntent.putExtra("sms_body", shareContent);
		// 附件
		File fileIn = new File(path);
		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileIn));
		ShareOpenChooser.getInstance(mActivity).openChooser(shareIntent);
		shareIntent = null;
	}

	/**
	 * 异步任务，加载主题数据
	 */
	private class AsyncShareImage extends AsyncTask<Void, Void, Void> {
		private Bitmap mImg;

		public AsyncShareImage(Bitmap img) {
			this.mImg = img;
		}

		@Override
		protected Void doInBackground(Void... params) {
			String path = LauncherEnv.Path.SHARE_IMAGE_PATH + "shareImg.jpg";
			BitmapUtility.saveBitmap(mImg, path, Bitmap.CompressFormat.JPEG);

			// 隐藏进度条
			Message msg = new Message();
			msg.what = MSG_OPEN_CHOOSER;
			mHandler.sendMessage(msg);
			return null;
		}
	}

	/***
	 * 显示Loading...
	 */
	public void showLoadView() {
		if (mLoadView != null) {
			mLoadView.setVisibility(View.VISIBLE);
		}
	}

	/***
	 * 隐藏Loading...
	 */
	public void hideLoadView() {
		if (mLoadView != null) {
			mLoadView.setVisibility(View.GONE);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return true;
	}

	public boolean isIsSharing() {
		return mIsSharing;
	}

	public void setIsSharing(boolean isSharing) {
		mIsSharing = isSharing;
	}

	/***
	 * 清理资源
	 */
	public void clear() {
		if (null != mShareContainer) {
			mShareContainer.clear();
			mShareContainer = null;
		}
		if (null != mShareButton) {
			mShareButton = null;
		}
		if (mLoadView != null) {
			mLoadView = null;
		}
	}
}

package com.jiubang.ggheart.apps.desks.snapshot;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.share.ShareOpenChooser;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 截屏界面
 * @author dengdazhong
 *
 */
public class SnapShotFrame extends AbstractFrame implements AnimationListener {
	public static final String DOWNLOAD_200 = "http://goo.gl/R6Vml"; // 200渠道的下載地址
	public static final String DOWNLOAD_UN_200 = "http://t.cn/zW0DwGX"; // 非200渠道的下載地址
	private static final int CREATE_SHARE_IMAGE_SUCCESS = 1;
	private static final int CREATE_SHARE_IMAGE_FAILD = 2;
	private static final int CREATE_SHARE_IMAGE_FAILD_OOM = 3; // 内存不足，创建失败
	private static final int ANIMATION_DURATION = 300; // 动画持续时间
	private Activity mActivity;
	private SnapShotLayout mSnapShotLayout;
	private ImageView mImage;
	private int mAnimeType; //动画类型
	private static final int TYPE_ANIMATION_IN = 0; // 进入动画
	private static final int TYPE_ANIMATION_OUT = 1; // 退出动画
	private Dialog mProgressDialog;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CREATE_SHARE_IMAGE_SUCCESS : // 成功生成分享图片
				{
					String path = (String) msg.obj;
					Intent shareIntent = new Intent(Intent.ACTION_SEND);
					shareIntent.setType("image/jpeg");
					shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mActivity
							.getResources().getString(R.string.share_title));

					String shareContent = mActivity.getResources().getString(
							R.string.share_content_text);
					if (GoStorePhoneStateUtil.is200ChannelUid(mActivity)) {
						shareContent += DOWNLOAD_200;
					} else {
						shareContent += DOWNLOAD_UN_200;
					}

					shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareContent);
					shareIntent.putExtra("sms_body", shareContent);
					// 附件
					File fileIn = new File(path);
					shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileIn));
					ShareOpenChooser.getInstance(GOLauncherApp.getContext()).openChooser(shareIntent);
				}
					break;
				case CREATE_SHARE_IMAGE_FAILD_OOM : {
					Toast.makeText(mActivity,
							mActivity.getString(R.string.snapshot_create_image_error_oom),
							Toast.LENGTH_SHORT).show();
				}
					break;
				default : {
					Toast.makeText(mActivity,
							mActivity.getString(R.string.snapshot_create_image_error),
							Toast.LENGTH_SHORT).show();
				}

			}
			if (mSnapShotLayout != null) {
				View view = mSnapShotLayout.findViewById(R.id.snapshot_operation_share);
				if (view != null) {
					view.setEnabled(true);
				}
			}
			cancelProgressDialog();
			super.handleMessage(msg);
		}
	};
	
	private View.OnClickListener mOperationListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {
			final int viewId = view.getId();
			switch (viewId) {
				case R.id.snapshot_operation_recapture : {
					exitAnimation();
				}
					break;
				case R.id.snapshot_operation_save : {
					if (SnapShotManager.getInstance(GOLauncherApp.getContext()).saveCapture()) {
						mSnapShotLayout.disableSaveButton();
						mSnapShotLayout.enableOpenButton();
						Toast.makeText(
								mActivity,
								mActivity.getString(R.string.snapshot_imagebeensave)
										+ SnapShotManager.SNAPSHOT_PATH, Toast.LENGTH_LONG).show();
					}
				}
					break;
				case R.id.snapshot_operation_share : {
					SnapShotManager.getInstance(GOLauncherApp.getContext()).saveCapture();
					view.setEnabled(false);
					showProgressDialog();
					new Worker(mActivity, SnapShotManager.getInstance(GOLauncherApp.getContext()).getSnapShotPath()).start();
					//					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					//							IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.SHARE_FRAME, null, null);
				}
					break;
				case R.id.snapshot_operation_open : {
					SnapShotManager.getInstance(GOLauncherApp.getContext()).saveCapture();
					File file = new File(SnapShotManager.getInstance(GOLauncherApp.getContext()).getSnapShotPath());
					if (file.exists()) {
						Intent intent = new Intent("android.intent.action.VIEW");
						intent.addCategory("android.intent.category.DEFAULT");
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Uri uri = Uri.fromFile(file);
						intent.setDataAndType(uri, "image/*");
						String title = mActivity.getResources().getText(R.string.snapshot_open_way)
								.toString();
						ShareOpenChooser.getInstance(GOLauncherApp.getContext()).openChooser(intent, title);
					}
				}
					break;
				default : // 取消截图界面
				{
					exitAnimation();
				}
			}

		}
	};

	public SnapShotFrame(Activity activity, IFrameManager frameManager, int arg1) {
		super(activity, frameManager, arg1);
		mActivity = activity;
		mSnapShotLayout = new SnapShotLayout(mActivity);
		mSnapShotLayout.initOperationButtons(mOperationListener); // 初始化各个操作按钮到监听器
		mImage = (ImageView) mSnapShotLayout.findViewById(R.id.snapshot);
		mImage.setImageBitmap(SnapShotManager.getInstance(GOLauncherApp.getContext()).getSnapShotBitmap());
		enterAnimation();
	}

	//	@Override
	//	public boolean dispatchTouchEvent(MotionEvent arg0) {
	//		if (null != mSnapShotLayout) {
	//			mSnapShotLayout.dispatchTouchEvent(arg0);
	//		}
	//		return false;
	//	}

	@Override
	public View getContentView() {
		return mSnapShotLayout;
	}

	/***
	 * 进入
	 */
	private void enterAnimation() {
		if (null == mSnapShotLayout) {
			return;
		}
		Animation animationIn = AnimationUtils.loadAnimation(mActivity, R.anim.zoom_enter);
		animationIn.setDuration(ANIMATION_DURATION);
		animationIn.setAnimationListener(this);
		mAnimeType = TYPE_ANIMATION_IN;
		mSnapShotLayout.startAnimation(animationIn);
	}

	/***
	 * 退出
	 */
	private void exitAnimation() {
		if (null == mSnapShotLayout) {
			return;
		}
		Animation animationout = AnimationUtils.loadAnimation(mActivity, R.anim.zoom_exit);
		animationout.setDuration(ANIMATION_DURATION);
		animationout.setAnimationListener(this);
		mAnimeType = TYPE_ANIMATION_OUT;
		mSnapShotLayout.startAnimation(animationout);
//		// 取消拖拽
//		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.DRAG_CANCEL, -1, null,
//				null);
//
//		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
//				IDiyMsgIds.SCREEN_CLEAR_OUTLINE_BITMAP, 1, null, null);
//		// //显示指示器
//		// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
//		// IDiyMsgIds.SHOW_INDICATOR, -1, null, null);
//
//		GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.DRAG_FINISH_BROADCAST, -1, null, null);
	}

	public void showProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new Dialog(mActivity, R.style.Dialog);
			mProgressDialog.setContentView(R.layout.snapshot_watting_for_require_root_dialog);
			TextView textView = (TextView) mProgressDialog.findViewById(R.id.message);
			textView.setText(R.string.snapshot_creating_share_image);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
		}
	}

	public void cancelProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}
	
	@Override
	public void onAnimationEnd(Animation animation) {
		switch (mAnimeType) {
			case TYPE_ANIMATION_OUT : {
				mActivity.finish();
			}
				break;
			case TYPE_ANIMATION_IN :
			default :
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onRemove() {
		DeskSettingConstants.selfDestruct(mSnapShotLayout);
		super.onRemove();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exitAnimation();
		}
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * 合图线程
	 * @author dengdazhong
	 *
	 */
	class Worker extends Thread {
		private String mPath;
		private Context mContext;
		private SpellImageWorker mSpeller;
		public Worker(Context context, String path) {
			mPath = path;
			mContext = context;
			mSpeller = new SpellImageWorker(mContext);
		}

		@Override
		public void run() {
			Message msg = new Message();
			if (mPath != null) {
				try {
					Bitmap bitmap = mSpeller.spellImageOne(mPath);
					if (bitmap != null) {
						String path = LauncherEnv.Path.SHARE_IMAGE_PATH + "shareImg.jpg";
						BitmapUtility.saveBitmap(bitmap, path, Bitmap.CompressFormat.JPEG);
						msg.what = CREATE_SHARE_IMAGE_SUCCESS;
						msg.obj = path;
						mHandler.sendMessage(msg);
						return;
					}
				} catch (OutOfMemoryError e) {
					msg.what = CREATE_SHARE_IMAGE_FAILD_OOM;
					mHandler.sendMessage(msg);
					return;
				}
			}
			msg.what = CREATE_SHARE_IMAGE_FAILD;
			mHandler.sendMessage(msg);
		}

	}
}

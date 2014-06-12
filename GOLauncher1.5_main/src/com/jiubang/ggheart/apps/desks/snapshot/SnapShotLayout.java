package com.jiubang.ggheart.apps.desks.snapshot;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
/**
 * 
 * @author dengdazhong
 *
 */
public class SnapShotLayout extends RelativeLayout {
	public SnapShotLayout(Context context) {
		super(context);
		inflate(context, R.layout.snapshot_layout, this);
	}

	public SnapShotLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.snapshot_layout, this);
	}

	public SnapShotLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		inflate(context, R.layout.snapshot_layout, this);
		
	}

	public void initOperationButtons(View.OnClickListener l) {
		Button button = (Button) findViewById(R.id.snapshot_operation_recapture);
		button.setOnClickListener(l);
		button = (Button) findViewById(R.id.snapshot_operation_save);
		button.setOnClickListener(l);
		button = (Button) findViewById(R.id.snapshot_operation_share);
		button.setOnClickListener(l);
		button = (Button) findViewById(R.id.snapshot_operation_open);
		button.setOnClickListener(l);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void disableSaveButton() {
		((Button) findViewById(R.id.snapshot_operation_save)).setEnabled(false);
	}

	public void enableOpenButton() {
		((Button) findViewById(R.id.snapshot_operation_open)).setEnabled(true);
	}
	
//	/**
//	 * 打开activity选择器，用于选择打开图片和音乐文件的程序
//	 * 
//	 * @param contentType
//	 * @param info
//	 * @param mimeType
//	 * @param type
//	 *            FileEngine里的TYPE类型（TYPE_IMAGE, TYPE_VIDEO, TYPE_AUDIO)
//	 * @param objs
//	 *            额外参数
//	 */
//	public void openChooser(Intent intent) {
//		mIsCheck = false;
//		initViews();
//		mIntent = intent;
//		initPKDatasByIntent(mIntent);
//		if (mApps == null || mApps.size() <= 0) {
//			DeskToast.makeText(mContext, R.string.no_way_to_open_file, Toast.LENGTH_SHORT).show();
//		} else if (mApps.size() == 1) {
//			// 如果只有一个可以打开媒体的程序，直接打开
//			open(mApps.get(0));
//		} else {
//			showDialog();
//		}
//	}
//
//	public void openChooser(Intent intent, String title) {
//		openChooser(intent);
//		mTitle.setText(title);
//	}
//	
//	private void initViews() {
//		LayoutInflater factory = LayoutInflater.from(mContext);
//		mRootView = factory.inflate(R.layout.appfunc_mediamanagement_activitychooser, null);
//		mTitle = (TextView) mRootView
//				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_title);
//		mCheckImg = (ImageView) mRootView
//				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_img);
//		TextView defaultText = (TextView) mRootView.findViewById(R.id.set_as_default);
//		View.OnClickListener listener = new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (mIsCheck) {
//					mCheckImg.setImageResource(R.drawable.apps_uninstall_not_selected);
//					mIsCheck = false;
//				} else {
//					mCheckImg.setImageResource(R.drawable.apps_uninstall_selected);
//					mIsCheck = true;
//				}
//			}
//		};
//		defaultText.setOnClickListener(listener);
//		mCheckImg.setOnClickListener(listener);
//		defaultText.setVisibility(View.GONE);
//		mCheckImg.setVisibility(View.GONE);
//		mList = (ListView) mRootView
//				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_list);
//		mList.setAdapter(mAdapter);
//		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
//				AppInfo info = (AppInfo) view.getTag();
//				StatisticsData.countMenuData(mContext, StatisticsData.SHARE_KEY + info.pkName);
//				open(info);
//				mDialog.dismiss();
//			}
//		});
//	}
}

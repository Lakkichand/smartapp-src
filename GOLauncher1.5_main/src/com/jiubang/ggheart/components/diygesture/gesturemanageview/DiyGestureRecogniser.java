package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureInfo;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureModelImpl;

/**
 * 手势输入界面
 * 
 * @author licanhui
 */
public class DiyGestureRecogniser extends Activity
		implements
			OnGesturePerformedListener,
			OnClickListener {
	private DiyGestureModelImpl mDiyGestureModelImpl;
	private ArrayList<DiyGestureInfo> mRecogizeGestureInfoList;
	private LinearLayout mUiLayout; // 总布局
	private GestureOverlayView mGestureOverlayView; // 画板
	private ImageView mAddBtn; // 添加按钮
	private ImageView mManageBtn; // 管理按钮
	private Button mCancleOneBtn; // 取消按钮1
	private Button mAddResponseBtn; // 添加响应
	private Button mReDrawBtn; // 重画按钮
	private Button mBackRedrawBtn; // 返回重画按钮
	private Button mSelectResponseBackBtn; // 添加响应布局返回按钮

	private TextView mDrawOneGestureText; // 画一个手势按钮
	private LinearLayout mCanleLayout; // 取消按钮布局
	private LinearLayout mRedrawLayout; // 取消、重画按钮布局
	private LinearLayout mWarmingLayout; // 没有匹配警告布局
	private LinearLayout mRecongniserLayout; // 输入的布局
	private LinearLayout mReslutLayout; // 多个相似的列表布局
	private LinearLayout mSelectResponseLayout; // 添加响应布局

	private DiyGestureItemView mDrawResultImageView; // 画完的手势图片
	private Gesture mGesture;
	private ListView mResultListView;
	private DiyGestureResultAdapter mResultAdapter;
	private ArrayList<DiyGestureInfo> mResultAdapterData;
	private static final int RESULT_MAX_SIZE = 3; // 匹配结果显示最大个数
	private float mStrokeWidth; // 画笔的大小

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_recogniser);
		if (!checksdCardExist()) {
			return;
		}
		initResources();
		DiyGestureConstants.checkLandChange(this, mUiLayout);
		initResultListView();
		mDiyGestureModelImpl = DiyGestureModelImpl.getInstance(this);
		DiyGestureModelImpl.addFlag(DiyGestureModelImpl.sFLAG_RECONIZE); // 记录已经打开此Activity
		checkGestureSize();
		
	}

	/**
	 * 初始化控件
	 */
	public void initResources() {
		mUiLayout = (LinearLayout) findViewById(R.id.uiLayout);

		mAddBtn = (ImageView) findViewById(R.id.addBtn);
		mAddBtn.setOnClickListener(this);

		mManageBtn = (ImageView) findViewById(R.id.manageBtn);
		mManageBtn.setOnClickListener(this);

		mCancleOneBtn = (Button) findViewById(R.id.cancleOneBtn);
		mCancleOneBtn.setOnClickListener(this);

		mAddResponseBtn = (Button) findViewById(R.id.addResponseBtn);
		mAddResponseBtn.setOnClickListener(this);

		mReDrawBtn = (Button) findViewById(R.id.reDrawBtn);
		mReDrawBtn.setOnClickListener(this);

		mBackRedrawBtn = (Button) findViewById(R.id.backRedrawBtn);
		mBackRedrawBtn.setOnClickListener(this);

		mSelectResponseBackBtn = (Button) findViewById(R.id.selectResponseBackBtn);
		mSelectResponseBackBtn.setOnClickListener(this);

		mDrawOneGestureText = (TextView) findViewById(R.id.drawOneGestureText);

		mDrawResultImageView = (DiyGestureItemView) findViewById(R.id.drawResultImageView);

		mWarmingLayout = (LinearLayout) findViewById(R.id.warmingLayout);
		mCanleLayout = (LinearLayout) findViewById(R.id.cancleLayout);
		mRedrawLayout = (LinearLayout) findViewById(R.id.reDrawLayout);

		mRecongniserLayout = (LinearLayout) findViewById(R.id.recongniserLayout);
		mReslutLayout = (LinearLayout) findViewById(R.id.reslutLayout);

		mSelectResponseLayout = (LinearLayout) findViewById(R.id.selectResponseLayout);

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.overlayView);
		mGestureOverlayView.setGestureStrokeSquarenessTreshold(0f);
		mStrokeWidth = getResources().getDimension(R.dimen.gesture_stroke_width);
		mGestureOverlayView.setGestureStrokeWidth(mStrokeWidth);

		DiyGestureOnGestureListener gestureListener = new DiyGestureOnGestureListener(mStrokeWidth);
		mGestureOverlayView.addOnGestureListener(gestureListener);
		mGestureOverlayView.addOnGesturePerformedListener(this);
	}

	/**
	 * 初始化匹配列表
	 */
	public void initResultListView() {
		mResultAdapterData = new ArrayList<DiyGestureInfo>();
		mResultAdapter = new DiyGestureResultAdapter(this, mResultAdapterData);
		mResultListView = (ListView) findViewById(R.id.resultListView);
		mResultListView.setAdapter(mResultAdapter);
		mResultListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// rect用于弹出联系人快捷方式
				int screenheight = GoLauncher.getScreenHeight();
				int screenwidth = GoLauncher.getScreenWidth();
				Rect rect = new Rect(screenwidth / 2, screenheight, screenwidth / 2, screenheight);
				mResultAdapterData.get(position).execute(rect); // 执行Intent
				setResult(RESULT_OK);
				finish();
			}
		});
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		mGesture = gesture;

		DiyGestureConstants.setFirstPointCircle(gesture, mStrokeWidth); // 设置预览图第一笔加粗

		mDrawResultImageView.setGestureImageView(gesture); // 设置手势预览图片
		mDrawResultImageView.setVisibility(View.VISIBLE);

		mGestureOverlayView.setEnabled(false); // 设置不能重复画手势

		mRecogizeGestureInfoList = mDiyGestureModelImpl.recogizeGesture(gesture);
		int size = mRecogizeGestureInfoList.size();
		if (size == 0) {
			mWarmingLayout.setVisibility(View.VISIBLE);
			mDrawOneGestureText.setVisibility(View.GONE);
			mRedrawLayout.setVisibility(View.VISIBLE);
			mCanleLayout.setVisibility(View.GONE);
		} else if (size == 1) {
			DiyGestureInfo diyGestureInfo = mRecogizeGestureInfoList.get(0);
			// rect用于弹出联系人快捷方式
			int screenheight = GoLauncher.getScreenHeight();
			int screenwidth = GoLauncher.getScreenWidth();
			Rect rect = new Rect(screenwidth / 2, screenheight, screenwidth / 2, screenheight);
			diyGestureInfo.execute(rect);
			setResult(RESULT_OK);
			finish();
		} else if (size > 1) {
			showResultListView();
		}
	}

	/**
	 * 显示冲突列表
	 */
	public void showResultListView() {
		if (mResultAdapterData == null) {
			mResultAdapterData = new ArrayList<DiyGestureInfo>();
		} else {
			mResultAdapterData.clear();
		}

		mRecongniserLayout.setVisibility(View.GONE);
		mReslutLayout.setVisibility(View.VISIBLE);

		int recogizeGestureInfoListSize = mRecogizeGestureInfoList.size();
		int size = recogizeGestureInfoListSize > RESULT_MAX_SIZE
				? RESULT_MAX_SIZE
				: recogizeGestureInfoListSize;
		for (int i = 0; i < size; i++) {
			DiyGestureInfo diyGestureInfo = mRecogizeGestureInfoList.get(i);
			mResultAdapterData.add(diyGestureInfo);
		}
		mResultAdapter.notifyDataSetChanged();
	}

	/**
	 * 重置手势画板
	 */
	public void resetOverlayView() {
		if (mGestureOverlayView != null) {
			mGesture = null;
			mDrawResultImageView.setVisibility(View.GONE);
			mGestureOverlayView.setEnabled(true);
			mGestureOverlayView.clear(false);
			mGestureOverlayView.removeAllViews();
			mWarmingLayout.setVisibility(View.GONE);
			mDrawOneGestureText.setVisibility(View.VISIBLE);
			mCanleLayout.setVisibility(View.VISIBLE);
			mRedrawLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 添加手势按钮
			case R.id.addBtn :
				Intent intent = new Intent(this, DiyGestureAddActivity.class);
				if (mGesture != null) {
					intent.putExtra(DiyGestureConstants.IS_ADD_GESTURE, mGesture);
					startActivity(intent);
				} else {
					startActivity(intent);
				}
				finish();
				break;

			// 管理列表按钮
			case R.id.manageBtn :
				startActivity(new Intent(this, MyGesture.class));
				finish();
				break;

			case R.id.cancleOneBtn :
				finish();
				break;

			case R.id.selectResponseBackBtn :
				mRecongniserLayout.setVisibility(View.VISIBLE);
				mSelectResponseLayout.setVisibility(View.GONE);
				break;

			// 重画
			case R.id.reDrawBtn :
				resetOverlayView();
				break;

			// 添 加响应
			case R.id.addResponseBtn :
				mRecongniserLayout.setVisibility(View.GONE);
				mSelectResponseLayout.setVisibility(View.VISIBLE);
				break;

			// 返回重画按钮
			case R.id.backRedrawBtn :
				mRecongniserLayout.setVisibility(View.VISIBLE);
				mReslutLayout.setVisibility(View.GONE);
				resetOverlayView();
				break;

			default :
				break;
		}
	}

	/**
	 * 检查手势个数，0就跳到添加界面
	 */
	public void checkGestureSize() {
		if (mDiyGestureModelImpl.getAllGestureInfosSize() == 0) {
			Intent intent = new Intent(this, DiyGestureAddActivity.class);
			intent.putExtra(DiyGestureConstants.CHECK_GESTURE_SIZE, true);
			startActivity(intent);
			finish();
		} else {
			//如果发现有数据,则永远不弹出关闭手势向导框
			PreferencesManager sharedPreferences = new PreferencesManager(this,
					IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
			sharedPreferences.putInt(IPreferencesIds.CANCLE_DIYGESTURE_TIME, 100);
			sharedPreferences.commit();
		}
	}

	/**
	 * 检查是否有SD卡
	 */
	public boolean checksdCardExist() {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (!sdCardExist) {
			finish();
			Toast.makeText(this, getResources().getString(R.string.no_sdcard), Toast.LENGTH_SHORT)
					.show();
		}
		return sdCardExist;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		DiyGestureConstants.onActivityResult(this, mDiyGestureModelImpl, mGesture, requestCode,
				resultCode, data);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		DiyGestureConstants.checkLandChange(this, mUiLayout);
	}

	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		if (mDiyGestureModelImpl != null) {
			DiyGestureModelImpl.removeFlag(DiyGestureModelImpl.sFLAG_RECONIZE); // 记录已经销毁此Activity
			DiyGestureModelImpl.checkClear(); // 注销数据单例
			mDiyGestureModelImpl = null;
		}
		if (mReDrawBtn != null && mReDrawBtn instanceof DeskButton) {
			((DeskButton) mReDrawBtn).selfDestruct();
			mReDrawBtn = null;
		}
		if (mCancleOneBtn != null && mCancleOneBtn instanceof DeskButton) {
			((DeskButton) mCancleOneBtn).selfDestruct();
			mCancleOneBtn = null;
		}
		if (mAddResponseBtn != null && mAddResponseBtn instanceof DeskButton) {
			((DeskButton) mAddResponseBtn).selfDestruct();
			mAddResponseBtn = null;
		}
		if (mBackRedrawBtn != null && mBackRedrawBtn instanceof DeskButton) {
			((DeskButton) mBackRedrawBtn).selfDestruct();
			mBackRedrawBtn = null;
		}
		if (mSelectResponseBackBtn != null && mSelectResponseBackBtn instanceof DeskButton) {
			((DeskButton) mSelectResponseBackBtn).selfDestruct();
			mSelectResponseBackBtn = null;
		}
		if (mDrawOneGestureText != null && mDrawOneGestureText instanceof DeskTextView) {
			((DeskTextView) mDrawOneGestureText).selfDestruct();
			mDrawOneGestureText = null;
		}
	}
	
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}

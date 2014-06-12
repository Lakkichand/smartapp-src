package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureInfo;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureModelImpl;

/**
 * 手势添加界面
 * 
 * @author licanhui
 */
public class DiyGestureAddActivity extends Activity
		implements
			OnGesturePerformedListener,
			OnClickListener,
			DiyGestureConflictAnimationListner {
	private DiyGestureModelImpl mDiyGestureModelImpl;
	private ArrayList<DiyGestureInfo> mRecogizeGestureInfoList; // 数据源
	private Gesture mGesture;
	private float mStrokeWidth; // 画笔的大小

	private LinearLayout mUiLayout; // 总布局
	private LinearLayout mAddLayout; // 添加输入布局
	private GestureOverlayView mGestureOverlayView; // 画板
	private DiyGestureItemView mDrawResultImageView; // 画完的手势图片

	private LinearLayout mFirstOpenLayout; // 第一次打开布局

	private LinearLayout mCancleBtnLayout; // 取消按钮布局
	private Button mCancelAddBtn; // 取消按钮

	private LinearLayout mReDrawBtnLayout; // 重画，下一步按钮布局
	private Button mReDrawBtn; // 重画按钮
	private Button mNextBtn; // 下一步按钮

	private LinearLayout mSelectResponseLayout; // 选择按钮布局
	private Button mUpBtn; // 重画按钮
	private Button mCancelSelectResponseBtn; // 取消按钮

	private LinearLayout mConflictLayout; // 冲突布局
	private DiyGestureConflictView mConflictDrawResultImageView; // 画完的手势图片
	private DiyGestureItemView mConflictImageView; // 冲突的图片
	private LinearLayout mConflictTipsViewLayout; // 冲突相似图片的布局
	private TextView mConflictTypeNameTextView; // 冲突类型名称
	private TextView mConflictNameTextView; // 冲突的名称
	private Button mConflictReDrawBtn; // 冲突布局重画按钮
	private Button mCancelConflictBtn; // 冲突布局取消按钮

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_add);
		initResource();
		DiyGestureConstants.checkLandChange(this, mUiLayout);
		checkIsFirstOpen();
		checkIsAddGesture();
		mDiyGestureModelImpl = DiyGestureModelImpl.getInstance(this);
		DiyGestureModelImpl.addFlag(DiyGestureModelImpl.sFLAG_ADD);
	}

	/**
	 * 初始化资源
	 */
	private void initResource() {
		mUiLayout = (LinearLayout) findViewById(R.id.uiLayout);
		mAddLayout = (LinearLayout) findViewById(R.id.addLayout);

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.overlayView);
		mGestureOverlayView.setGestureStrokeSquarenessTreshold(0f);
		mStrokeWidth = getResources().getDimension(R.dimen.gesture_stroke_width);
		mGestureOverlayView.setGestureStrokeWidth(mStrokeWidth);
		AddOnGestureListener gestureListener = new AddOnGestureListener(mStrokeWidth);
		mGestureOverlayView.addOnGestureListener(gestureListener);
		mGestureOverlayView.addOnGesturePerformedListener(this);

		mDrawResultImageView = (DiyGestureItemView) findViewById(R.id.drawResultImageView);

		mFirstOpenLayout = (LinearLayout) findViewById(R.id.firstOpenLayout);

		mCancleBtnLayout = (LinearLayout) findViewById(R.id.cancleBtnLayout);
		mCancelAddBtn = (Button) findViewById(R.id.cancelAddBtn);
		mCancelAddBtn.setOnClickListener(this);

		mReDrawBtnLayout = (LinearLayout) findViewById(R.id.reDrawBtnLayout);
		mReDrawBtn = (Button) findViewById(R.id.reDrawBtn);
		mReDrawBtn.setOnClickListener(this);
		mNextBtn = (Button) findViewById(R.id.nextBtn);
		mNextBtn.setOnClickListener(this);

		mSelectResponseLayout = (LinearLayout) findViewById(R.id.selectResponseLayout);
		mUpBtn = (Button) findViewById(R.id.upBtn);
		mUpBtn.setOnClickListener(this);
		mCancelSelectResponseBtn = (Button) findViewById(R.id.cancelSelectResponseBtn);
		mCancelSelectResponseBtn.setOnClickListener(this);

		mConflictLayout = (LinearLayout) findViewById(R.id.conflictLayout);
		mConflictDrawResultImageView = (DiyGestureConflictView) findViewById(R.id.conflictDrawResultImageView);
		mConflictImageView = (DiyGestureItemView) findViewById(R.id.my_gesture_item_icon);
		mConflictTipsViewLayout = (LinearLayout) findViewById(R.id.conflictTipsViewLayout);
		mConflictTypeNameTextView = (TextView) findViewById(R.id.my_gesture_item_type_name);
		mConflictNameTextView = (TextView) findViewById(R.id.my_gesture_item_name);
		mConflictReDrawBtn = (Button) findViewById(R.id.conflictReDrawBtn);
		mConflictReDrawBtn.setOnClickListener(this);
		mCancelConflictBtn = (Button) findViewById(R.id.cancelConflictBtn);
		mCancelConflictBtn.setOnClickListener(this);
	}

	/**
	 * 检查是否第一次打开手势功能
	 */
	public void checkIsFirstOpen() {
		boolean isFirstOpen = getIntent().getBooleanExtra(DiyGestureConstants.CHECK_GESTURE_SIZE,
				false);
		if (isFirstOpen) {
			mFirstOpenLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 检查是否从输入界面画了手势后添加新手势
	 */
	public void checkIsAddGesture() {
		Gesture gesture = getIntent().getParcelableExtra(DiyGestureConstants.IS_ADD_GESTURE);
		if (gesture != null) {
			setNoConflictView(gesture);
		}
	}
	/**
	 * 
	 * @author 
	 *
	 */
	public class AddOnGestureListener extends DiyGestureOnGestureListener {

		public AddOnGestureListener(float strokeWidth) {
			super(strokeWidth);
		}

		@Override
		public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
			// 隐藏第一次打开手势的提示
			if (mFirstOpenLayout.getVisibility() == View.VISIBLE) {
				mFirstOpenLayout.setVisibility(View.GONE);
			}
			mIsDrawPoint = true; // 父类画点的标志位
		}
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		mRecogizeGestureInfoList = mDiyGestureModelImpl.recogizeGesture(gesture);

		DiyGestureConstants.setFirstPointCircle(gesture, mStrokeWidth); // 设置预览图第一笔加粗
		// 检查是否有冲突
		if (mRecogizeGestureInfoList.size() >= 1) {
			mConflictDrawResultImageView.startConflictAnimation(mStrokeWidth, gesture); // 显示冲突界面动画
			mConflictDrawResultImageView.setConflictAnimationListner(this); // 设置动画结束监听器

			DiyGestureInfo gestureInfo = mRecogizeGestureInfoList.get(0);
			Gesture conflictGesture = gestureInfo.getmGesture();
			mConflictImageView.setGestureImageView(conflictGesture); // 设置冲突手势预览图片
			mConflictTypeNameTextView.setText(gestureInfo.getTypeName());
			mConflictNameTextView.setText(gestureInfo.getName());

			// 检查到有冲突
			mConflictLayout.setVisibility(View.VISIBLE);
			mAddLayout.setVisibility(View.GONE);
			mGesture = null;
		} else {
			setNoConflictView(gesture);
		}
	}

	/**
	 * 设置画完手势后没有冲突的View
	 * 
	 * @param gesture
	 */
	public void setNoConflictView(Gesture gesture) {
		if (gesture == null) {
			return;
		}

		mDrawResultImageView.setGestureImageView(gesture); // 设置手势预览图片
		mDrawResultImageView.setVisibility(View.VISIBLE);

		mGesture = gesture;
		mReDrawBtnLayout.setVisibility(View.VISIBLE);
		mCancleBtnLayout.setVisibility(View.GONE);
		mCancelAddBtn.setVisibility(View.GONE);
		mGestureOverlayView.setEnabled(false);
	}

	/**
	 * 重画
	 */
	private void resetOverlayView() {
		if (mGestureOverlayView != null) {
			mGesture = null;
			mDrawResultImageView.setVisibility(View.GONE);
			mGestureOverlayView.setEnabled(true);
			mGestureOverlayView.clear(false);
			mGestureOverlayView.removeAllViews();
			mAddLayout.setVisibility(View.VISIBLE);
			mCancelAddBtn.setVisibility(View.VISIBLE);
			mCancleBtnLayout.setVisibility(View.VISIBLE);
			mReDrawBtnLayout.setVisibility(View.GONE);
			mConflictLayout.setVisibility(View.GONE);
			mReDrawBtnLayout.setVisibility(View.GONE);
			mConflictTipsViewLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 取消
			case R.id.cancelAddBtn :
			case R.id.cancelSelectResponseBtn :
			case R.id.cancelConflictBtn :
				finish();
				break;

			// 下一步
			case R.id.nextBtn :
				mSelectResponseLayout.setVisibility(View.VISIBLE);
				mAddLayout.setVisibility(View.GONE);
				break;

			// 上一步
			case R.id.upBtn :
				mSelectResponseLayout.setVisibility(View.GONE);
				mAddLayout.setVisibility(View.VISIBLE);
				break;

			// 重画
			case R.id.reDrawBtn :
			case R.id.conflictReDrawBtn :
				resetOverlayView();
				break;

			default :
				break;
		}
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
			DiyGestureModelImpl.removeFlag(DiyGestureModelImpl.sFLAG_ADD);
			DiyGestureModelImpl.checkClear();
			mDiyGestureModelImpl = null;
		}
		if (mCancelAddBtn != null && mCancelAddBtn instanceof DeskButton) {
			((DeskButton) mCancelAddBtn).selfDestruct();
			mCancelAddBtn = null;
		}
		if (mReDrawBtn != null && mReDrawBtn instanceof DeskButton) {
			((DeskButton) mReDrawBtn).selfDestruct();
			mReDrawBtn = null;
		}
		if (mNextBtn != null && mNextBtn instanceof DeskButton) {
			((DeskButton) mNextBtn).selfDestruct();
			mNextBtn = null;
		}
		if (mUpBtn != null && mUpBtn instanceof DeskButton) {
			((DeskButton) mUpBtn).selfDestruct();
			mUpBtn = null;
		}
		if (mCancelSelectResponseBtn != null && mCancelSelectResponseBtn instanceof DeskButton) {
			((DeskButton) mCancelSelectResponseBtn).selfDestruct();
			mCancelSelectResponseBtn = null;
		}
		if (mConflictReDrawBtn != null && mConflictReDrawBtn instanceof DeskButton) {
			((DeskButton) mConflictReDrawBtn).selfDestruct();
			mConflictReDrawBtn = null;
		}		
		if (mCancelConflictBtn != null && mCancelConflictBtn instanceof DeskButton) {
			((DeskButton) mCancelConflictBtn).selfDestruct();
			mCancelConflictBtn = null;
		}		
		if (mConflictTypeNameTextView != null && mConflictTypeNameTextView instanceof DeskTextView) {
			((DeskTextView) mConflictTypeNameTextView).selfDestruct();
			mConflictTypeNameTextView = null;
		}		
		if (mConflictNameTextView != null && mConflictNameTextView instanceof DeskTextView) {
			((DeskTextView) mConflictNameTextView).selfDestruct();
			mConflictNameTextView = null;
		}		
	}

	@Override
	public void setConflictViewVisable() {
		// 检查到有冲突
		mConflictTipsViewLayout.setVisibility(View.VISIBLE);
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

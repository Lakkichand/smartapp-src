package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Configuration;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
public class DiyGestureEditActivity extends Activity
		implements
			OnGesturePerformedListener,
			OnClickListener,
			DiyGestureConflictAnimationListner {
	private LinearLayout mUiLayout; // 总布局
	private RelativeLayout mEditLayout; // 编辑输入布局
	private GestureOverlayView mGestureOverlayView; // 画板
	private DiyGestureItemView mDrawResultImageView; // 画完的手势图片

	private RelativeLayout mConflictLayout; // 冲突布局
	private DiyGestureConflictView mConflictDrawResultImageView; // 画完的手势图片
	private LinearLayout mConflictTipsViewLayout; // 冲突相似图片的布局
	private DiyGestureItemView mConflictImageView; // 冲突的图片
	private TextView mConflictTypeNameTextView; // 冲突类型名称
	private TextView mConflictNameTextView; // 冲突的名称

	private LinearLayout mCancleRedrawBtnLayout;
	private Button mCancleRedrawBtn;
	private Button mCancleBtn; // 取消按钮

	private LinearLayout mReDrawBtnLayout;
	private Button mReDrawBtn;
	private Button mFinishBtn;

	private float mStrokeWidth; // 画笔的大小

	private DiyGestureModelImpl mDiyGestureModelImpl;
	private ArrayList<DiyGestureInfo> mRecogizeGestureInfoList; // 数据源
	private Gesture mGesture;
	private DiyGestureInfo mModifyDiyGestureInfo = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesture_edit);

		mDiyGestureModelImpl = DiyGestureModelImpl.getInstance(this);
		DiyGestureModelImpl.addFlag(DiyGestureModelImpl.sFLAG_EDIT);
		initResource();
		DiyGestureConstants.checkLandChange(this, mUiLayout);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String gestrueName = bundle.getString(DiyGestureConstants.CHANGE_GESTURE_NAME);
			if (gestrueName != null) {
				mModifyDiyGestureInfo = mDiyGestureModelImpl
						.getDiyGestureInfoByGestureName(gestrueName);
				// 内存不足，GO桌面被杀掉的时候缓存会被清空
				if (mModifyDiyGestureInfo == null) {
					finish();
				} else {
					Gesture gesture = mModifyDiyGestureInfo.getmGesture();
					DiyGestureConstants.setFirstPointCircle(gesture, mStrokeWidth); // 设置预览图第一笔加粗
					mDrawResultImageView.setGestureImageView(gesture);
					mDrawResultImageView.setVisibility(View.VISIBLE);
					mGestureOverlayView.setEnabled(false);
				}
			}
		}
	}

	/**
	 * 初始化资源
	 */
	private void initResource() {
		mUiLayout = (LinearLayout) findViewById(R.id.uiLayout);

		mEditLayout = (RelativeLayout) findViewById(R.id.editLayout);

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.overlayView);
		mGestureOverlayView.setGestureStrokeSquarenessTreshold(0f);

		mStrokeWidth = getResources().getDimension(R.dimen.gesture_stroke_width);
		mGestureOverlayView.setGestureStrokeWidth(mStrokeWidth);

		DiyGestureOnGestureListener gestureListener = new DiyGestureOnGestureListener(mStrokeWidth);
		mGestureOverlayView.addOnGestureListener(gestureListener);
		mGestureOverlayView.addOnGesturePerformedListener(this);

		mDrawResultImageView = (DiyGestureItemView) findViewById(R.id.drawResultImageView);

		mConflictLayout = (RelativeLayout) findViewById(R.id.conflictLayout);
		mConflictDrawResultImageView = (DiyGestureConflictView) findViewById(R.id.conflictDrawResultImageView);
		mConflictTipsViewLayout = (LinearLayout) findViewById(R.id.conflictTipsViewLayout);
		mConflictImageView = (DiyGestureItemView) findViewById(R.id.my_gesture_item_icon);
		mConflictTypeNameTextView = (TextView) findViewById(R.id.my_gesture_item_type_name);
		mConflictNameTextView = (TextView) findViewById(R.id.my_gesture_item_name);

		mCancleRedrawBtnLayout = (LinearLayout) findViewById(R.id.cancleRedrawBtnLayout);
		mCancleRedrawBtn = (Button) findViewById(R.id.cancleRedrawBtn);
		mCancleRedrawBtn.setOnClickListener(this);

		mCancleBtn = (Button) findViewById(R.id.cancleBtn);
		mCancleBtn.setOnClickListener(this);

		mReDrawBtnLayout = (LinearLayout) findViewById(R.id.reDrawBtnLayout);
		mReDrawBtn = (Button) findViewById(R.id.reDrawBtn);
		mReDrawBtn.setOnClickListener(this);
		mFinishBtn = (Button) findViewById(R.id.finishBtn);
		mFinishBtn.setOnClickListener(this);
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		mRecogizeGestureInfoList = mDiyGestureModelImpl.recogizeGesture(gesture);
		DiyGestureConstants.setFirstPointCircle(gesture, mStrokeWidth); // 设置预览图第一笔加粗

		// 检查是否有冲突
		if (mRecogizeGestureInfoList.size() >= 1) {
			DiyGestureInfo gestureInfo = mRecogizeGestureInfoList.get(0);
			// 判断修改是否等于自身
			if (mModifyDiyGestureInfo == gestureInfo) {
				setNoConflictView(gesture);
				return;
			}
			// 播放冲突动画
			mConflictDrawResultImageView.startConflictAnimation(mStrokeWidth, gesture);
			mConflictDrawResultImageView.setConflictAnimationListner(this);

			Gesture conflictGesture = gestureInfo.getmGesture();
			mConflictImageView.setGestureImageView(conflictGesture); // 设置冲突手势预览图片
			mConflictTypeNameTextView.setText(gestureInfo.getTypeName());
			mConflictNameTextView.setText(gestureInfo.getName());

			// 检查到有冲突
			mConflictLayout.setVisibility(View.VISIBLE);
			mEditLayout.setVisibility(View.GONE);
			mCancleBtn.setVisibility(View.GONE);
			mCancleRedrawBtn.setVisibility(View.VISIBLE);
			mGesture = null;
		} else {
			setNoConflictView(gesture);
		}
	}

	/**
	 * 设置没有冲突的View
	 * 
	 * @param gesture
	 */
	public void setNoConflictView(Gesture gesture) {
		if (gesture == null) {
			return;
		}
		mDrawResultImageView.setIsMoveToCenter(false); // 设置预览图不自动居中
		mDrawResultImageView.setGestureImageView(gesture); // 设置手势预览图片
		mDrawResultImageView.setVisibility(View.VISIBLE);

		mGesture = gesture;
		mCancleRedrawBtnLayout.setVisibility(View.GONE);
		mReDrawBtnLayout.setVisibility(View.VISIBLE);
		mGestureOverlayView.setEnabled(false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 取消
			case R.id.cancleBtn :
			case R.id.cancelConflictBtn :
				finish();
				break;

			// 重画
			case R.id.cancleRedrawBtn :
			case R.id.reDrawBtn :
				resetOverlayView();
				break;

			// 保存修改
			case R.id.finishBtn :
				mModifyDiyGestureInfo.setGesture(mGesture);
				if (mDiyGestureModelImpl.modifyGestureResetGesture(mModifyDiyGestureInfo)) {
					Toast.makeText(this,
							this.getResources().getString(R.string.modify_gesture_success),
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this,
							this.getResources().getString(R.string.modify_gesture_fail),
							Toast.LENGTH_SHORT).show();
				}
				finish();
				break;

			default :
				break;
		}
	}

	/**
	 * 重画
	 */
	private void resetOverlayView() {
		if (mGestureOverlayView != null) {
			mGesture = null;
			mGestureOverlayView.setEnabled(true);
			mGestureOverlayView.clear(false);
			mGestureOverlayView.removeAllViews();

			mDrawResultImageView.setVisibility(View.GONE);
			mEditLayout.setVisibility(View.VISIBLE);
			mCancleRedrawBtnLayout.setVisibility(View.VISIBLE);
			mReDrawBtnLayout.setVisibility(View.GONE);
			mConflictLayout.setVisibility(View.GONE);
			mReDrawBtnLayout.setVisibility(View.GONE);
			mCancleBtn.setVisibility(View.VISIBLE);
			mCancleRedrawBtn.setVisibility(View.GONE);
			mConflictTipsViewLayout.setVisibility(View.GONE);
		}
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
			DiyGestureModelImpl.removeFlag(DiyGestureModelImpl.sFLAG_EDIT);
			DiyGestureModelImpl.checkClear();
			mDiyGestureModelImpl = null;
		}

		if (mReDrawBtn != null && mReDrawBtn instanceof DeskButton) {
			((DeskButton) mReDrawBtn).selfDestruct();
			mReDrawBtn = null;
		}
		if (mFinishBtn != null && mFinishBtn instanceof DeskButton) {
			((DeskButton) mFinishBtn).selfDestruct();
			mFinishBtn = null;
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

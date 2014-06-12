/**
 * 
 */
package com.jiubang.ggheart.components.renamewindow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.GridView;
import android.widget.ImageButton;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.DeskEditText;
import com.jiubang.ggheart.components.DeskTextView;

/**
 * 重命名框组件
 * 
 * @author ruxueqin
 * 
 */
public class RenameActivity extends Activity implements OnTouchListener, OnClickListener {

	private DeskEditText mEditTextView;
	private ImageButton mDelButton;
	private GridView mGridView;
	private DeskButton mOkButton;
	private DeskButton mCancleButton;

	private int mHandlerId; // 消息处理者,例如：iDiyFrameId.ScreenFrame
	private long mItemId; // 重命名图标id
	private String mOldName; // 原来名称
	private boolean mShowRecommendedName = false; // 是否显示建议名称
	private boolean mFinishWhenChangeOrientation = false; // 在切屏时是否退出

	public final static String NAME = "name"; // 名称
	public final static String HANDLERID = "handlerid"; // 消息处理者id
	public final static String ITEMID = "itemid"; // 修改项id
	public final static String SHOW_RECOMMENDEDNAME = "showrecommendedname"; // 是否显示建议名称
	public final static String FINISH_WHEN_CHANGE_ORIENTATION = "finishwhenchangeorientation"; // 在切屏时是否退出

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (intent != null) {
			mHandlerId = intent.getIntExtra(HANDLERID, -1);
			mItemId = intent.getLongExtra(ITEMID, -1);
			mShowRecommendedName = intent.getBooleanExtra(SHOW_RECOMMENDEDNAME, false);
			mFinishWhenChangeOrientation = intent.getBooleanExtra(FINISH_WHEN_CHANGE_ORIENTATION,
					false);

			mOldName = intent.getStringExtra(NAME);
		}
		int layoutid = mShowRecommendedName
				? R.layout.rename_box_recommendedname
				: R.layout.rename_box;
		setContentView(layoutid);
		findViews();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (mFinishWhenChangeOrientation) {
			finish();
		}
	}

	private void findViews() {
		mEditTextView = (DeskEditText) findViewById(R.id.edit);
		mEditTextView.selectAll();

		// delay原因：Activity的onCreate()是弹不出软键盘的，原因是屏幕绘制没完成
		mEditTextView.postDelayed(new Runnable() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mEditTextView, 0);
				if (mOldName != null) {
					mEditTextView.setText(mOldName);
					Editable editable = mEditTextView.getEditableText();
					if (editable != null) {
						Selection.setSelection(editable, 0, editable.length());
					}
				}
			}
		}, 100);

		mDelButton = (ImageButton) findViewById(R.id.del);
		mDelButton.setOnClickListener(this);
		mOkButton = (DeskButton) findViewById(R.id.finish_btn);
		mOkButton.setOnClickListener(this);
		mCancleButton = (DeskButton) findViewById(R.id.cancle_btn);
		mCancleButton.setOnClickListener(this);

		if (mShowRecommendedName) {
			mGridView = (GridView) findViewById(R.id.suggestnamegrid);
			initGridView();
		}
	}

	private void initGridView() {
		String[] names = getResources().getStringArray(R.array.rename_window_names);
		RenameAdapter adapter = new RenameAdapter(this, names, this);
		mGridView.setAdapter(adapter);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		DeskTextView deskTextView = (DeskTextView) v;
		switch (action) {
			case MotionEvent.ACTION_DOWN :
				deskTextView.setTextColor(0xff99cc00);
				break;

			case MotionEvent.ACTION_CANCEL :
				deskTextView.setTextColor(0xff656565);
				break;

			case MotionEvent.ACTION_UP :
				deskTextView.setTextColor(0xff656565);
				// click item处理
				mEditTextView.setText(deskTextView.getText());

				Editable edit = mEditTextView.getText();
				Selection.setSelection(edit, edit.length());
				break;

			default :
				break;
		}

		return true;
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mEditTextView != null) {
			mEditTextView.selfDestruct();
			mEditTextView = null;
		}
		if (mOkButton != null) {
			mOkButton.selfDestruct();
			mOkButton = null;
		}
		if (mCancleButton != null) {
			mCancleButton.selfDestruct();
			mCancleButton = null;
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v == mDelButton) {
			mEditTextView.setText(null);
		} else if (v == mOkButton) {
			boolean change = false;
			try {
				String editText = (mEditTextView.getText() != null) ? mEditTextView.getText()
						.toString() : null;
				//svn：24178	回滚 原因：用户反馈，桌面图标或文件夹要可以命名为空
				/*if (editText != null) {
					change = (mOldName.equals(editText) || editText.trim().equals("")) ? false : true;
				}*/
				change = (mOldName.equals(editText)) ? false : true;
			} catch (Exception e) {
			}

			if (change) {
				Intent intent = new Intent();
				String name = (null != mEditTextView.getText()) ? mEditTextView.getText()
						.toString() : null;
				intent.putExtra(NAME, name);
				intent.putExtra(HANDLERID, mHandlerId);
				intent.putExtra(ITEMID, mItemId);

				setResult(RESULT_OK, intent);
			}
			finish();
		} else if (v == mCancleButton) {
			setResult(RESULT_CANCELED);
			finish();
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

package com.jiubang.ggheart.apps.desks.diy.frames.preview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;

/**
 * 
 * 类描述:屏幕编辑提示
 * 功能详细描述:屏幕编辑的第二页提示使用的自定义view，删除屏幕
 * 
 * @author  
 * @date  [2012-9-6]
 */
public class GuideScreenEditNextPage extends ViewGroup implements OnClickListener {

	private Context mContext;
	private TextView mTitle;
	private TextView mTips;
	private Button mButton;
	private ImageView mDelView;
	private ImageView mArrowView;
	private ImageView mFingerView;

	private int mTitleLeft;
	private int mNextPageTitleTop;
	private int mNextPageTitleTopLand;
	private int mFolderTextToTitle;
	private int mNextPageButtonTipTop;
	private int mNextPageButtonTipTopLand;
	private int mFolderButtonW;
	private int mFolderButtonH;
	private int mNextPageTrashTop;
	private int mNextPageTrashTopLand;
	private int mFingerViewH;
	private int mFingerViewW;
	private int mDelViewW;
	private int mDelViewH;
	private int mTitleTextSize;
	private int mTipsTextSize;

	public GuideScreenEditNextPage(Context context) {
		this(context, null);
	}

	public GuideScreenEditNextPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		initView();
	}

	private void initView() {
		mTitle = new TextView(mContext);
		mTitle.setText(R.string.preview_delete_tutoria);
		mTitle.setTextColor(Color.rgb(175, 248, 112));
		mTitle.setTextSize(20F);
		addView(mTitle);

		mTips = new TextView(mContext);
		mTips.setText(R.string.preview_delete_detail_tips);
		mTips.setTextColor(0xffd4d4d4);
		mTips.setTextSize(14.67F);
		addView(mTips);

		mButton = new Button(mContext);
		mButton.setBackgroundResource(R.drawable.guide_for_screenfolder_okbutton);
		mButton.setText(R.string.tip_btn_txt);
		mButton.setTextColor(Color.WHITE);
		mButton.setTextSize(17.3F);
		mButton.setGravity(Gravity.CENTER);
		mButton.setLayoutParams(new LayoutParams((int) getResources().getDimension(
				R.dimen.screenfolder_button_width), (int) getResources().getDimension(
				R.dimen.screenfolder_button_height)));
		mButton.setOnClickListener(this);
		addView(mButton);

		mDelView = new ImageView(mContext);
		mDelView.setBackgroundResource(R.drawable.guide_screenedit_next_page_trash);
		addView(mDelView);

		mFingerView = new ImageView(mContext);
		mFingerView.setBackgroundResource(R.drawable.guide_screenedit_next_page_finger);
		addView(mFingerView);

		mArrowView = new ImageView(mContext);
		mArrowView.setBackgroundResource(R.drawable.guide_screenedit_next_page_arrow);
		addView(mArrowView);

		mTitleLeft = getResources().getDimensionPixelSize(R.dimen.screenedit_title_marginleft);
		mNextPageTitleTop = getResources().getDimensionPixelSize(
				R.dimen.screenedit_next_page_title_margintop);
		mNextPageTitleTopLand = getResources().getDimensionPixelSize(
				R.dimen.screenedit_next_page_title_margintop_land);
		mFolderTextToTitle = getResources().getDimensionPixelSize(
				R.dimen.screenfolder_text_to_title);
		mNextPageButtonTipTop = getResources().getDimensionPixelSize(
				R.dimen.screenedit_next_page_button_to_tips_margintop);
		mNextPageButtonTipTopLand = getResources().getDimensionPixelSize(
				R.dimen.screenedit_next_page_button_to_tips_margintop_land);

		mFolderButtonW = getResources().getDimensionPixelSize(R.dimen.screenfolder_button_width);
		mFolderButtonH = getResources().getDimensionPixelSize(R.dimen.screenfolder_button_height);
		mNextPageTrashTop = getResources().getDimensionPixelSize(
				R.dimen.screenedit_next_page_trash_image_margintop);
		mNextPageTrashTopLand = getResources().getDimensionPixelSize(
				R.dimen.screenedit_next_page_trash_image_margintop_land);

		mFingerViewH = mFingerView.getBackground().getIntrinsicHeight();
		mFingerViewW = mFingerView.getBackground().getIntrinsicWidth();
		mDelViewW = mDelView.getBackground().getIntrinsicWidth();
		mDelViewH = mDelView.getBackground().getIntrinsicHeight();

		mTitleTextSize = (int) mTitle.getTextSize();
		mTipsTextSize = (int) mTips.getTextSize();

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		int left = 0;
		int top = 0;
		boolean isPortait = GoLauncher.isPortait();
		int statusBarHeight = GoLauncher.getStatusbarHeight();
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View view = getChildAt(i);
			if (view == mTitle) {
				if (isPortait) {
					top = mNextPageTitleTop;
				} else {
					top = mNextPageTitleTopLand;
				}
				view.layout(mTitleLeft, top, r, b);
			} else if (view == mTips) {
				top = top + mTitleTextSize + mFolderTextToTitle; // title距状态栏的高度+title的高度+tips距离title的间隔
				view.layout(left, top, r, b);
			} else if (view == mButton) {
				if (isPortait) {
					top = top + mTipsTextSize + mNextPageButtonTipTop;
				} else {
					top = top + mTipsTextSize + mNextPageButtonTipTopLand;
				}
				view.layout(mTitleLeft, top, mTitleLeft + mFolderButtonW, top + mFolderButtonH);
			} else if (view == mDelView) {
				left = (SensePreviewFrame.sScreenW - mDelViewW) / 2;
				if (isPortait) {
					top = mNextPageTrashTop - statusBarHeight;
				} else {
					top = mNextPageTrashTopLand - statusBarHeight;
				}
				view.layout(left, top, left + mDelViewW, top + mDelViewH);
			} else if (view == mFingerView) {
				left = 2 * SenseWorkspace.sCardWidth + SenseWorkspace.sCardWidth / 2
						+ SenseWorkspace.sSpaceX;
				top = SenseWorkspace.sMarginTop + SenseWorkspace.sCardHeight / 2;
				view.layout(left, top, left + mFingerViewW, top + mFingerViewH);
			} else if (view == mArrowView) {
				if (isPortait) {
					mArrowView.setBackgroundResource(R.drawable.guide_screenedit_next_page_arrow);
				} else {
					mArrowView
							.setBackgroundResource(R.drawable.guide_screenedit_next_page_arrow_land);
				}
				int right = left;
				int bottm = top;
				top = top - mArrowView.getBackground().getIntrinsicHeight();
				left = left - mArrowView.getBackground().getIntrinsicWidth();
				view.layout(left, top, right, bottm);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mButton) {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					IDiyFrameIds.GUIDE_GL_FRAME, null, null);
		}
	}

}

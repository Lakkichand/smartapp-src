package com.jiubang.ggheart.apps.desks.diy;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;

public class GoGuideView extends Gallery
		implements
			android.widget.AdapterView.OnItemSelectedListener {
	private boolean mPapreMove;
	private Activity mActivity;
	private boolean mNeedNextPage;

	public GoGuideView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.galleryStyle);
		// setBackgroundResource(R.drawable.guide_bg);

		// TODO Auto-generated constructor stub
	}

	public GoGuideView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mActivity = (Activity) context;
		setOnItemSelectedListener(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// switch (event.getAction())
		// {
		// case MotionEvent.ACTION_MOVE:
		// {
		// Adapter adapter = getAdapter();
		// GuideAdapter guideAdapter = null;
		// if(adapter instanceof GuideAdapter)
		// {
		// guideAdapter = (GuideAdapter)adapter;
		// }
		// if(guideAdapter != null && guideAdapter.getmCurrentPos() ==
		// getCount()){
		// if(null != mActivity){
		// }
		// }
		// break;
		// }
		// case MotionEvent.ACTION_UP:
		//
		// break;
		// default :
		// break;
		// }
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		super.onFling(e1, e2, velocityX, velocityY);
		int kEvent;
		if (isScrollingLeft(e1, e2)) {
			// Check if scrolling left
			kEvent = KeyEvent.KEYCODE_DPAD_LEFT;
		} else {
			// Otherwise scrolling right
			kEvent = KeyEvent.KEYCODE_DPAD_RIGHT;
		}
		onKeyDown(kEvent, null);
		// if(!isScrollingLeft(e1, e2))
		// {
		// Adapter adapter = getAdapter();
		// GuideAdapter guideAdapter = null;
		// if(adapter instanceof GuideAdapter)
		// {
		// guideAdapter = (GuideAdapter)adapter;
		// }
		// if(guideAdapter != null && getSelectedItemPosition() ==
		// getCount()-1){
		// if(null != mActivity){
		// GoGuideActivity guideActivity = (GoGuideActivity) mActivity;
		// guideActivity.getmScreenScroller().gotoScreen(1, 400, true);
		// return true;
		// }
		// }
		// }
		return false;
	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {
		return e2.getX() > e1.getX();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		// ((GoGuideActivity)mActivity).updateIndicator(position);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

}

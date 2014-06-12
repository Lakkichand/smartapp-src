package com.jiubang.ggheart.apps.desks.diy.frames.preview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.data.DataProvider;

/**
 * 
 * 类描述:屏幕编辑提示
 * 功能详细描述:屏幕编辑的第一页提示所使用的自定义view，添加屏幕
 * 
 * @author  
 * @date  [2012-9-6]
 */
public class SensePriviewTutorial extends ViewGroup implements View.OnClickListener {

	private Button mOk;
	private TextView mEditScreenTitle;
	private TextView mTextHome;
	private TextView mTextAdd;
	private int mImageAddW;
	private int mImageAddH;
	private Context mContext;
	private int mScreenCnt;
	private static final float TEXTSIZE = 14.67f;
	private int mCurrentScreen = 0;
	private ScreenEditLightView mCircleLight;
	private int mRadius;
	private int mCircleX;
	private int mCircleY;

	public SensePriviewTutorial(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public SensePriviewTutorial(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
		mRadius = getResources().getDimensionPixelSize(R.dimen.screenedit_light_circle_radius);

		DataProvider dataProvider = DataProvider.getInstance(mContext);
		mScreenCnt = dataProvider.getScreenCount();

		mCircleLight = new ScreenEditLightView(context);
		int homeX;
		int homeY = SenseWorkspace.sMarginTop + SenseWorkspace.sHomeImageViewTop;
		if (StatusBarHandler.isHide()) {
			homeY -= StatusBarHandler.getStatusbarHeight();
		}
		if (mScreenCnt <= 2) {
			homeX = SenseWorkspace.sCardWidth / 2 + SenseWorkspace.sMarginLeft;
		} else {
			homeX = 2 * SenseWorkspace.sCardWidth + SenseWorkspace.sCardWidth / 2 + 2
					* SenseWorkspace.sSpaceX + SenseWorkspace.sMarginLeft;
		}
		ScreenEditLightView.setHomeXY(homeX, homeY);
		int[] xy = new int[2];
		computeAddXY(xy);
		mCircleX = xy[0];
		mCircleY = xy[1];
		ScreenEditLightView.setPlusXY(xy[0], xy[1]);
		addView(mCircleLight);

		mTextAdd = new TextView(context);
		mTextAdd.setTextSize(TEXTSIZE);
		mTextAdd.setTextColor(0xffd4d4d4);
		mTextAdd.setText(R.string.preview_add_tutoria);
		if (mScreenCnt >= SenseWorkspace.MAX_CARD_NUMS) {
			mTextAdd.setText(R.string.too_many_screen_tip);
		}
		addView(mTextAdd);

		mTextHome = new TextView(context);
		mTextHome.setTextSize(TEXTSIZE);
		mTextHome.setTextColor(0xffd4d4d4);
		mTextHome.setText(R.string.preview_sethome_tutoria);
		addView(mTextHome);

		mEditScreenTitle = new TextView(context);
		mEditScreenTitle.setText(R.string.preview_edit_screen_title);
		mEditScreenTitle.setTextColor(Color.rgb(175, 248, 112));
		mEditScreenTitle.setTextSize(20F);
		addView(mEditScreenTitle);

		mOk = new Button(context);
		mOk.setText(R.string.tip_btn_txt);
		mOk.setBackgroundResource(R.drawable.guide_for_screenfolder_okbutton);
		mOk.setTextColor(Color.WHITE);
		mOk.setTextSize(17.3F);
		mOk.setGravity(Gravity.CENTER);
		mOk.setOnClickListener(this);
		addView(mOk);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		boolean isPortait = GoLauncher.isPortait();
		int count = getChildCount();
		if (count > 0) {
			int screenCount = mScreenCnt % 9;
			for (int i = 0; i < count; i++) {
				View view = getChildAt(i);
				if (view == mEditScreenTitle) {
					int left = (int) getResources().getDimension(
							R.dimen.screenedit_title_marginleft);
					int top = (int) getResources().getDimension(R.dimen.screenfolder_title_to_top)
							- GoLauncher.getStatusbarHeight();
					view.layout(left, top, r, b - mEditScreenTitle.getWidth());
				} else if (view == mTextHome) {
					int homeX;
					int homeY = SenseWorkspace.sMarginTop + SenseWorkspace.sHomeImageViewTop;
					if (mScreenCnt <= 2) {
						homeX = SenseWorkspace.sCardWidth / 2 + SenseWorkspace.sMarginLeft;

					} else {
						homeX = 2 * SenseWorkspace.sCardWidth + SenseWorkspace.sCardWidth / 2 + 2
								* SenseWorkspace.sSpaceX + SenseWorkspace.sMarginLeft;
					}
					view.layout(homeX - 2 * mRadius,
							homeY - mRadius - 2 * (int) mTextHome.getTextSize(), r, b);
				} else if (view == mTextAdd) {
					if (mScreenCnt < SenseWorkspace.MAX_CARD_NUMS) {
						if (mScreenCnt / 9 != mCurrentScreen) {
							view.setVisibility(View.GONE);
							continue;
						}
					}
					if (mScreenCnt < SenseWorkspace.MAX_CARD_NUMS) {
						view.layout(mCircleX - mRadius / 2 - mRadius, mCircleY + mRadius
								+ (int) mTextAdd.getTextSize() * 2, r, b);
					}
					/*
					 * else { view.layout(x,
					 * mImgAdd.getTop(),SenseWorkspace.CardWidth*2
					 * +SenseWorkspace.SpaceX*2, b); }
					 */
				} else if (view == mOk) {
					int width = getContext().getResources().getDimensionPixelSize(
							R.dimen.screenfolder_button_width);
					int height = getContext().getResources().getDimensionPixelSize(
							R.dimen.screenfolder_button_height);
					int left = 0;
					int top = 0;

					top = SenseWorkspace.sMarginTop + SenseWorkspace.sCardHeight * 2/*
																					* +
																					* SenseWorkspace
																					* .
																					* MarginTop
																					* /
																					* 5
																					*/;
					if (mScreenCnt >= SenseWorkspace.MAX_CARD_NUMS) {
						left = SenseWorkspace.sCardWidth;
						top = SenseWorkspace.sMarginTop + SenseWorkspace.sCardHeight * 2
								+ SenseWorkspace.sSpaceY * 2 + SenseWorkspace.sCardHeight * 3 / 4;
					} else if (screenCount <= 5 || screenCount == 8) {
						// left =
						// getContext().getResources().getDimensionPixelSize(R.dimen.screenedit_button_marginleft);
						left = SenseWorkspace.sCardPaddingLeft;
					} else {
						left = 2 * SenseWorkspace.sCardWidth + SenseWorkspace.sSpaceX;
					}

					view.layout(left, top, left + width, top + height);
				} else if (view == mCircleLight) {
					view.layout(0, 0, GoLauncher.getScreenWidth(), GoLauncher.getScreenHeight());
				}
			}

		}
	}

	private void computeAddXY(int[] xy) {
		int x = 0;
		int y = 0;
		if (mScreenCnt >= 9) {
			mCircleLight.is9Screen(true);
			x = SenseWorkspace.sCardWidth * 2 + SenseWorkspace.sSpaceX * 2
					+ SenseWorkspace.sMarginLeft + (SenseWorkspace.sCardWidth - mImageAddW) / 2;
			y = SenseWorkspace.sCardHeight * 2 + SenseWorkspace.sSpaceY * 2
					+ (SenseWorkspace.sCardHeight - mImageAddH) / 2 + +SenseWorkspace.sMarginTop;

		} else {
			switch (mScreenCnt % 9) {
				case 1 :
					x = SenseWorkspace.sCardWidth + SenseWorkspace.sCardWidth / 2
							+ SenseWorkspace.sSpaceX + SenseWorkspace.sMarginLeft;
					y = SenseWorkspace.sCardHeight / 2 + SenseWorkspace.sMarginTop;
					break;
				case 2 :
					x = SenseWorkspace.sCardWidth * 2 + SenseWorkspace.sCardWidth / 2
							+ SenseWorkspace.sSpaceX * 2 + SenseWorkspace.sMarginLeft;
					y = SenseWorkspace.sCardHeight / 2 + SenseWorkspace.sMarginTop;
					break;
				case 3 :
					x = SenseWorkspace.sCardWidth / 2 + SenseWorkspace.sMarginLeft;
					y = SenseWorkspace.sCardHeight + SenseWorkspace.sCardHeight / 2
							+ SenseWorkspace.sSpaceY + SenseWorkspace.sMarginTop;
					break;
				case 4 :
					x = SenseWorkspace.sCardWidth + SenseWorkspace.sCardWidth / 2
							+ SenseWorkspace.sSpaceX + SenseWorkspace.sMarginLeft;
					y = SenseWorkspace.sCardHeight + SenseWorkspace.sCardHeight / 2
							+ SenseWorkspace.sSpaceY + SenseWorkspace.sMarginTop;
					break;
				case 5 :
					x = SenseWorkspace.sCardWidth * 2 + SenseWorkspace.sCardWidth / 2
							+ SenseWorkspace.sSpaceX * 2 + SenseWorkspace.sMarginLeft;
					y = SenseWorkspace.sCardHeight + SenseWorkspace.sCardHeight / 2
							+ SenseWorkspace.sSpaceY + SenseWorkspace.sMarginTop;
					break;
				case 6 :
					x = SenseWorkspace.sCardWidth / 2 + SenseWorkspace.sMarginLeft;
					y = SenseWorkspace.sCardHeight * 2 + SenseWorkspace.sCardHeight / 2
							+ SenseWorkspace.sSpaceY * 2 + SenseWorkspace.sMarginTop;
					break;
				case 7 :
					x = SenseWorkspace.sCardWidth + SenseWorkspace.sCardWidth / 2
							+ SenseWorkspace.sSpaceX + SenseWorkspace.sMarginLeft;
					y = SenseWorkspace.sCardHeight * 2 + SenseWorkspace.sCardHeight / 2
							+ SenseWorkspace.sSpaceY * 2 + SenseWorkspace.sMarginTop;
					break;
				case 8 :
				case 0 :
					x = SenseWorkspace.sCardWidth * 2 + SenseWorkspace.sCardWidth / 2
							+ SenseWorkspace.sSpaceX * 2 + SenseWorkspace.sMarginLeft;
					y = SenseWorkspace.sCardHeight * 2 + SenseWorkspace.sCardHeight / 2
							+ SenseWorkspace.sSpaceY * 2 + SenseWorkspace.sMarginTop;
				default :
					break;
			}
			// x = x;
			// y = y - GoLauncher.getStatusbarHeight();

		}
		if (StatusBarHandler.isHide()) {
			y = y - StatusBarHandler.getStatusbarHeight();
		}
		xy[0] = x;
		xy[1] = y;
	}

	/*
	 * private int computeAddTextX(int txtWidth){ int x=0; if(mScreenCnt >=
	 * SenseWorkspace.MAX_CARD_NUMS) { return SenseWorkspace.CardWidth/3; }
	 * switch (mScreenCnt%9) { case 1: case 4: case 7: x =
	 * mImgAdd.getLeft()-SenseWorkspace.CardWidth/2; break; case 2: case 5: case
	 * 8: x = mImgAdd.getLeft()-txtWidth/2; break; case 3: case 6: x =
	 * mImgAdd.getLeft(); break; default: break; } return x; }
	 */

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	@Override
	public void onClick(View v) {
		if (mCircleLight != null) {
			mCircleLight.recycle();
			mCircleLight = null;
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
				IDiyFrameIds.GUIDE_GL_FRAME, null, null);

	}

	public void setPreviewCurrentScreen(int screen) {
		mCurrentScreen = screen;
	}
}

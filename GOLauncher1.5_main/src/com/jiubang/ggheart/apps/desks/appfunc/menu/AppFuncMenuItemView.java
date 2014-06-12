package com.jiubang.ggheart.apps.desks.appfunc.menu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;

/**
 * 功能表列表菜单选项View
 * @author yangguanxiang
 *
 */
public class AppFuncMenuItemView extends TextView {

	private int mTitleNum;
	private Paint mTitleNumPaint;
	private Drawable mTitleNumDrawable;
	private Rect mTitleNumRect;
	public AppFuncMenuItemView(Context context) {
		super(context);
	}

	public AppFuncMenuItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		layoutTitleNum();
	}

	protected void layoutTitleNum() {
		CharSequence text = getText();
		if (text != null && mTitleNum > 0) {
			if (mTitleNumPaint == null) {
				mTitleNumPaint = new Paint();
				int fontSize = getResources().getDimensionPixelSize(R.dimen.dock_notify_font_size);
				mTitleNumPaint.setTextSize(fontSize);
				mTitleNumPaint.setStyle(Style.FILL_AND_STROKE);
				mTitleNumPaint.setAntiAlias(true);
				mTitleNumPaint.setColor(android.graphics.Color.WHITE);
				mTitleNumPaint.setTypeface(Typeface.DEFAULT_BOLD);
				mTitleNumPaint.setTextAlign(Paint.Align.CENTER);
			}
			if (mTitleNumDrawable == null) {
//				mTitleNumDrawable = getResources().getDrawable(
//						R.drawable.recomm_appsmanagement_update_count_notification_orange);
				mTitleNumDrawable = getResources().getDrawable(
						R.drawable.stat_notify);
			}
			if (mTitleNumRect == null) {
				mTitleNumRect = new Rect();
			}
			String numStr = String.valueOf(mTitleNum);
			mTitleNumPaint.getTextBounds(numStr, 0, numStr.length(), mTitleNumRect);
			Rect rect = new Rect();
			getPaint().getTextBounds(text.toString(), 0, text.length(), rect);
			int defaultWidth = getResources().getDimensionPixelSize(R.dimen.dock_notify_width);
			int numWidth = mTitleNumRect.width() + (int) (defaultWidth * 2.0 / 3.0f);
			if (mTitleNum < 10) {
				numWidth = defaultWidth;
			}
			int numHeight = getResources().getDimensionPixelSize(R.dimen.dock_notify_height);
			int numLeft = getPaddingLeft() + rect.width();
			int numRight = numLeft + numWidth;
			int numTop = (int) ((getHeight() - rect.height()) / 2.0f - numHeight / 2.0f);
			int numBottom = numTop + numHeight;
			mTitleNumDrawable.setBounds(numLeft, numTop, numRight, numBottom);
		}
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		CharSequence text = getText();
		if (text != null && mTitleNum > 0) {
			if (mTitleNumPaint != null && mTitleNumDrawable != null) {
				//				mTitleNumDrawable.draw(canvas);
				Rect rect = mTitleNumDrawable.getBounds();
				ImageUtil.drawImage(canvas, mTitleNumDrawable, ImageUtil.STRETCHMODE, rect.left,
						rect.top, rect.right, rect.bottom, mTitleNumPaint);
				canvas.drawText(String.valueOf(mTitleNum), rect.centerX(), rect.centerY()
						+ mTitleNumRect.height() / 3.0f, mTitleNumPaint);
			}
		}
	}
	public void setTitleNum(int num) {
		mTitleNum = num;
		layoutTitleNum();
	}
}

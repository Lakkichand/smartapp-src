package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageView;

import com.gau.go.launcherex.R;

/**
 * 在图片上写 已修改 三个字
 * 
 * @author yangbing
 * */
public class ModifyImageView extends ImageView {
	private Paint mPaint;
	private String mText;

	public ModifyImageView(Context context) {
		super(context);
		mPaint = new Paint();
		mPaint.setTextSize(20);
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.WHITE);
		mText = getResources().getString(R.string.theme_modifyed_text);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		System.out.println("onDraw");
		super.onDraw(canvas);
		canvas.save();
		canvas.rotate(45);
		canvas.drawText(mText, 40, -15, mPaint);
		canvas.restore();

	}

}

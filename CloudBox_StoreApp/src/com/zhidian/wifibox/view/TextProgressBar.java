package com.zhidian.wifibox.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.download.DownloadTask;

public class TextProgressBar extends ProgressBar {
	String text;
	Paint mPaint;
	String status;

	public TextProgressBar(Context context) {
		super(context);
		initText();
	}

	public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initText();
	}

	public TextProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initText();
	}

	@Override
	public synchronized void setProgress(int progress) {
		setText(progress);
		super.setProgress(progress);

	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// this.setText();
		Rect rect = new Rect();
		Resources resources = getResources();
		float size = resources.getDimension(R.dimen.no_result_padding_length);
		this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
		float x = (getWidth() / 2 + size) - rect.centerX();
		int y = (getHeight() / 2) - rect.centerY();
		canvas.drawText(this.text, x, y, this.mPaint);
	}

	// 初始化，画笔
	private void initText() {
		this.mPaint = new Paint();
		this.mPaint.setColor(Color.WHITE);
		Resources resources = getResources();
		float size = resources.getDimension(R.dimen.main_game_info_jr_more_size);
		this.mPaint.setTextSize(size);

	}

	private void setText() {
		setText(this.getProgress());
	}

	// 设置文字内容
	private void setText(int progress) {
		int i = (progress * 100) / this.getMax();
		this.text = String.valueOf(i) + "%";
	}

	public void setStatus(int s) {
		switch (s) {

		case DownloadTask.DOWNLOADING:// 下载中
			status = "暂停" + "　";
			break;
		case DownloadTask.PAUSING:// 暂停
			status = "继续" + "　";
			break;
		}
		
	}

}
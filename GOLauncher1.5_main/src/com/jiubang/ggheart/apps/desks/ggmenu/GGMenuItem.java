package com.jiubang.ggheart.apps.desks.ggmenu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageManager;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 菜单中的菜单项
 * 
 * @author ouyongqiang
 * 
 */
public class GGMenuItem extends RelativeLayout {

	/**
	 * 菜单项图片
	 */
	private ImageView mImage;

	/**
	 * 菜单项文字说明
	 */
	private TextView mText;

	private ImageView mScreenEditInfo;

	private ImageView mNewTheme;
	/**
	 * 构造函数
	 * 
	 * @param context
	 *            程序上下文
	 */
	public GGMenuItem(Context context) {
		super(context);
	}

	/**
	 * 构造函数
	 * 
	 * @param context
	 *            程序上下文
	 * @param attrs
	 *            属性集
	 */
	public GGMenuItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		mImage = (ImageView) findViewById(R.id.bottom_item_image);
		mText = (TextView) findViewById(R.id.bottom_item_text);
		mScreenEditInfo = (ImageView) findViewById(R.id.screen_edit_info);
	}

	/**
	 * 绑定数据到菜单项
	 * 
	 * @param text
	 *            菜单项的文字说明
	 * @param resId
	 *            菜单项的图片资源Id
	 */
	public void bind(String text, Drawable drawable) {
		mText.setText(text);
		mImage.setImageDrawable(drawable);
	}

	public void bind(String text, int color, Drawable drawable) {
		mText.setText(text);
		mText.setTextColor(color);
		mImage.setImageDrawable(drawable);

	}

	/**
	 * 返回TextView
	 * 
	 * @return TextView 返回的TextView
	 */
	public TextView getTextView() {
		return mText;
	}

	/**
	 * 返回ImageView
	 * 
	 * @return ImageView 返回的ImageView
	 */
	public ImageView getImageView() {
		return mImage;
	}

	/**
	 * 
	 * @param visibility
	 *            View visibility
	 */
	public void setScreenEditInfoVisibility(int visibility) {
		if (mScreenEditInfo != null) {
			if (mScreenEditInfo.getVisibility() != visibility) {
				mScreenEditInfo.setVisibility(visibility);
			}
		}
	}

	public void generatorMessageCountImage() {
		int cnt = MessageManager.getMessageManager(GOLauncherApp.getContext()).getUnreadedCnt();
		NinePatchDrawable bgNine = (NinePatchDrawable) getContext().getResources().getDrawable(
				R.drawable.message_unread_notification);
		Drawable drawable = mImage.getDrawable();
		if (null == bgNine || cnt == 0 || !(drawable instanceof BitmapDrawable)) {
			return;
		}

		int size = (int) GOLauncherApp.getContext().getResources()
				.getDimension(R.dimen.message_notify_size);
		bgNine.setBounds(new Rect(0, 0, size, size));

		Bitmap bgIcon = Bitmap.createBitmap(bgNine.getBounds().width(),
				bgNine.getBounds().height(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bgIcon);
		bgNine.draw(canvas);

		Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
		countPaint.setColor(Color.WHITE);
		countPaint.setTextSize(getContext().getResources().getDimension(
				R.dimen.new_message_count_text_size));
		countPaint.setTypeface(Typeface.DEFAULT_BOLD);
		String number = String.valueOf(cnt);
		float w = countPaint.measureText(number);

		// add by Ryan at 2012.08.22
		if (canvas == null || bgIcon == null) {
			return;
		}
		// end

		canvas.drawText(number, (bgIcon.getWidth() - w) / 2, bgIcon.getHeight() * 2 / 3, countPaint);
		Bitmap msgCntIcon = ((BitmapDrawable) drawable).getBitmap().copy(Config.ARGB_8888, true);
		canvas = new Canvas(msgCntIcon);
		canvas.drawBitmap(bgIcon, msgCntIcon.getWidth() - bgIcon.getWidth(), 0, countPaint);
		mImage.setImageBitmap(msgCntIcon);
	}

	public void generatorMessageCountImage(int count) {
		NinePatchDrawable bgNine = (NinePatchDrawable) getContext().getResources().getDrawable(
				R.drawable.message_unread_notification);
		Drawable drawable = mImage.getDrawable();
		if (null == bgNine || count == 0 || !(drawable instanceof BitmapDrawable)) {
			return;
		}

		int size = (int) GOLauncherApp.getContext().getResources()
				.getDimension(R.dimen.message_notify_size);
		bgNine.setBounds(new Rect(0, 0, size, size));

		Bitmap bgIcon = Bitmap.createBitmap(bgNine.getBounds().width(),
				bgNine.getBounds().height(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bgIcon);
		bgNine.draw(canvas);

		Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
		countPaint.setColor(Color.WHITE);
		countPaint.setTextSize(getContext().getResources().getDimension(
				R.dimen.new_message_count_text_size));
		countPaint.setTypeface(Typeface.DEFAULT_BOLD);
		String number = String.valueOf(count);
		float w = countPaint.measureText(number);

		canvas.drawText(number, (bgIcon.getWidth() - w) / 2, bgIcon.getHeight() * 2 / 3, countPaint);
		Bitmap msgCntIcon = ((BitmapDrawable) drawable).getBitmap().copy(Config.ARGB_8888, true);
		canvas = new Canvas(msgCntIcon);
		canvas.drawBitmap(bgIcon, msgCntIcon.getWidth() - bgIcon.getWidth(), 0, countPaint);
		mImage.setImageBitmap(msgCntIcon);
	}

	public void addNewThemeLogo() {
		if (mNewTheme == null) {
			mNewTheme = new ImageView(getContext());
			mNewTheme.setImageResource(R.drawable.theme_new_log);
		} else {
			return;
		}

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		addView(mNewTheme, params);
	}

	public void removeNewThemeLogo() {
		if (mNewTheme != null) {
			removeView(mNewTheme);
			mNewTheme = null;
		}
	}

	public void cleanup() {
		if (mText != null && mText instanceof DeskTextView) {
			((DeskTextView) mText).selfDestruct();
		}
	}
}

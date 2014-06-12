package com.go.launcher.colorpicker;

import java.text.NumberFormat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.StateSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogBase;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 
 * <br>
 * 类描述:颜色选择器 <br>
 * 功能详细描述:
 * 
 */
public class ColorPickerDialog extends Dialog implements View.OnClickListener {
	private int mScenterX = 100;
	private int mScenterY = 100;
	private int mScenterRadius = 45;
	private int mAlpha = 255;
	private boolean mTextChangeSMS = true;

	private Button mBtnConfirm = null;
	private Button mBtnCancel = null;
	private Button mBtnDefault = null;
	private TextView mTitle = null;
	private boolean mCustom;

	private static final int MIN_VALUE = 0;
	private static final int MAX_VALUE = 255;

	public static final int FONT = 0;
	public static final int ICON = 1;
	public static final float NOXY = 999999999;
	private int mType = FONT;
	private PreferencesManager mPreferencesManager = null;

	private static final String INITIAL_X_FONT = "initia_x_font";
	private static final String INITIAL_Y_FONT = "initia_y_font";
	private static final String FONT_COLOR = "font_color";

	private static final String INITIAL_X_ICON = "initia_x_icon";
	private static final String INITIAL_Y_ICON = "initia_y_icon";
	private static final String ICON_COLOR = "icon_color";

	private boolean mTrackingCenter;
	private boolean mHighlightCenter;

	private float mOutRadius = 0; // 外围圆的半径
	private float mPointX;
	private float mPointY;
	private static final String DEFAULT_COLOR = "#ffffffff";
	private String mColorText = DEFAULT_COLOR;
	private String mColor;
	private boolean mFlag = true;

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 */
	public interface OnColorChangedListener {

		void colorIsSave(boolean isSave);

		void colorChanged(int color);

		void useCustom(boolean custom);
	}

	private OnColorChangedListener mListener;
	private int mInitialColor;

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 */
	private class ColorPickerView extends View {
		private Paint mPaint;
		private Paint mCenterPaint;
		private final int[] mColors;
		private OnColorChangedListener mListener;

		ColorPickerView(Context c, OnColorChangedListener l, int color) {
			super(c);
			mListener = l;
			mColors = new int[] { 0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
					0xFF00FFFF, 0xFF00FF00, 0xFFFF9900, 0xFFFF7800, 0xFFFFFF00,
					0xFFFFFFFF, 0xFF808080, 0xFF000000, 0xFFFF0000 };
			Shader s = new SweepGradient(0, 0, mColors, null);

			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setShader(s);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(mScenterRadius);

			mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCenterPaint.setColor(color);
			mCenterPaint.setStrokeWidth(5);

			mAlpha = Color.alpha(color);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			float r = mScenterX - mPaint.getStrokeWidth() * 0.8f;
			mOutRadius = r;
			canvas.translate(mScenterX * 1.5f, mScenterX);
			canvas.drawOval(new RectF(-r, -r, r, r), mPaint);

			// 判断是否用户自定义
			if (mCustom) {
				// 画中心圆
				canvas.drawCircle(0, 0, mScenterRadius, mCenterPaint);
			}

			if (mFlag) {
				boolean isInFlag;
				float pointRadius = (float) java.lang.Math.sqrt(mPointX
						* mPointX + mPointY * mPointY);
				if (mOutRadius - mScenterRadius <= pointRadius
						&& pointRadius <= mOutRadius) {
					isInFlag = true;
				} else {
					isInFlag = false;
				}

				float scan = pointRadius / mOutRadius;

				float x = mPointX / scan;
				float y = mPointY / scan;

				Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
						R.drawable.desk_setting_huan);

				int w = bitmap.getWidth() / 2;
				int h = bitmap.getHeight() / 2;

				canvas.drawBitmap(bitmap, x - w, y - h, null);
			}

			if (mTrackingCenter && mCustom) {
				int c = mCenterPaint.getColor();
				mCenterPaint.setStyle(Paint.Style.STROKE);

				if (mHighlightCenter) {
					mCenterPaint.setAlpha(0xFF);
				} else {
					mCenterPaint.setAlpha(0x80);
				}
				canvas.drawCircle(0, 0,
						mScenterRadius + mCenterPaint.getStrokeWidth(),
						mCenterPaint);

				mCenterPaint.setStyle(Paint.Style.FILL);
				mCenterPaint.setColor(c);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			setMeasuredDimension(mScenterX * 3, mScenterY * 2);
		}

		public void setCenterColor(int color) {
			mCenterPaint.setColor(color);
			invalidate();
		}

		public void setTransparency(int alpha) {
			mTextChangeSMS = false;
			mAlpha = alpha;
			int color = mCenterPaint.getColor();
			int newColor = Color.argb(alpha, Color.red(color),
					Color.green(color), Color.blue(color));
			mCenterPaint.setColor(newColor);
			mEditText.setText(convertToARGB(newColor));
			invalidate();
			mTextChangeSMS = true;
		}

		private int ave(int s, int d, float p) {
			return s + java.lang.Math.round(p * (d - s));
		}

		private int interpColor(int colors[], float unit) {
			if (unit <= 0) {
				return colors[0];
			}
			if (unit >= 1) {
				return colors[colors.length - 1];
			}

			float p = unit * (colors.length - 1);
			int i = (int) p;
			p -= i;

			// now p is just the fractional part [0...1) and i is the index
			int c0 = colors[i];
			int c1 = colors[i + 1];
			// int a = ave(Color.alpha(c0), Color.alpha(c1), p);
			int a = mAlpha;
			int r = ave(Color.red(c0), Color.red(c1), p);
			int g = ave(Color.green(c0), Color.green(c1), p);
			int b = ave(Color.blue(c0), Color.blue(c1), p);

			return Color.argb(a, r, g, b);
		}

		private static final float PI = 3.1415926f;

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX() - mScenterX * 1.5f;
			float y = event.getY() - mScenterY;
			boolean inCenter = java.lang.Math.sqrt(x * x + y * y) <= mScenterRadius;
			mPointX = x;
			mPointY = y;
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// mTrackingCenter = inCenter;
				// if (inCenter) {
				// if(mCustom){
				// mHighlightCenter = true;
				// invalidate();
				// break;
				// }
				// }else {
				mCustom = true;
				mColor = convertToARGB(mCenterPaint.getColor());

				// break;
				// }
			case MotionEvent.ACTION_MOVE:
				mFlag = true;
				if (mTrackingCenter) {
					if (mHighlightCenter != inCenter) {
						mHighlightCenter = inCenter;
						invalidate();
					}
				} else {
					float angle = (float) java.lang.Math.atan2(y, x);
					// need to turn angle [-PI ... PI] into unit [0....1]
					float unit = angle / (2 * PI);
					if (unit < 0) {
						unit += 1;
					}
					int color = interpColor(mColors, unit);
					mCenterPaint.setColor(color);
					mColorText = convertToARGB(color);
						if (mEditText != null) {
							mEditText.setText(mColorText);
						}
					invalidate();
				}
				mColor = convertToARGB(mCenterPaint.getColor());

				break;
			case MotionEvent.ACTION_UP:
				mFlag = true;
				mListener.colorChanged(mCenterPaint.getColor());
				mListener.useCustom(mCustom);
				mListener.colorIsSave(false);

				if (mTrackingCenter) {
					if (inCenter && mCustom) {
						mListener.colorChanged(mCenterPaint.getColor());
						mListener.useCustom(mCustom);
						mListener.colorIsSave(false);

					}
					mTrackingCenter = false; // so we draw w/o halo
					invalidate();
				}
				mColor = convertToARGB(mCenterPaint.getColor());

				break;
			}
			return true;
		}

		public int getColor() {
			return mCenterPaint.getColor();
		}

		// public void setColor(int color) {
		// mCenterPaint.setColor(color);
		// mEditText.setText(convertToARGB(color));
		// invalidate();
		// }
	}

	private String convertToARGB(int color) {
		String alpha = Integer.toHexString(Color.alpha(color));
		String red = Integer.toHexString(Color.red(color));
		String green = Integer.toHexString(Color.green(color));
		String blue = Integer.toHexString(Color.blue(color));

		if (alpha.length() == 1) {
			alpha = "0" + alpha;
		}

		if (red.length() == 1) {
			red = "0" + red;
		}

		if (green.length() == 1) {
			green = "0" + green;
		}

		if (blue.length() == 1) {
			blue = "0" + blue;
		}

		return "#" + alpha + red + green + blue;
	}

	private int convertToColorInt(String argb) throws NumberFormatException {

		int alpha = -1, red = -1, green = -1, blue = -1;

		if (argb.length() == 8) {
			alpha = Integer.parseInt(argb.substring(0, 2), 16);
			red = Integer.parseInt(argb.substring(2, 4), 16);
			green = Integer.parseInt(argb.substring(4, 6), 16);
			blue = Integer.parseInt(argb.substring(6, 8), 16);
		} else if (argb.length() == 6) {
			alpha = 255;
			red = Integer.parseInt(argb.substring(0, 2), 16);
			green = Integer.parseInt(argb.substring(2, 4), 16);
			blue = Integer.parseInt(argb.substring(4, 6), 16);
		}

		return Color.argb(alpha, red, green, blue);
	}

	private Context mContext;
	private EditText mEditText;
	private ColorPickerView mColorPickerView;
	private SeekBar mTransparencyBar;
	private TextView mAlphaText;

	public ColorPickerDialog(Context context, OnColorChangedListener listener, boolean custom,
			int currentColor, int type, float x, float y) {
		super(context, R.style.msg_dialog);
		mContext = context;
		mListener = listener;
		mCustom = custom;
		mInitialColor = currentColor;
		if (!mCustom) {
			mInitialColor = 0xFF000000 | mInitialColor;
		}
		mAlpha = Color.alpha(mInitialColor);
		Configuration configuration = context.getResources().getConfiguration();
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		final int pickerCenter = context.getResources().getDimensionPixelSize(
				R.dimen.color_picker_center);
		final int pickerRadius = context.getResources().getDimensionPixelSize(
				R.dimen.color_picker_radius);
		if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
			mScenterY = mScenterX = pickerCenter * metrics.heightPixels / 800;
			mScenterRadius = pickerRadius * metrics.heightPixels / 800;
		} else {
			mScenterY = mScenterX = pickerCenter * metrics.heightPixels / 800;
			mScenterRadius = pickerRadius * metrics.heightPixels / 800;
		}

		mType = type;

		if (mType == FONT) {
			mPreferencesManager = new PreferencesManager(getContext(),
					IPreferencesIds.ORIENTATION_XY_FONT, Context.MODE_PRIVATE);
		} else {
			mPreferencesManager = new PreferencesManager(getContext(),
					IPreferencesIds.ORIENTATION_XY_ICON, Context.MODE_PRIVATE);
		}
		if (x != NOXY && y != NOXY) {
			mPointX = x;
			mPointY = y;
		} else {
			getXY();
		}

		if (mColor != null) {
			if (!mColor.equals(convertToARGB(mInitialColor))) {
				mFlag = false;
			} else {
				mFlag = true;
			}
		}
		// CENTER_Y = CENTER_X =
		// context.getResources().getDimensionPixelSize(R.dimen.color_picker_center);
		// CENTER_RADIUS =
		// context.getResources().getDimensionPixelSize(R.dimen.color_picker_radius);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.desk_setting_color_select_view);
		mTitle = (TextView) findViewById(R.id.title);

		setTitleVisible(); // 横屏隐藏标题
		LinearLayout dialogLayout = (LinearLayout) findViewById(R.id.dialog_layout);
		DialogBase.setDialogWidth(dialogLayout, mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout colorLayout = (LinearLayout) findViewById(R.id.color_layout);
		mColorPickerView = new ColorPickerView(getContext(),
				mOnColorChangedListener, mInitialColor);
		colorLayout.addView(mColorPickerView, params); // 添加颜色选择器

		mAlphaText = (TextView) findViewById(R.id.desk_setting_colorpicker_seekbar_title);
		TextView minTextView = (TextView) findViewById(R.id.min_value);
		TextView maxTextView = (TextView) findViewById(R.id.max_value);

		mTransparencyBar = (SeekBar) findViewById(R.id.desk_setting_colorpicker_seekbar);
		// 根据按钮图片的大小调整调节条的宽度。不然会有空白位置显示。
		DeskSettingConstants.setSeekBarPadding(mTransparencyBar, mContext);
		mTransparencyBar.setMax(MAX_VALUE);

		/**
		 * change by dengdazhong date 2012.8.2 ADT-6932 桌面设置图标高亮背景设置，透明度的默认值有误
		 */
		if (mCustom || Color.alpha(mInitialColor) >= 0) {
			mTransparencyBar.setProgress(Color.alpha(mInitialColor));
		} else {
			mTransparencyBar.setProgress(mTransparencyBar.getMax());
		}
		mTransparencyBar
				.setOnSeekBarChangeListener(mOnTransparencyChangedListener);

		minTextView.setText("0");
		maxTextView.setText("100"); // 显示0 - 100

		NumberFormat nf = NumberFormat.getPercentInstance();
		String curAlphaString = nf
				.format(mTransparencyBar.getProgress() / 255f);

		mAlphaText.setText(mContext.getResources().getString(
				R.string.pref_dialog_color_picker_alpha)
				+ ":" + curAlphaString);

		mEditText = (EditText) findViewById(R.id.color_edit_text);
		mEditText.addTextChangedListener(mEditTextListener);
		if (mCustom) {
			mColorText = convertToARGB(mInitialColor);
			mEditText.setText(mColorText);
		} else {
			mColorText = DEFAULT_COLOR;
			mEditText.setText(mColorText);
		}

		mBtnConfirm = (Button) findViewById(R.id.ok_btn);
		mBtnConfirm.setOnClickListener(this);

		mBtnCancel = (Button) findViewById(R.id.cancel_btn);
		mBtnCancel.setOnClickListener(this);

		mBtnDefault = (Button) findViewById(R.id.reset_btn);
		mBtnDefault.setOnClickListener(this);

		if (mType == FONT) {
			mTitle.setText(R.string.app_labels_color);
		} else {
			mTitle.setText(R.string.pref_title_icon_highlights);
		}

	}

	/**
	 * <br>
	 * 功能简述:横屏隐藏标题 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void setTitleVisible() {
		if (GoLauncher.getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
			mTitle.setVisibility(View.GONE);
		}
	}

	private OnColorChangedListener mOnColorChangedListener = new OnColorChangedListener() {
		@Override
		public void colorChanged(int color) {
			mListener.colorChanged(color);
		}

		@Override
		public void useCustom(boolean custom) {
			mListener.useCustom(custom);
		}

		@Override
		public void colorIsSave(boolean isSave) {
			// TODO Auto-generated method stub
			mListener.colorIsSave(isSave);
		}
	};

	private SeekBar.OnSeekBarChangeListener mOnTransparencyChangedListener = new SeekBar.OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			mCustom = true;
			mColorPickerView.setTransparency(progress);

			NumberFormat nf = NumberFormat.getPercentInstance();
			String curAlphaString = nf.format(progress / 255f);
			mAlphaText.setText(mContext.getResources().getString(
					R.string.pref_dialog_color_picker_alpha)
					+ ":" + curAlphaString);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};

	private TextWatcher mEditTextListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			try {
				if (!mTextChangeSMS) {
					return;
				}
				String s2 = (s.toString()).replace("#", "");
				if (s2.length() == 6 || s2.length() == 8) {
					mCustom = true;
					int color = convertToColorInt(s2);
					mColorPickerView.setCenterColor(color);
					mAlpha = Color.alpha(color);
					mTransparencyBar.setProgress(mAlpha);
				}
			} catch (NumberFormatException e) {
			}
		}
	};

	// Source: http://www.anddev.org/announce_color_picker_dialog-t10771.html
	static final int[] STATE_FOCUSED = { android.R.attr.state_focused };
	static final int[] STATE_PRESSED = { android.R.attr.state_pressed };

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 */
	static class TextSeekBarDrawable extends Drawable implements Runnable {

		private static final long DELAY = 50;
		private String mText;
		private Drawable mProgress;
		private Paint mPaint;
		private Paint mOutlinePaint;
		private float mTextWidth;
		private boolean mActive;
		private float mTextXScale;
		private int mDelta;
		private ScrollAnimation mAnimation;

		public TextSeekBarDrawable(Resources res, String label,
				boolean labelOnRight) {
			mText = label;
			mProgress = res.getDrawable(android.R.drawable.progress_horizontal);
			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setTypeface(Typeface.DEFAULT_BOLD);
			mPaint.setTextSize(16);
			mPaint.setColor(0xff000000);
			mOutlinePaint = new Paint(mPaint);
			mOutlinePaint.setStyle(Style.STROKE);
			mOutlinePaint.setStrokeWidth(3);
			mOutlinePaint.setColor(0xbbffc300);
			mOutlinePaint.setMaskFilter(new BlurMaskFilter(1, Blur.NORMAL));
			mTextWidth = mOutlinePaint.measureText(mText);
			mTextXScale = labelOnRight ? 1 : 0;
			mAnimation = new ScrollAnimation();
		}

		@Override
		protected void onBoundsChange(Rect bounds) {
			mProgress.setBounds(bounds);
		}

		@Override
		protected boolean onStateChange(int[] state) {
			mActive = StateSet.stateSetMatches(STATE_FOCUSED, state)
					| StateSet.stateSetMatches(STATE_PRESSED, state);
			invalidateSelf();
			return false;
		}

		@Override
		public boolean isStateful() {
			return true;
		}

		@Override
		protected boolean onLevelChange(int level) {
			// Log.d(TAG, "onLevelChange " + level);
			if (level < 4000 && mDelta <= 0) {
				// Log.d(TAG, "onLevelChange scheduleSelf ++");
				mDelta = 1;
				mAnimation.startScrolling(mTextXScale, 1);
				scheduleSelf(this, SystemClock.uptimeMillis() + DELAY);
			} else if (level > 6000 && mDelta >= 0) {
				// Log.d(TAG, "onLevelChange scheduleSelf --");
				mDelta = -1;
				mAnimation.startScrolling(mTextXScale, 0);
				scheduleSelf(this, SystemClock.uptimeMillis() + DELAY);
			}
			return mProgress.setLevel(level);
		}

		@Override
		public void draw(Canvas canvas) {
			mProgress.draw(canvas);
			if (mAnimation.hasStarted() && !mAnimation.hasEnded()) {
				// pending animation
				mAnimation.getTransformation(
						AnimationUtils.currentAnimationTimeMillis(), null);
				mTextXScale = mAnimation.getCurrent();
				// Log.d(TAG, "draw " + mTextX + " " +
				// SystemClock.uptimeMillis());
			}

			Rect bounds = getBounds();
			float x = 6 + mTextXScale * (bounds.width() - mTextWidth - 6 - 6);
			float y = (bounds.height() + mPaint.getTextSize()) / 2;
			// 让字体向上移动一点，不然会粘到滑动条上
			y = y - 20;
			mOutlinePaint.setAlpha(mActive ? 255 : 255 / 2);
			mPaint.setAlpha(mActive ? 255 : 255 / 2);
			canvas.drawText(mText, x, y, mOutlinePaint);
			canvas.drawText(mText, x, y, mPaint);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int alpha) {
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
		}

		@Override
		public void run() {
			mAnimation.getTransformation(
					AnimationUtils.currentAnimationTimeMillis(), null);
			// close interpolation of mTextX
			mTextXScale = mAnimation.getCurrent();
			if (!mAnimation.hasEnded()) {
				scheduleSelf(this, SystemClock.uptimeMillis() + DELAY);
			}
			invalidateSelf();
			// Log.d(TAG, "run " + mTextX + " " + SystemClock.uptimeMillis());
		}
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 */
	static class ScrollAnimation extends Animation {
		private static final long DURATION = 750;
		private float mFrom;
		private float mTo;
		private float mCurrent;

		public ScrollAnimation() {
			setDuration(DURATION);
			setInterpolator(new DecelerateInterpolator());
		}

		public void startScrolling(float from, float to) {
			mFrom = from;
			mTo = to;
			startNow();
		}

		@Override
		protected void applyTransformation(float interpolatedTime,
				Transformation t) {
			mCurrent = mFrom + (mTo - mFrom) * interpolatedTime;
			// Log.d(TAG, "applyTransformation " + mCurrent);
		}

		public float getCurrent() {
			return mCurrent;
		}
	}

	// @Override
	// public boolean onKeyUp(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// confirmCancelDialog();
	// return true;
	// }
	// return super.onKeyUp(keyCode, event);
	// }

	private void confirmCancelDialog() {
		final AlertDialog dialog = new AlertDialog.Builder(mContext)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.color_picker_alert_dialog_title)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dismiss();
							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).create();
		dialog.show();
	}

	public float getmPointX() {
		return mPointX;
	}

	public float getmPointY() {
		return mPointY;
	}

	private void saveXY(float pointX, float ponitY) {
		if (mType == FONT) {
			mPreferencesManager.putFloat(INITIAL_X_FONT, pointX);
			mPreferencesManager.putFloat(INITIAL_Y_FONT, ponitY);
			mPreferencesManager.putString(FONT_COLOR, mColor);

		} else {
			mPreferencesManager.putFloat(INITIAL_X_ICON, pointX);
			mPreferencesManager.putFloat(INITIAL_Y_ICON, ponitY);
			mPreferencesManager.putString(ICON_COLOR, mColor);
		}
		mPreferencesManager.commit();

	}

	private void saveXYToDefault() {
		if (mType == FONT) {
			mPreferencesManager.putFloat(INITIAL_X_FONT, -18);
			mPreferencesManager.putFloat(INITIAL_Y_FONT, -122);
			mPreferencesManager.putString(FONT_COLOR, DEFAULT_COLOR);

		} else {
			mPreferencesManager.putFloat(INITIAL_X_ICON, -18);
			mPreferencesManager.putFloat(INITIAL_Y_ICON, -122);
			mPreferencesManager.putString(ICON_COLOR, DEFAULT_COLOR);

		}
		mPreferencesManager.commit();
	}

	private void getXY() {
		if (mType == FONT) {
			mPointX = mPreferencesManager.getFloat(INITIAL_X_FONT, -18);
			mPointY = mPreferencesManager.getFloat(INITIAL_Y_FONT, -122);
			mColor = mPreferencesManager.getString(FONT_COLOR, DEFAULT_COLOR);
		} else {
			mPointX = mPreferencesManager.getFloat(INITIAL_X_ICON, -18);
			mPointY = mPreferencesManager.getFloat(INITIAL_Y_ICON, -122);
			mColor = mPreferencesManager.getString(ICON_COLOR, DEFAULT_COLOR);
		}

	}

	@Override
	public void onClick(View source) {
		if (source == mBtnConfirm) {
			if (mCustom) {
				mListener.colorChanged(mColorPickerView.getColor());
				mListener.useCustom(mCustom);
				mListener.colorIsSave(true);
				if (mPreferencesManager != null) {
					saveXY(mPointX, mPointY);
				} 
			}
			dismiss();
		} else if (source == mBtnCancel) {
			dismiss();
		} else if (source == mBtnDefault) {		
			mListener.colorChanged(convertToColorInt(DEFAULT_COLOR));
			mListener.useCustom(mCustom);
			mListener.colorIsSave(true);
			if (mPreferencesManager != null) {
				saveXYToDefault();
			}
			dismiss();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mEditText != null) {
			mEditText = null;
		}
		// 对话框关闭时遍历所有控件，把DeskView和DeskButton反注册
		DeskSettingConstants.selfDestruct(getWindow().getDecorView());
	}

	public int getPickerViewColor() {
		if (mColorPickerView != null) {
			return mColorPickerView.getColor();
		}
		return convertToColorInt(DEFAULT_COLOR);
	}

}

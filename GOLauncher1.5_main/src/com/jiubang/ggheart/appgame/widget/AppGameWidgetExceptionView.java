package com.jiubang.ggheart.appgame.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;

/**
 * 没有网络，本地没有数据时显示网络异常提示界面
 * @author zhoujun
 *
 */
public class AppGameWidgetExceptionView extends LinearLayout {

	private Context mContext;
	private static final int BASE_IMAGE_WIDTH = 390;
	private static final int BASE_IMAGE_HEIGHT = 94;

	private static final int BG_IMAGE_WIDTH = 404;
	private static final int BG_IMAGE_HEIGHT = 96;

	private static final int LEFT_MARGIN = 25;

	private RelativeLayout mExceptionView;
	private Button mRefreshButton;

	public AppGameWidgetExceptionView(Context context) {
		super(context);
		init(context);
	}

	public AppGameWidgetExceptionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		Drawable drawable = bitmapToDrawable(createBaseBitmap());
		this.setBackgroundDrawable(drawable);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		int margin = GoStoreDisplayUtil.scalePxToMachine(mContext, LEFT_MARGIN);
		layoutParams.leftMargin = margin;
		layoutParams.rightMargin = margin;
		this.setLayoutParams(layoutParams);

		initView();
	}
	private void initView() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		mExceptionView = (RelativeLayout) layoutInflater.inflate(R.layout.appgame_widget_exception,
				null);

		mRefreshButton = (Button) mExceptionView.findViewById(R.id.appgame_widget_retry_button);
		mRefreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Machine.isNetworkOK(mContext)) {
					AppGameWidgetDataManager.getInstance(mContext).getWidgetData();
				} else {
					Toast.makeText(mContext,
							mContext.getString(R.string.appgame_widget_exception_info),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		this.addView(mExceptionView, new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
	}

	private Drawable bitmapToDrawable(Bitmap bitmap) {
		BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
		Drawable drawable = bitmapDrawable;
		return drawable;
	}

	/**
	 * 创建底座
	 * 
	 * @return
	 */
	private Bitmap createBaseBitmap() {
		// NinePatchDrawable bgNine = (NinePatchDrawable) getContext()
		// .getResources().getDrawable(
		// R.drawable.recomm_apps_management_tab_alpha_bg);
		// bgNine.setBounds(new Rect(0, 0, BASE_IMAGE_WIDTH,
		// BASE_IMAGE_HEIGHT));
		// int bitWidth = bgNine.getBounds().width();
		// int bitHeight = bgNine.getBounds().height();
		// Bitmap bgBit = null;
		// if (bitWidth > 0 && bitHeight > 0) {
		// bgBit = Bitmap.createBitmap(bitWidth, bitHeight, Config.ARGB_8888);
		// Canvas iconCanvas = new Canvas(bgBit);
		// bgNine.draw(iconCanvas);
		// }
		// return bgBit;
		Bitmap baseBg = createNineBitmap(R.drawable.appgame_widget_banner_backpages_bg,
				BASE_IMAGE_WIDTH, BASE_IMAGE_HEIGHT);
		Bitmap bg = createNineBitmap(R.drawable.appgame_widget_exception_bg, BG_IMAGE_WIDTH,
				BG_IMAGE_HEIGHT);
		Bitmap bitmap = Bitmap.createBitmap(BG_IMAGE_WIDTH, BG_IMAGE_HEIGHT + 9, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(baseBg, 7, 0, null);
		canvas.drawBitmap(bg, 0, 9, null);
		bitmap = createReflectionImageWithOrigin(bitmap, 11);
		return bitmap;
	}

	/**
	 * 获得带倒影的图片方法
	 * 
	 * @param bitmap
	 * @param scaleH
	 *            获得倒影高度比例值;
	 * @return
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap, int reflectHeight) {
		final int reflectionGap = 0;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, height + reflectHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		canvas.drawBitmap(bitmap, 0, 0, null);
		// canvas.drawRect(0, height, width, (height + height * scaleH),
		// deafalutPaint);
		Paint deafalutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x99ffffff, 0x30ffffff,
				TileMode.CLAMP);
		deafalutPaint.setShader(shader);
		deafalutPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height - reflectHeight, width,
				reflectHeight, matrix, false);
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap,
				deafalutPaint);

		return bitmapWithReflection;
	}

	private Bitmap createNineBitmap(int resourceId, int width, int height) {
		NinePatchDrawable bgNine = (NinePatchDrawable) getContext().getResources().getDrawable(
				resourceId);
		bgNine.setBounds(new Rect(0, 0, width, height));
		int bitWidth = bgNine.getBounds().width();
		int bitHeight = bgNine.getBounds().height();
		Bitmap bgBit = null;
		if (bitWidth > 0 && bitHeight > 0) {
			bgBit = Bitmap.createBitmap(bitWidth, bitHeight, Config.ARGB_8888);
			Canvas iconCanvas = new Canvas(bgBit);
			bgNine.draw(iconCanvas);
		}
		return bgBit;
	}
}

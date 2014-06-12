package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import java.util.HashMap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
/**
 * 类描述: 消息中心的强制弹出框的Background的绘制
 * 功能详细描述: 根据内容的大小，为消息中心强制弹出框，绘制绘制不同大小的BackGround
 * @author  shenjinbao
 * @date  [2012-9-17]
 */
public class BackgroundView extends LinearLayout {

	public static int TOP_CORNER_LEFT = 0; // 左上角
	public static int TOP_CORNER_RIGHT = 1; // 右上角
	public static int TOP_SIDE_1 = 2; // 上边框蓝色
	public static int TOP_SIDE_2 = 3; // 上边框红色
	public static int LEFT_SIDE_1 = 4; // 左边蓝色
	public static int LEFT_SIDE_2 = 5; // 左边红色
	public static int CENTER_BG = 6; // 中间背景
	public static int RIGHT_SIDE_1 = 7; // 右边蓝色
	public static int RIGHT_SIDE_2 = 8; // 右边红色
	public static int BOTTOM_SIDE_1 = 9; // 下边蓝色
	public static int BOTTOM_SIDE_2 = 10; // 下边红色
	public static int BOTTOM_CORNER_LEFT = 11; // 左下角
	public static int BOTTOM_CORNER_RIGHT = 12; // 右下角

	private int[] mFourAngle = { TOP_CORNER_LEFT, TOP_CORNER_RIGHT, BOTTOM_CORNER_LEFT,
			BOTTOM_CORNER_RIGHT };
	private int[] mLeftMiddle = { LEFT_SIDE_1, LEFT_SIDE_2 };
	private int mCenter = CENTER_BG;
	private int[] mRightMiddle = { RIGHT_SIDE_1, RIGHT_SIDE_2 };
	private int[] mTopCenter = { TOP_SIDE_1, TOP_SIDE_2 };
	private int[] mButtomCenter = { BOTTOM_SIDE_1, BOTTOM_SIDE_2 };
	// 13张基本图片的资源
	private int[] mPicResources = { R.drawable.message_center_dialog_bg_top_left,
			R.drawable.message_center_dialog_bg_top_right,
			R.drawable.message_center_dialog_bg_top_center1,
			R.drawable.message_center_dialog_bg_top_center2,
			R.drawable.message_center_dialog_bg_left1, R.drawable.message_center_dialog_bg_left2,
			R.drawable.message_center_dialog_bg_center, R.drawable.message_center_dialog_bg_left1,
			R.drawable.message_center_dialog_bg_left2,
			R.drawable.message_center_dialog_bg_top_center1,
			R.drawable.message_center_dialog_bg_top_center2,
			R.drawable.message_center_dialog_bg_buttom_left,
			R.drawable.message_center_dialog_bg_buttom_right, };

	private BitmapDrawable mBg_Drawable;
	private int mMargin; // 左边偏移的大小
	private HashMap<String, Bitmap> mHashMap;   // 利用HashMap存放13张基本图片的Bitmap对象

	public BackgroundView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public BackgroundView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		int backgroundHeight = getMeasuredHeight();
		int backgroundWidth = getMeasuredWidth();
		if (backgroundHeight <= 0 || backgroundWidth <= 0) {
			return;
		}

		mBg_Drawable = initBgDrawable(backgroundWidth, backgroundHeight);
		LinearLayout.LayoutParams margin = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		margin.setMargins(mMargin, 0, 0, 0);

		if (mBg_Drawable != null) {
			this.setLayoutParams(margin);
			this.setBackgroundDrawable(mBg_Drawable);
		}
	}

	/*
	 * 创建背景图片
	 */
	public BitmapDrawable initBgDrawable(int bg_width, int bg_height) {

		if (mHashMap == null) {
			mHashMap = new HashMap();
			int len2 = mPicResources.length;
			Bitmap[] bitmaps = new Bitmap[len2];

			for (int i = 0; i < len2; i++) {
				bitmaps[i] = ((BitmapDrawable) getResources().getDrawable(mPicResources[i]))
						.getBitmap();
				if (i == RIGHT_SIDE_1 || i == RIGHT_SIDE_2 || i == BOTTOM_SIDE_1
						|| i == BOTTOM_SIDE_2) {
					bitmaps[i] = rotateBitmap(bitmaps[i]);
				}
				mHashMap.put(String.valueOf(i), bitmaps[i]);
			}
		}

		Resources resource = this.getResources();

		// 左右两个角 width和上面top中间的width
		int widthAngles = resource.getDrawable(R.drawable.message_center_dialog_bg_top_left)
				.getIntrinsicWidth()
				+ resource.getDrawable(R.drawable.message_center_dialog_bg_top_right)
						.getIntrinsicWidth();
		int widthTopCenter = resource.getDrawable(R.drawable.message_center_dialog_bg_top_center1)
				.getIntrinsicWidth();

		// 上下两个角height 和中间那些center的height
		int heightAngles = resource.getDrawable(R.drawable.message_center_dialog_bg_top_left)
				.getIntrinsicHeight()
				+ resource.getDrawable(R.drawable.message_center_dialog_bg_buttom_right)
						.getIntrinsicHeight();
		int heightMiddle = resource.getDrawable(R.drawable.message_center_dialog_bg_center)
				.getIntrinsicHeight();

		// 行列数
		int columns = (bg_width - widthAngles) / widthTopCenter + 2;
		int rows = (bg_height - heightAngles) / heightMiddle + 2;

		mMargin = (bg_width - widthAngles - (columns - 2) * widthTopCenter) / 2; // 两边的margin

		// 图片二维的drawable资源
		int[][] temp = new int[rows][columns];
		int arg = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				// 当时第一行或者最后一行的时候
				if (i == 0 || i == (rows - 1)) {
					if (j == 0 || j == (columns - 1)) { // 当时四个角的时候
						temp[i][j] = mFourAngle[arg];
						arg++;
					} else if (i == 0) { // 当是第一行的时候添加中间元素
						temp[i][j] = mTopCenter[(j + 1) % 2];
					} else {
						temp[i][j] = mButtomCenter[(j + 1) % 2];
					}
				} else { // 当不是第一行和最后一行的时候
					if (j == 0) { // 填充除角外的左边
						temp[i][j] = mLeftMiddle[(i + 1) % 2];
					} else if (j == columns - 1) { // 填充除角外的右边
						temp[i][j] = mRightMiddle[(i + 1) % 2];
					} else { // 填充中间空的部分
						temp[i][j] = mCenter;
					}
				}
			}
		}

		Bitmap[][] bg_bitmaps = new Bitmap[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				bg_bitmaps[i][j] = mHashMap.get(Integer.valueOf(temp[i][j]).toString());
			}
		}
		final int heigh = bg_height;
		final int width = bg_width;

		Bitmap bg_bitmap = null;
		bg_bitmap = MixBitmaps.combineBitmaps(columns, bg_bitmaps, width, heigh);

		if (null == bg_bitmap) {
			return null;
		}
		BitmapDrawable drawable = new BitmapDrawable(this.getResources(), bg_bitmap);

		return drawable;
	}

	/**
	 * 功能简述:  返回旋转180度的Bitmap
	 * 功能详细描述: 因为左边蓝色、红色与右边蓝色、红色的图形只是相差旋转180度 ，为了节省图片资源。
	 * 				同理，对于上边蓝色、红色和下边蓝色、红色。
	 * @param orgin
	 * @return
	 */
	private Bitmap rotateBitmap(Bitmap orgin) {
		Bitmap bmp = orgin.copy(Config.ARGB_8888, true);
		if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0) {
			return null;
		}
		Matrix matrix = new Matrix();
		matrix.postRotate(180);
		bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		return bmp;

	}
}

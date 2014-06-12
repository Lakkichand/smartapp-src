package com.jiubang.ggheart.apps.appfunc.common.component;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncDockContent;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
/**
 * 
 * <br>类描述:功能表底部操作栏通用组件
 * <br>功能详细描述:
 * 
 * @author  
 * @date  [2012-9-26]
 */
public class CommonActionBar extends AppFuncDockContent {

	/**
	 * 普通
	 */
	public final static int TYPE_SIMPLE = 0;
	/**
	 * 除左右两边最外组件处于最边缘，内面的组件平均分配空间
	 */
	public final static int TYPE_AVERAGE_INSIDE = 1;
	/**
	 * 所有组件平均分配空间
	 */
	public final static int TYPE_AVERAGE_ALL = 2;
	/**
	 * 靠左排列
	 */
	public final static int LAYOUT_GRAVITY_LEFT = 0;
	/**
	 * 靠右排列
	 */
	public final static int LAYOUT_GRAVITY_RIGHT = 1;
	/**
	 * 靠左排列向右拉伸，且actionBar类型必须是TYPE_SIMPLE
	 */
	public final static int LAYOUT_GRAVITY_LEFT_STRETCH = 2;
	/**
	 * 靠右排列向右拉伸，且actionBar类型必须是TYPE_SIMPLE
	 */
	public final static int LAYOUT_GRAVITY_RIGHT_STRETCH = 3;

	private int mType = TYPE_SIMPLE;
	private boolean mShowDivider;
	protected Drawable mDividerV;
	protected Drawable mDividerH;
	protected int mDividerSpace;
	protected Paint mDividerPaint = new Paint();
	private XComponent mStretchComponent;
	private ArrayList<XComponent> mLeftComponentList = new ArrayList<XComponent>();
	private ArrayList<XComponent> mRightComponentList = new ArrayList<XComponent>();
	private ArrayList<XComponent> mComponentList = new ArrayList<XComponent>();
	private int mItemPadding;

	public CommonActionBar(Context context, int tickCount, int x, int y, int width, int height,
			int hideType) {
		super(context, tickCount, x, y, width, height, hideType);
		setLayout(null);
	}

	public CommonActionBar(Context context, int tickCount, int x, int y, int width, int height,
			int type, int hideType) {
		this(context, tickCount, x, y, width, height, hideType);
		mType = type;
		setLayout(null);
	}

	@Override
	public void resetResource() {
		mDividerSpace = 0;
		mDividerV = null;
		mDividerH = null;
	}

	@Override
	public void loadResource(String packageName) {
		mDividerSpace = mUtils.getStandardSize(2);
		mDividerV = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mHomeBean.mHomeDeliverLineV,
				packageName);
		mDividerH = mThemeCtrl.getDrawable(mThemeCtrl.getThemeBean().mHomeBean.mHomeDeliverLineH,
				packageName);
	}

	@Override
	public synchronized void addComponent(XComponent component) throws IllegalArgumentException {
		addComponent(component, LAYOUT_GRAVITY_LEFT);
	}

	public synchronized void addComponent(XComponent component, int layoutGravity)
			throws IllegalArgumentException {
		if (indexOfComponent(component) < 0) {
			if (layoutGravity == LAYOUT_GRAVITY_LEFT_STRETCH
					|| layoutGravity == LAYOUT_GRAVITY_RIGHT_STRETCH) {
				if (mStretchComponent != null) {
					throw new IllegalArgumentException(
							"Only one stretch component can be in action bar");
				} else {
					mStretchComponent = component;
					super.addComponent(component);
					if (layoutGravity == LAYOUT_GRAVITY_LEFT_STRETCH) {
						mLeftComponentList.add(component);
					} else if (layoutGravity == LAYOUT_GRAVITY_RIGHT_STRETCH) {
						mRightComponentList.add(component);
					}
					return;
				}
			}
			super.addComponent(component);
			if (layoutGravity == LAYOUT_GRAVITY_LEFT) {
				mLeftComponentList.add(component);
			} else if (layoutGravity == LAYOUT_GRAVITY_RIGHT) {
				mRightComponentList.add(component);
			}
		}
	}

	@Override
	public synchronized void removeComponent(XComponent component) throws IllegalArgumentException {
		super.removeComponent(component);
		if (mLeftComponentList.contains(component)) {
			mLeftComponentList.remove(component);
		} else if (mRightComponentList.contains(component)) {
			mRightComponentList.remove(component);
		}
		if (mStretchComponent == component) {
			mStretchComponent = null;
		}
	}

	@Override
	public synchronized void removeAllComponent() throws IllegalArgumentException {
		super.removeAllComponent();
		mLeftComponentList.clear();
		mRightComponentList.clear();
		mComponentList.clear();
		mStretchComponent = null;
		mItemPadding = 0;
	}

	public boolean isStretch(XComponent component) {
		return mStretchComponent == component;
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		super.layout(left, top, right, bottom);
		mItemPadding = 0;
		mComponentList.clear();
		switch (mType) {
			case TYPE_SIMPLE :
				layoutSimpleType();
				break;
			case TYPE_AVERAGE_INSIDE :
			case TYPE_AVERAGE_ALL :
				layoutAverageType();
				break;
			default :
				break;
		}
	}

	private int computeStretchComponentSpace() {
		int dividerSpace = 0;
		if (mShowDivider && mDividerV != null) {
			dividerSpace = mDividerSpace;
		}
		int space = 0;
		if (GoLauncher.isPortait()) {
			space = mWidth - mPaddingLeft - mPaddingRight;
			for (int i = 0; i < mLeftComponentList.size(); i++) {
				XComponent component = mLeftComponentList.get(i);
				int width = component.getWidth();
				if (component != mStretchComponent) {
					space -= width;
				}
			}
			for (int i = 0; i < mRightComponentList.size(); i++) {
				XComponent component = mRightComponentList.get(i);
				int width = component.getWidth();
				if (component != mStretchComponent) {
					space -= width;
				}
			}
		} else {
			space = mHeight - mPaddingTop - mPaddingBottom;
			for (int i = 0; i < mLeftComponentList.size(); i++) {
				XComponent component = mLeftComponentList.get(i);
				int height = component.getHeight();
				if (component != mStretchComponent) {
					space -= height;
				}
			}
			for (int i = 0; i < mRightComponentList.size(); i++) {
				XComponent component = mRightComponentList.get(i);
				int height = component.getHeight();
				if (component != mStretchComponent) {
					space -= height;
				}
			}
		}
		return space -= dividerSpace * (mLeftComponentList.size() + mRightComponentList.size() - 1);
	}

	private void layoutSimpleType() {
		int dividerSpace = 0;
		if (GoLauncher.isPortait()) {
			if (mShowDivider && mDividerV != null) {
				dividerSpace = mDividerSpace;
			}
			int startLeft = mPaddingLeft;
			for (int i = 0; i < mLeftComponentList.size(); i++) {
				XComponent component = mLeftComponentList.get(i);
				int width = 0;
				if (component != mStretchComponent) {
					width = component.getWidth();
				} else {
					width = computeStretchComponentSpace();
					component.setSize(width,
							component.getHeight() == 0 ? mHeight : component.getHeight());
				}
				int top = computeTop(component);
				component.setXY(startLeft, top);
				if (component == mStretchComponent) {
					layoutStretchComponent(component);
				}
				startLeft += width + dividerSpace;
			}
			int startRight = mWidth - mPaddingRight;
			for (int i = 0; i < mRightComponentList.size(); i++) {
				XComponent component = mRightComponentList.get(i);
				int width = 0;
				if (component != mStretchComponent) {
					width = component.getWidth();
				} else {
					width = computeStretchComponentSpace();
					component.setSize(width,
							component.getHeight() == 0 ? mHeight : component.getHeight());
				}
				int top = computeTop(component);
				component.setXY(startRight - width, top);
				if (component == mStretchComponent) {
					layoutStretchComponent(component);
				}
				startRight -= width + dividerSpace;
			}
		} else {
			if (mShowDivider && mDividerH != null) {
				dividerSpace = mDividerSpace;
			}
			int startTop = mPaddingTop;
			for (int i = 0; i < mRightComponentList.size(); i++) {
				XComponent component = mRightComponentList.get(i);
				int height = 0;
				if (component != mStretchComponent) {
					height = component.getHeight();
				} else {
					height = computeStretchComponentSpace();
					component.setSize(component.getWidth() == 0 ? mWidth : component.getWidth(),
							height);
				}
				int left = computeLeft(component);
				component.setXY(left, startTop);
				if (component == mStretchComponent) {
					layoutStretchComponent(component);
				}
				startTop += height + dividerSpace;
			}
			int startBottom = mHeight - mPaddingBottom;
			for (int i = 0; i < mLeftComponentList.size(); i++) {
				XComponent component = mLeftComponentList.get(i);
				int height = 0;
				if (component != mStretchComponent) {
					height = component.getHeight();
				} else {
					height = computeStretchComponentSpace();
					component.setSize(component.getWidth() == 0 ? mWidth : component.getWidth(),
							height);
				}
				int left = computeLeft(component);
				component.setXY(left, startBottom - height);
				if (component == mStretchComponent) {
					layoutStretchComponent(component);
				}
				startBottom -= height + dividerSpace;
			}
		}
	}

	private void layoutStretchComponent(XComponent component) {
		component.layout(component.mX, component.mY, component.mX + component.getWidth(),
				component.mY + component.getHeight());
	}

	private int computeLeft(XComponent component) {
		int left = Math.round(mPaddingLeft
				+ (mWidth - mPaddingLeft - mPaddingRight - component.getWidth()) / 2.0f);
		return left;
	}

	private int computeTop(XComponent component) {
		int top = Math.round(mPaddingTop
				+ (mHeight - mPaddingTop - mPaddingBottom - component.getHeight()) / 2.0f);
		return top;
	}

	private void layoutAverageType() {
		mComponentList = new ArrayList<XComponent>();
		mComponentList.addAll(mLeftComponentList);
		mComponentList.addAll(mRightComponentList);
		int dividerSpace = 0;
		int size = mComponentList.size();
		if (GoLauncher.isPortait()) {
			if (mShowDivider && mDividerV != null) {
				dividerSpace = mDividerSpace;
			}
			int displayAreaSize = mWidth - mPaddingLeft - mPaddingRight;
			int spaceSize = displayAreaSize;
			for (int i = 0; i < size; i++) {
				XComponent component = mComponentList.get(i);
				spaceSize -= component.getWidth();
			}
			spaceSize -= dividerSpace * (size - 1);
			if (mType == TYPE_AVERAGE_INSIDE) {
				int avg = size - 1;
				if (avg < 1) {
					avg = 1;
				}
				mItemPadding = spaceSize / avg;
			} else {
				mItemPadding = spaceSize / (size + 1);
			}
			int startLeft = mPaddingLeft;
			if (mType == TYPE_AVERAGE_ALL) {
				startLeft += mItemPadding;
			}
			int startRight = mWidth - mPaddingRight;
			if (mType == TYPE_AVERAGE_ALL) {
				startRight -= mItemPadding;
			}
			for (int i = 0; i < size; i++) {
				XComponent component = mComponentList.get(i);
				int width = component.getWidth();
				int top = computeTop(component);
				if (mLeftComponentList.contains(component)) {
					component.setXY(startLeft, top);
					startLeft += width + mItemPadding + dividerSpace;
				} else if (mRightComponentList.contains(component)) {
					component.setXY(startRight - width, top);
					startRight -= width + mItemPadding + dividerSpace;
				}
				layoutStretchComponent(component);
			}
		} else {
			if (mShowDivider && mDividerH != null) {
				dividerSpace = mDividerSpace;
			}
			int displayAreaSize = mHeight - mPaddingTop - mPaddingBottom;
			int spaceSize = displayAreaSize;
			for (int i = 0; i < size; i++) {
				XComponent component = mComponentList.get(i);
				spaceSize -= component.getHeight();
			}
			if (mType == TYPE_AVERAGE_INSIDE) {
				int avg = size - 1;
				if (avg < 1) {
					avg = 1;
				}
				mItemPadding = spaceSize / avg;
			} else {
				mItemPadding = spaceSize / (size + 1);
			}
			int startTop = mPaddingTop;
			if (mType == TYPE_AVERAGE_ALL) {
				startTop += mItemPadding;
			}
			int startBottom = mHeight - mPaddingBottom;
			if (mType == TYPE_AVERAGE_ALL) {
				startBottom -= mItemPadding;
			}
			for (int i = 0; i < size; i++) {
				XComponent component = mComponentList.get(i);
				int height = component.getHeight();
				int left = computeLeft(component);
				if (mRightComponentList.contains(component)) {
					component.setXY(left, startTop);
					startTop += height + mItemPadding + dividerSpace;
				} else if (mLeftComponentList.contains(component)) {
					component.setXY(left, startBottom - height);
					startBottom -= height + mItemPadding + dividerSpace;
				}
				layoutStretchComponent(component);
			}
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		super.drawCurrentFrame(canvas);
		if (mShowDivider) {
			if (GoLauncher.isPortait() && mDividerV != null) {
				if (mType == TYPE_SIMPLE) {
					for (int i = 0; i < mLeftComponentList.size(); i++) {
						XComponent component = mLeftComponentList.get(i);
						int pos = component.mX + component.getWidth();
						if (i == mLeftComponentList.size() - 1) {
							if (component == mStretchComponent && !mRightComponentList.isEmpty()) {
								ImageUtil.drawImage(canvas, mDividerV, ImageUtil.CENTERMODE, pos,
										mPaddingTop, pos + mDividerSpace, mHeight - mPaddingBottom,
										mDividerPaint);
							}
						} else {
							ImageUtil.drawImage(canvas, mDividerV, ImageUtil.CENTERMODE, pos,
									mPaddingTop, pos + mDividerSpace, mHeight - mPaddingBottom,
									mDividerPaint);
						}
					}
					for (int i = 0; i < mRightComponentList.size() - 1; i++) {
						XComponent component = mRightComponentList.get(i);
						int pos = component.mX - mDividerSpace;
						if (i == mRightComponentList.size() - 1) {
							if (component == mStretchComponent && !mLeftComponentList.isEmpty()) {
								ImageUtil.drawImage(canvas, mDividerV, ImageUtil.CENTERMODE, pos,
										mPaddingTop, pos + mDividerSpace, mHeight - mPaddingBottom,
										mDividerPaint);
							}
						} else {
							ImageUtil.drawImage(canvas, mDividerV, ImageUtil.CENTERMODE, pos,
									mPaddingTop, pos + mDividerSpace, mHeight - mPaddingBottom,
									mDividerPaint);
						}
					}
				} else if (mType == TYPE_AVERAGE_INSIDE || mType == TYPE_AVERAGE_ALL) {
					int size = mComponentList.size();
					for (int i = 1; i < size; i++) {
						XComponent component = mComponentList.get(i);
						int pos = component.mX - mItemPadding / 2 - mDividerSpace;
						ImageUtil.drawImage(canvas, mDividerV, ImageUtil.CENTERMODE, pos,
								mPaddingTop, pos + mDividerSpace, mHeight - mPaddingBottom,
								mDividerPaint);
					}
				}
			} else if (!GoLauncher.isPortait() && mDividerH != null) {
				if (mType == TYPE_SIMPLE) {
					for (int i = 0; i < mRightComponentList.size(); i++) {
						XComponent component = mRightComponentList.get(i);
						int pos = component.mY + component.getHeight();
						if (i == mRightComponentList.size() - 1) {
							if (component == mStretchComponent && !mLeftComponentList.isEmpty()) {
								ImageUtil.drawImage(canvas, mDividerH, ImageUtil.CENTERMODE,
										mPaddingLeft, pos, mWidth - mPaddingRight, pos
												+ mDividerSpace, mDividerPaint);
							}
						} else {
							ImageUtil.drawImage(canvas, mDividerH, ImageUtil.CENTERMODE,
									mPaddingLeft, pos, mWidth - mPaddingRight, pos + mDividerSpace,
									mDividerPaint);
						}
					}
					for (int i = 0; i < mLeftComponentList.size(); i++) {
						XComponent component = mLeftComponentList.get(i);
						int pos = component.mY - mDividerSpace;
						if (i == mLeftComponentList.size() - 1) {
							if (component == mStretchComponent && !mRightComponentList.isEmpty()) {
								ImageUtil.drawImage(canvas, mDividerH, ImageUtil.CENTERMODE,
										mPaddingLeft, pos, mWidth - mPaddingRight, pos
												+ mDividerSpace, mDividerPaint);
							}
						} else {
							ImageUtil.drawImage(canvas, mDividerH, ImageUtil.CENTERMODE,
									mPaddingLeft, pos, mWidth - mPaddingRight, pos + mDividerSpace,
									mDividerPaint);
						}
					}

				} else if (mType == TYPE_AVERAGE_INSIDE || mType == TYPE_AVERAGE_ALL) {
					int size = mComponentList.size();
					for (int i = 1; i < size; i++) {
						XComponent component = mComponentList.get(i);
						int pos = component.mY + component.getHeight() + mItemPadding / 2;
						ImageUtil.drawImage(canvas, mDividerH, ImageUtil.CENTERMODE, mPaddingLeft,
								pos, mWidth - mPaddingRight, pos + mDividerSpace, mDividerPaint);
					}
				}
			}
		}
	}

	public void setType(int type) {
		mType = type;
	}

	public void showDivider(boolean show) {
		mShowDivider = show;
	}

	// public void onOneIconSelected(boolean selected) {
	//
	// }
	//
	// public void onAllIconsSelected(boolean selected) {
	//
	// }
	/**
	 * 当图标选择数量发生改变时被调用
	 * 
	 * @param selectedCount
	 *            选中的图标数量
	 * @param totalCount
	 *            图标总数
	 */
	public void onIconSelectedChanged(int selectedCount, int totalCount) {

	}
}

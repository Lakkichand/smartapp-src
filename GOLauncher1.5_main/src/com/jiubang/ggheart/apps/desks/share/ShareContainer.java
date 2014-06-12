package com.jiubang.ggheart.apps.desks.share;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;

/***
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  maxiaojun
 * @date  [2012-9-14]
 */
public class ShareContainer extends ViewGroup implements OnClickListener {
	private ArrayList<ShareItem> mItemList = new ArrayList<ShareItem>(); // 数据信息

	private Bitmap mUnSelImg; // 单选框未选中图片
	private Bitmap mSelImg; // 单选框选中图片
	private NinePatchDrawable mBgNine; // 子项的背景图片

	private int mScreenHight; // 屏幕高度
	private int mScreenWidth; // 屏幕宽度
	private int mItemWidth; // Item宽度
	private int mItemHeight; // Item高度
	private int mItemCountV; // 每行Item的数量
	private int mItemImgW; // 内部图片的宽度
	private int mItemImgH; // 内部图片的高度
	private Rect mItemImgEdge = new Rect(); // 图片与相框左右边缘的距离
	private int mCheckboxOffset; // 多选按钮与相框偏移的距离
	private int mEdgePadding; // 图片离屏幕2边的距离
	private int mEachotherPadding; // 图片之间的距离
	private int mLineSpace; // 行距
	private int mEachotherPadding_min; // 图片之间最小距离

	private static final int REFRESH_CHILD_UI = 0; // 刷新子视图UI
	private static final int CREATE_CHILD_IMG = 1; // 创建子视图图片

	private String mChooseMaxTip;
	private String mNoWallpaperTip;
	private Button mShareButton; // 分享按钮

	private int maxCount = ShareLayout.MAX_COUNT_HIGHT;

	private int mType = ShareFrame.TYPE_SHARE;
	/***
	 * 构造方法
	 * 
	 * @param context
	 * @param attrs
	 */
	public ShareContainer(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	/***
	 * 初始化资源
	 */
	private void initRes() {
		final Resources resource = getResources();
		mSelImg = ((BitmapDrawable) resource.getDrawable(R.drawable.apps_uninstall_selected))
				.getBitmap();
		mUnSelImg = ((BitmapDrawable) resource.getDrawable(R.drawable.apps_uninstall_not_selected))
				.getBitmap();
		mBgNine = (NinePatchDrawable) resource.getDrawable(R.drawable.theme_item_bg);

		// 取屏幕高宽
		mScreenWidth = GoLauncher.getDisplayWidth();
		mScreenHight = GoLauncher.getDisplayHeight();

		if (mScreenWidth > mScreenHight) {
			mItemWidth = resource.getDimensionPixelSize(R.dimen.share_item_width_land);
		} else {
			mItemWidth = resource.getDimensionPixelSize(R.dimen.share_item_width_port);
		}

		mCheckboxOffset = resource.getDimensionPixelSize(R.dimen.share_item_checkbox_offset);
		mEdgePadding = resource.getDimensionPixelSize(R.dimen.share_list_item_padding_edge);
		mLineSpace = resource.getDimensionPixelSize(R.dimen.share_list_line_space);
		mEachotherPadding_min = resource
				.getDimensionPixelSize(R.dimen.share_list_item_padding_eachother);

		if (maxCount == ShareLayout.MAX_COUNT_HIGHT) {
			mChooseMaxTip = resource.getString(R.string.share_text_choose_max_tip);
		} else {
			mChooseMaxTip = resource.getString(R.string.share_text_choose_max_tip_low);
		}
		mNoWallpaperTip = resource.getString(R.string.share_img_no_wallpaper);
	}

	/**
	 * 计算坐标信息及每行显示的主题个数
	 */
	public void calculateListItemCount() {
		mBgNine.getPadding(mItemImgEdge);
		mItemImgW = mItemWidth - mCheckboxOffset - mItemImgEdge.left - mItemImgEdge.right;
		mItemImgH = mScreenHight * mItemImgW / mScreenWidth;
		mItemHeight = mItemImgH + mCheckboxOffset + mItemImgEdge.top + mItemImgEdge.bottom;
		mItemCountV = Math.round((mScreenWidth - 2 * mEdgePadding + mEachotherPadding_min)
				/ ((mItemWidth + mEachotherPadding_min) * 1.0f));

		int padding = (mScreenWidth - mItemCountV * mItemWidth) / (mItemCountV + 1);
		mEachotherPadding = padding;
		mEdgePadding = padding;
	}

	/***
	 * 是否能拿到壁纸
	 * 
	 * @param index
	 */
	private boolean checkWallpaper() {
		return GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.CAN_GET_WALLPAPER, -1, null, null);
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	/***
	 * 初始化数据
	 * 
	 * @param count
	 *            屏幕数量
	 */
	public void initData(int count, int type) {
		mType = type;
		// 初始化图片资源
		try {
			initRes();
			calculateListItemCount();
			android.widget.AbsListView.LayoutParams layoutParams = new android.widget.AbsListView.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			mItemList.clear();
			for (int i = 0; i < count; i++) {
				ShareItem item = new ShareItem(false, null, i, mItemWidth,
						mItemHeight, mSelImg, mUnSelImg, mBgNine, mItemImgW, mItemImgH);
				mItemList.add(item);
				View child = new ShareItemView(getContext());
				layoutParams.width = mItemWidth;
				layoutParams.height = mItemHeight;
				child.setLayoutParams(layoutParams);
				child.setOnClickListener(ShareContainer.this);
				((ShareItemView) child).initData(item, mCheckboxOffset);
				addView(child);
			}
		} catch (OutOfMemoryError error) {
			OutOfMemoryHandler.handle();
		}
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		int size = mItemList.size();
		int lineNum = size % mItemCountV == 0 ? size / mItemCountV : size / mItemCountV + 1;
		params.width = mScreenWidth;
		params.height = (mItemHeight + mLineSpace) * lineNum;
		setLayoutParams(params);
		invalidate();
		if (!checkWallpaper()) {
			Toast.makeText(getContext(), mNoWallpaperTip, 200).show();
		}

		AsyncLoadImage load = new AsyncLoadImage();
		load.execute();
	}

	/***
	 * <br>功能简述: 获取当前主页面索引
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private int getMainScreenIndex() {
		Bundle bundle = new Bundle();
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.GET_MAIN_SCREEN_INDEX,
				-1, bundle, null);
		return bundle.getInt("mainscreen");
	}

	/**
	 * 异步任务，加载页面图片
	 */
	private class AsyncLoadImage extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			int size = mItemList.size();
			for (int i = 0; i < size; i++) {
				Message msg = new Message();
				msg.arg1 = i;
				msg.what = CREATE_CHILD_IMG;
				mHandler.sendMessage(msg);
			}
			return null;
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case REFRESH_CHILD_UI : {
					getChildAt(msg.arg1).invalidate();
					break;
				}
				case CREATE_CHILD_IMG : {
					loadItemImage(msg.arg1);
					break;
				}
			}
		}
	};

	/***
	 * 生成指定页面的截图
	 * 
	 * @param index
	 */
	private void loadItemImage(int index) {
		ArrayList<Float> list = new ArrayList<Float>(2);
		list.clear();
		list.add((float) index);
		list.add((float) (1.0 * mItemImgW / GoLauncher.getDisplayWidth()));
		Bitmap bitmap = null;
		
		Bundle bundle = new Bundle();
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.GET_SHARE_IMAGE, -1,
				bundle, list);

		bitmap = bundle.getParcelable("image");
		
		ShareItem item = mItemList.get(index);
		if (bitmap != null && item != null) {
			item.setBitmap(bitmap);
			Message msg = new Message();
			msg.arg1 = index;
			msg.what = REFRESH_CHILD_UI;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mItemCountV < 1) {
			return;
		}
		int count = this.mItemList.size();
		int left;
		int top;
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			left = mEdgePadding + (mItemWidth + mEachotherPadding) * (i % mItemCountV);
			top = (mItemHeight + mLineSpace) * (i / mItemCountV);
			child.layout(left, top, left + mItemWidth, top + mItemHeight);
		}
	}

	/***
	 * 获取选中的图片索引列表
	 * 
	 * @return
	 */
	public ArrayList<Integer> getSelList() {

		ShareItem item;
		ArrayList<Integer> selList = new ArrayList<Integer>(3);
		if (mItemList == null) {
			return selList;
		}
		int size = mItemList.size();
		for (int i = 0; i < size; i++) {
			item = mItemList.get(i);
			if (item.isSelect) {
				selList.add(i);
			}
		}
		return selList;
	}

	/***
	 * 设置分享按钮设
	 * 
	 * @param shareButton
	 */
	public void setShareButton(Button shareButton) {
		mShareButton = shareButton;
	}

	@Override
	public void onClick(View v) {
		if (null != v && v instanceof ShareItemView) {
			ShareItem item = ((ShareItemView) v).getShareItem();
			int selNum = getSelectNum();
			if (selNum == 0) { // 分享按钮变亮
				item.setIsSelect(true);
				setButtonClickable(true);
			} else if (selNum == 1 && item.isSelect) { // 分享按钮变暗
				item.setIsSelect(false);
				setButtonClickable(false);
			} else { // 其他情况
				if (!item.isSelect && isMoreMax()) { // 不能再选了
					Toast.makeText(getContext(), mChooseMaxTip, 500).show();
				} else {
					item.setIsSelect(!item.isSelect);
				}
			}
			v.invalidate();
		}
	}

	/***
	 * 设置分享按钮是否可点击
	 * 
	 * @param clickable
	 */
	private void setButtonClickable(boolean clickable) {
		if (clickable) {
			mShareButton.setCompoundDrawables(null, null, null, null);
			mShareButton.setTextColor(0xff6BB24B);
		} else {
			mShareButton.setCompoundDrawables(null, null, null, null);
			mShareButton.setTextColor(0xffaaaaaa);
		}
		mShareButton.setClickable(clickable);
	}

	/***
	 * 检查选中项数是否超过了限制的数量
	 * 
	 * @return
	 */
	private boolean isMoreMax() {
		int size = mItemList.size();
		if (size <= maxCount) {
			return false;
		}
		ShareItem item;
		int count = 0;
		for (int i = 0; i < size; i++) {
			item = mItemList.get(i);
			if (item.isSelect) {
				count++;
			}
			if (count >= maxCount) {
				return true;
			}
		}
		return false;
	}

	/***
	 * 检查选中项数
	 * 
	 * @return
	 */
	private int getSelectNum() {
		int size = mItemList.size();
		ShareItem item;
		int count = 0;
		for (int i = 0; i < size; i++) {
			item = mItemList.get(i);
			if (item.isSelect) {
				count++;
			}
		}
		return count;
	}

	/***
	 * 清理资源
	 */
	public void clear() {
		if (mItemList != null) {
			for (int i = 0; i < mItemList.size(); i++) {
				ShareItem item = mItemList.get(i);
				if (null != item) {
					item.clear();
					item = null;
				}
			}
		}
		if (null != mUnSelImg) {
			mUnSelImg.recycle();
			mUnSelImg = null;
		}
		if (null != mSelImg) {
			mSelImg.recycle();
			mSelImg = null;
		}
		if (null != mBgNine) {
			mBgNine = null;
		}
		if (null != mShareButton) {
			mShareButton = null;
		}
	}

}

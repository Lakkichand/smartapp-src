package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.components.DeskIcon;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;

public class FolderSelectItemView extends FrameLayout implements BroadCasterObserver {

	private ImageView mChoiceImg;
	private DeskIcon mLabel;
	private Drawable mChoiceDrawable;
	public int mPosition;
	private ScreenModifyFolderActivity mActivity;

	public FolderSelectItemView(Context context) {
		super(context, null);
		// TODO Auto-generated constructor stub
	}

	public FolderSelectItemView(Context context, AttributeSet att) {
		super(context, att);
		mActivity = (ScreenModifyFolderActivity) context;
		mChoiceDrawable = context.getResources().getDrawable(R.drawable.theme_button_apply);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		// mLabel.layout(left, top, right, bottom);
		// int w = mChoiceDrawable.getIntrinsicWidth();
		// int h = mChoiceDrawable.getIntrinsicHeight();
		// int t = bottom - 2*h;
		// int l = bottom - 2*w;
		// mChoiceImg.layout(l, t, l+w, t+h);
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mChoiceImg = (ImageView) findViewById(R.id.choice);
		mLabel = (DeskIcon) findViewById(R.id.name);
		mChoiceImg.setImageDrawable(mChoiceDrawable);
	}

	public void setIcon(Drawable icon) {
		mLabel.setIcon(icon);
	}

	public void setText(String title) {
		mLabel.setText(title);
	}

	public void setText(CharSequence title) {
		mLabel.setText(title);
	}

	public void setChoiceDrawable(Drawable drawable) {
		// mChoiceImg.setImageDrawable(drawable);
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		// TODO Auto-generated method stub
		switch (msgId) {
			case AppItemInfo.INCONCHANGE : {
				try {
					if (null != mActivity.getAllList()) {
						if (mActivity.getAllList().get(mPosition) instanceof ItemInfo) {
							Drawable drawable = ((ShortCutInfo) mActivity.getAllList().get(
									mPosition)).mIcon;
							mLabel.setIcon(drawable);
							// title.setCompoundDrawablesWithIntrinsicBounds(drawable,
							// null, null, null);
						} else if (mActivity.getAllList().get(mPosition) instanceof AppItemInfo) {
							BitmapDrawable drawable = ((AppItemInfo) mActivity.getAllList().get(
									mPosition)).mIcon;
							mLabel.setIcon(drawable);
							// title.setCompoundDrawablesWithIntrinsicBounds(drawable,
							// null, null, null);
						}
					}
				} catch (IndexOutOfBoundsException e) {
					e.printStackTrace();
				} catch (NullPointerException e) {
					e.printStackTrace();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}
				break;
			default :
				break;
		}
	}

}

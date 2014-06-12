package com.jiubang.ggheart.plugin.mediamanagement;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 选择对话框列表每列所对应的item
 * @author yangguanxiang
 *
 */
public class ChooserListViewIcon extends FrameLayout {

	private ImageView mIcon = null;
	private TextView mTitle = null;

	public ChooserListViewIcon(Context context) {
		super(context);
		initViews();
	}

	public ChooserListViewIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		initViews();
	}

	public ChooserListViewIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initViews();
	}

	private void initViews() {

		LayoutInflater li = LayoutInflater.from(getContext());
		li.inflate(R.layout.appfunc_mediamanagement_chooser_listview_item, this, true);
		mIcon = (ImageView) findViewById(R.id.appfunc_mediamanagement_shooser_list_item_icon);
		mTitle = (TextView) findViewById(R.id.appfunc_mediamanagement_shooser_list_item_title);
	}

	/**
	 * 设置item的图标
	 * @param icon
	 */
	public void setIcon(Drawable icon) {
		mIcon.setImageDrawable(icon);
	}

	/**
	 * 设置item的文字
	 * @param title
	 */
	public void setTitle(String title) {
		mTitle.setText(title);
	}
}

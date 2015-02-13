package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.TopicDataBean;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 搜索专题View
 * 
 * @author zhaoyl
 * 
 */
public class SearchTopicView extends LinearLayout implements OnClickListener {

	private ImageView imgAvator;// 图片
	private TextView tvName; // 名称
	private TextView tvTotal; // 应用总数
	private TextView tvIntro; // 专题简介
	private LinearLayout btnButton;
	private TopicDataBean bean;

	public SearchTopicView(Context context) {
		super(context);
	}

	public SearchTopicView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		imgAvator = (ImageView) findViewById(R.id.search_topic_img);
		tvName = (TextView) findViewById(R.id.search_topic_name);
		tvTotal = (TextView) findViewById(R.id.search_topic_total);
		tvIntro = (TextView) findViewById(R.id.search_topic_intro);
		btnButton = (LinearLayout) findViewById(R.id.button);
		btnButton.setOnClickListener(this);
	}

	public void setView(TopicDataBean bean) {
		this.bean = bean;
		tvName.setText(bean.title);
		tvTotal.setText(bean.message + "款应用");
		tvIntro.setText(bean.description);

		imgAvator.setTag(bean.bannerUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, bean.bannerUrl.hashCode() + "",
				bean.bannerUrl, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						if (imgAvator.getTag().equals(imgUrl)) {
							imgAvator.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm != null) {
			imgAvator.setImageBitmap(bm);
		} else {
			// 默认
			imgAvator.setImageBitmap(DrawUtil.sTopicDefaultBanner);
		}

	}

	/**
	 * 可见
	 */
	public void viewVisible() {
		imgAvator.setVisibility(View.VISIBLE);
		tvName.setVisibility(View.VISIBLE);
		tvTotal.setVisibility(View.VISIBLE);
		tvIntro.setVisibility(View.VISIBLE);
		btnButton.setVisibility(View.VISIBLE);
		findViewById(R.id.view1).setVisibility(View.VISIBLE);
		findViewById(R.id.view2).setVisibility(View.VISIBLE);
		findViewById(R.id.view3).setVisibility(View.VISIBLE);
		findViewById(R.id.view4).setVisibility(View.VISIBLE);
		findViewById(R.id.view5).setVisibility(View.VISIBLE);
		this.setVisibility(View.VISIBLE);
	}

	/**
	 * 不可见
	 */
	public void viewGone() {
		imgAvator.setVisibility(View.GONE);
		tvName.setVisibility(View.GONE);
		tvTotal.setVisibility(View.GONE);
		tvIntro.setVisibility(View.GONE);
		btnButton.setVisibility(View.GONE);
		findViewById(R.id.view1).setVisibility(View.GONE);
		findViewById(R.id.view2).setVisibility(View.GONE);
		findViewById(R.id.view3).setVisibility(View.GONE);
		findViewById(R.id.view4).setVisibility(View.GONE);
		findViewById(R.id.view5).setVisibility(View.GONE);
		this.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button:
			if (bean != null) {
				List<Object> list = new ArrayList<Object>();
				list.add(bean);
				// 通知TabManageView跳转下一层级，把TopicDataBean带过去
				TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
						CDataDownloader.getTopicContentUrl(bean.id, 1), list);
				// 移除搜索View
				TAApplication.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
						IDiyMsgIds.REMOVE_SEARCHVIEW, -1, null, list);
			}

			break;

		default:
			break;
		}
	}

}

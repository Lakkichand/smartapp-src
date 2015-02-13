package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.ActivitActivity;
import com.zhidian.wifibox.activity.HTMLGameActivity;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.TopicDataBean;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 首页推荐导航条
 * 
 * @author xiedezhi
 * 
 */
public class HomeFeatureNavigation extends LinearLayout {

	private int mGap = DrawUtil.dip2px(TAApplication.getApplication(), 5);
	private InfoUtil infoUtil;

	private Button mH5Btn = null;

	/**
	 * 点击事件
	 */
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int type = (Integer) v.getTag(R.id.about_us_tv);
			if (type == 1) {// 专题
				String idStr = (String) v.getTag();
				long id = Long.valueOf(idStr);
				String title = (String) v.getTag(R.id.app);
				TopicDataBean bean = new TopicDataBean();
				bean.id = id;
				bean.title = title;
				// 跳转到专题内容
				List<Object> list = new ArrayList<Object>();
				list.add(bean);
				// 通知TabManageView跳转下一层级，把TopicDataBean带过去
				TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
						CDataDownloader.getTopicContentUrl(bean.id, 1), list);
			} else if (type == 2) {
				// 活动
				String ActivitUrl = (String) v.getTag();
				String title = (String) v.getTag(R.id.back);
				Intent intent = new Intent();
				intent.setClass(getContext(), ActivitActivity.class);
				intent.putExtra(ActivitActivity.TITLE, title);
				intent.putExtra(ActivitActivity.URL, ActivitUrl);
				getContext().startActivity(intent);
			} else if (type == 3) {
				// HTML游戏
				String gameUrl = (String) v.getTag();
				Intent intent = new Intent(getContext(), HTMLGameActivity.class);
				intent.putExtra(HTMLGameActivity.GAMEURLKEY, gameUrl);
				getContext().startActivity(intent);
			}

		}
	};

	public HomeFeatureNavigation(Context context) {
		super(context);
		init();
	}

	public HomeFeatureNavigation(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		infoUtil = new InfoUtil(getContext());
		setOrientation(HORIZONTAL);
		setPadding(mGap, mGap, mGap, mGap);
	}

	public void init(List<String> labels, List<String> ids,
			List<Integer> backgrounds, List<Integer> types, List<String> titles) {
		removeAllViews();
		if (labels == null || labels.size() <= 0 || ids == null
				|| ids.size() <= 0 || backgrounds == null
				|| backgrounds.size() <= 0 || labels.size() != ids.size()
				|| ids.size() != backgrounds.size()) {
			setVisibility(View.GONE);
		}
		setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
				DrawUtil.dip2px(getContext(), 38));
		lp.leftMargin = lp.rightMargin = mGap;
		lp.weight = 1.0f;
		for (int i = 0; i < labels.size(); i++) {
			Button btn = new Button(getContext());
			btn.setSingleLine();
			btn.setText(labels.get(i));
			btn.setTag(ids.get(i));
			btn.setTag(R.id.app, labels.get(i));
			btn.setTag(R.id.about_us_tv, types.get(i));
			btn.setTag(R.id.back, titles.get(i));
			btn.setBackgroundResource(backgrounds.get(i));
			btn.setTextColor(Color.WHITE);
			if (infoUtil.getHeight() > 900) {
				btn.setTextSize(16);
			} else {
				btn.setTextSize(14);
			}

			btn.setOnClickListener(mClickListener);
			btn.setPadding(DrawUtil.dip2px(getContext(), 5), 0,
					DrawUtil.dip2px(getContext(), 5), 0);
			btn.getPaint().setFakeBoldText(true);
			addView(btn, lp);
			if (types.get(i) == 3) {
				mH5Btn = btn;
			}
		}
	}

	/**
	 * 获取HTML游戏标签按钮
	 */
	public Button getH5Btn() {
		return mH5Btn;
	}
}

package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.TopicDataBean;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DrawUtil;

/**
 * 首页推荐导航条
 * 
 * @author xiedezhi
 * 
 */
public class HomeFeatureNavigation extends LinearLayout {

	private int mGap = DrawUtil.dip2px(TAApplication.getApplication(), 5);

	/**
	 * 点击事件
	 */
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			long id = (Long) v.getTag();
			String title = (String) v.getTag(R.id.app);
			TopicDataBean bean = new TopicDataBean();
			bean.id = id;
			bean.title = title;
			// 跳转到专题内容
			List<Object> list = new ArrayList<Object>();
			list.add(bean);
			// 通知TabManageView跳转下一层级，把TopicDataBean带过去
			MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
					IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
					CDataDownloader.getTopicContentUrl(bean.id, 1), list);
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
		setOrientation(HORIZONTAL);
		setPadding(mGap, mGap, mGap, mGap);
	}

	public void init(List<String> titles, List<Long> ids,
			List<Integer> backgrounds) {
		removeAllViews();
		if (titles == null || titles.size() <= 0 || ids == null
				|| ids.size() <= 0 || backgrounds == null
				|| backgrounds.size() <= 0 || titles.size() != ids.size()
				|| ids.size() != backgrounds.size()) {
			setVisibility(View.GONE);
		}
		setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
				DrawUtil.dip2px(getContext(), 38));
		lp.leftMargin = lp.rightMargin = mGap;
		lp.weight = 1.0f;
		for (int i = 0; i < titles.size(); i++) {
			Button btn = new Button(getContext());
			btn.setSingleLine();
			btn.setText(titles.get(i));
			btn.setTag(ids.get(i));
			btn.setTag(R.id.app, titles.get(i));
			btn.setBackgroundResource(backgrounds.get(i));
			btn.setTextColor(Color.WHITE);
			btn.setTextSize(16);
			btn.setOnClickListener(mClickListener);
			btn.setPadding(DrawUtil.dip2px(getContext(), 5), 0,
					DrawUtil.dip2px(getContext(), 5), 0);
			btn.getPaint().setFakeBoldText(true);
			addView(btn, lp);
		}
	}
}

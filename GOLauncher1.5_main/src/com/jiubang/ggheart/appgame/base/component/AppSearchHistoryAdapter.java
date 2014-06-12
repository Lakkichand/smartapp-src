package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.bean.SearchHistoryBean;

/**
 * 
 * 搜索关键字数据adapter
 * 
 * @author xiedezhi
 * @date [2012-9-12]
 */
public class AppSearchHistoryAdapter extends BaseAdapter {
	/**
	 * 展示最多的关键字个数
	 */
	private final int mMaxKeyword = 20;

	/**
	 * 列表数据源
	 */
	private List<SearchHistoryBean> mDataSource = new ArrayList<SearchHistoryBean>();

	private Context mContext = null;
	private LayoutInflater mInflater = null;

	public AppSearchHistoryAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mDataSource == null ? 0 : mDataSource.size();
	}

	@Override
	public Object getItem(int position) {
		try {
			return mDataSource.get(position);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position < 0 || position >= mDataSource.size()) {
			return convertView;
		}
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.appgame_search_history_item, null);
		}
		ImageView icon = (ImageView) convertView.findViewById(R.id.appgame_search_history_icon);
		TextView text = (TextView) convertView.findViewById(R.id.appgame_search_history_text);
		SearchHistoryBean bean = mDataSource.get(position);
		if (bean.mType == SearchHistoryBean.SEARCH_KEYWORD_TYPE_HISTORY) {
			icon.setImageResource(R.drawable.appgame_search_history_icon);
		} else if (bean.mType == SearchHistoryBean.SEARCH_KEYWORD_TYPE_NET) {
			icon.setImageResource(R.drawable.appgame_search_index_icon);
		} else {
			icon.setImageDrawable(null);
		}
		text.setText(bean.mKeyword);
		convertView.setTag(bean);
		return convertView;
	}

	/**
	 * 更新数据源，并调用notifyDataSetChanged
	 */
	public void update(List<SearchHistoryBean> list) {
		mDataSource.clear();
		if (list != null) {
			for (SearchHistoryBean bean : list) {
				mDataSource.add(bean);
			}
		}
		// 如果列表项大于最大数，是取前Max个
		if (mDataSource.size() > mMaxKeyword) {
			mDataSource = mDataSource.subList(0, mMaxKeyword);
		}
		notifyDataSetChanged();
	}

	/**
	 * 增加数据源(新增的数据源如果跟已有数据源重复，新增的会被过滤)，并调用notifyDataSetChanged
	 */
	public void append(List<SearchHistoryBean> list) {
		if (list == null) {
			return;
		}
		for (SearchHistoryBean bean : list) {
			boolean success = true;
			for (SearchHistoryBean oBean : mDataSource) {
				if (oBean.mKeyword.trim().equals(bean.mKeyword.trim())) {
					success = false;
					break;
				}
			}
			if (success) {
				mDataSource.add(bean);
			}
		}
		// 如果列表项大于最大数，是取前Max个
		if (mDataSource.size() > mMaxKeyword) {
			mDataSource = mDataSource.subList(0, mMaxKeyword);
		}
		notifyDataSetChanged();
	}

}

package com.jiubang.ggheart.apps.desks.appfunc.search;

import java.util.List;

import android.widget.BaseAdapter;

import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-10-23]
 */
public abstract class SearchListBaseAdapter extends BaseAdapter {
	protected List<FuncSearchResultItem> mDataSource;

	public SearchListBaseAdapter(List<FuncSearchResultItem> dataSource) {
		super();
		mDataSource = dataSource;
	}

	@Override
	public int getCount() {
		if (mDataSource == null) {
			return -1;
		}
		return mDataSource.size();
	}

	@Override
	public Object getItem(int position) {
		if (mDataSource == null) {
			return null;
		}
		if (position < 0) {
			return null;
		}
		return mDataSource.get(position);
	}

	@Override
	public long getItemId(int position) {
		if (mDataSource == null || mDataSource.isEmpty()) {
			return -1;
		}
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		if (mDataSource == null || mDataSource.isEmpty()) {
			return -1;
		}
		return mDataSource.get(position).mType;
	}

	public void updateDataSource(List<FuncSearchResultItem> dataSource) {
		mDataSource = dataSource;
		notifyDataSetChanged();
	}

	public void recyle() {
		mDataSource = null;
	}
}

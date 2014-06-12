package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * 主题模块(一级页面)
 * 
 * 
 */
public class ThemeTab extends BaseTab {

	private static final int CHILD_COUNT = 2; // 子项个数

	public ThemeTab(Context context, String tag, int level) {
		super(context, tag, level);

	}

	@Override
	public int getItemCount() {
		return CHILD_COUNT;
	}

	@Override
	public View getView(int position) {
		View view = mInflater.inflate(R.layout.screen_edit_item, null);
		ImageView image = (ImageView) view.findViewById(R.id.thumb);
		TextView mText = (TextView) view.findViewById(R.id.title);
		switch (position) {
			case 0 :
				// 桌面主题
				image.setImageResource(R.drawable.change_theme_4_def3);
				mText.setText(mContext.getString(R.string.tab_add_visual_theme));
				view.setTag(BaseTab.TAB_THEME);
				break;

			case 1 :
				// 锁屏
				image.setImageResource(R.drawable.screen_edit_golocker);
				mText.setText(mContext.getString(R.string.tab_add_visual_locker));
				view.setTag(BaseTab.TAB_LOCKER);
				break;

			default :
				break;
		}
		return view;
	}

	@Override
	public void onClick(View v) {
		String tag = (String) v.getTag();
		//用户行为统计。
		if (tag.equals("theme")) {
			StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
					StatisticsData.USER_ACTION_SEVENTEEN, IPreferencesIds.DESK_ACTION_DATA);
		} else if (tag.equals("locker")) {
			StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
					StatisticsData.USER_ACTION_EIGHTEEN, IPreferencesIds.DESK_ACTION_DATA);
		}
		if (mTabActionListener != null) {
			mTabActionListener.setCurrentTab(tag);
			mTabActionListener.onRefreshTopBack(tag);
		}
		super.onClick(v);

	}

	@Override
	public void clearData() {
		super.clearData();
	}

	@Override
	public ArrayList<Object> getDtataList() {
		return null;
	}

	@Override
	public void resetData() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

}

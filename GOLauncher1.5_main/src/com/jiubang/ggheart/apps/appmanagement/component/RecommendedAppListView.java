/**
 * 
 */
package com.jiubang.ggheart.apps.appmanagement.component;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class RecommendedAppListView extends ExpandableListView
// implements
// View.OnClickListener, AbsListView.OnScrollListener
{

	// private RelativeLayout parentLayout = null; // listview所在的父层对象
	// private boolean isFixGroup = true; // 是否要固定显示组名在上边
	// private View FixGroupView = null; // 始终固定显示的组view
	// private ExpandableListAdapter mAdapter;
	// private AbsListView.OnScrollListener mOnScrollListener = null;
	// private int indicatorGroupId = -1; // 当前固定显示的组索引
	// private int indicatorGroupHeight = 0;
	// private LinearLayout indicatorGroup;
	// private Context mContext;
	// private Handler handler;

	// public void setHandler(Handler handler) {
	// this.handler = handler;
	// }

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
	}

	/**
	 * @param context
	 */
	public RecommendedAppListView(Context context) {
		super(context);
		// mContext = context;
		// this.isFixGroup = true;

		// super.setOnScrollListener(this);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public RecommendedAppListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// mContext = context;
		// this.isFixGroup = true;

		// super.setOnScrollListener(this);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public RecommendedAppListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// mContext = context;
		// this.isFixGroup = true;

		// super.setOnScrollListener(this);
	}

	// private void loadIndicatorGroup() {
	// if (null == indicatorGroup || indicatorGroup.getHeight() > 0) {
	// createHanderView();
	// indicatorGroup.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	//
	// if (indicatorGroupId == 0
	// && !RecommendedAppListView.this
	// .isGroupExpanded(indicatorGroupId)) {
	// expandGroup(indicatorGroupId);
	// } else {
	// collapseGroup(indicatorGroupId);
	// }
	//
	// handler.post(new Runnable() {
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// parentLayout.removeView(indicatorGroup);
	// parentLayout
	// .addView(
	// indicatorGroup,
	// new MarginLayoutParams(
	// LayoutParams.FILL_PARENT,
	// ViewGroup.LayoutParams.WRAP_CONTENT));
	//
	// }
	// });
	//
	// }
	// });
	// }
	// }

	// private void createHanderView() {
	// indicatorGroup = new LinearLayout(mContext);
	// indicatorGroup.setBackgroundColor(Color.RED);
	// parentLayout.addView(indicatorGroup, new MarginLayoutParams(
	// LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	// Log.i("iphone", "indicatorGroup addView");
	//
	// LayoutInflater inflater = (LayoutInflater) mContext
	// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// View view = inflater.inflate(
	// R.layout.appsmanagement_recomm_list_group_item, null);
	// TextView title = (TextView) view.findViewById(R.id.recomm_app_nametext);
	// title.setText("测试");
	// indicatorGroup.addView(view);
	// }

	// @Override
	// public void onScroll(AbsListView view, int firstVisibleItem,
	// int visibleItemCount, int totalItemCount) {
	// if (isFixGroup) {// 是否显示固定组头
	// boolean isFrameLayoutParent = getParent() instanceof RelativeLayout;
	// mAdapter = this.getExpandableListAdapter();
	// if (isFrameLayoutParent) {// 当前listview是放到FrameLayout的容器中
	// parentLayout = (RelativeLayout) getParent();
	//
	// loadIndicatorGroup();
	//
	// final RecommendedAppListView listView = (RecommendedAppListView) view; //
	// 得到当前listview
	// int npos = view.pointToPosition(0, 0);
	// if (npos != AdapterView.INVALID_POSITION) {
	// long pos = listView.getExpandableListPosition(npos);
	// int childPos = ExpandableListView
	// .getPackedPositionChild(pos);
	// int groupPos = ExpandableListView
	// .getPackedPositionGroup(pos);
	// if (childPos == AdapterView.INVALID_POSITION) {
	// View groupView = listView.getChildAt(npos
	// - listView.getFirstVisiblePosition());
	// indicatorGroupHeight = groupView.getHeight();
	// }
	// // get an error data, so return now
	// if (indicatorGroupHeight == 0) {
	// return;
	// }
	// // update the data of indicator group view
	// if (groupPos != indicatorGroupId) {
	// Log.e("iphone", " iphone ExListView : "
	// + "bind to new group,group position = "
	// + groupPos);
	// if (mAdapter != null) {
	// FixGroupView = mAdapter.getGroupView(groupPos,
	// false, null, indicatorGroup);
	// indicatorGroupId = groupPos;
	// // ((RecommendedAppsAdapter) mAdapter)
	// // .hideGroup(indicatorGroupId);
	// ((RecommendedAppsAdapter) mAdapter)
	// .notifyDataSetChanged();// .notifyDataSetChanged();
	// }
	// }
	// }
	// if (indicatorGroupId == -1) {
	// return;
	// }
	//
	// /**
	// * calculate point (0,indicatorGroupHeight)
	// */
	// int showHeight = indicatorGroupHeight;
	// int nEndPos = listView.pointToPosition(0, indicatorGroupHeight);
	// if (nEndPos != AdapterView.INVALID_POSITION) {
	// long pos = listView.getExpandableListPosition(nEndPos);
	// int groupPos = ExpandableListView
	// .getPackedPositionGroup(pos);
	// if (groupPos != indicatorGroupId) {
	// View viewNext = listView.getChildAt(nEndPos
	// - listView.getFirstVisiblePosition());
	// showHeight = viewNext.getTop();
	// }
	// }
	//
	// if (indicatorGroup == null
	// || indicatorGroup.getChildCount() == 0) {
	// parentLayout.removeView(indicatorGroup);
	// parentLayout.addView(indicatorGroup, new MarginLayoutParams(
	// LayoutParams.FILL_PARENT,
	// ViewGroup.LayoutParams.WRAP_CONTENT));
	// }
	// TextView title = (TextView) FixGroupView
	// .findViewById(R.id.recomm_app_nametext);
	// String titleValue = title.getText().toString();
	// ((TextView) ((ViewGroup) indicatorGroup.getChildAt(0))
	// .getChildAt(0)).setText(titleValue);
	//
	// // if (FixGroupView.getParent() == null) {
	// // indicatorGroup.removeAllViews();
	// // indicatorGroup.addView(FixGroupView);
	// // Log.i("iphone", "indicatorGroup add View");
	// // }
	// // else {
	// // if (indicatorGroup.getChildAt(0) == null ) {
	// // if (FixGroupView.getParent() == null){
	// // indicatorGroup.removeAllViews();
	// // indicatorGroup.addView(FixGroupView);
	// // }
	// //
	// // } else {
	// // TextView title = (TextView) FixGroupView
	// // .findViewById(R.id.recomm_app_nametext);
	// // String titleValue = title.getText().toString();
	// // ((TextView) ((ViewGroup) indicatorGroup.getChildAt(0))
	// // .getChildAt(0)).setText(titleValue);
	// // }
	//
	// // }
	// // update group position
	// ViewGroup.LayoutParams vlparams = indicatorGroup
	// .getLayoutParams();
	// if ((null != vlparams)
	// && (vlparams instanceof MarginLayoutParams)) {
	// MarginLayoutParams layoutParams = (MarginLayoutParams) vlparams;
	// layoutParams.topMargin = -(indicatorGroupHeight - showHeight);
	// indicatorGroup.setLayoutParams(layoutParams);
	// } else if (null != vlparams) {
	// Log.i("iphone", " layout class:"
	// + vlparams.getClass().toString());
	// } else {
	// Log.i("iphone", " layout class is null");
	// }
	// }
	// }
	// if (this.mOnScrollListener != null)
	// this.mOnScrollListener.onScroll(view, firstVisibleItem,
	// visibleItemCount, totalItemCount);
	// }

	// @Override
	// public void onScrollStateChanged(AbsListView view, int scrollState) {
	// if (this.mOnScrollListener != null)
	// this.mOnScrollListener.onScrollStateChanged(view, scrollState);
	// }

	@Override
	public void setOnScrollListener(AbsListView.OnScrollListener paramOnScrollListener) {
		// this.mOnScrollListener = paramOnScrollListener;
	}

	@Override
	public void setAdapter(ExpandableListAdapter paramExpandableListAdapter) {
		super.setAdapter(paramExpandableListAdapter);
	}

}

package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureInfo;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureModelImpl;
/**
 * 
 * @author
 *
 */
public class MyGestureListAdapter extends BaseAdapter {

	private final static int CHANGE_GESTURE_ITEM_POSITION = 0; // “修改手势”item的位置
	private final static int CHANGE_APP_ITEM_POSITION = 1; // “修改响应”item的位置
	private final static int DELETE_GESTURE_ITEM_POSITION = 2; // “删除手势”item的位置
	private final static int OPEN_GESTURE_ITEM_POSITION = 3; // “打开手势”item的位置

	private Context mContext = null;
	private LayoutInflater mInflater = null;
	private ArrayList<DiyGestureInfo> mInfoList = null; // 全部信息
	private int mMenuPosition = -1; // 当前点击打开项

	public MyGestureListAdapter(Context context, ArrayList<DiyGestureInfo> infoList) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		this.mInfoList = infoList;
		if (mInfoList == null) {
			mInfoList = new ArrayList<DiyGestureInfo>();
		}
	}

	@Override
	public int getCount() {
		return mInfoList.size();
	}

	@Override
	public Object getItem(int position) {
		return mInfoList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout itemLayout = null;
		if (convertView != null) {
			itemLayout = (LinearLayout) convertView;
		} else {
			itemLayout = (LinearLayout) mInflater.inflate(R.layout.my_gesture_list_item, null);
		}

		DiyGestureItemView gestureIcon = (DiyGestureItemView) itemLayout
				.findViewById(R.id.my_gesture_item_icon);
		TextView gestureTypeName = (TextView) itemLayout
				.findViewById(R.id.my_gesture_item_type_name);
		TextView gestureName = (TextView) itemLayout.findViewById(R.id.my_gesture_item_name);
		DeskSettingConstants.setTextViewTypeFace(gestureName);
		DeskSettingConstants.setTextViewTypeFace(gestureTypeName);
		DiyGestureInfo info = mInfoList.get(position);
		gestureIcon.setGestureImageView(info.getmGesture());
		gestureTypeName.setText(info.getTypeName());
		gestureName.setText(info.getName());

		GridView menuGridView = (GridView) itemLayout.findViewById(R.id.gesture_menu);
		menuGridView.setVisibility(View.GONE);
		if (mMenuPosition == position) {
			menuGridView.setVisibility(View.VISIBLE);
			menuGridView.setClickable(false);
			menuGridView.setFocusable(false);
			SimpleAdapter adapter = getSimpleAdapter(mContext);
			menuGridView.setAdapter(adapter);
			menuGridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					if (mMenuPosition < 0 || mMenuPosition >= mInfoList.size()) {
						return;
					}

					DiyGestureInfo info = mInfoList.get(mMenuPosition);
					switch (position) {
						case CHANGE_GESTURE_ITEM_POSITION :
							// 修改手势
							Intent intent = new Intent(mContext, DiyGestureEditActivity.class);
							intent.putExtra(DiyGestureConstants.CHANGE_GESTURE_NAME,
									info.getGestureFileName());
							mContext.startActivity(intent);
							break;

						case CHANGE_APP_ITEM_POSITION :
							// 修改响应应用的处理
							DiyGestureConstants.showGestureAppsDialog(mContext);
							break;

						case DELETE_GESTURE_ITEM_POSITION :
							// 1：删除数据
							DiyGestureModelImpl business = DiyGestureModelImpl
									.getInstance(mContext);
							business.deleteGesture(info);
							// 2：列表刷新
							setGestureMenuPosition(-1);
							notifyDataSetChanged();
							break;

						case OPEN_GESTURE_ITEM_POSITION :
							int screenheight = GoLauncher.getScreenHeight();
							int screenwidth = GoLauncher.getScreenWidth();
							Rect rect = new Rect(screenwidth / 2, screenheight, screenwidth / 2,
									screenheight);
							info.execute(rect);
							((Activity) mContext).finish();
							break;

						default :
							break;
					}
				}
			});
		}

		return itemLayout;
	}

	public void setGestureMenuPosition(int position) {
		mMenuPosition = position;
	}

	public int getGestuureMenuPosition() {
		return mMenuPosition;
	}

	/**
	 * 提供给手势菜单的adapter
	 * 
	 * @param context
	 * @return
	 */
	private SimpleAdapter getSimpleAdapter(Context context) {
		String itemIcon = "itemIcon";
		String itemText = "itemText";
		Resources resources = context.getResources();
		ArrayList<HashMap<String, Object>> data = null;
		HashMap<String, Object> hashMap = null;
		int[] icons = new int[] { R.drawable.gesture_menu_item_drawgesture,
				R.drawable.gesture_menu_item_info, R.drawable.gesture_menu_item_delete,
				R.drawable.gesture_menu_item_open };
		String[] names = resources.getStringArray(R.array.gesture_menu_item_names);
		if (icons.length != names.length) {
			return null;
		} else {
			data = new ArrayList<HashMap<String, Object>>();
			for (int i = 0; i < names.length; i++) {
				hashMap = new HashMap<String, Object>();
				hashMap.put(itemIcon, icons[i]);
				hashMap.put(itemText, names[i]);
				data.add(hashMap);
			}
		}
		SimpleAdapter simpleAdapter = new SimpleAdapter(context, data, R.layout.gesture_menu_item,
				new String[] { itemIcon, itemText }, new int[] { R.id.gesture_menu_item_image,
						R.id.gesture_menu_item_text });
		return simpleAdapter;
	}
}

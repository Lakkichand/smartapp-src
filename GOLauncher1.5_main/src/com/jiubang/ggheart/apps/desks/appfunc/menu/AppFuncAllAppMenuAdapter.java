package com.jiubang.ggheart.apps.desks.appfunc.menu;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-27]
 */
public class AppFuncAllAppMenuAdapter extends BaseMenuAdapter {

	public AppFuncAllAppMenuAdapter(Context context, ArrayList<BaseMenuItemInfo> list) {
		super(context, list);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = super.getView(position, convertView, parent);
		BaseMenuItemInfo info = (BaseMenuItemInfo) view.getTag();
		//		ImageView imageView = (ImageView) view.findViewById(R.id.app_func_menu_img);
		AppFuncMenuItemView txtView = (AppFuncMenuItemView) view
				.findViewById(R.id.app_func_menu_text);
//		LayoutParams lp = txtView.getLayoutParams();
		if (info.mActionId == AppFuncAllAppMenuItemInfo.ACTION_APP_CENTER
				|| info.mActionId == AppFuncAllAppMenuItemInfo.ACTION_APP_MANAGEMENT) {
//			lp.width = LayoutParams.WRAP_CONTENT;
//			txtView.setLayoutParams(lp);
			int cnt = AppFuncFrame.getFunControler().getmBeancount();
			txtView.setTitleNum(cnt);
			//			generateMessageCountImage(imageView);
			//			imageView.setVisibility(View.VISIBLE);
		} else {
//			lp.width = LayoutParams.FILL_PARENT;
//			txtView.setLayoutParams(lp);
			///			imageView.setVisibility(View.GONE);
			txtView.setTitleNum(0);
		}
		return view;
	}

//	public void generateMessageCountImage(ImageView imageView) {
//		int cnt = AppFuncFrame.getFunControler().getmBeancount();
//		NinePatchDrawable bgNine = (NinePatchDrawable) mContext.getResources().getDrawable(
//				R.drawable.message_unread_notification);
//		if (null == bgNine || cnt == 0) {
//			return;
//		}
//
//		PreferencesManager preferences = new PreferencesManager(mContext,
//				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
//		int appMenuControl = preferences.getInt(FunControler.APPFUNC_APPMENU_SHOW_MESSAGE, 0);
//		if (appMenuControl == 0) {
//			return;
//		}
//
//		int size = (int) GOLauncherApp.getContext().getResources()
//				.getDimension(R.dimen.message_notify_size);
//		bgNine.setBounds(new Rect(0, 0, size, size));
//
//		Bitmap bgIcon = Bitmap.createBitmap(bgNine.getBounds().width(),
//				bgNine.getBounds().height(), Config.ARGB_8888);
//		Canvas canvas = new Canvas(bgIcon);
//		bgNine.draw(canvas);
//
//		Paint countPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
//		countPaint.setColor(Color.WHITE);
//		countPaint.setTextSize(mContext.getResources().getDimension(
//				R.dimen.new_message_count_text_size));
//		countPaint.setTypeface(Typeface.DEFAULT_BOLD);
//		String number = String.valueOf(cnt);
//		float w = countPaint.measureText(number);
//
//		canvas.drawText(number, (bgIcon.getWidth() - w) / 2, bgIcon.getHeight() * 2 / 3, countPaint);
//		imageView.setImageBitmap(bgIcon);
//	}
}

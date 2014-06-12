/*
 * 文 件 名:  WrapOnClickListener.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-11-12
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-11-12]
 */
public abstract class WrapOnClickListener implements OnClickListener {

	private boolean mIsSelected = false;

	private Dialog mDialog = null;

	private static final String sSHAREDPREFERENCES = "WrapOnClickListener";

	private static final String sMODE = "MODE";
	/**
	 * type == 1 , BoutiqueApp
	 * type == 2 , RecommendedApp
	 */
	private int mType = 0;

	/** {@inheritDoc} */
	@Override
	public void onClick(View v) {
		boolean sdAccess = GoStorePhoneStateUtil.isSDCardAccess();
		if (sdAccess != true) {
			Context context = v.getContext();
			SharedPreferences settings = context.getSharedPreferences(sSHAREDPREFERENCES, 0);
			int mode = settings.getInt(sMODE, 0);
			// 没有记住选项
			if (mode == 0) {
				View view = createDialog(context, v);
				mDialog = null;
				mDialog = new Dialog(context, R.style.AppGameSettingDialog);
				mDialog.setContentView(view);
				mDialog.show();
			} else if (mode == 1) {
				// 没有SD卡也下载到手机内存
				withoutSDCard(v);
			} else if (mode == 2) {
				// 没有SD卡就不下载
				Toast.makeText(context, context.getString(R.string.appgame_sd_card_cancel_text),
						1000).show();
			}
		} else {
			withSDCard(v);
		}
	}

	/**
	 * <br>功能简述:产生对话框，显示SD卡未连接时的操作的dialog view
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	private View createDialog(final Context context, final View parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.appgame_internal_storage_download_tip, null);
		RelativeLayout relativeLayout = (RelativeLayout) view
				.findViewById(R.id.appgame_internal_storage_select);
		final ImageView checkbox = (ImageView) view
				.findViewById(R.id.appgame_internal_storage_checkbox);
		relativeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsSelected) {
					checkbox.setImageResource(R.drawable.apps_uninstall_not_selected);
					mIsSelected = false;
				} else {
					checkbox.setImageResource(R.drawable.apps_uninstall_selected);
					mIsSelected = true;
				}
			}
		});
		Button button = (Button) view.findViewById(R.id.appgame_internal_storage_ok);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				// 勾选了“记住该选项"
				if (mIsSelected == true) {
					SharedPreferences settings = context
							.getSharedPreferences(sSHAREDPREFERENCES, 0);
					settings.edit().putInt(sMODE, 1).commit();
				}
				withoutSDCard(parent);
			}
		});
		Button cancelBtn = (Button) view.findViewById(R.id.appgame_internal_storage_cancel);
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
				// 勾选了“记住该选项"
				if (mIsSelected == true) {
					SharedPreferences settings = context
							.getSharedPreferences(sSHAREDPREFERENCES, 0);
					settings.edit().putInt(sMODE, 2).commit();
				}
				Toast.makeText(context, context.getString(R.string.appgame_sd_card_cancel_text),
						1000).show();
			}
		});
		return view;
	}

	public abstract void withoutSDCard(View v);

	public abstract void withSDCard(View v);
}

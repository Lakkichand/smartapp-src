/**
 * 
 */
package com.jiubang.ggheart.appgame.base.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 省流量模式提醒
 * @author liguoliang
 *
 */
public class SaveFlowRemain {

	private static final String KEY_SAVE_FLOW_REMAIN_OP = "key_save_flow_remain_op";

	/**
	 * 弹出对话框并且默认选择继续提醒
	 */
	public static final int REMAIN_OP_SHOW_DIALOG_AGAIN = 101;

	/**
	 * 弹出Toast 
	 */
	public static final int REMAIN_OP_SHOW_TOAST = 103;

	/**
	 * 什么都不弹出 
	 */
	public static final int REMAIN_OP_SHOW_NULL = 104;

	private boolean mIsSelected = true;

	public static final int SAVE_FLOW_OP_OK = 201;

	public static final int SAVE_FLOW_OP_CANCEL = 202;

	public static final int SAVE_FLOW_OP_NULL = 203;

	private SaveFlowListener mListener;

	/**
	 * 省流量回调函数
	 * @author liguoliang
	 *
	 */
	public interface SaveFlowListener {
		void saveFlowCallback(int op);
	}

	/**
	 * 如果用户未设置不再提醒是否显示，则弹出对话框，否则弹出Toast
	 * @param context
	 */
	public void showSaveFlowNotice(Context context, SaveFlowListener listener) {
		if (context == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		mListener = listener;
		if (!isNeedSaveFlow(context)) {
			// 非数据网络下不需要弹出提示
			if (mListener != null) {
				mListener.saveFlowCallback(SAVE_FLOW_OP_NULL);
			}
			return;
		}

		int op = getRemainOp(context);
		switch (op) {
			case REMAIN_OP_SHOW_DIALOG_AGAIN :
				// 初始化设置为省流量模式
				AppGameSettingData.getInstance(context).updateValue(
						AppGameSettingTable.TRAFFIC_SAVING_MODE, 0);
				showSaveFlowDialog(context, true);
				break;
			case REMAIN_OP_SHOW_TOAST :
				if (mListener != null) {
					mListener.saveFlowCallback(SAVE_FLOW_OP_NULL);
				}
				if (AppGameSettingData.getInstance(context).getTrafficSavingMode() == AppGameSettingData.NOT_LOADING_IMAGES) {
					showSaveFlowToast(context);
				}
				break;
			default :
				break;
		}
	}

	/**
	 * 获取省流量模式的操作状态，包括弹出对话框、弹出toast、什么都不弹（根据上次对对话框操作决定，默认操作状态是弹出对话框)
	 * 
	 * @see REMAIN_OP_SHOW_DIALOG_AGAIN
	 * @see REMAIN_OP_SHOW_TOAST
	 * @see REMAIN_OP_SHOW_NULL
	 */
	public static int getRemainOp(Context context) {
		PreferencesManager sp = new PreferencesManager(context,
				IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		return sp.getInt(KEY_SAVE_FLOW_REMAIN_OP, REMAIN_OP_SHOW_DIALOG_AGAIN);
	}

	private void setRemainOp(Context context, int op) {
		PreferencesManager sp = new PreferencesManager(context,
				IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		sp.putInt(KEY_SAVE_FLOW_REMAIN_OP, op);
		sp.commit();
	}
	/**
	 * 判断是否需要使用省流量模式,在2G/3G网络状态下为true
	 * @param context
	 * @return
	 */
	public static boolean isNeedSaveFlow(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		}
		NetworkInfo info = connectivity.getActiveNetworkInfo();

		boolean channelEnable = ChannelConfig.getInstance(context).isNeedShowSaveFlow();
		// 网络在数据网络并且非200渠道下提示省流量模式
		if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE && channelEnable) {
			return true;
		}
		return false;
	}

	private void showSaveFlowDialog(final Context context, boolean isRemainAgain) {
		final Dialog dialog = new Dialog(context, R.style.AppGameSettingDialog);
		dialog.setContentView(R.layout.appgame_downloadmanager_delete_dialog);
		((TextView) dialog.findViewById(R.id.appgame_download_delete_dialog_title))
				.setText(R.string.traffic_saving_mode_mobile_network);
		((TextView) dialog.findViewById(R.id.appgame_download_manager_tip))
				.setText(R.string.app_save_flow_remain_dialog_tip);

		final ImageView remainAgain = (ImageView) dialog
				.findViewById(R.id.appgame_download_manager_delete_checkbox);
		if (isRemainAgain) {
			remainAgain.setImageResource(R.drawable.apps_uninstall_not_selected);
			mIsSelected = false;
		} else {
			remainAgain.setImageResource(R.drawable.apps_uninstall_selected);
			mIsSelected = true;
		}
		((TextView) dialog.findViewById(R.id.appgame_download_manager_delete_checkbox_tip))
				.setText(R.string.app_save_flow_remain_dialog_remember_this);
		((RelativeLayout) dialog.findViewById(R.id.appgame_download_manager_delete_select))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mIsSelected) {
							remainAgain.setImageResource(R.drawable.apps_uninstall_not_selected);
							mIsSelected = false;
						} else {
							remainAgain.setImageResource(R.drawable.apps_uninstall_selected);
							mIsSelected = true;
						}
					}
				});
		((LinearLayout) dialog.findViewById(R.id.buttons)).setVisibility(View.VISIBLE);
		((Button) dialog.findViewById(R.id.appgame_download_delete_dialog_ok))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mIsSelected) {
							setRemainOp(context, REMAIN_OP_SHOW_TOAST);
						} else {
							setRemainOp(context, REMAIN_OP_SHOW_DIALOG_AGAIN);
						}
						AppGameSettingData.getInstance(context).updateValue(
								AppGameSettingTable.TRAFFIC_SAVING_MODE, 0);
						if (mListener != null) {
							mListener.saveFlowCallback(SAVE_FLOW_OP_OK);
						}
						dialog.dismiss();
					}
				});
		((Button) dialog.findViewById(R.id.appgame_download_delete_dialog_cancel))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						cancel(context);
						dialog.dismiss();
					}
				});
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				cancel(context);
			}
		});
		dialog.show();
	}

	private void cancel(Context context) {
		if (mIsSelected) {
			setRemainOp(context, REMAIN_OP_SHOW_NULL);
		} else {
			setRemainOp(context, REMAIN_OP_SHOW_DIALOG_AGAIN);
		}
		AppGameSettingData.getInstance(context).updateValue(
				AppGameSettingTable.TRAFFIC_SAVING_MODE, 2);
		if (mListener != null) {
			mListener.saveFlowCallback(SAVE_FLOW_OP_CANCEL);
		}
	}

	private static void showSaveFlowToast(Context context) {
		Toast.makeText(context, R.string.app_save_flow_remain_toast_tip, Toast.LENGTH_LONG).show();
	}
}

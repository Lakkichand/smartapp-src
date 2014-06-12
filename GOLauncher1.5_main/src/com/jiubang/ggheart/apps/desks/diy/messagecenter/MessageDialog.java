package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import android.app.AlertDialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 
 */
public class MessageDialog {
	private String mTitle;
	private String mMsgContent;
	private String mLeftBtnText;
	private Context mContext;

	public MessageDialog(Context context) {
		mContext = context;
	}

	public AlertDialog createDialog(String title, String content, String leftBtnTxt,
			Drawable drawable) {
		AlertDialog dialog = null;
		mTitle = title;
		mMsgContent = content;
		mLeftBtnText = leftBtnTxt;
		View view = LayoutInflater.from(mContext).inflate(R.layout.message_content, null);
		TextView titleTxt = (TextView) view.findViewById(R.id.title);
		TextView contentTxt = (TextView) view.findViewById(R.id.message_content);
		ImageView image = (ImageView) view.findViewById(R.id.message_image);
		image.setImageDrawable(drawable);
//		Button lftBtn = (Button) view.findViewById(R.id.left_btn);
//		lftBtn.setText(mLeftBtnText);
		dialog = new AlertDialog.Builder(mContext).setView(view).create();
		return dialog;
	}
}

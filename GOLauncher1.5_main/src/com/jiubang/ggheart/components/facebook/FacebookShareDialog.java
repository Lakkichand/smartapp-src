/*
 * 文 件 名:  FacebookShareDialog.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  ruxueqin
 * 修改时间:  2012-11-29
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.components.facebook;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.Session;
import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogBase;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-11-29]
 */
public class FacebookShareDialog extends Dialog {

	public static final int TYPE_NORMAL = 1; // 一般分享框
	public static final int TYPE_RESTORE = 2; // facebook恢复分享框
	private int mType = TYPE_NORMAL; // 分享框类型
	public static String sGOLAUNCHERPAGE_FACEBOOK = "http://www.facebook.com/golauncher";
	public static String sGOLAUNCHERPAGE_FACEBOOK_KO = "https://www.facebook.com/GoLauncherExKorea";

	private Activity mActivity;

	private TextView mTitleText;
	private TextView mSummuryText;
	private EditText mContenText;

	/** <默认构造函数>
	 */
	public FacebookShareDialog(Activity activity) {
		super(activity, R.style.Dialog);
		mActivity = activity;
	}

	public void setType(int type) {
		mType = type;
	}

	public void setTitle(String title) {
		if (mTitleText != null && title != null) {
			mTitleText.setText(title);
		}
	}
	public void setSummury(String summury) {
		if (mSummuryText != null && summury != null) {
			mSummuryText.setText(summury);
		}
	}

	public void setContent(String content) {
		if (mContenText != null && content != null) {
			mContenText.setText(content);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.facebook_share_dialog, null);
		mTitleText = (TextView) view.findViewById(R.id.title);
		mSummuryText = (TextView) view.findViewById(R.id.summury);
		mContenText = (EditText) view.findViewById(R.id.shareonfbtext);

		setContentView(view);

		DialogBase.setDialogWidth(view, getContext());

		setCanceledOnTouchOutside(true);

		final EditText editText = (EditText) findViewById(R.id.shareonfbtext);
		String content = editText.getText() != null ? editText.getText().toString() : null;
		int length = content != null ? content.length() : 0;
		editText.setSelection(length);

		findViewById(R.id.sharebutton).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String msg = (editText.getText() != null) ? editText.getText().toString() : null;
				SessionStatusCallback callback = new SessionStatusCallback(mActivity,
						GoFacebookUtil.TYPE_SHAREALINK, null);
				Session session = Session.getActiveSession();
				switch (mType) {
					case TYPE_NORMAL :
						if (session != null && session.isOpened()) {
							if (Machine.getCountry(mActivity).equals("kr")) {
								callback.shareALink(sGOLAUNCHERPAGE_FACEBOOK_KO, msg);
							} else {
								callback.shareALink(sGOLAUNCHERPAGE_FACEBOOK, msg);
							}
						}
						break;

					case TYPE_RESTORE :
						callback.setAccessType(GoFacebookUtil.TYPE_SHAREALINK_RESOTRE);
						String data = msg != null ? msg.toString() : null;
						callback.setData(data);
						GoFacebookUtil.login(mActivity, callback, false);
						break;

					default :
						break;
				}

				dismiss();
			}
		});

		findViewById(R.id.remindlater).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

	}
}

package com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageDownLoadObserver;

/**
 * 
 * 类描述:
 */
public class HrefWidgetBean extends MessageWidgetBean {

	public HrefWidgetBean() {
		mType = TYPE_HERF;
		mHrefSpan = new HrefSpan();
	}

	private HrefSpan mHrefSpan;

	@Override
	public void prase(JSONObject obj) {
		// TODO Auto-generated method stub
		// super.prase(obj);
		if (obj != null) {
			try {
				mValue = obj.getString(TAG_VALUE);
				mActtype = obj.getInt(TAG_ACTTYPE);
				mActvaule = obj.getString(TAG_ACTVAULE);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * 
	 * 类描述:
	 * 功能详细描述:
	 * @date  [2012-9-28]
	 */
	private class HrefSpan extends ClickableSpan {

		@Override
		public void onClick(View widget) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void initView(View view, MessageDownLoadObserver observer) {
		// TODO Auto-generated method stub
		super.initView(view, observer);
		TextView txtView = (TextView) view;
		SpannableString linkString = new SpannableString(mValue);
		linkString.setSpan(mHrefSpan, 0, linkString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		txtView.setMovementMethod(new LinkMovementMethod());
		txtView.setText(linkString);
	}

}

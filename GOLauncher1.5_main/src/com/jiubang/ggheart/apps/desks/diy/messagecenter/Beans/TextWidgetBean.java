package com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.TextView;

import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageDownLoadObserver;
/**
 * 
 * 类描述:
 * 功能详细描述:
 * @date  [2012-9-28]
 */
public class TextWidgetBean extends MessageWidgetBean {

	public TextWidgetBean() {
		mType = TYPE_TEXT;
	}

	@Override
	public void prase(JSONObject obj) {
		// TODO Auto-generated method stub
		super.prase(obj);
		if (obj != null) {
			try {
				mValue = obj.getString(TAG_VALUE);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initView(View view, MessageDownLoadObserver observer) {
		// TODO Auto-generated method stub
		super.initView(view, observer);
		((TextView) view).setText(mValue);
		((TextView) view).setTextColor(0xff363636);
	}

}

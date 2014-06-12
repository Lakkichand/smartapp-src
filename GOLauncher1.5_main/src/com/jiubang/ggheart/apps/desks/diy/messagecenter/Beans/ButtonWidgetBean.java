package com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.Button;

import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageDownLoadObserver;

/**
 * 
 */
public class ButtonWidgetBean extends MessageWidgetBean {

	public ButtonWidgetBean() {
		mType = TYPE_BTN;
	}

	@Override
	public void prase(JSONObject obj) {
		// TODO Auto-generated method stub
		super.prase(obj);
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

	@Override
	public void initView(View view, MessageDownLoadObserver observer) {
		// TODO Auto-generated method stub
		super.initView(view, observer);

		((Button) view).setText(mValue);
	}

}

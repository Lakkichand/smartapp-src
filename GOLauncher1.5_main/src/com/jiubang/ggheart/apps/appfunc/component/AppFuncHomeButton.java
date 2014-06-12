package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;

import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncImageButton.OnClickListener;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;

public class AppFuncHomeButton extends AppFuncImageButton implements OnClickListener {

	public AppFuncHomeButton(Activity activity, int i, int j, int k, int l, int i1) {
		super(activity, i, j, k, l, i1);
		setClickListener(this);
	}

	@Override
	public void onClick(XComponent view) {
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
				AppFuncConstants.EXITAPPFUNCFRAME, null);
	}
}

package com.jiubang.ggheart.components;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;

public class DeskBuilder extends Builder implements ISelfObject {
	private int mTheme;

	public DeskBuilder(Context context) {
		super(context);

		mTheme = -1;

		selfConstruct();
	}

	public DeskBuilder(Context context, int theme) {
		super(context);

		mTheme = theme;

		selfConstruct();
	}

	@Override
	public AlertDialog create() {
		// final AlertDialog dialog = new AlertDialog(P.mContext, mTheme);
		// P.apply(dialog.mAlert);
		// dialog.setCancelable(P.mCancelable);
		// dialog.setOnCancelListener(P.mOnCancelListener);
		// if (P.mOnKeyListener != null) {
		// dialog.setOnKeyListener(P.mOnKeyListener);
		// }
		// return dialog;

		DeskAlertDialog dlg = null;
		try {
			// Build的AlertController.AlertParams P
			// Field field_P = getClass().getSuperclass().getDeclaredField("P");
			Field field_P = Builder.class.getDeclaredField("P");
			field_P.setAccessible(true);
			Object obj_P = field_P.get(this);

			// P 的 Context mContext
			Field field_mContext = obj_P.getClass().getDeclaredField("mContext");
			field_mContext.setAccessible(true);
			Object obj_mContext = field_mContext.get(obj_P);

			if (-1 == mTheme) {
				dlg = new DeskAlertDialog((Context) obj_mContext);
			} else {
				dlg = new DeskAlertDialog((Context) obj_mContext, mTheme);
			}
			// Dialog的AlertController mAlert对象
			// Field field =
			// mDialog.getClass().getSuperclass().getDeclaredField("mAlert");
			Field field_mAlert = AlertDialog.class.getDeclaredField("mAlert");
			field_mAlert.setAccessible(true);
			Object obj_mAlert = field_mAlert.get(dlg);

			// apply 方法
			Method method = obj_P.getClass().getMethod("apply", obj_mAlert.getClass());
			method.invoke(obj_P, obj_mAlert);

			// TODO 其他方法在创建完成后设置，避免在TRY中作过多的事情，发生不必要的异常

			return dlg;
		} catch (Exception e) {
			if (null != dlg) {
				dlg.selfDestruct();
				dlg = null;
			}
			return super.create();
		}
	}

	@Override
	public void selfConstruct() {

	}

	@Override
	public void selfDestruct() {

	}
}

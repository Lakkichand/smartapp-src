package com.jiubang.ggheart.plugin.mediamanagement;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogBase;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaBaseDialog;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaConfirmDialog;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaSingChoiceDialog;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-11-23]
 */
public class MediaDialog implements IMediaBaseDialog, IMediaConfirmDialog, IMediaSingChoiceDialog {

	private DialogConfirm mDialogConfirm;

	private DialogBase mDialogBase;

	private DialogSingleChoice mSingleChoice;
	
	private Dialog mBaseDialog;

	private Context mContext;

	public MediaDialog(Context context) {
		super();
		mContext = context;
	}

	@Override
	public View getView() {

		return mDialogBase.getView();
	}

	@Override
	public void findTipCheckBox(View view) {

		mDialogConfirm.findTipCheckBox(view);

	}

	@Override
	public void setMessage(String msg) {

		mDialogConfirm.setMessage(msg);

	}

	@Override
	public void showTipCheckBox() {

		mDialogConfirm.showTipCheckBox();

	}

	@Override
	public CheckBox getTipCheckBox() {

		return mDialogConfirm.getTipCheckBox();
	}

	@Override
	public void setTipCheckBoxText(CharSequence text) {

		mDialogConfirm.setTipCheckBoxText(text);

	}

	@Override
	public void setDialogWidth(LinearLayout layout, Context context) {

		DialogConfirm.setDialogWidth(layout, context);

	}

	@Override
	public void setTitle(String titleString) {
		mDialogBase.setTitle(titleString);

	}

	@Override
	public void setPositiveButtonVisible(int visible) {

		mDialogBase.setPositiveButtonVisible(visible);

	}

	@Override
	public void setNegativeButtonVisible(int visible) {
		mDialogBase.setNegativeButtonVisible(visible);

	}

	@Override
	public void setPositiveButton(CharSequence text, OnClickListener listener) {
		mDialogBase.setPositiveButton(text, listener);

	}

	@Override
	public void setNegativeButton(CharSequence text, OnClickListener listener) {
		mDialogBase.setNegativeButton(text, listener);

	}

	@Override
	public void setButtonLister() {
		mDialogBase.setButtonLister();

	}

	@Override
	public void show() {

		mDialogBase.show();

	}

	@Override
	public boolean isShowing() {
		return mDialogBase.isShowing();
	}

	@Override
	public void dismiss() {
		mDialogBase.dismiss();

	}

	@Override
	public IMediaConfirmDialog createConfirmDialog() {

		MediaDialog dialog = new MediaDialog(mContext);

		dialog.mDialogConfirm = new DialogConfirm(mContext);

		dialog.mDialogBase = dialog.mDialogConfirm;

		return dialog;

	}

	@Override
	public void setItemData(CharSequence[] items, int checkItem, boolean isShowCheckBox) {

		mSingleChoice.setItemData(items, checkItem, isShowCheckBox);

	}

	@Override
	public void setOnItemClickListener(android.content.DialogInterface.OnClickListener listener) {

		mSingleChoice.setOnItemClickListener(listener);

	}

	@Override
	public IMediaSingChoiceDialog createSingChoiceDialog() {

		MediaDialog dialog = new MediaDialog(mContext);

		dialog.mSingleChoice = new DialogSingleChoice(mContext);

		dialog.mDialogBase = dialog.mSingleChoice;

		return dialog;
	}
	
	public Dialog createBaseDialog() {
		Dialog dialog = new Dialog(mContext, R.style.media_open_chooser_dialog);
		return dialog;
	}

}

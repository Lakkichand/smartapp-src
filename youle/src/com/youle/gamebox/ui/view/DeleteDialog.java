package com.youle.gamebox.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 14-7-23.
 */
public class DeleteDialog extends Dialog implements View.OnClickListener {
    TextView mContent;
    TextView mSure;
    TextView mCancel;
    private String title;
    private String content;

    public void setListener(IDialogOperationListener listener) {
        this.listener = listener;
    }

    private  IDialogOperationListener listener ;
    public interface IDialogOperationListener {
        public void onSure();
    }

    public DeleteDialog(Context context) {
        super(context);
    }

    public DeleteDialog(Context context, String title, String content) {
        super(context);
        this.title = title;
        this.content = content;
    }
    public DeleteDialog(Context context,String content) {
        super(context);
        this.content = content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delete_dialog);
        mCancel = (TextView) findViewById(R.id.cancel);
        mSure = (TextView) findViewById(R.id.sure);
        mContent = (TextView) findViewById(R.id.content);
        if(!TextUtils.isEmpty(content)){
            mContent.setText(content);
        }
        mCancel.setOnClickListener(this);
        mSure.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sure) {
            if(listener!=null){
                dismiss();
                listener.onSure();
            }
        }else {
            dismiss();
        }
    }
}

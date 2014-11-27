package com.youle.gamebox.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 14-7-23.
 */
public class LoadDataDialog extends Dialog {
    TextView mContent;
    private String title;




    public LoadDataDialog(Context context) {
        super(context);
    }

    public LoadDataDialog(Context context, String title) {
        super(context,R.style.Theme_loading);
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);
        mContent = (TextView) findViewById(R.id.title);
        mContent.setText(title);
    }

}

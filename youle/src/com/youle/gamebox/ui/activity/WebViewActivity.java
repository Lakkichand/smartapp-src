package com.youle.gamebox.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 2014/5/19.
 */
public class WebViewActivity extends BaseActivity implements View.OnClickListener {

    @InjectView(R.id.userinfo_but)
    Button mUserinfoBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        ButterKnife.inject(this);
        mUserinfoBut.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        this.startActivity(new Intent(this,AppDetailActivity.class));
    }
}

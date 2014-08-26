package com.youle.gamebox.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import butterknife.ButterKnife;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.bean.UpdataBean;

/**
 * Created by Administrator on 14-7-23.
 */
public class UpdataDialog extends Dialog implements View.OnClickListener {
    TextView desc;
    private UpdataBean bean;

    ShowType showType = ShowType.IS_LAST ;

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.update_sure){
            update();
        }else if(v.getId()==R.id.sure){
            dismiss();
        }else {
            dismiss();
            if(bean.isForce()){
                YouleAplication.getApplication().exitApp(false);
            }
        }

    }

    private void update() {
        Uri  uri = Uri.parse(bean.getUrl());
        Intent intent = new  Intent(Intent.ACTION_VIEW, uri);
        getContext().startActivity(intent);
    }

    public enum ShowType{
        IS_LAST,NEED_UPDATE
    }



    public UpdataDialog(Context context) {
        super(context,R.style.Theme_loading);
    }

    public UpdataDialog(Context context, UpdataBean bean) {
        super(context,R.style.Theme_loading);
        showType = ShowType.NEED_UPDATE ;
        this.bean = bean ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.updata_dialog);
        if(showType == ShowType.NEED_UPDATE){
           findViewById(R.id.isNewLayout).setVisibility(View.GONE);
        }else {
            findViewById(R.id.up_desc_layout).setVisibility(View.GONE);
        }
        if(bean!=null) {
            desc = (TextView) findViewById(R.id.desc);
            desc.setText(bean.getUpdateDesc());
        }
        findViewById(R.id.sure).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.update_sure).setOnClickListener(this);
    }

}

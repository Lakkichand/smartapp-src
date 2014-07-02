package com.youle.gamebox.ui.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 2014/5/14.
 */
public class LoginCheckDialog extends BaseDialogView{
    @InjectView(R.id.dialog_check_login)
    Button dialog_check_login;
    @InjectView(R.id.dialog_check_canle)
    Button dialog_check_canle;
    @InjectView(R.id.dialog_check_message)
    TextView dialog_check_message;
    private DialogOnListener dialogOnListener;

    public LoginCheckDialog(Context context) {
        super(context);
    }

    @Override
    public int getDialogView() {
        return R.layout.dialog_login_layout;
    }


    @Override
    public void getDialogView(View view) {
        dialog_check_message.setText("你还没有登录！");
        dialog_check_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if(dialogOnListener!=null)dialogOnListener.onclick(0);
            }
        });
        dialog_check_canle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if(dialogOnListener!=null)dialogOnListener.onclick(1);
            }
        });
    }

    public void setDialogOnListener(DialogOnListener dialogOnListener){
        this.dialogOnListener = dialogOnListener;
    }

}

package com.youle.gamebox.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Created by Administrator on 2014/5/14.
 */
public class UserInfoModfyPhotoDialog extends BaseDialogView{
    @InjectView(R.id.dialog_check_login)
    Button dialog_check_login;
    @InjectView(R.id.dialog_check_canle)
    Button dialog_check_canle;
    private DialogOnListener dialogOnListener;


    public UserInfoModfyPhotoDialog(Context context) {
        super(context);
    }

    @Override
    public int getDialogView() {
        return R.layout.dialog_modfy_photo_layout;
    }


    @Override
    public void getDialogView(View view) {
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


    public void onActivityResult(int requestCode, int resultCode, Intent data){










    }


    public void setDialogOnListener(DialogOnListener dialogOnListener){
        this.dialogOnListener = dialogOnListener;
    }




}

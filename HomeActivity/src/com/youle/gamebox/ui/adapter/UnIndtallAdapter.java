package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.AppInfoBean;
import com.youle.gamebox.ui.receiver.PackageReceiver;
import com.youle.gamebox.ui.util.AppInfoUtils;

import java.util.List;

/**
 * Created by Administrator on 2014/5/4.
 */
public class UnIndtallAdapter extends YouleBaseAdapter<AppInfoBean> {
    private LayoutInflater inflater = null;
    private List<AppInfoBean> mList =null;

    public UnIndtallAdapter(Context mContext, List<AppInfoBean> mList) {
        super(mContext, mList);
        this.mList = mList;
        inflater = LayoutInflater.from(mContext);
    }
    public Handler getRemoveHandler(){
        Handler  removeHandler = new Handler(){
            @Override
            public void dispatchMessage(Message msg) {
                super.dispatchMessage(msg);
                switch (msg.what){
                    case PackageReceiver.PACKAGE_REMOVED_WHAT:
                        Bundle bundle = (Bundle)msg.obj;
                        AppInfoBean removeAppInfoBean = null;
                        String packName = bundle.getString(PackageReceiver.PACKAGE_KEY);
                        for (int i = 0; i <mList.size() ; i++) {
                            AppInfoBean appInfoBean = mList.get(i);
                            if(packName.equals(appInfoBean.getPackageName())){
                                removeAppInfoBean = appInfoBean;
                                break;
                            }
                        }
                        if(removeAppInfoBean!=null){
                            mList.remove(removeAppInfoBean);
                            notifyDataSetChanged();
                        }

                        break;
                }

            }
        };
        return removeHandler;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        UninstallHolderView uninstallHolderView = null;
        AppInfoBean appInfoBean = getItem(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.test_uninstall_item, null);
            uninstallHolderView = new UninstallHolderView(convertView);
            convertView.setTag(uninstallHolderView);
        } else {
            uninstallHolderView = (UninstallHolderView) convertView.getTag();
        }
//        uninstallHolderView.uninstallImageView.setImageDrawable(appInfoBean.getAppIcon());
        uninstallHolderView.uninstallTextView.setText(appInfoBean.getName());
        uninstallHolderView.uninstallBut.setOnClickListener(new ButOnClickListener(this,appInfoBean));
        return convertView;
    }

    class ButOnClickListener implements  View.OnClickListener{
        private  AppInfoBean appInfoBean;
        UnIndtallAdapter unIndtallAdapter;
        ButOnClickListener(UnIndtallAdapter unIndtallAdapter,AppInfoBean appInfoBean) {
            this.appInfoBean = appInfoBean;
            this.unIndtallAdapter = unIndtallAdapter;
        }
        @Override
        public void onClick(View view) {
            if(mList!=null){
                AppInfoUtils.uninstall(getContext(),appInfoBean.getPackageName());
            }

        }
    }

    class UninstallHolderView {
        UninstallHolderView(View view) {
            ButterKnife.inject(this, view);
        }

        @InjectView(R.id.uninstall_imageview)
        ImageView uninstallImageView;
        @InjectView(R.id.uninstall_descri)
        TextView uninstallTextView;
        @InjectView(R.id.uninstall_but)
        Button uninstallBut;
    }

}

package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.LogUserCache;
import com.youle.gamebox.ui.greendao.LogUser;

import java.util.List;

/**
 * Created by Administrator on 2014/5/26.
 */
public class LoginListitemAdapter extends YouleBaseAdapter<LogUser> {



    public LoginListitemAdapter(Context mContext, List<LogUser> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        HolerView holerView = null;
        if(view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.login_layout_listitem_item, null);
            holerView = new HolerView(view);
            view.setTag(holerView);
        }else {
            holerView = (HolerView)view.getTag();
        }
        LogUser logUser = mList.get(i);
        holerView.mLoginListitemUsername.setText(logUser.getUserName());
        holerView.mLoginListitemDel.setOnClickListener(new delOnClickListener(i));
        return view;
    }

    class delOnClickListener implements View.OnClickListener{
        int index = 0;
        delOnClickListener(int index) {
            this.index = index;
        }
        @Override
        public void onClick(View view) {
            LogUser logUser = mList.get(index);
            new LogUserCache().delLogUser(logUser.getUserName());
            remove(mList.get(index));
        }
    }

    class HolerView{
    @InjectView(R.id.login_listitem_username)
    TextView mLoginListitemUsername;
    @InjectView(R.id.login_listitem_del)
    TextView mLoginListitemDel;
        HolerView(View view) {
            ButterKnife.inject(this,view);
        }
    }
}

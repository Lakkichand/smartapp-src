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
import com.youle.gamebox.ui.account.UserCache;
import com.youle.gamebox.ui.bean.LogAccount;
import com.youle.gamebox.ui.greendao.LogUser;
import com.youle.gamebox.ui.view.LoginUserListView;

import java.util.List;

/**
 * Created by Administrator on 2014/5/26.
 */
public class LoginListitemAdapter extends YouleBaseAdapter<LogAccount> {
    private LoginUserListView.LogUserOnclickItem onclickItem ;

    public void setOnclickItem(LoginUserListView.LogUserOnclickItem onclickItem) {
        this.onclickItem = onclickItem;
    }

    public LoginListitemAdapter(Context mContext, List<LogAccount> mList) {
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
        LogAccount logUser = mList.get(i);
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
            LogAccount logUser = mList.get(index);
            new UserCache().deleteAccount(logUser.getUserName());
            remove(mList.get(index));
            if(onclickItem!=null){
                onclickItem.onUserDelete(logUser);
            }
        }
    }

    class HolerView{
    @InjectView(R.id.login_listitem_username)
    TextView mLoginListitemUsername;
    @InjectView(R.id.login_listitem_del)
    View mLoginListitemDel;
        HolerView(View view) {
            ButterKnife.inject(this,view);
        }
    }
}

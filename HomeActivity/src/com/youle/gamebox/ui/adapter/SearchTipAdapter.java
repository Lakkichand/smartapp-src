package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserCache;
import com.youle.gamebox.ui.bean.LogAccount;
import com.youle.gamebox.ui.bean.SearchTipBean;
import com.youle.gamebox.ui.view.SearchPopView;

import java.util.List;

/**
 * Created by Administrator on 2014/5/26.
 */
public class SearchTipAdapter extends YouleBaseAdapter<SearchTipBean> {

    private SearchPopView.SearchItemOnClick mSearchItemOnClick ;

    public SearchTipAdapter(Context mContext, List<SearchTipBean> mList,SearchPopView.SearchItemOnClick searchItemOnClick ) {
        super(mContext, mList);
        this.mSearchItemOnClick = searchItemOnClick ;
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
        final String logUser = mList.get(i).title;
        holerView.mLoginListitemUsername.setText(logUser);
        holerView.mLoginListitemDel.setOnClickListener(new delOnClickListener(i));
        view.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(mSearchItemOnClick!=null){
                    mSearchItemOnClick.onclik(logUser);
                }
            }
        });
        return view;
    }

    class delOnClickListener implements View.OnClickListener{
        int index = 0;
        delOnClickListener(int index) {
            this.index = index;
        }
        @Override
        public void onClick(View view) {
            remove(mList.get(index));
            notifyDataSetChanged();
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

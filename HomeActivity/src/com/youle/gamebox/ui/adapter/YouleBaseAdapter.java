package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by Administrator on 14-4-23.
 */
public abstract class YouleBaseAdapter<T> extends BaseAdapter {
    protected Context mContext ;
    protected List<T>  mList  ;
    protected String TAG = getClass().getSimpleName() ;
    private boolean isFliping = false ;

    public boolean isFliping() {
        return isFliping;
    }

    public void setFliping(boolean isFliping) {
        this.isFliping = isFliping;
    }

    public YouleBaseAdapter(Context mContext, List<T> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    public Context getContext(){
        return mContext;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public T getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public  void addDate(List<T> t){
        mList.addAll(t) ;
        this.notifyDataSetChanged();
    }
    public  void resetDate(List<T> t){
       mList.clear();
       mList.addAll(t) ;
       notifyDataSetChanged();
    }

    public void remove(T t){
        mList.remove(t) ;
        notifyDataSetChanged();
    }

    public void add(T t){
        mList.add(t) ;
        notifyDataSetChanged();
    }
}

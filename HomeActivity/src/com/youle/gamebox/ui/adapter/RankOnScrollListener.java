package com.youle.gamebox.ui.adapter;

import android.widget.AbsListView;
import android.widget.ListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 14-6-9.
 */
public class RankOnScrollListener extends PauseOnScrollListener {
    private String TAG = "RankOnScrollListener" ;
    private YouleBaseAdapter mAdapter;
    public RankOnScrollListener(YouleBaseAdapter adapter, ImageLoader imageLoader, boolean pauseOnScroll, boolean pauseOnFling) {
        super(imageLoader, pauseOnScroll, pauseOnFling);
        this.mAdapter=adapter ;
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        super.onScrollStateChanged(view, scrollState);
        if(mAdapter!=null) {
            if (scrollState == SCROLL_STATE_FLING) {
                mAdapter.setFliping(true);
            } else {
                mAdapter.setFliping(false);
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
    }
}

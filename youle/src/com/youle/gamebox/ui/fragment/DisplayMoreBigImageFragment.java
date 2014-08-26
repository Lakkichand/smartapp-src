package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

import java.util.List;


/**
 * Created by Administrator on 14-8-7.
 */
public class DisplayMoreBigImageFragment extends BaseFragment {
    @InjectView(R.id.imageViewPager)
    ViewPager mImageViewPager;
    private List<String> urls ;
    private String gameName;
    public DisplayMoreBigImageFragment() {
    }

    public DisplayMoreBigImageFragment(List<String> urls,String gameName) {
        this.urls = urls;
        this.gameName = gameName ;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDefaultTitle(gameName);
        mImageViewPager.setAdapter(new ImageAdapter());
    }

    @Override
    protected int getViewId() {
        return R.layout.show_big_image_fragment;
    }

    @Override
    protected String getModelName() {
        return "游戏大图";
    }
    class ImageAdapter extends FragmentPagerAdapter{

        public ImageAdapter() {
            super(getChildFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            return new BigImageFragment(urls.get(position));
        }

        @Override
        public int getCount() {
            return urls.size();
        }
    }
}

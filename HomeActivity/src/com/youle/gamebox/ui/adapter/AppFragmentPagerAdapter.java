package com.youle.gamebox.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewGroup;
import com.youle.gamebox.ui.fragment.BaseFragment;
import com.youle.gamebox.ui.util.LOGUtil;

import java.util.List;

public class AppFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentsList;
    FragmentManager fm ;
    public AppFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public AppFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fm = fm;
        this.fragmentsList = fragments;
    }

    @Override
    public int getCount() {
        return fragmentsList.size();
    }

    @Override
    public Fragment getItem(int postion) {
        LOGUtil.d("junjun","postion:  "+postion);
        return fragmentsList.get(postion);
    }

    @Override
    public CharSequence getPageTitle(int position) {
       String title = ((BaseFragment)fragmentsList.get(position)).getArguments().getString("fragmentTag");
       return title;
    }


    /*@Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = fragmentsList.get(position);
        if (!fragment.isAdded()) { // 如果fragment还没有added
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(fragment, fragment.getClass().getSimpleName());
            ft.commit();
            fm.executePendingTransactions();
        }
        if (fragment.getView().getParent() == null) {
            container.addView(fragment.getView());
        }
        return fragment.getView();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        container.removeView(fragmentsList.get(position).getView());
    }*/
}

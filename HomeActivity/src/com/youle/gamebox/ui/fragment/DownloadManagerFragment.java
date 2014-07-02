package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 14-6-16.
 */
public class DownloadManagerFragment extends BaseFragment implements ViewPager.OnPageChangeListener,View.OnClickListener {
    @InjectView(R.id.installed)
    TextView mInstalled;
    @InjectView(R.id.needUpdate)
    TextView mNeedUpdate;
    @InjectView(R.id.download)
    RelativeLayout mDownload;
    DownloadManagerItemFragment installFragment ;
    DownloadManagerItemFragment updateFragment ;
    DownloadManagerItemFragment downloadFragment;
    @InjectView(R.id.downloadViewPager)
    ViewPager mDownloadViewPager;
    @InjectView(R.id.downloadText)
    TextView mDownloadText;
    @InjectView(R.id.back)
    ImageView mBack;

    @Override
    protected int getViewId() {
        return R.layout.fragment_download;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DownloadManagerAdapter adapter = new DownloadManagerAdapter(getFragmentManager());
        mDownloadViewPager.setAdapter(adapter);
        mDownloadViewPager.setOnPageChangeListener(this);
        mInstalled.setSelected(true);
        mInstalled.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mNeedUpdate.setOnClickListener(this);
        mDownload.setOnClickListener(this);
    }


    private void reset(){
        mInstalled.setSelected(false);
        mNeedUpdate.setSelected(false);
        mDownload.setSelected(false);
        mDownloadText.setSelected(false);
    }
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        reset();
        if(position==0){
           mInstalled.setSelected(true);
        }else if (position==1){
            mNeedUpdate.setSelected(true);
        }else {
            mDownload.setSelected(true);
            mDownloadText.setSelected(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
       if(v.getId()==R.id.back){
           getActivity().finish();
       }else if(v.getId()==R.id.installed){
           mDownloadViewPager.setCurrentItem(0);
       }else if(v.getId()==R.id.needUpdate){
           mDownloadViewPager.setCurrentItem(1);
       }else  if(v.getId()==R.id.download){
           mDownloadViewPager.setCurrentItem(2);
       }
    }

    class DownloadManagerAdapter  extends FragmentPagerAdapter{

        public DownloadManagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0){
                if(installFragment==null){
                    installFragment = new DownloadManagerItemFragment(DownloadManagerItemFragment.INSTALL);
                }
                return installFragment ;
            }else if(position==1){
                if(updateFragment==null){
                    updateFragment = new DownloadManagerItemFragment(DownloadManagerItemFragment.UPDATA) ;
                }
                return updateFragment;
            }else {
                if(downloadFragment==null ){
                    downloadFragment = new DownloadManagerItemFragment(DownloadManagerItemFragment.DOWNLOAD);
                }
                return downloadFragment;
            }

        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}

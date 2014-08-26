package com.youle.gamebox.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.fragment.BigImageFragment;

/**
 * Created by Administrator on 14-7-21.
 */
public class DisplayBigImageActivity extends BaseActivity {
    @InjectView(R.id.imageViewPager)
    ViewPager mImageViewPager;
    public static final String BIG_IMAGE ="big_url";
    private String[] bigImage ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_big_image);
//        ButterKnife.inject(this);
        String big = getIntent().getStringExtra(BIG_IMAGE);
//        if(!TextUtils.isEmpty(big)){
//            bigImage = big.split(",") ;
//        }
//        ImageAdapter imageAdapter = new ImageAdapter() ;
//        mImageViewPager.setAdapter(imageAdapter);
        BigImageFragment bigImageFragment = new BigImageFragment(big);
        addFragment(bigImageFragment,true);
    }

    public static void startDisplayImage(Context c,String big){
        Intent intent = new Intent(c,DisplayBigImageActivity.class);
        intent.putExtra(BIG_IMAGE,big);
        c.startActivity(intent);
    }
    class ImageAdapter extends FragmentPagerAdapter{

        public ImageAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            return new BigImageFragment(bigImage[position]);
        }

        @Override
        public int getCount() {
            if(bigImage!=null){
                return bigImage.length;
            }
            return 0;
        }
    }
}

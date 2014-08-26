package com.youle.gamebox.ui.view;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.ta.TAActivity;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.GiftActivity;
import com.youle.gamebox.ui.activity.GonglueActivity;
import com.youle.gamebox.ui.activity.MyRelationActivity;
import com.youle.gamebox.ui.activity.NewsActivity;
import com.youle.gamebox.ui.bean.IndexHeadBean;
import com.youle.gamebox.ui.fragment.IndexPPtFragment;
import com.youle.gamebox.ui.greendao.GameBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 14-5-30.
 */
public class IndexHomeHeadView extends LinearLayout implements ViewPager.OnPageChangeListener, View.OnClickListener {
    private final int PPT = 1;
    private final int THEME = 2;
    private final int NEWS = 3;
    private final int DAY_RECOMMEND = 1;
    @InjectView(R.id.pptViewPager)
    ViewPager mViewPage;
    @InjectView(R.id.gameName)
    TextView mGameName;
    @InjectView(R.id.news)
    LinearLayout mNews;
    @InjectView(R.id.gift)
    LinearLayout mGift;
    @InjectView(R.id.strategy)
    LinearLayout mStrategy;
    @InjectView(R.id.dayRecomend)
    LinearLayout mDayRecomend;
    @InjectView(R.id.newsLayout)
    LinearLayout mNewsLayout;
    @InjectView(R.id.themeLayout)
    LinearLayout mThemeLayout;
    @InjectView(R.id.theme)
    LinearLayout mTheme;
    @InjectView(R.id.imageIndicator)
    LinearLayout mImageIndicator;

    private List<IndexHeadBean> pptList = new ArrayList<IndexHeadBean>();
    private List<IndexHeadBean> newsList = new ArrayList<IndexHeadBean>();
    private List<IndexHeadBean> dayRecommendList = new ArrayList<IndexHeadBean>();
    private Map<Integer, Fragment> fragmentMap = new HashMap<Integer, Fragment>();
    private FragmentManager mFragmentManager;

    public IndexHomeHeadView(Context context) {
        super(context);
        mFragmentManager = ((TAActivity) context).getSupportFragmentManager();
        LayoutInflater.from(context).inflate(R.layout.index_head, this);
        ButterKnife.inject(this);
        mTheme.setOnClickListener(this);
        mGift.setOnClickListener(this);
        mStrategy.setOnClickListener(this);
        mNews.setOnClickListener(this);
    }

    private int currentPostion = 0;

    public void initHead(List<IndexHeadBean> beans) {
        //初始化首页幻灯片
        pptList.clear();
        for (IndexHeadBean b : beans) {
            if (b.getPosition() == PPT) {
                pptList.add(b);
            }
        }
        initViewpageIndicator();
        initViewpager();
        selectPositionIndicator(0);
        //新闻头条
        newsList.clear();
        for (IndexHeadBean b : beans) {
            if (b.getPosition() == NEWS) {
                newsList.add(b);
            }
        }
        initNews();
        //专题
        dayRecommendList.clear();
        for (IndexHeadBean b : beans) {
            if (b.getPosition() == THEME) {
                dayRecommendList.add(b);
            }
        }
        initTheme();
    }

    private void initTheme() {
        mThemeLayout.removeAllViews();
        for (IndexHeadBean b : dayRecommendList) {
            HomeThemeView view = new HomeThemeView(getContext());
            view.initData(b);
            LayoutParams p = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            p.weight = 1;
            mThemeLayout.addView(view, p);
        }
    }

    //每日推荐
    public void initRecomendGame(List<GameBean> gameBeanList) {
        for (int i = 0; i < mDayRecomend.getChildCount(); i++) {
            RecommendGridItem item = (RecommendGridItem) mDayRecomend.getChildAt(i);
            item.unRegist();
        }
        mDayRecomend.removeAllViews();
        for (GameBean b : gameBeanList) {
            View gameItem = new RecommendGridItem(getContext(), b);
            mDayRecomend.addView(gameItem);
        }
    }

    private void selectPositionIndicator(int position) {
        for (int i = 0; i < mImageIndicator.getChildCount(); i++) {
            ImageView im = (ImageView) mImageIndicator.getChildAt(i);
            im.setSelected(false);
        }
        ImageView im = (ImageView) mImageIndicator.getChildAt(position);
        if(im != null) {
            im.setSelected(true);
        }
    }

    //头条
    public void initNews() {
        mNewsLayout.removeAllViews();
        for (int i = 0; i < newsList.size(); i++) {
            View newItem = new NewsItemView(getContext(), newsList.get(i));
            mNewsLayout.addView(newItem);
        }
    }

    //幻灯片图片
    boolean first=true;
    private void initViewpager() {
        if (pptList.size() > 0) {
            mGameName.setText(pptList.get(0).getTitle());
        }
        mViewPage.setAdapter(new PPTAdapter(mFragmentManager));
        mViewPage.setOnPageChangeListener(this);
        if(first) {
            mViewPage.postDelayed(playImageRunnable, 3000);
        }
        first = false ;
    }
    int i = 1 ;
    private Runnable playImageRunnable = new Runnable() {
        @Override
        public void run() {
            if(i==pptList.size()){
                i=0 ;
            }
            mViewPage.setCurrentItem(i);
            i++ ;
            mViewPage.postDelayed(this,3000);
        }
    };
    private void initViewpageIndicator() {
        mImageIndicator.removeAllViews();
        LayoutParams p = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        int margin = getResources().getDimensionPixelOffset(R.dimen.home_image_indicator);
        p.setMargins(margin, 0, margin, 0);
        for (int i = 0; i < pptList.size(); i++) {
            ImageView im = new ImageView(getContext());
            im.setImageDrawable(getResources().getDrawable(R.drawable.home_image_indicator));
            mImageIndicator.addView(im, p);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        IndexHeadBean b = pptList.get(position);
        mGameName.setText(b.getTitle());
        currentPostion = position;
        selectPositionIndicator(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
//            ((IndexPPtFragment) fragmentMap.get(currentPostion)).loadData();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.theme) {
            Intent intent = new Intent(getContext(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.RELATION, MyRelationActivity.SPECIAL);
            getContext().startActivity(intent);
        } else if (v.getId() == R.id.gift) {
            Intent intent = new Intent(getContext(), GiftActivity.class);
            getContext().startActivity(intent);
        } else if (v.getId() == R.id.strategy) {
            Intent intent = new Intent(getContext(), GonglueActivity.class);
            getContext().startActivity(intent);
        }else if (v.getId() == R.id.news) {
        	Intent intent = new Intent(getContext(), NewsActivity.class);
            getContext().startActivity(intent);
		}
    }

    class PPTAdapter extends FragmentPagerAdapter {

        public PPTAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment f = fragmentMap.get(i);
            if (f == null) {
                f = new IndexPPtFragment(pptList.get(i));
                fragmentMap.put(i, f);
            }
            return f;
        }

        @Override
        public int getCount() {
            return pptList.size();
        }

    }
//    class PPTAdapter extends PagerAdapter {
//        List<ImageView> imageViews = new ArrayList<ImageView>() ;
//        @Override
//        public int getCount() {
//            return pptList.size();
//        }
//
//        @Override
//        public Object instantiateItem(ViewGroup container, int position) {
//            if(imageViews.size()<=position) {
//                ImageView imageView = new ImageView(getContext());
//                imageView.setImageResource(R.drawable.ic_launcher);
//                imageViews.add(imageView) ;
//            }
//            container.addView(imageViews.get(position));
//            return  imageViews.get(position);
//        }
//
//        @Override
//        public boolean isViewFromObject(View view, Object object) {
//            return view == object;
//        }
//
//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            container.removeView(imageViews.get(position));
//        }
//    }
}

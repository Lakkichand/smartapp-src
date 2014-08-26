package com.youle.gamebox.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import com.youle.gamebox.ui.fragment.CountryFragment;
import com.youle.gamebox.ui.fragment.HomePageDymaicListFragment;
import com.youle.gamebox.ui.fragment.WebViewFragment;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 14-7-9.
 */
public class ComunityActivity extends BaseActivity {
    HomePageDymaicListFragment fragment ;
    WebViewFragment webViewFragment ;

    public void setWebViewFragment(WebViewFragment webViewFragment) {
        this.webViewFragment = webViewFragment;
    }

    public void setFragment(HomePageDymaicListFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CountryFragment countryFragment = new CountryFragment();
        addFragment(countryFragment, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(fragment!=null){
            fragment.onActivityResult(requestCode, resultCode, data);
        }
        if(requestCode==WebViewFragment.FILECHOOSER_RESULTCODE){
            if(webViewFragment!=null){
                webViewFragment.onActivityResult(requestCode,resultCode,data);
            }
        }
    }
}

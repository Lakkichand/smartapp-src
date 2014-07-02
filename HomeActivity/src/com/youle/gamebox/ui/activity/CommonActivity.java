package com.youle.gamebox.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.api.TestRequestApi;
import com.youle.gamebox.ui.fragment.*;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 2014/5/23.
 */
public class CommonActivity extends BaseActivity{
    public final static String KET_FRAGMENT="fragmentType";
    public final static String KET_BUNDLE="b";
    public final static int FRAGMENT_LOGIN=1; //登录fragment
    public final static int FRAGMENT_REGISTER=2; //注册fragment
    public final static int FRAGMENT_USERINO_MODFY=3; //修改资料fragment
    public final static int FRAGMENT_HOMEPAGE=4; //修改资料fragment
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        int fragmentType =FRAGMENT_LOGIN;
        Bundle b =null;
        if(extras.containsKey(KET_FRAGMENT)){
             fragmentType = extras.getInt(KET_FRAGMENT);
        }
        if(extras.containsKey(KET_BUNDLE)){
            b = extras.getBundle(KET_BUNDLE);
        }
        selectLoadFragment(fragmentType,b);
//        testRequest();
    }

    public  void addFragment(BaseFragment fragment){
        addFragment(fragment, true);
    }


    private  void selectLoadFragment(int fragmentType,Bundle bundle){
        switch (fragmentType){
            case FRAGMENT_LOGIN:
                LoginFragment loginFragment = new LoginFragment();
                addFragment(loginFragment);
                break;
            case FRAGMENT_REGISTER:
                RegisterFragment registerFragment = new RegisterFragment();
                addFragment(registerFragment);
                break;
            case FRAGMENT_USERINO_MODFY:
                UserInfoModfyFragment userInfoModfyFragment = new UserInfoModfyFragment();
                userInfoModfyFragment.setArguments(bundle);
                addFragment(userInfoModfyFragment);
                break;
            case FRAGMENT_HOMEPAGE:
                HomepageFragment homepageFragment = new HomepageFragment();
                homepageFragment.setArguments(bundle);
                addFragment(homepageFragment);
                break;
            default:
                break;
        }
    }

    public static void startCommonA(Context c, int fragmentType,Bundle b){
        Intent i = new Intent(c,CommonActivity.class);
        Bundle bundle = new Bundle();
        if(b!=null)bundle.putBundle(KET_BUNDLE,b);
        bundle.putInt(KET_FRAGMENT,fragmentType);
        i.putExtras(bundle);
        c.startActivity(i);
    }

    private void testRequest(){
        LOGUtil.d("junjun", "----------");
        TestRequestApi testRequestApi = new TestRequestApi();
        ZhidianHttpClient.request(testRequestApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                LOGUtil.d("junjun", "----------" + jsonString);
            }
        });
    }
}

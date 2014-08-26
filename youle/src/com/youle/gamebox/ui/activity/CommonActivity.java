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
    public final static int FRAGMENT_LOGIN=1; //登录fragment
    public final static int FRAGMENT_REGISTER=2; //注册fragment
    public final static int FRAGMENT_USERINO_MODFY=3; //修改资料fragment
    public final static int FRAGMENT_HOMEPAGE=4; //修改资料fragment
    public final static int FRAGMENT_USERINFO=5; //个人资料fragment
    public final static String UID = "uid";
    public final static String NICK_NAME= "nick_name";
    private long uid ;
    private String nickName  ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        uid = extras.getLong(UID,-1);
        nickName = extras.getString(NICK_NAME);
        int fragmentType =FRAGMENT_LOGIN;
        if(extras.containsKey(KET_FRAGMENT)){
             fragmentType = extras.getInt(KET_FRAGMENT);
        }
        selectLoadFragment(fragmentType);
    }

    public  void addFragment(BaseFragment fragment){
        addFragment(fragment, true);
    }


    private  void selectLoadFragment(int fragmentType){
        switch (fragmentType){
            case FRAGMENT_LOGIN://登录
                LoginFragment loginFragment = new LoginFragment();
                addFragment(loginFragment);
                break;
            case FRAGMENT_REGISTER://注册
                RegisterFragment registerFragment = new RegisterFragment();
                addFragment(registerFragment);
                break;
            case FRAGMENT_HOMEPAGE://别人的个人中心
                HomepageFragment homepageFragment = new HomepageFragment(uid,nickName);
                addFragment(homepageFragment);
                break;
            case FRAGMENT_USERINFO://用户详情
                UserInfoFragment userInfoFragment = new UserInfoFragment();
                addFragment(userInfoFragment);
                break;
            default:
                break;
        }
    }

    public static void startCommonA(Context c, int fragmentType,long uid){
        Intent i = new Intent(c,CommonActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(KET_FRAGMENT,fragmentType);
        bundle.putLong(UID,uid);
        i.putExtras(bundle);
        c.startActivity(i);
    }

    public static void  startOtherUserDetail(Context c,long uid,String nickName){
        Intent i = new Intent(c,CommonActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(KET_FRAGMENT,FRAGMENT_HOMEPAGE);
        bundle.putLong(UID,uid);
        bundle.putString(NICK_NAME,nickName);
        i.putExtras(bundle);
        c.startActivity(i);
    }
}

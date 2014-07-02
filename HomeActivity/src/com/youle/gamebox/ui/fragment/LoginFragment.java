package com.youle.gamebox.ui.fragment;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.LogUserCache;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.api.pcenter.PCLoginApi;
import com.youle.gamebox.ui.bean.pcenter.PCUserInfoBean;
import com.youle.gamebox.ui.greendao.LogUser;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.*;
import com.youle.gamebox.ui.view.BaseTitleBarView;
import com.youle.gamebox.ui.view.LoginUserListView;
import com.youle.gamebox.ui.view.RoundImageView;

import java.util.List;

/**
 * Created by Administrator on 2014/5/22.
 */
public class LoginFragment extends  BaseFragment implements View.OnClickListener,LoginUserListView.LogUserOnclickItem {

    PopupWindow pop;
    LoginUserListView loginUserListView;
    @InjectView(R.id.userinfo_but)
    Button mUserinfoBut;
    @InjectView(R.id.load_url)
    Button mLoadUrl;
    @InjectView(R.id.relative)
    RelativeLayout mRelative;
    RoundImageView mRoundImageTwoBorder;
    @InjectView(R.id.login_userName_edit)
    EditText mLoginUserNameEdit;
    @InjectView(R.id.login_userName_image)
    ImageView mLoginUserNameImage;
    @InjectView(R.id.login_userPwd_edit)
    EditText mLoginUserPwdEdit;
    @InjectView(R.id.pwd_input_del)
    ImageView mPwdInputDel;
    @InjectView(R.id.input_left_image)
    ImageView mInputLeftImage;
    @InjectView(R.id.login_login)
    Button mLoginLogin;
    @InjectView(R.id.login_error_text)
    TextView mLoginErrorText;
    @InjectView(R.id.login_error_linear)
    LinearLayout mLoginErrorLinear;
    @InjectView(R.id.login_bootom_register)
    TextView mLoginBootomRegister;
    @InjectView(R.id.login_userName_linear)
    RelativeLayout mLoginUserNameLinear;
    @InjectView(R.id.pwd_input_left_image)
    ImageView mPwdInputLeftImage;
    @InjectView(R.id.login_userPwd_linear)
    RelativeLayout mLoginUserPwdLinear;


    @Override
    protected int getViewId() {
        return R.layout.login_layout;
    }




    protected void loadData() {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BaseTitleBarView baseTitleBarView = setTitleView();
        baseTitleBarView.setTitleBarMiddleView(null, "登录");
        baseTitleBarView.setVisiableRightView(View.GONE);
        mUserinfoBut.setOnClickListener(this);
        mLoadUrl.setOnClickListener(this);
        mLoginUserNameImage.setOnClickListener(this);
        mPwdInputDel.setOnClickListener(this);
        mLoginLogin.setOnClickListener(this);
        mLoginBootomRegister.setOnClickListener(this);
        mLoginUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_ed);
        mInputLeftImage.setImageResource(R.drawable.login_name_image_ed);
        mLoginUserNameEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mLoginUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_ed);
                mLoginUserPwdLinear.setBackgroundResource(R.drawable.login_input_bg_nor);
                mInputLeftImage.setImageResource(R.drawable.login_name_image_ed);
                mPwdInputLeftImage.setImageResource(R.drawable.login_pwd_image);
                return false;
            }
        });
        mLoginUserPwdEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mLoginUserPwdLinear.setBackgroundResource(R.drawable.login_input_bg_ed);
                mLoginUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_nor);
                mInputLeftImage.setImageResource(R.drawable.login_name_image);
                mPwdInputLeftImage.setImageResource(R.drawable.login_pwd_image_ed);
                return false;
            }
        });

       // loadApk();
        ViewTreeObserver vto = mLoginUserNameEdit.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if(pop==null)initPop();
                return true;
            }
        });

        List<LogUser> logUserList = new LogUserCache().getLogUserList();
        if(logUserList !=null){
          if(logUserList.size()>0){
              LogUser logUser = logUserList.get(0);
              if(logUser!=null){
                  mLoginUserNameEdit.setText(logUser.getUserName());
                  String pwd = "";
                  try {
                      pwd =  CoderString.decrypt(logUser.getPassword());
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
                  mLoginUserPwdEdit.setText(pwd);
              }
          }
        }

    }



    public void initPop() {
        loginUserListView = new LoginUserListView(getActivity());
        loginUserListView.setLogUserOnclickItem(this);//set interface
        pop = new PopupWindow(loginUserListView, mLoginUserNameEdit.getMeasuredWidth(), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        pop.setOutsideTouchable(false);
        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {


            }
        });
        pop.setBackgroundDrawable(new BitmapDrawable());
    }


    @Override
    public void onClick(View view) {
        if(view == mUserinfoBut){
            UserInfoFragment userInfoFragment = new UserInfoFragment();
            //TestUninstallFragment testUninstallFragment = new TestUninstallFragment();
                    ((CommonActivity) getActivity()).addFragment(userInfoFragment);
        }else if(view ==mLoginUserNameImage ){ //下拉列表
            List<LogUser> logUserList = new LogUserCache().getLogUserList();
            if(logUserList !=null){
                if(loginUserListView!=null)loginUserListView.setData(logUserList);
                pop.showAsDropDown(mLoginUserNameEdit,0,-3);
                mLoginUserNameImage.setImageResource(R.drawable.login_name_down);
                pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mLoginUserNameImage.setImageResource(R.drawable.login_name_up);
                    }
                });


            }
        }else if(view ==mPwdInputDel ){
            mLoginUserPwdEdit.setText("");
        }else if(view ==mLoginLogin ){
            login();

        }else if(view == mLoadUrl){

            //open dymaic
            CountryFragment countryFragment = new CountryFragment();
            ((CommonActivity)getActivity()).addFragment(countryFragment);

            // open homepage
           /* HomepageFragment homepageFragment = new HomepageFragment();
            ((CommonActivity)getActivity()).addFragment(homepageFragment);
           */

            //open url
            /* WebViewFragment webViewFragment = new WebViewFragment("百度","http://www.baidu.com");
            ((CommonActivity)getActivity()).addFragment(webViewFragment);*/
        }else if(view == mLoginBootomRegister){
            RegisterFragment registerFragment = new RegisterFragment();
            ((CommonActivity) getActivity()).addFragment(registerFragment);
        }
    }

    //login

    private void login(){

        mLoginErrorLinear.setVisibility(View.GONE);
        String userName = mLoginUserNameEdit.getText().toString();
        String userPwd = mLoginUserPwdEdit.getText().toString();
        String loginErrorInfo = checkLoginInfo(userName, userPwd);
        if(!"".equals(loginErrorInfo)){
            showError(loginErrorInfo);
            return;
        }
        PCLoginApi pcLoginApi =new PCLoginApi();
        pcLoginApi.setUserName(userName);
        pcLoginApi.setPassword(userPwd);
        ZhidianHttpClient.request(pcLoginApi,new JsonHttpListener(this) {

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                LOGUtil.d("login", " ----- " + jsonString);
                String pcCheckCode = CodeCheck.pcCheckCode(jsonString);
                if(pcCheckCode != null && !"".equals(pcCheckCode)){
                    showError(pcCheckCode);
                }
            }

            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                LOGUtil.d("login"," ----- "+jsonString);
                try {
                    PCUserInfoBean pcUserInfoBean = jsonToBean(PCUserInfoBean.class, jsonString, "data");
                    if(pcUserInfoBean!=null){
                        Toast.makeText(getActivity(),"--- "+pcUserInfoBean.getNickName()+" ---",Toast.LENGTH_SHORT).show();
                    }
                    String sid = CodeCheck.jsonToCode(jsonString, "sid");
                    saveUserInfo(pcUserInfoBean,sid);
                    back(); // 退出

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });


    }


    // show error
    private void showError(String loginErrorInfo){
        mLoginErrorLinear.setVisibility(View.VISIBLE);
        mLoginErrorText.setText(loginErrorInfo);
    }


    // check login info
    private  String checkLoginInfo(String userName,String userPwd){
        String login_error="";
        if(null == userName || "".equals(userName))
            return "用户名不能为空";
        if(null == userPwd || "".equals(userPwd))
            return "密码不能为空";
        if(CheckMatcherUtil.checkfilename(userName)){
            return "用户名不能包含中文";
        }
        if(userName.length()<6)
            return "用户名不能少于六位";
        if(userPwd.length()<6)
            return "密码不能少于六位";
        return login_error;
    }

    // list user onclick
    @Override
    public void onclik(LogUser logUser) {
        if(logUser!=null){
            mLoginUserNameEdit.setText(logUser.getUserName());
            String pwd = "";
            try {
                pwd =  CoderString.decrypt(logUser.getPassword());
            } catch (Exception e) {
                e.printStackTrace();
            }
            mLoginUserPwdEdit.setText(pwd);
        }
        if(pop !=null){
            pop.dismiss();
        }
    }


    // sava user info
    private void saveUserInfo(PCUserInfoBean pcUserInfoBean,String sid){
        if(pcUserInfoBean==null)return;
        UserInfo userInfo = new UserInfo();
        userInfo.setSid(sid);
        userInfo.setUid(pcUserInfoBean.getUid());
        userInfo.setUserName(pcUserInfoBean.getUserName());
        userInfo.setNickName(pcUserInfoBean.getNickName());
        userInfo.setSignature(pcUserInfoBean.getSignature());
        userInfo.setQq(pcUserInfoBean.getQq());
        userInfo.setContact(pcUserInfoBean.getContact());
        userInfo.setBigAvatarUrl(pcUserInfoBean.getBigAvatarUrl());
        userInfo.setSmallAvatarUrl(pcUserInfoBean.getSmallAvatarUrl());
        userInfo.setIsSign(pcUserInfoBean.isSign());
        new UserInfoCache().saveUserInfo(userInfo);
    }
}

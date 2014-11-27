package com.youle.gamebox.ui.fragment;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.account.UserCache;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.api.pcenter.PCLoginApi;
import com.youle.gamebox.ui.bean.LogAccount;
import com.youle.gamebox.ui.bean.MessageNumberBean;
import com.youle.gamebox.ui.bean.pcenter.PCUserInfoBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.*;
import com.youle.gamebox.ui.view.LoginUserListView;
import com.youle.gamebox.ui.view.RoundImageView;

import java.util.List;

/**
 * Created by Administrator on 2014/5/22.
 */
public class LoginFragment extends BaseFragment implements View.OnClickListener, LoginUserListView.LogUserOnclickItem {

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
    @InjectView(R.id.login_error_image)
    ImageView mLoginErrorImage;
    @InjectView(R.id.forgotPassword)
    TextView mForgotPassword;
    List<LogAccount> logUserList;

    @Override
    protected int getViewId() {
        return R.layout.login_layout;
    }

    @Override
    protected String getModelName() {
        return "登录";
    }

    protected void loadData() {

    }


    @Override
    public void goBack() {
        SoftkeyboardUtil.hideSoftKeyBoard(getActivity(), mLoginUserNameEdit);
        super.goBack();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDefaultTitle("登录");
        mForgotPassword.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
        mForgotPassword.setTextColor(getResources().getColor(R.color.for_gray));
        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewFragment webViewFragment = new WebViewFragment("找回密码", YouleAplication.FORFOT_PASS);
                ((BaseActivity) getActivity()).addFragment(webViewFragment, true);
            }
        });
        mUserinfoBut.setOnClickListener(this);
        mLoadUrl.setOnClickListener(this);
        mLoginUserNameImage.setOnClickListener(this);
        mPwdInputDel.setOnClickListener(this);
        mLoginLogin.setOnClickListener(this);
        mLoginBootomRegister.setOnClickListener(this);
        mInputLeftImage.setImageResource(R.drawable.login_name_image_ed);
        mLoginUserNameEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mInputLeftImage.setImageResource(R.drawable.login_name_image_ed);
                mPwdInputLeftImage.setImageResource(R.drawable.login_pwd_image);
                return false;
            }
        });
        mLoginUserPwdEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mInputLeftImage.setImageResource(R.drawable.login_name_image);
                mPwdInputLeftImage.setImageResource(R.drawable.login_pwd_image_ed);
                return false;
            }
        });

        // loadApk();
        ViewTreeObserver vto = mLoginUserNameEdit.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                if (pop == null) initPop();
                return true;
            }
        });
        List<LogAccount> logAccountList = new UserCache().getAccountList();
//        List<LogUser> logUserList = new LogUserCache().getLogUserList();
        if (logAccountList != null) {
            if (logAccountList.size() > 0) {
                LogAccount logUser = logAccountList.get(0);
                if (logUser != null) {
                    mLoginUserNameEdit.setText(logUser.getUserName());
                    mLoginUserPwdEdit.setText(logUser.getPassword());
                }
            }
        }

    }


    public void initPop() {
        loginUserListView = new LoginUserListView(getActivity());
        loginUserListView.setLogUserOnclickItem(this);//set interface
        pop = new PopupWindow(loginUserListView, mLoginUserNameLinear.getMeasuredWidth(), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        pop.setOutsideTouchable(false);
        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mLoginUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_ed);

            }
        });
        pop.setBackgroundDrawable(new BitmapDrawable());
    }


    @Override
    public void onClick(View view) {
        if (view == mUserinfoBut) {
            UserInfoFragment userInfoFragment = new UserInfoFragment();
            //TestUninstallFragment testUninstallFragment = new TestUninstallFragment();
            ((CommonActivity) getActivity()).addFragment(userInfoFragment);
        } else if (view == mLoginUserNameImage) { //下拉列表
            logUserList = new UserCache().getAccountList();
            if (logUserList != null) {
                if (loginUserListView != null) loginUserListView.setData(logUserList);
                pop.showAsDropDown(mLoginUserNameLinear, 0, 0);
                mLoginUserNameImage.setImageResource(R.drawable.login_name_down);
                pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        mLoginUserNameImage.setImageResource(R.drawable.login_name_up);
                        mLoginUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_nor);
                    }
                });


            }
        } else if (view == mPwdInputDel) {
            mLoginUserPwdEdit.setText("");
        } else if (view == mLoginLogin) {
            login();
            SoftkeyboardUtil.hideSoftKeyBoard(getActivity(), mLoginUserNameEdit);
        } else if (view == mLoadUrl) {

            //open dymaic
            CountryFragment countryFragment = new CountryFragment();
            ((CommonActivity) getActivity()).addFragment(countryFragment);
        } else if (view == mLoginBootomRegister) {
            RegisterFragment registerFragment = new RegisterFragment();
            ((CommonActivity) getActivity()).addFragment(registerFragment);
        }
    }

    //login

    private void login() {

//        mLoginErrorLinear.setVisibility(View.GONE);
        String userName = mLoginUserNameEdit.getText().toString();
        String userPwd = mLoginUserPwdEdit.getText().toString();
        String loginErrorInfo = checkLoginInfo(userName, userPwd);
        if (!"".equals(loginErrorInfo)) {
            showError(loginErrorInfo);
            return;
        }
        PCLoginApi pcLoginApi = new PCLoginApi();
        pcLoginApi.setUserName(userName);
        pcLoginApi.setPassword(userPwd);
        pcLoginApi.setPackageVersions(AppInfoUtils.getPkgAndVersion(getActivity()));
        ZhidianHttpClient.request(pcLoginApi, new JsonHttpListener(getActivity(), getString(R.string.logining)) {

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                LOGUtil.d("login", " ----- " + jsonString);
                String pcCheckCode = CodeCheck.pcCheckCode(jsonString);
                if (pcCheckCode != null && !"".equals(pcCheckCode)) {
                    showError(pcCheckCode);
                }
            }

            @Override
            public void onRequestSuccess(String jsonString) {
                LOGUtil.d("login", " ----- " + jsonString);
                try {
                    PCUserInfoBean pcUserInfoBean = jsonToBean(PCUserInfoBean.class, jsonString, "data");
                    if (pcUserInfoBean != null) {
                        TOASTUtil.showSHORT(getActivity(), "登录成功");
                    }
                    String sid = CodeCheck.jsonToCode(jsonString, "sid");
                    saveUserInfo(pcUserInfoBean, sid);
                    MessageNumberBean b = jsonToBean(MessageNumberBean.class, jsonString, "leftprompt");
                    YouleAplication.messageNumberBean = b;
                    getActivity().finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


    }


    // show error
    private void showError(String loginErrorInfo) {
        mLoginErrorText.setText(loginErrorInfo);
        mLoginErrorImage.setVisibility(View.VISIBLE);
    }


    // check login info
    private String checkLoginInfo(String userName, String userPwd) {
        mLoginUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_nor);
        mLoginUserPwdLinear.setBackgroundResource(R.drawable.login_input_bg_nor);
        String login_error = "";
        if (null == userName || "".equals(userName)) {
            mLoginUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_ed);
            return "用户名不能为空";
        }
        if (null == userPwd || "".equals(userPwd)) {
            mLoginUserPwdLinear.setBackgroundResource(R.drawable.login_input_bg_ed);
            return "密码不能为空";
        }
        if (CheckMatcherUtil.checkfilename(userName)) {
            mLoginUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_ed);
            return "用户名不能包含中文";
        }
        return login_error;
    }

    // list user onclick
    @Override
    public void onUserSelect(LogAccount logUser) {
        if (logUser != null) {
            mLoginUserNameEdit.setText(logUser.getUserName());
            mLoginUserPwdEdit.setText(logUser.getPassword());
        }
        if (pop != null) {
            pop.dismiss();
        }
    }

    @Override
    public void onUserDelete(LogAccount user) {
        if (mLoginUserNameEdit.getText().toString().equals(user.getUserName())) {
            mLoginUserNameEdit.setText("");
            mLoginUserPwdEdit.setText("");
        }
        if (logUserList.size() == 0) {
            pop.dismiss();
        }
    }


    // sava user info
    private void saveUserInfo(PCUserInfoBean pcUserInfoBean, String sid) {
        if (pcUserInfoBean == null) return;
        UserInfo userInfo = new UserInfo();
        userInfo.setSid(sid);
        userInfo.setUid(pcUserInfoBean.getUid());
        userInfo.setUserName(pcUserInfoBean.getUserName());
        userInfo.setNickName(pcUserInfoBean.getNickName());
        userInfo.setSignature(pcUserInfoBean.getSignature());
        userInfo.setQq(pcUserInfoBean.getQq());
        userInfo.setContact(pcUserInfoBean.getEmail());
        userInfo.setBigAvatarUrl(pcUserInfoBean.getBigAvatarUrl());
        userInfo.setSmallAvatarUrl(pcUserInfoBean.getSmallAvatarUrl());
        userInfo.setIsSign(pcUserInfoBean.isSign());
        userInfo.setScore(pcUserInfoBean.getScore());
        new UserInfoCache().saveUserInfo(userInfo);
        LogAccount account = new LogAccount();
        account.setUserName(pcUserInfoBean.getUserName());
        account.setPassword(mLoginUserPwdEdit.getText().toString());
        account.setOption("0");
        new UserCache().saveAcount(account);
        CookiesUtil.getInstance(getActivity()).setCookies("http://y6.cn", sid, ".y6.cn");
    }
}

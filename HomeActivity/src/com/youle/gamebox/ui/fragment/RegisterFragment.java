package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.LogUserCache;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.api.pcenter.PCRegisterApi;
import com.youle.gamebox.ui.bean.pcenter.PCUserInfoBean;
import com.youle.gamebox.ui.greendao.LogUser;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.CheckMatcherUtil;
import com.youle.gamebox.ui.util.CodeCheck;
import com.youle.gamebox.ui.view.BaseTitleBarView;

/**
 * Created by Administrator on 2014/5/22.
 */
public class RegisterFragment extends  BaseFragment implements View.OnClickListener ,OnClickAggreen {


    @InjectView(R.id.input_left_image)
    ImageView mInputLeftImage;
    @InjectView(R.id.register_userName_edit)
    EditText mRegisterUserNameEdit;
    @InjectView(R.id.register_userName_image)
    ImageView mRegisterUserNameImage;
    @InjectView(R.id.pwd_input_left_image)
    ImageView mPwdInputLeftImage;
    @InjectView(R.id.register_userPwd_edit)
    EditText mRegisterUserPwdEdit;
    @InjectView(R.id.pwd_input_del)
    ImageView mPwdInputDel;
    @InjectView(R.id.register_error_image)
    ImageView mRegisterErrorImage;
    @InjectView(R.id.register_error_text)
    TextView mRegisterErrorText;
    @InjectView(R.id.register_error_linear)
    LinearLayout mRegisterErrorLinear;
    @InjectView(R.id.register_check)
    CheckBox mRegisterCheck;
    @InjectView(R.id.register_check_text)
    TextView mRegisterCheckText;
    @InjectView(R.id.register_register)
    Button mRegisterRegister;
    @InjectView(R.id.register_bootom_register)
    TextView mRegisterBootomRegister;
    @InjectView(R.id.register_bootom_text)
    LinearLayout mRegisterBootomText;
    @InjectView(R.id.register_check_xieyi)
    TextView mRegisterCheckXieyi;
    @InjectView(R.id.register_userName_linear)
    RelativeLayout mRegisterUserNameLinear;
    @InjectView(R.id.register_userPwd_linear)
    RelativeLayout mRegisterUserPwdLinear;

    @Override
    protected int getViewId() {
        return R.layout.register_layout;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BaseTitleBarView baseTitleBarView = setTitleView();
        baseTitleBarView.setTitleBarMiddleView(null, "注册");
        baseTitleBarView.setVisiableRightView(View.GONE);
        mRegisterRegister.setOnClickListener(this);
        mRegisterUserNameImage.setOnClickListener(this);
        mPwdInputDel.setOnClickListener(this);
        mRegisterCheckText.setOnClickListener(this);
        mRegisterCheckXieyi.setOnClickListener(this);
        mRegisterBootomRegister.setOnClickListener(this);
        mRegisterUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_ed);
        mInputLeftImage.setImageResource(R.drawable.login_name_image_ed);
        mRegisterUserNameEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mRegisterUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_ed);
                mRegisterUserPwdLinear.setBackgroundResource(R.drawable.login_input_bg_nor);
                mInputLeftImage.setImageResource(R.drawable.login_name_image_ed);
                mPwdInputLeftImage.setImageResource(R.drawable.login_pwd_image);
                return false;
            }
        });
        mRegisterUserPwdEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mRegisterUserPwdLinear.setBackgroundResource(R.drawable.login_input_bg_ed);
                mRegisterUserNameLinear.setBackgroundResource(R.drawable.login_input_bg_nor);
                mInputLeftImage.setImageResource(R.drawable.login_name_image);
                mPwdInputLeftImage.setImageResource(R.drawable.login_pwd_image_ed);
                return false;
            }
        });

    }
    PCRegisterApi pcRegisterApi = null;
    private void register(){
        pcRegisterApi = null;
        mRegisterErrorLinear.setVisibility(View.GONE);
        String userName = mRegisterUserNameEdit.getText().toString();
        String userPwd = mRegisterUserPwdEdit.getText().toString();
        String registerErrorInfo = checkLoginInfo(userName, userPwd);
        if(!"".equals(registerErrorInfo)){
            showError(registerErrorInfo);
            return;
        }
        pcRegisterApi =new PCRegisterApi();
        pcRegisterApi.setUserName(userName);
        pcRegisterApi.setPassword(userPwd);

        //=======================

        LogUser logUser = new LogUser();
        logUser.setUserName(pcRegisterApi.getUserName());
        logUser.setPassword(pcRegisterApi.getPassword());
        long timeMillis = System.currentTimeMillis();
        logUser.setLastLogin(timeMillis);
        new LogUserCache().SaveLogUser(logUser);
        ZhidianHttpClient.request(pcRegisterApi, new JsonHttpListener(this) {

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                String pcCheckCode = CodeCheck.pcCheckCode(jsonString);
                if (pcCheckCode != null && !"".equals(pcCheckCode)) {
                    showError(pcCheckCode);
                }


            }

            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                    PCUserInfoBean pcUserInfoBean = jsonToBean(PCUserInfoBean.class, jsonString, "data");
                    LogUser logUser = new LogUser();
                    logUser.setUserName(pcRegisterApi.getUserName());
                    logUser.setPassword(pcRegisterApi.getPassword());
                    long timeMillis = System.currentTimeMillis();
                    logUser.setLastLogin(timeMillis);
                    new LogUserCache().SaveLogUser(logUser);
                    if (pcUserInfoBean != null) {
                        Toast.makeText(getActivity(), "--- " + pcUserInfoBean.getNickName() + " ---", Toast.LENGTH_SHORT).show();
                    }
                    String sid = CodeCheck.jsonToCode(jsonString, "sid");
                    saveUserInfo(pcUserInfoBean,sid);
                    back(); // 退出
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });





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
        if(userPwd.length()<6)
            return "密码不能少于六位";

        if(!mRegisterCheck.isChecked())
            return "必须勾选用户协议";
        return login_error;
    }
    // show error
    private void showError(String loginErrorInfo){
        mRegisterErrorLinear.setVisibility(View.VISIBLE);
        mRegisterErrorText.setText(loginErrorInfo);
    }

    @Override
    public void onClick(View v) {
        if(v == mRegisterRegister){
            register();
        }else if(v == mRegisterUserNameImage){
            mRegisterUserNameEdit.setText("");
        }else if(v == mPwdInputDel){
            mRegisterUserPwdEdit.setText("");
        }else if(v == mRegisterCheckText){
           if(mRegisterCheck.isChecked()){
               mRegisterCheck.setChecked(false);
           }else{
               mRegisterCheck.setChecked(true);
           }
        }else if(v == mRegisterCheckXieyi){ // 用户协议
            PCenterAgreementFragment pCenterAgreementFragment = new PCenterAgreementFragment();
            pCenterAgreementFragment.setOnClickAggreen(this);
            ((CommonActivity) getActivity()).addFragment(pCenterAgreementFragment);
        }else if(v == mRegisterBootomRegister){ // 用户协议
            LoginFragment loginFragment = new LoginFragment();
            ((CommonActivity) getActivity()).addFragment(loginFragment);
        }


    }

    @Override
    public boolean onclikAgreen(boolean isbool) {
        mRegisterCheck.setChecked(isbool);
        return false;
    }
}
 interface OnClickAggreen{
    boolean  onclikAgreen(boolean isbool);
}


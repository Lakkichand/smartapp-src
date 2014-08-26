package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.pcenter.PCUserInfoModfyNickNameApi;
import com.youle.gamebox.ui.api.pcenter.PCUserInfoModfyQQApi;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.SoftkeyboardUtil;
import com.youle.gamebox.ui.util.TOASTUtil;
import com.youle.gamebox.ui.util.UIUtil;

import java.util.regex.Pattern;

/**
 * Created by Administrator on 2014/6/12.
 */
public class UserInfoModfyFragment extends BaseFragment implements View.OnClickListener {
    @InjectView(R.id.userinfo_photo_modfy_edit)
    EditText mUserinfoPhotoModfyEdit;
    @InjectView(R.id.userinfo_photo_modfy_but)
    Button mUserinfoPhotoModfyBut;

    String content = "";
    String title = "";

    public UserInfoModfyFragment(String content, String title) {
        this.content = content;
        this.title = title;
    }

    @Override
    protected int getViewId() {
        return R.layout.userinfo_modfy_layout;
    }

    @Override
    protected String getModelName() {
        return "修改资料";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if("QQ".equals(title)){
            mUserinfoPhotoModfyEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
        }
        if (content == null || "".equals(content)) {
            String hit = "";
            if ("昵称".equals(title)) {
                hit = "请输入昵称";
            } else if ("QQ".equals(title)) {
                hit = "请输入QQ号";
            }
            mUserinfoPhotoModfyEdit.setHint(hit);
        } else {
            mUserinfoPhotoModfyEdit.setText(content);
        }
        setDefaultTitle(getString(R.string.modyfy_title_format, title));
        mUserinfoPhotoModfyBut.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if ("昵称".equals(title)) {
            requestNickName();
        } else if ("QQ".equals(title)) {
            requestQQ();
        }
    }

    // 1 昵称
    // 2.qq
    private boolean checkValue(int type) {
        boolean falg = true;
        String editValue = mUserinfoPhotoModfyEdit.getText().toString().trim();
        if (null == editValue || "".equals(editValue)) {
            TOASTUtil.showSHORT(getActivity(), R.string.userinfo_photo_modfy_toast);
            falg = false;
        }
        if (editValue.equals(content)) {
            TOASTUtil.showSHORT(getActivity(), R.string.userinfo_photo_modfy_equels);
            falg = false;
        }
        if (type == 1) {
            if (editValue.length() > 16) {
                TOASTUtil.showSHORT(getActivity(), R.string.name_length);
                falg = false;
            }
        } else {
            Pattern p = Pattern.compile("[0-9]{6,15}");
            if (!p.matcher(editValue).matches()) {
                UIUtil.toast(getActivity(),R.string.qq_tip);
                falg = false;
            }
        }
        return falg;
    }

    public void request(AbstractApi abstractApi, final int type) {
        SoftkeyboardUtil.hideSoftKeyBoard(getActivity(),mUserinfoPhotoModfyEdit);
        ZhidianHttpClient.request(abstractApi, new JsonHttpListener(getActivity(),"正在修改"+title) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                TOASTUtil.showSHORT(getActivity(), R.string.userinfo_photo_modfy_sucess);
                UserInfo userInfo = new UserInfoCache().getUserInfo();
                content = mUserinfoPhotoModfyEdit.getText().toString().trim();

                if (type == 1) {
                    userInfo.setNickName(mUserinfoPhotoModfyEdit.getText().toString().trim());
                } else if (type == 2) {
                    userInfo.setQq(mUserinfoPhotoModfyEdit.getText().toString().trim());
                }
                new UserInfoCache().saveUserInfo(userInfo);
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                TOASTUtil.showSHORT(getActivity(), R.string.userinfo_photo_modfy_fail);
            }
        });
    }

    private void requestNickName() {
        String editValue = mUserinfoPhotoModfyEdit.getText().toString().trim();
        if (!checkValue(1)) return;
        final PCUserInfoModfyNickNameApi pcUserInfoModfyNickNameApi = new PCUserInfoModfyNickNameApi();
        String sid = new UserInfoCache().getSid();
        LOGUtil.d("junjun", "------" + sid);
        pcUserInfoModfyNickNameApi.setSid(new UserInfoCache().getSid());
        pcUserInfoModfyNickNameApi.setNickName(editValue);
        request(pcUserInfoModfyNickNameApi, 1);

    }

    private void requestQQ() {
        String editValue = mUserinfoPhotoModfyEdit.getText().toString().trim();
        if (!checkValue(2)) return;
        final PCUserInfoModfyQQApi pcUserInfoModfyQQApi = new PCUserInfoModfyQQApi();
        pcUserInfoModfyQQApi.setSid(new UserInfoCache().getSid());
        pcUserInfoModfyQQApi.setQq(editValue);
        request(pcUserInfoModfyQQApi, 2);
    }


}

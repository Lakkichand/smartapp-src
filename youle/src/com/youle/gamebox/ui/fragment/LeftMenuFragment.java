package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.Html;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.InjectView;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.*;
import com.youle.gamebox.ui.api.CheckUpdataApi;
import com.youle.gamebox.ui.api.LogoutApi;
import com.youle.gamebox.ui.bean.MessageNumberBean;
import com.youle.gamebox.ui.bean.UpdataBean;
import com.youle.gamebox.ui.bean.pcenter.PCUserInfoBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.*;
import com.youle.gamebox.ui.view.RoundImageView;
import com.youle.gamebox.ui.view.UpdataDialog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;

/**
 * Created by Administrator on 14-5-12.
 */
public class LeftMenuFragment extends BaseFragment implements View.OnClickListener {

    @InjectView(R.id.phoneMermeryProgress)
    ProgressBar mermeryPro;
    @InjectView(R.id.phoneMermery)
    TextView mPhoneMermery;
    @InjectView(R.id.sdcardProgress)
    ProgressBar sdcarMermeryPro;
    @InjectView(R.id.sdcardMemery)
    TextView mSdcardMermery;
    @InjectView(R.id.appManager)
    View appManager;
    @InjectView(R.id.gift)
    View gift;
    @InjectView(R.id.strategy)
    View stragegy;
    @InjectView(R.id.message)
    View message;
    @InjectView(R.id.setting)
    View setting;
    @InjectView(R.id.update)
    View update;
    @InjectView(R.id.opinion)
    View opinion;
    @InjectView(R.id.about)
    View about;
    @InjectView(R.id.logout)
    View logout;
    @InjectView(R.id.exit)
    View exit;
    @InjectView(R.id.loginLayout)
    View loginLayout;
    @InjectView(R.id.userName)
    TextView userName;
    @InjectView(R.id.userAvatar)
    RoundImageView avatarImage;
    @InjectView(R.id.email)
    TextView emailText;
    @InjectView(R.id.managerNumber)
    TextView managerNumber;
    @InjectView(R.id.giftNumber)
    TextView giftNumber;
    @InjectView(R.id.stagoryNumber)
    TextView stagoryNumber;
    @InjectView(R.id.messageNumber)
    TextView messageNumber;

    @Override
    protected int getViewId() {
        return R.layout.fragment_left_menu;
    }

    @Override
    protected String getModelName() {
        return "左侧菜单";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((HomeActivity)getActivity()).setLeftMenuFragment(this);
        loadData();

    }

    @Override
    public void onResume() {
        super.onResume();
        initUser();
        initNumber();
    }

    public void initUser() {
        final UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (userInfo != null) {
            ImageLoadUtil.displayAvatarImage(userInfo.getBigAvatarUrl(), avatarImage);
            userName.setText(userInfo.getNickName());
            emailText.setText(userInfo.getContact());
            loginLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonActivity.startOtherUserDetail(getActivity(),userInfo.getUid(),userInfo.getNickName());
                }
            });
        } else {
            userName.setText("未登录");
            emailText.setText("点击登录，享跟多特权");
            avatarImage.setImageDrawable(getResources().getDrawable(R.drawable.pc_user_photo));
            loginLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommonActivity.startCommonA(getActivity(), CommonActivity.FRAGMENT_LOGIN, -1);
                }
            });
        }
    }

    protected void loadData() {
        getRomSpace();
        getSDCardSpace();
        initEvent();
    }

    private void initEvent() {
        appManager.setOnClickListener(this);
        gift.setOnClickListener(this);
        stragegy.setOnClickListener(this);
        message.setOnClickListener(this);
        setting.setOnClickListener(this);
        update.setOnClickListener(this);
        opinion.setOnClickListener(this);
        about.setOnClickListener(this);
        logout.setOnClickListener(this);
        exit.setOnClickListener(this);
    }


    public void initNumber() {
        MessageNumberBean b = YouleAplication.messageNumberBean;
        if (b != null) {
//            if (b.getGlCount() > 0) {
//                stagoryNumber.setVisibility(View.VISIBLE);
//                stagoryNumber.setText(b.getGlCount() + "");
//            } else {
//                stagoryNumber.setVisibility(View.GONE);
//            }
            stagoryNumber.setVisibility(View.GONE);
            if (b.getGmCount() > 0) {
                managerNumber.setVisibility(View.VISIBLE);
                managerNumber.setText(b.getGmCount() + "");
            } else {
                managerNumber.setVisibility(View.GONE);
            }
            if (b.getMsgCount() > 0) {
                messageNumber.setVisibility(View.VISIBLE);
                messageNumber.setText(b.getMsgCount() + "");
            } else {
                messageNumber.setVisibility(View.GONE);
            }
        } else {
            stagoryNumber.setVisibility(View.GONE);
            managerNumber.setVisibility(View.GONE);
            messageNumber.setVisibility(View.GONE);
        }
    }

    private void getSDCardSpace() {
        File path = Environment.getExternalStorageDirectory();//得到SD卡的路径
        StatFs stat = new StatFs(path.getPath());//创建StatFs对象，用来获取文件系统的状态
        long blockCount = stat.getBlockCount();
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        String totalSize = Formatter.formatFileSize(getActivity().getBaseContext(), blockCount * blockSize);//格式化获得SD卡总容量
        String availableSize = Formatter.formatFileSize(getActivity().getBaseContext(), blockSize * (blockCount - availableBlocks));//获得SD卡可用容量
        String html = getString(R.string.left_cach_format, availableSize + "", totalSize + "");
        mSdcardMermery.setText(Html.fromHtml(html));
        sdcarMermeryPro.setMax((int) (blockCount * blockSize / 1000));
        sdcarMermeryPro.setProgress((int) (blockSize * (blockCount - availableBlocks) / 1000));
        LOGUtil.e("getSD", blockCount * blockSize + "-->" + availableBlocks * blockSize);
    }

    private void getRomSpace() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockCount = stat.getBlockCount();
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        String totalSize = Formatter.formatFileSize(getActivity().getBaseContext(), blockCount * blockSize);
        String availableSize = Formatter.formatFileSize(getActivity().getBaseContext(), blockSize * (blockCount - availableBlocks));
        mermeryPro.setMax((int) (blockCount * blockSize / 1000));
        mermeryPro.setProgress((int) (blockSize * (blockCount - availableBlocks)/ 1000));
        String html = getString(R.string.left_cach_format, availableSize + "", totalSize + "");
        mPhoneMermery.setText(Html.fromHtml(html));
    }


    @Override
    public void onClick(View v) {
        if(getActivity()==null) return;
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (v.getId() == R.id.appManager) {
            Intent intent = new Intent(getActivity(), DownLoadManagerActivity.class);
            intent.putExtra(DownLoadManagerActivity.TYPE,DownloadManagerFragment.UPDATE);
            startActivity(intent);
        } else if (v.getId() == R.id.setting) {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.strategy) {
                Intent intent = new Intent(getActivity(), MyRelationActivity.class);
                intent.putExtra(MyRelationActivity.RELATION, MyRelationActivity.CATAGRORY);
                startActivity(intent);
        } else if (v.getId() == R.id.gift) {
            if (userInfo != null) {
                Intent intent = new Intent(getActivity(), MyRelationActivity.class);
                intent.putExtra(MyRelationActivity.RELATION, MyRelationActivity.GIFT);
                startActivity(intent);
            } else {
                CommonActivity.startCommonA(getActivity(), CommonActivity.FRAGMENT_LOGIN, -1);
            }
        } else if (v.getId() == R.id.message) {
            if (userInfo != null) {
                Intent intent = new Intent(getActivity(), MessageActivity.class);
                startActivity(intent);
            } else {
                CommonActivity.startCommonA(getActivity(), CommonActivity.FRAGMENT_LOGIN, -1);
            }
        } else if (v.getId() == R.id.logout) {
            logout();
        } else if (v.getId() == R.id.opinion) {
            Intent intent = new Intent(getActivity(), FeedbackActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.about) {
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            startActivity(intent);
        } else if(v.getId() == R.id.update){
            checkUpdate();
        }
    }

    private void checkUpdate() {
        CheckUpdataApi checkUpdataApi = new CheckUpdataApi();
        try {
            PackageInfo pi=getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            checkUpdataApi.setChannelCode(SDKUtils.getChanalID(getActivity()));
            checkUpdataApi.setVersionCode(pi.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ZhidianHttpClient.request(checkUpdataApi,new JsonHttpListener(getActivity(),"正在检查版本"){
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    UpdataBean upBean = jsonToBean(UpdataBean.class,jsonString);
                    if(upBean!=null){
                        new UpdataDialog(getActivity(),upBean).show();
                    }else {
                        new UpdataDialog(getActivity()).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void logout() {
        UserInfoCache userInfoCache = new UserInfoCache();
        if (userInfoCache.getUserInfo() == null) {
            TOASTUtil.showSHORT(getActivity(), "您未登录");
        } else {
            logoutFromServer(userInfoCache.getUserInfo());
        }
    }

    private void logoutFromServer(UserInfo userInfo) {
        if(userInfo == null) {
            return;
        }

        LogoutApi logoutApi = new LogoutApi();
        logoutApi.setSid(userInfo.getSid());
        ZhidianHttpClient.request(logoutApi, new JsonHttpListener(getActivity(),getString(R.string.logout_ing)) {
            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
            }

            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                TOASTUtil.showSHORT(getActivity(), "已注销");
                //通知服务器退出登录
                //删除本地数据
                new UserInfoCache().delUserInfoTable();
                //改变内存变量
                YouleAplication.messageNumberBean.setGlCount(0);
                YouleAplication.messageNumberBean.setMsgCount(0);
                initUser();
                ((HomeActivity)getActivity()).onLogoutSuccess();
            }
        });
    }

}

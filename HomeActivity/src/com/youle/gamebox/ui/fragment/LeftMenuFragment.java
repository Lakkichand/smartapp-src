package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.DownLoadManagerActivity;
import com.youle.gamebox.ui.activity.MessageActivity;
import com.youle.gamebox.ui.activity.MyRelationActivity;
import com.youle.gamebox.ui.activity.SettingActivity;
import com.youle.gamebox.ui.util.LOGUtil;

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

    @Override
    protected int getViewId() {
        return R.layout.fragment_left_menu;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
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

    private void getSDCardSpace() {
        File path = Environment.getExternalStorageDirectory();//得到SD卡的路径
        StatFs stat = new StatFs(path.getPath());//创建StatFs对象，用来获取文件系统的状态
        long blockCount = stat.getBlockCount();
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();

        String totalSize = Formatter.formatFileSize(getActivity().getBaseContext(), blockCount * blockSize);//格式化获得SD卡总容量
        String availableSize = Formatter.formatFileSize(getActivity().getBaseContext(), blockSize * (blockCount - availableBlocks));//获得SD卡可用容量
        mSdcardMermery.setText("SD卡内存：" + availableSize + "/" + totalSize);
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
        mermeryPro.setMax((int) (blockCount * blockSize));
        mermeryPro.setProgress((int) (blockSize * (blockCount - availableBlocks)));
        mPhoneMermery.setText("手机内存：" + availableSize + "/" + totalSize);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.appManager) {
            Intent intent = new Intent(getActivity(), DownLoadManagerActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.setting) {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.strategy) {
            Intent intent = new Intent(getActivity(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.RELATION, MyRelationActivity.CATAGRORY);
            startActivity(intent);
        } else if (v.getId() == R.id.gift) {
            Intent intent = new Intent(getActivity(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.RELATION, MyRelationActivity.GIFT);
            startActivity(intent);
        } else if (v.getId() == R.id.message) {
            Intent intent = new Intent(getActivity(), MessageActivity.class);
            startActivity(intent);
        }

    }
}

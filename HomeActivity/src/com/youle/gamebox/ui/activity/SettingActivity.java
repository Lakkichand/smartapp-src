package com.youle.gamebox.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.ta.util.config.TAIConfig;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 14-6-19.
 */
public class SettingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    @InjectView(R.id.back)
    ImageView mBack;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.acceptMessage)
    CheckBox mAcceptMessage;
    @InjectView(R.id.autoInstall)
    CheckBox mAutoInstall;
    @InjectView(R.id.deleteInstalled)
    CheckBox mDeleteInstalled;
    @InjectView(R.id.voice)
    CheckBox mVoice;
    @InjectView(R.id.autoUpdate)
    CheckBox mAutoUpdate;
    @InjectView(R.id.cleanCache)
    CheckBox mCleanCache;
    @InjectView(R.id.cashLost)
    CheckBox mCashLost;
    @InjectView(R.id.showImage)
    CheckBox mShowImage;

    private String IS_PUTH = "isPush";
    private String DELETE_AFTERINSTALL = "deletAfterInstall";
    private String AUTO_INSTALL = "autoInstall";
    private String AUTO_UPDATE = "autoUpdate";
    private String NOTIFY_VOICE = "notifyVoice";
    private String CLEAN_CACH = "cleanCach";
    private String CACH_LOST = "cachLost";
    private String SHOW_IMAGE = "showImage";


    TAIConfig mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        ButterKnife.inject(this);
        mConfig = getTAApplication().getPreferenceConfig();
        initValue(mConfig);
        bindEvent();
        mTitle.setText(R.string.left_setting);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void bindEvent() {
        mAcceptMessage.setOnCheckedChangeListener(this);
        mCashLost.setOnCheckedChangeListener(this);
        mCleanCache.setOnCheckedChangeListener(this);
        mShowImage.setOnCheckedChangeListener(this);
        mAutoUpdate.setOnCheckedChangeListener(this);
        mDeleteInstalled.setOnCheckedChangeListener(this);
        mAutoInstall.setOnCheckedChangeListener(this);
        mVoice.setOnCheckedChangeListener(this);
    }

    private void initValue(TAIConfig config) {
        mAcceptMessage.setChecked(config.getBoolean(IS_PUTH, true));
        mAutoInstall.setChecked(config.getBoolean(AUTO_INSTALL, true));
        mDeleteInstalled.setChecked(config.getBoolean(DELETE_AFTERINSTALL, true));
        mVoice.setChecked(config.getBoolean(NOTIFY_VOICE, true));
        mAutoUpdate.setChecked(config.getBoolean(AUTO_UPDATE, false));
        mCleanCache.setChecked(config.getBoolean(CLEAN_CACH, true));
        mShowImage.setChecked(config.getBoolean(SHOW_IMAGE, true));
        mCashLost.setChecked(config.getBoolean(CACH_LOST, true));
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.acceptMessage:
                mConfig.setBoolean(IS_PUTH, isChecked);
                break;
            case R.id.deleteInstalled:
                mConfig.setBoolean(DELETE_AFTERINSTALL, isChecked);
                break;
            case R.id.autoInstall:
                mConfig.setBoolean(AUTO_INSTALL, isChecked);
                break;
            case R.id.autoUpdate:
                mConfig.setBoolean(AUTO_UPDATE, isChecked);
                break;
            case R.id.voice:
                mConfig.setBoolean(NOTIFY_VOICE, isChecked);
                break;
            case R.id.showImage:
                mConfig.setBoolean(SHOW_IMAGE, isChecked);
                break;
            case R.id.cashLost:
                mConfig.setBoolean(CACH_LOST, isChecked);
                break;
            case R.id.cleanCache:
                mConfig.setBoolean(CLEAN_CACH, isChecked);
                break;
        }
    }
}

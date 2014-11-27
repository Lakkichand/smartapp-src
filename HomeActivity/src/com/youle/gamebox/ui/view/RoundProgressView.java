package com.youle.gamebox.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.youle.gamebox.ui.R;

public class RoundProgressView extends RelativeLayout {
    RoundProgressBar mRoundProgressBar;
    TextView mRoundProgressn;
    ImageView mRoundProgressnImage;
    int mRoundProgress;


    public RoundProgressView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        initView(context);
    }

    public RoundProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RoundProgressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        initView(context);
    }

    private void initView(Context context) {
        setGravity(Gravity.CENTER);
        View view = LayoutInflater.from(context).inflate(
                R.layout.round_cricle_progress, null);
        mRoundProgressBar = (RoundProgressBar) view
                .findViewById(R.id.mroundProgress_bar);
        mRoundProgressnImage = (ImageView) view
                .findViewById(R.id.mroundProgress_image);
        mRoundProgressn = (TextView) view
                .findViewById(R.id.mroundProgress_text);
        this.removeAllViews();
        this.addView(view);
    }


    public void setProgress(int progress) {
        if (progress <= 0) {
            progress = 0;
        } else if (progress >= 100) {
            progress = 100;
        } else {
            mRoundProgress = progress;
        }
            mRoundProgressBar.setProgress(progress);
    }


    /**
     * 默认
     */
    public void setLoadStautsUI() {
        selectLoadColor(STAUTS_LOAD, -1);
    }

    /**
     * 下载中
     */
    public void setLoadingStautsUI(int progress) {
        mRoundProgress= progress;
        selectLoadColor(STAUTS_LOADING, progress);
    }

    /**
     * 继续
     */
    public void setLoadConntineStautsUI() {
        selectLoadColor(STAUTS_CONNTIUE, -1);
    }

    /**
     * 重试
     */
    public void setLoadRestartStautsUI() {
        selectLoadColor(STAUTS_RESTART, -1);
    }

    /**
     * 安装
     */
    public void setLoadInstallStautsUI() {
        selectLoadColor(STAUTS_INSTALL, -1);
    }

    /**
     * 打开
     */
    public void setLoadOpenStautsUI() {
        selectLoadColor(STAUTS_OPEN, -1);
    }

    /**
     * 升级
     */
    public void setUpdateStatusUI() {
        selectLoadColor(STAUTS_UPDATE, -1);
    }
    public static final int STAUTS_LOAD = 1; //默认
    public static final int STAUTS_LOADING = 2; //下载中
    public static final int STAUTS_CONNTIUE = 3; //继续
    public static final int STAUTS_RESTART = 4; // 重试
    public static final int STAUTS_INSTALL = 5; //安装
    public static final int STAUTS_OPEN = 6; //打开
    public static final int STAUTS_UPDATE= 7; //升级
    String roundColor = "";
    String roundBColor = "";
    String textColor = "";
    String text = "";
    int falgImage = -1;

    public void selectLoadColor(int seletType, int progress) {
        switch (seletType) {
            case STAUTS_LOAD:
                roundColor = "#c9c9c9";
                roundBColor = "#c9c9c9";
                textColor = "#999999";
                text = "下载";
                falgImage = R.drawable.load_befer;
                mRoundProgressBar.setTextIsDisplayable(false);
                progress = 0;
                break;
            case STAUTS_LOADING:
                roundColor = "#dedddd";
                roundBColor = "#33ce16";
                textColor = "#35ce1f";
                text = mRoundProgress + "%";
                falgImage = R.drawable.load_ing;
                mRoundProgressBar.setTextIsDisplayable(true);
                setProgress(progress);
                break;
            case STAUTS_CONNTIUE:
                roundColor = "#f89543";
                textColor = "#999999";
                roundBColor = "";
                text = "继续";
                progress = 0;
                mRoundProgressBar.setTextIsDisplayable(false);
                falgImage = R.drawable.load_conntine;
                break;
            case STAUTS_RESTART:
                roundColor = "#fe7174";
                textColor = "#999999";
                roundBColor = "";
                text = "重试";
                progress = 0;
                mRoundProgressBar.setTextIsDisplayable(false);
                falgImage = R.drawable.load_restart;
                break;
            case STAUTS_INSTALL:
                roundColor = "#34cf17";
                textColor = "#999999";
                roundBColor = "";
                mRoundProgressBar.setTextIsDisplayable(false);
                text = "安装";
                progress = 0;
                falgImage = R.drawable.load_install;
                break;
            case STAUTS_OPEN:
                roundColor = "#85d3e8";
                textColor = "#999999";
                roundBColor = "";
                progress = 0;
                mRoundProgressBar.setTextIsDisplayable(false);
                text = "打开";
                falgImage = R.drawable.load_open;
                break;
            case STAUTS_UPDATE:
                roundColor = "#A1CD2B";
                textColor = "#999999";
                roundBColor = "";
                progress = 0;
                mRoundProgressBar.setTextIsDisplayable(false);
                text = "升级";
                falgImage = R.drawable.pro_update;
                break;
        }
        setUi();
        setProgress(progress);

    }


    private void setUi() {
        if (mRoundProgressBar != null) {
            if (mRoundProgressBar != null) {
                if (!"".equals(roundColor)) {
                    mRoundProgressBar.setRoundColor(Color.parseColor(roundColor));
                }
                if (!"".equals(roundBColor)) {
                    mRoundProgressBar.setRoundProgressColor(Color.parseColor(roundBColor));
                }
                if (!"".equals(textColor)) {
                    if (mRoundProgressn != null)
                        mRoundProgressn.setTextColor(Color.parseColor(textColor));
                }
                if (!"".equals(text)) {
                    if (mRoundProgressn != null)
                        mRoundProgressn.setText(text);
                }
                if (falgImage != -1) {
                    if (mRoundProgressnImage != null)
                        mRoundProgressnImage
                                .setImageResource(falgImage);
                }

            }
        }
    }

    public void initView(int roundColor, int roundProgressColor, int textColor, int textSize) {
        if (mRoundProgressBar != null) {
            if (roundColor > 0) mRoundProgressBar.setRoundColor(roundColor);
            if (roundProgressColor > 0) mRoundProgressBar.setRoundProgressColor(roundProgressColor);
            if (textColor > 0) mRoundProgressBar.setTextColor(textColor);
            if (textSize > 0) mRoundProgressBar.setTextColor(textSize);

        }
    }

}

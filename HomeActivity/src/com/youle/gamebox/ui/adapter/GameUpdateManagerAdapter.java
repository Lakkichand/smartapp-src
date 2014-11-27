package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.ta.util.download.DownloadManager;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.fragment.DownloadManagerItemFragment;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.DownLoadUtil;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.DeleteDialog;
import com.youle.gamebox.ui.view.RoundProgressView;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 14-6-16.
 * //游戏管理界面Adapter
 */
public class GameUpdateManagerAdapter extends DownloadAdapter<GameBean> implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private int type;
    private OnSelectChange onSelectChange;

    public void setOnSelectChange(OnSelectChange onSelectChange) {
        this.onSelectChange = onSelectChange;
    }

    public interface OnSelectChange {
        public void OnAllCheckedCancel();

        public void onDelete(GameBean b);
    }

    public GameUpdateManagerAdapter(Context mContext, List<GameBean> mList, int type) {
        super(mContext, mList);
        this.type = type;
    }

    public Set<GameBean> selecteGameSet = new HashSet<GameBean>();

    public Set<GameBean> getSelecteGameSet() {
        return selecteGameSet;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameBean b = getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.update_game_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holdersSet.add(holder.mRoundProgress);
        if (b.getHasSpree() != null && b.getHasSpree()) {
            holder.mGift.setVisibility(View.VISIBLE);
        } else {
            holder.mGift.setVisibility(View.GONE);
        }
        holder.mGameName.setText(b.getName());
        holder.mGameSize.setText(b.getSize());
        if (b.getScore() != null) {
            holder.mScro.setRating(b.getScore() / 2.0f);
        }
        holder.mRoundProgress.setTag(b);
        holder.mRoundProgress.setFocusable(true);
        holder.mDownloadNumber.setText(b.getDownloads());
        holder.mGameType.setText(" | " + b.getCategory());
        holder.mRoundProgress.requestFocus();
        holder.mRoundProgress.setOnClickListener(downloadListener);
        ImageLoadUtil.displayImage(b.getIconUrl(), holder.mGameIcon);
        initDownloadStatus(holder.mRoundProgress, b);
        holder.mCheck.setTag(b);
        holder.mCheck.setOnCheckedChangeListener(this);
        if (selecteGameSet.contains(b)) {
            holder.mCheck.setChecked(true);
        } else {
            holder.mCheck.setChecked(false);
        }
        holder.mDeleteLayout.setVisibility(View.GONE);
        if (type == DownloadManagerItemFragment.DOWNLOAD) {
        } else if (type == DownloadManagerItemFragment.INSTALL) {
            holder.mTextDesc.setText(R.string.uninstall);
        }else {
            holder.mTextDesc.setText(R.string.update);
            holder.mIcon.setImageResource(R.drawable.update_icon);
            holder.mDeleteLayout.setBackgroundColor(mContext.getResources().getColor(R.color.update_bg));
        }
        final ViewHolder finalHolder = holder;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int vi = finalHolder.mDeleteLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
                finalHolder.mDeleteLayout.setVisibility(vi);
            }
        });
        holder.mDeleteLayout.setTag(b);
        holder.mDeleteLayout.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            selecteGameSet.add((GameBean) buttonView.getTag());
        } else {
            selecteGameSet.remove((GameBean) buttonView.getTag());
            if (selecteGameSet.isEmpty()) {
                if (onSelectChange != null) {
                    onSelectChange.OnAllCheckedCancel();
                }
            }
        }
    }

    public void selectedAll() {
        for (int i = 0; i < getCount(); i++) {
            selecteGameSet.add(getItem(i));
        }
        notifyDataSetChanged();
    }

    public void cancelAll() {
        selecteGameSet.clear();
        notifyDataSetChanged();
    }

    public void down(GameBean gameBean) {
        GameBean downLoadBean = downLoadUtil.getDownloadBeanByUrl(gameBean.getDownloadUrl());
        if (downLoadBean != null) {
            if (downLoadBean.getDownloadStatus() != DownLoadUtil.DOWNLOADING && downLoadBean.getDownloadStatus() != DownLoadUtil.SUCCESS) {
                if (downLoadBean.getDownloadStatus() == DownLoadUtil.PAUSE) {
                    downLoadUtil.continueDown(gameBean.getDownloadUrl());
                }
                if (downLoadBean.getDownloadStatus() == DownLoadUtil.FAIL) {
                    downLoadUtil.addHandler(gameBean);
                }
            }
        } else {
            downLoadUtil.addHandler(gameBean);
        }
    }

    public void delete(GameBean bean) {
        downLoadUtil.deleteDown(bean.getDownloadUrl());
        mList.remove(bean);
        onSelectChange.onDelete(bean);
        notifyDataSetChanged();
    }

    GameBean bean;

    @Override
    public void onClick(View v) {
        final GameBean b = (GameBean) v.getTag();
        bean = b;
        if(type==DownloadManagerItemFragment.UPDATA){
            downLoadBean(b);
        }else {
            String conent ="你确定删除该文件";
            if(type==DownloadManagerItemFragment.INSTALL){
                conent ="你确定卸载该游戏";
            }
            DeleteDialog d = new DeleteDialog(mContext,conent);
            d.setListener(new DeleteDialog.IDialogOperationListener() {
                @Override
                public void onSure() {
                    if (type == DownloadManagerItemFragment.DOWNLOAD) {
                        delete(bean);
                    } else if (type == DownloadManagerItemFragment.INSTALL) {
                        AppInfoUtils.uninstall(mContext, b.getPackageName());
                    }
                }
            });
            d.show();
        }
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'null'
     * for easy to all layout elements.
     *
     * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)
     */
    static class ViewHolder {
        @InjectView(R.id.check)
        CheckBox mCheck;
        @InjectView(R.id.gameIcon)
        ImageView mGameIcon;
        @InjectView(R.id.gift)
        ImageView mGift;
        @InjectView(R.id.gameIconLayout)
        RelativeLayout mGameIconLayout;
        @InjectView(R.id.gameName)
        TextView mGameName;
        @InjectView(R.id.gameSize)
        TextView mGameSize;
        @InjectView(R.id.gameType)
        TextView mGameType;
        @InjectView(R.id.gamedesLayout)
        LinearLayout mGamedesLayout;
        @InjectView(R.id.scro)
        RatingBar mScro;
        @InjectView(R.id.roundProgress)
        RoundProgressView mRoundProgress;
        @InjectView(R.id.downloadNumber)
        TextView mDownloadNumber;
        @InjectView(R.id.deleteLayout)
        LinearLayout mDeleteLayout;
        @InjectView(R.id.textDesc)
        TextView mTextDesc;
        @InjectView(R.id.manager_more_icon)
        ImageView mIcon ;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

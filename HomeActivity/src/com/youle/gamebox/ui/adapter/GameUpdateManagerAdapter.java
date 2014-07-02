package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.RoundProgressView;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 14-6-16.
 */
public class GameUpdateManagerAdapter extends DownloadAdapter<GameBean> {
    private Set<ViewHolder> holdersSet = new HashSet<ViewHolder>();
    public GameUpdateManagerAdapter(Context mContext, List<GameBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameBean b = getItem(position);
        ViewHolder holder =null ;
        if(convertView==null){
           convertView = LayoutInflater.from(mContext).inflate(R.layout.update_game_item,null) ;
            holder = new ViewHolder(convertView) ;
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        holdersSet.add(holder);
        if(b.getHasSpree()){
            holder.mGift.setVisibility(View.VISIBLE);
        }else {
            holder.mGift.setVisibility(View.GONE);
        }
        holder.mGameName.setText(b.getName());
        holder.mGameSize.setText(b.getSize());
        if(b.getScore()!=null) {
            holder.mScro.setRating(b.getScore() / 2.0f);
        }
        holder.mRoundProgress.setTag(b);
        holder.mRoundProgress.setFocusable(true);
        holder.mDownloadNumber.setText(b.getDownloads());
        holder.mGameType.setText(" | " + b.getCategory());
        holder.mRoundProgress.requestFocus();
        holder.mRoundProgress.setOnClickListener(downloadListener);
        ImageLoadUtil.displayImage(b.getIconUrl(), holder.mGameIcon);
        initDownloadStatus(holder.mRoundProgress,b);
        return convertView;
    }

    private View.OnClickListener downloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GameBean bean = (GameBean) v.getTag();
            downLoadBean(bean, (RoundProgressView) v);
        }
    };

    @Override
    public void onAdd(String url, Boolean isInterrupt) {
    }

    @Override
    public void onLoading(String url, long totalSize, long currentSize, long speed) {
        if (isFliping()) return;
        ViewHolder holder = getHolderByUrl(url);
        if (holder != null) {
            int pro = (int) (currentSize * 100 / totalSize);
            LOGUtil.d(TAG, "url=" + url + " pro=" + pro);
            holder.mRoundProgress.setLoadingStautsUI(pro);
        }
    }

    @Override
    public void onSuccess(String url, File file) {
        ViewHolder holder = getHolderByUrl(url);
        if (holder != null) {
            holder.mRoundProgress.setLoadInstallStautsUI();
        }
    }

    @Override
    public void onFailure(String url, String strMsg) {
        Toast.makeText(mContext, "下载失败-" + url + " msg=" + strMsg, Toast.LENGTH_SHORT).show();
        ViewHolder holder = getHolderByUrl(url);
        if (holder != null) {
            holder.mRoundProgress.setLoadRestartStautsUI();
        }
    }

    @Override
    public void onContinue(String url) {
        GameBean downLoadBean = downLoadUtil.getDownloadBeanByUrl(url);
        ViewHolder holder = getHolderByUrl(url);
        if (downLoadBean != null && holder != null) {
            int pro = (int) (downLoadBean.getCurrentSize() * 100 / downLoadBean.getTotalSize());
            holder.mRoundProgress.setLoadingStautsUI(pro);
        }
    }

    @Override
    public void onPause(String url) {
        ViewHolder holder = getHolderByUrl(url);
        if (holder != null) {
            holder.mRoundProgress.setLoadConntineStautsUI();
        }
    }
    private ViewHolder getHolderByUrl(String url) {
        for (ViewHolder h : holdersSet) {
            GameBean bean = (GameBean) h.mRoundProgress.getTag();
            if (bean.getDownloadUrl().equals(url)) {
                return h;
            }
        }
        return null;
    }
/**
 * This class contains all butterknife-injected Views & Layouts from layout file 'null'
 * for easy to all layout elements.
 *
 * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)
 */
    static

    class ViewHolder {
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

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

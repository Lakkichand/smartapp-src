package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.view.RoundProgressView;

import java.util.List;

/**
 * Created by Administrator on 14-6-11.
 */
public class HomeGameAdapter extends ListAsGridBaseAdapter {
    @InjectView(R.id.gameName)
    TextView mGameName;
    @InjectView(R.id.gameIcon)
    ImageView mGameIcon;
    @InjectView(R.id.gift)
    ImageView mGift;
    @InjectView(R.id.gameType)
    TextView mGameType;
    @InjectView(R.id.gameSize)
    TextView mGameSize;
    @InjectView(R.id.progressBar)
    RoundProgressView mProgressBar;
    private List<GameBean> dataList  ;
    private Context mContext ;
    public HomeGameAdapter(Context context,List<GameBean> list) {
        super(context,list);
        this.dataList = list ;
        this.mContext = context;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    protected View getItemView(int position, View view, ViewGroup parent) {
        GameBean b = getItem(position) ;
        GameHolder holder =null ;
        if(view==null){
            view = LayoutInflater.from(mContext).inflate(R.layout.home_game_item,null) ;
            holder = new GameHolder(view);
            view.setTag(holder);
        }else {
            holder = (GameHolder) view.getTag();
        }
        holder.mGameName.setText(b.getName());
        holder.mGameSize.setText(b.getSize());
        holder.mGameType.setText(b.getCategory());
        if(b.getHasSpree()){
            holder.mGift.setVisibility(View.VISIBLE);
        }else {
            holder.mGift.setVisibility(View.GONE);
        }
        ImageLoadUtil.displayImage(b.getIconUrl(),holder.mGameIcon);
        if(position%2==0){
            view.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }else {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.home_item_bg_1));
        }
        initDownloadStatus(holder.mProgressView,b);
        holder.mProgressView.setTag(b);
        holder.mProgressView.setOnClickListener(downloadListener);
        return view;
    }

    @Override
    public GameBean getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
/**
 * This class contains all butterknife-injected Views & Layouts from layout file 'null'
 * for easy to all layout elements.
 *
 * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)
 */
    static

    class GameHolder{
        @InjectView(R.id.gameName)
        TextView mGameName;
        @InjectView(R.id.gameIcon)
        ImageView mGameIcon;
        @InjectView(R.id.gift)
        ImageView mGift;
        @InjectView(R.id.gameSize)
        TextView mGameSize;
        @InjectView(R.id.gameType)
        TextView mGameType;
        @InjectView(R.id.progressBar)
       RoundProgressView mProgressView ;

        GameHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}

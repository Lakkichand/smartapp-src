package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 应用详情图片列表
 * @author zhaoyl
 *
 */
public class AppInfoPictureAdapter  extends BaseAdapter {
    Context mContext;
    LayoutInflater mInflater;
    List<String> mshoImages = new ArrayList<String>();
    private Handler handler = null;
    
    public AppInfoPictureAdapter(Context context,List<String> shoImages, TAApplication application) {
        mContext = context;
        mshoImages=shoImages;
        mInflater = LayoutInflater.from(mContext);
        handler = new Handler();
    }
    
    public AppInfoPictureAdapter(){
    	
    }

    @Override
    public int getCount() {
        return mshoImages.size();
    }

    @Override
    public Object getItem(int arg0) {
        return arg0;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View contentView, ViewGroup arg2) {
    	TestViewHolder holder;
        if (contentView == null) {
            holder = new TestViewHolder();
            contentView = mInflater.inflate(R.layout.gridview_item, null);
            holder.mImg = (ImageView) contentView.findViewById(R.id.mImage);
            contentView.setTag(holder);
           
        } else {
            holder = (TestViewHolder) contentView.getTag();
        }
        final ImageView image = holder.mImg;
		image.setTag(mshoImages.get(position));
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, mshoImages.get(position).hashCode() + "",
				mshoImages.get(position), true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							image.setScaleType(ScaleType.CENTER);
							image.setImageResource(R.drawable.loading_bg_ad_after);
						}else {
							if (image.getTag().equals(imgUrl)) {
								image.setImageBitmap(imageBitmap);
								image.setScaleType(ScaleType.CENTER_CROP);
								image.setBackgroundColor(Color.parseColor("#00000000"));
							}
						}
						
					}
				});
		if (bm != null) {			
			image.setScaleType(ScaleType.CENTER_CROP);
			image.setBackgroundColor(Color.parseColor("#00000000"));
			image.setImageBitmap(bm);
		} else {
			// 默认
			image.setScaleType(ScaleType.CENTER);
			image.setImageResource(R.drawable.loading_s);
		}
        return contentView;
    }
    static class TestViewHolder {
        ImageView mImg;
    }
 
}

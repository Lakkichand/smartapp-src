package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.bean.HotSearchKeyword;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 热门搜索关键字数据adapter
 * 
 * @author xiedezhi
 * @date [2012-9-24]
 */
public class AppHotKeywordAdapter extends BaseAdapter {

	/**
	 * 热门搜索关键字数据列表
	 */
	private List<HotSearchKeyword> mList = new ArrayList<HotSearchKeyword>();

	private Context mContext = null;

	private LayoutInflater mInflater = null;
	/**
	 * 图片管理器
	 */
	private AsyncImageManager mImgManager = null;

	public AppHotKeywordAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);

		mImgManager = AsyncImageManager.getInstance();
	}

	/**
	 * 更新数据并调用notifyDataSetChanged
	 */
	public void update(List<HotSearchKeyword> list) {
		mList.clear();
		if (list != null) {
			for (HotSearchKeyword word : list) {
				mList.add(word);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mList == null) {
			return 0;
		}
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		if (position < 0 || position >= mList.size()) {
			return null;
		}
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.appgame_hot_search_keyword_item, null);
		}
		if (position < 0 || position >= mList.size()) {
			return convertView;
		}

		HotSearchKeyword word = mList.get(position);

		TextView num = (TextView) convertView.findViewById(R.id.appgame_hot_search_keyword_num);
		TextView text = (TextView) convertView.findViewById(R.id.appgame_hot_search_keyword_text);
		ImageView img = (ImageView) convertView.findViewById(R.id.appgame_hot_search_keyword_img);
		num.setText((position + 1) + "");
		if (position >= 0 && position <= 2) {
			num.setBackgroundResource(R.drawable.appgame_hot_keyword_num_hit);
			num.setTextColor(0xFFFFFFFF);
		} else {
			num.setBackgroundResource(R.drawable.appgame_hot_keyword_num_nor);
			num.setTextColor(0xFF343434);
		}
		text.setText(word.name);
		if (word.state == 1) {
			// 新增
			img.setImageResource(R.drawable.appgame_hot_keyword_new);
		} else if (word.state == 2) {
			// 上升
			img.setImageResource(R.drawable.appgame_hot_keyword_up);
		} else if (word.state == 3) {
			// 平稳
			img.setImageResource(R.drawable.appgame_hot_keyword_smooth);
		} else if (word.state == 4) {
			// 下降
			img.setImageResource(R.drawable.appgame_hot_keyword_down);
		} else {
			if (!TextUtils.isEmpty(word.sicon)) {
				// 其他
				setIcon(img, word.sicon, LauncherEnv.Path.APP_MANAGER_ICON_PATH, String.valueOf(word.sicon.hashCode()));
			}
		}
		convertView.setTag(word);
		return convertView;
	}

	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setIcon(final ImageView imageView, String imgUrl, String imgPath, String imgName) {
		imageView.setTag(imgUrl);
		Bitmap bm = mImgManager.loadImage(imgPath, imgName, imgUrl, true, false, null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageView.getTag().equals(imgUrl)) {
							imageView.setImageBitmap(imageBitmap);
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		if (bm != null) {
			imageView.setImageBitmap(bm);
		} else {
			imageView.setImageDrawable(null);
		}
	}
}

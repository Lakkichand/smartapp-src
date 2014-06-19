package com.jiubang.go.backup.pro.image.util;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author jiangpeihe
 *
 */
public class CommonImageAdapter extends BaseAdapter {

	public LayoutInflater mInflater;
	public Map<String, SoftReference<Bitmap>> mPhotos;
	public ExecutorService mThreadPoll;
	public BaseEntry mBaseEntry;
	public CheckBox mCheckBox;
	public Context mContext = null;
	public Bitmap mDefaultImage = null;
	public ListView mListView = null;
	public ViewHolder mHolder;
	public CommonImageAdapter(Context context, BaseEntry entry, CheckBox checkBox,
			ListView listview, Bitmap bitmap) {
		mInflater = LayoutInflater.from(context);
		mBaseEntry = entry;
		mListView = listview;
		mDefaultImage = bitmap;
		mContext = context;
		mCheckBox = checkBox;
		mPhotos = new HashMap<String, SoftReference<Bitmap>>();
		mThreadPoll = Executors.newFixedThreadPool(5);
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.layout_dialog_entry_view, null);
			holder.title = (TextView) convertView.findViewById(R.id.entry_title);
			holder.img = (ImageView) convertView.findViewById(R.id.icon);
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
			holder.imageSize = (TextView) convertView.findViewById(R.id.entry_summary2);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.checkBox.setOnCheckedChangeListener(null);
		ImageBean image = getImage(position);
		if (getEntry(position).isSelected()) {
			holder.checkBox.setChecked(true);
		} else {
			holder.checkBox.setChecked(false);
		}
		final int pos = position;
		holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				getEntry(pos).setSelected(isChecked);
				if (isAllEntriesSelected(mBaseEntry)) {
					mCheckBox.setChecked(true);
				} else {
					mCheckBox.setChecked(false);
				}
			}
		});

		holder.title.setText(image.mImageDisplayName);
		holder.imageSize.setText(Util.formatFileSize(image.mImageSize));
		holder.img.setAdjustViewBounds(true);
		updateView(convertView, position);
		return convertView;
	}

	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			int position = message.arg1;
			int firstVisiblePosition = mListView.getFirstVisiblePosition();
			int lastVisiblePosition = mListView.getLastVisiblePosition();
			if (position < firstVisiblePosition || position > lastVisiblePosition) {
				return;
			}
			View childView = mListView.getChildAt(position - firstVisiblePosition);
			updateView(childView, position);
		}
	};

	//更新listView的某一个子view
	public void updateView(View convertView, int position) {
		if (convertView == null) {
			return;
		}
		ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
		ImageBean image = getImage(position);
		String imageKey = image.mImagePath;
		Bitmap bitmap = mDefaultImage;
		SoftReference<Bitmap> softReferencePhoto = mPhotos.get(imageKey);
		if (softReferencePhoto != null) {
			bitmap = softReferencePhoto.get();
			if (bitmap == null) {
				bitmap = mDefaultImage;
				loadPicture(imageKey, position);
			}
		} else {
			loadPicture(imageKey, position);
		}
		imageView.setImageBitmap(bitmap);
	}

	public void loadPicture(final String path, int position) {
		if (!mThreadPoll.isShutdown()) {
			mThreadPoll.execute(new LoadPicureThread(path, position));
		}
	}

	public boolean isAllEntriesSelected(BaseEntry entry) {
		if (!entry.isSelected()) {
			return false;
		}
		return true;

	}

	/**
	 * @author jiangpeihe
	 *加载照片／／//
	 */
	class LoadPicureThread implements Runnable {
		String mPath;
		int mPosition;
		public LoadPicureThread(String path, int position) {
			mPath = path;
			mPosition = position;
		}
		@Override
		public void run() {
			int firstVisiblePosition = mListView.getFirstVisiblePosition();
			int lastVisiblePosition = mListView.getLastVisiblePosition();
			//listView显示的窗口个数
			int count = lastVisiblePosition - firstVisiblePosition + 1;
			int offset = count / 2;
			if ((lastVisiblePosition > firstVisiblePosition)
					&& (mPosition < (firstVisiblePosition - offset) || mPosition > (lastVisiblePosition + offset))) {
				return;
			}
			if (!TextUtils.isEmpty(mPath)) {
				Bitmap thumnailBitmap = getThumbnailBitmap(mPath);
				if (thumnailBitmap != null) {
					mPhotos.put(mPath, new SoftReference<Bitmap>(thumnailBitmap));
					Message args = new Message();
					args.arg1 = mPosition;
					mHandler.sendMessage(args);
				}
			}
		}
	}

	public void release() {
		if (mPhotos != null) {
			mPhotos.clear();
		}

		if (mThreadPoll != null) {
			mThreadPoll.shutdown();
		}
	}

	//获取缩略图由子类实现
	public Bitmap getThumbnailBitmap(String imagePath) {
		return null;

	}
	//由子类实现
	public BaseEntry getEntry(int posititon) {
		return null;
	}

	//由子类实现
	public ImageBean getImage(int position) {
		return null;
	}

}

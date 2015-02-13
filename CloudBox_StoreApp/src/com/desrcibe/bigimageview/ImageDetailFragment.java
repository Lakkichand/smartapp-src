package com.desrcibe.bigimageview;

import com.desrcibe.bigimageview.PhotoViewAttacher.OnPhotoTapListener;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ImageDetailFragment extends Fragment {
	private String mImageUrl;
	private ImageView mImageView;
	private ProgressBar progressBar;
	private PhotoViewAttacher mAttacher;

	public static ImageDetailFragment newInstance(String imageUrl) {
		final ImageDetailFragment f = new ImageDetailFragment();
		final Bundle args = new Bundle();
		args.putString("url", imageUrl);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString("url") : null;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
		mImageView = (ImageView) v.findViewById(R.id.image);
		mAttacher = new PhotoViewAttacher(mImageView);
		
		mAttacher.setOnPhotoTapListener(new OnPhotoTapListener() {
			
			@Override
			public void onPhotoTap(View arg0, float arg1, float arg2) {
				if (getActivity() == null) {
					return;
				}
				getActivity().finish();
				getActivity().overridePendingTransition(android.R.anim.fade_in,
						android.R.anim.fade_out);
			}
		});
		
		progressBar = (ProgressBar) v.findViewById(R.id.loading);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		progressBar.setVisibility(View.VISIBLE);
		final ImageView image = mImageView;
		image.setTag(mImageUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, mImageUrl.hashCode() + "",
				mImageUrl, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							image.setImageResource(R.drawable.game_info_gry_load_bg_after);
							progressBar.setVisibility(View.GONE);
							mAttacher.update();
						}else {
							if (image.getTag().equals(imgUrl)) {
								image.setImageBitmap(imageBitmap);
								progressBar.setVisibility(View.GONE);
								mAttacher.update();
							}
						}
						
					}
				});
		if (bm != null) {
			image.setImageBitmap(bm);
			progressBar.setVisibility(View.GONE);
			mAttacher.update();
		} else {
			// 默认
		}
		
	}

}

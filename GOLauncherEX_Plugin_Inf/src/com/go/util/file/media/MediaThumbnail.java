package com.go.util.file.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

public class MediaThumbnail {
	public final static int TYPE_IMAGE = 0;
	public final static int TYPE_VIDEO = 1;
	public int type;
	public int dbId;
	public String path;
	public int width;
	public int height;

	public Bitmap getThumbnail(Context context) {
		Bitmap bitmap = null;
		if (context != null) {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4;
			if (type == TYPE_IMAGE) {
				bitmap = Images.Thumbnails.getThumbnail(context.getContentResolver(), dbId,
						Images.Thumbnails.MICRO_KIND, options);
			} else {
				bitmap = Video.Thumbnails.getThumbnail(context.getContentResolver(), dbId,
						Video.Thumbnails.MICRO_KIND, options);
			}
		}
		return bitmap;
	}
}

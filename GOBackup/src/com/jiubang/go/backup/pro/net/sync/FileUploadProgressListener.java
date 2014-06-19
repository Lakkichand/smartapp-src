package com.jiubang.go.backup.pro.net.sync;

import java.io.File;
import java.io.IOException;

import android.util.Log;

import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;

/**
 * The File Upload  Progress Listener.
 * 
 * @author ReyZhang
 */
public class FileUploadProgressListener implements MediaHttpUploaderProgressListener {

	private ActionListener mListener;
	private File mFile;

	public FileUploadProgressListener(ActionListener actionListener, File file) {
		if (actionListener == null || file == null) {
			throw new IllegalArgumentException("invalid arguments");
		}
		mListener = actionListener;
		mFile = file;
	}

	@Override
	public void progressChanged(MediaHttpUploader uploader) throws IOException {
		switch (uploader.getUploadState()) {
			case INITIATION_STARTED:
				Log.v("GoBackup", "onInitiation_started");
				break;
			case INITIATION_COMPLETE:
				Log.v("GoBackup", "on Initiation_complete");
				break;
			case MEDIA_IN_PROGRESS:
				mListener.onProgress(uploader.getNumBytesUploaded(),
						mFile.length(),
						null);
				Log.v("GoBackup", "on in progress:" + uploader.getProgress());
				break;
			case MEDIA_COMPLETE:
				Log.v("GoBackup", "on media  complete");
				break;
		}
	}
}

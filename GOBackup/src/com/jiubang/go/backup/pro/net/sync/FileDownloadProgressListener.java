/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jiubang.go.backup.pro.net.sync;

import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.services.drive.model.File;
import com.jiubang.go.backup.pro.net.sync.FileHostingServiceProvider.ActionListener;

/**
 * The File Download Progress Listener.
 *
 * @author ReyZhang
 */
public class FileDownloadProgressListener implements MediaHttpDownloaderProgressListener {

	private File mFile;
	private ActionListener mListener;
	
	public FileDownloadProgressListener(File file, ActionListener listener) {
		if (file == null) {
			throw new IllegalArgumentException("invalid argument");
		}
		mFile = file;
		mListener = listener;
	}
  @Override
  public void progressChanged(MediaHttpDownloader downloader) {
    switch (downloader.getDownloadState()) {
      case MEDIA_IN_PROGRESS:
    	  if (mListener != null) {
    		  mListener.onProgress(downloader.getNumBytesDownloaded(), mFile.getFileSize(), null);
    	  }
        break;
      case MEDIA_COMPLETE:
        break;
    }
  }
}

package de.acepe.fritzstreams.backend;

import android.util.Log;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

class Downloader {

    private static final long UPDATE_UI_INTERVAL = 1000;

    interface DownloadCallback {
        void reportProgress(DownloadInfo progressInfo);
    }

    private static final String TAG = "Downloader";

    private final DownloadInfo mDownloadInfo;
    private final DownloadCallback callback;
    private long lastupdate;

    Downloader(DownloadInfo downloadInfo, DownloadCallback callback) {
        this.mDownloadInfo = downloadInfo;
        this.callback = callback;
    }

    public void download() {
        mDownloadInfo.setState(DownloadState.DOWNLOADING);
        String pathname = mDownloadInfo.getFilename();
        File file = new File(pathname);

        URLConnection connection;
        try {
            connection = new URL(mDownloadInfo.getStreamURL()).openConnection();
        } catch (IOException e) {
            Log.e(TAG, "couldn't open connection to: " + mDownloadInfo.getStreamURL(), e);
            mDownloadInfo.setState(DownloadState.FAILED);
            return;
        }
        try (InputStream is = connection.getInputStream();
             OutputStream outstream = new BufferedOutputStream(new FileOutputStream(file))) {
            int size = connection.getContentLength();

            Log.d(TAG, "Size is: " + size);
            mDownloadInfo.setTotalSize(size);
            callback.reportProgress(mDownloadInfo);

            byte[] buffer = new byte[4096];
            int downloadedSum = 0;
            int len;
            while ((len = is.read(buffer)) > 0) {
                if (mDownloadInfo.getState().equals(DownloadState.CANCELLED)) {
                    Log.i(TAG, "Download was cancelled: " + mDownloadInfo.getStreamURL());
                    file.delete();
                    return;
                }
                outstream.write(buffer, 0, len);

                downloadedSum += len;
                int progressPercent = (int) (downloadedSum / (float) size * 100);
                mDownloadInfo.setProgressPercent(progressPercent);
                mDownloadInfo.setDownloadedSize(downloadedSum);

                if (System.currentTimeMillis() - lastupdate >= UPDATE_UI_INTERVAL) {
                    callback.reportProgress(mDownloadInfo);
                    lastupdate = System.currentTimeMillis();
                }
                Log.d(TAG, "Downloaded Bytes: " + downloadedSum + " / " + size);
            }
        } catch (IOException e) {
            Log.e(TAG, "Download failed: " + mDownloadInfo.getStreamURL(), e);
            mDownloadInfo.setState(DownloadState.FAILED);
            renameToFailed(file);
            return;
        }
        mDownloadInfo.setState(DownloadState.FINISHED);
        mDownloadInfo.setProgressPercent(100);
        Log.i(TAG, "Download successful: " + mDownloadInfo.getStreamURL());
    }

    private void renameToFailed(File file) {
        File newFile = new File(file.getParent(), "failed_" + file.getName());
        if (newFile.exists()) {
            newFile.delete();
        }
        file.renameTo(newFile);
    }

}

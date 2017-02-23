package de.acepe.fritzstreams.backend;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

public class Downloader {

    private boolean cancelled;

    public interface DownloadCallback {
        void reportProgress(ProgressInfo progressInfo);

        boolean isCancelled();
    }

    private static final String TAG = "Downloader";

    private final DownloadInfo mDownloadInfo;
    private final DownloadCallback callback;

    public Downloader(DownloadInfo downloadInfo, DownloadCallback callback) {
        this.mDownloadInfo = downloadInfo;
        this.callback = callback;
    }

    public TaskResult download() {
        String pathname = mDownloadInfo.getFilename();
        File file = new File(pathname);

        URLConnection connection;
        try {
            connection = new URL(mDownloadInfo.getStreamURL()).openConnection();
        } catch (IOException e) {
            return TaskResult.failed;
        }
        try (InputStream is = connection.getInputStream();
                OutputStream outstream = new BufferedOutputStream(new FileOutputStream(file))) {
            int size = connection.getContentLength();

            Log.d(TAG, "Size is: " + size);
            callback.reportProgress(new ProgressInfo(mDownloadInfo.getStreamURL(),
                                                     0,
                                                     size,
                                                     0,
                                                     ProgressInfo.State.downloading));

            byte[] buffer = new byte[4096];
            int downloadedSum = 0;
            int len;
            while ((len = is.read(buffer)) > 0) {
                if (callback.isCancelled()) {
                    cancelled = true;
                    break;
                }
                outstream.write(buffer, 0, len);

                downloadedSum += len;
                int progressPercent = (int) (downloadedSum / (float) size * 100);
                callback.reportProgress(new ProgressInfo(mDownloadInfo.getStreamURL(),
                                                         progressPercent,
                                                         size,
                                                         downloadedSum,
                                                         ProgressInfo.State.downloading));
                Log.d(TAG, "Downloaded Bytes: " + downloadedSum + " / " + size);
            }
        } catch (IOException e) {
            return TaskResult.failed;
        }

        return cancelled ? TaskResult.cancelled : TaskResult.successful;
    }

}

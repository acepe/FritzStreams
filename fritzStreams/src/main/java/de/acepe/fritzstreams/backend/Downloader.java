package de.acepe.fritzstreams.backend;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.widget.Toast;

import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.StreamDownload.State;

public class Downloader implements DownloadTask.Callback {

    private final LinkedList<StreamDownload> mDownloads = new LinkedList<>();
    private final Context mContext;

    private StreamDownload mActiveDownload;
    private DownloadTask mDownloadTask;

    public Downloader(Context mContext) {
        this.mContext = mContext;
    }

    public void scheduleDownload(StreamDownload streamDownload) {
        mDownloads.add(streamDownload);
        if (!isDownloadInProgress()) {
            Toast.makeText(mContext, R.string.download_noti_started, Toast.LENGTH_SHORT).show();
            downloadNext();
        }
    }

    private void downloadNext() {
        StreamDownload firstWaiting = findFirstWaiting();
        if (firstWaiting == null)
            return;
        mActiveDownload = firstWaiting;

        mActiveDownload.setState(State.downloading);
        mDownloadTask = new DownloadTask(mContext, mActiveDownload, this);
        mDownloadTask.execute();
    }

    @Override
    public void onDownloadFinished(TaskResult taskResult) {
        switch (taskResult) {
            case successful:

                MediaScannerConnection.scanFile(mContext,
                                                new String[] { mActiveDownload.getStreamInfo().getPath() },
                                                null,
                                                null);
                mActiveDownload.setState(State.finished);
                break;
            case failed:
                mActiveDownload.setState(State.failed);
                break;
            case onlyWifi:
                mActiveDownload.setState(State.onlyWifi);
                break;
            case cancelled:
                mActiveDownload.setState(State.cancelled);
                break;
        }
        mActiveDownload = null;
        downloadNext();
    }

    private boolean isDownloadInProgress() {
        return mActiveDownload != null;
    }

    private StreamDownload findFirstWaiting() {
        for (StreamDownload streamDownload : mDownloads) {
            if (streamDownload.getState() == State.waiting)
                return streamDownload;
        }
        return null;
    }

    public boolean isEmpty() {
        return mDownloads.isEmpty();
    }

    public State getState(int position) {
        return mDownloads.get(position).getState();
    }

    public Uri getOutFileUri(int position) {
        return mDownloads.get(position).getStreamInfo().getFileUri();
    }

    public StreamDownload getDownload(int position) {
        return mDownloads.get(position);
    }

    public List<StreamDownload> getDownloads() {
        return mDownloads;
    }

    public void cancelDownload(StreamDownload download) {
        if (download.getState() != StreamDownload.State.finished) {
            Toast.makeText(mContext, R.string.download_noti_canceled, Toast.LENGTH_SHORT).show();
        }
        if (download == mActiveDownload && mActiveDownload.getState() == State.downloading) {
            mDownloadTask.cancel(true);
            downloadNext();
        } else {
            mDownloads.remove(download);
        }
    }

}

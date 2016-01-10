package de.acepe.fritzstreams.backend;

import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaScannerConnection;
import android.net.Uri;

import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.util.Utilities;

public class StreamDownload implements DownloadTask.Callback {

    private int mDownloadedKB = 0;
    private int mCurrentProgress;
    private DownloadTask mDownloadTask;
    private int size;

    public enum State {
        waiting, downloading, failed, finished, onlyWifi, cancelled
    }

    private final StreamInfo mStreamInfo;
    private Context mContext;
    private State state;

    public StreamDownload(Context mContext, StreamInfo streamInfo) {
        this.mContext = mContext;
        this.state = State.waiting;
        mStreamInfo = streamInfo;
    }

    public void downloadAndConvert() {
        App.activeDownload = this;
        state = State.downloading;
        mDownloadTask = new DownloadTask(mContext, mStreamInfo, this);
        mDownloadTask.execute();
    }

    public void cancel() {
        if (state == State.downloading) {
            mDownloadTask.cancel(true);
        } else {
            App.downloaders.remove(this);
        }
    }

    public int getCurrentProgress() {
        return mCurrentProgress;
    }

    public String getTitle() {
        return mStreamInfo.getTitle() + " " + mStreamInfo.getSubtitle();
    }

    public String getSubtitle() {
        Resources res = mContext.getResources();
        String localizedState = res.getString(res.getIdentifier(state.name(), "string", mContext.getPackageName()));
        if (state == State.downloading)
            return localizedState
                   + ": "
                   + Utilities.humanReadableBytes(mDownloadedKB, false)
                   + " / "
                   + Utilities.humanReadableBytes(size, false);

        return localizedState;
    }

    public Uri getOutFileUri() {
        File outFileMp3 = new File(mStreamInfo.getFilename());
        return Uri.fromFile(outFileMp3);
    }

    public State getState() {
        return state;
    }

    @Override
    public void onDownloadFinished(TaskResult taskResult) {
        switch (taskResult) {
            case successful:
                MediaScannerConnection.scanFile(mContext,
                                                new String[] { new File(mStreamInfo.getFilename()).getAbsolutePath() },
                                                null,
                                                null);
                state = State.finished;
                break;
            case failed:
                state = State.failed;
                break;
            case onlyWifi:
                state = State.onlyWifi;
                break;
            case cancelled:
                state = State.cancelled;
                break;
        }
        App.activeDownload = null;
        startNext();
    }

    @Override
    public void setCurrentProgress(int currentProgress) {
        mCurrentProgress = currentProgress;
    }

    @Override
    public void setDownloadedKB(int downloadedKB) {
        mDownloadedKB = downloadedKB;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
    }

    private void startNext() {
        for (StreamDownload streamDownload : App.downloaders) {
            if (streamDownload.getState() == State.waiting) {
                streamDownload.downloadAndConvert();
                return;
            }
        }
    }
}

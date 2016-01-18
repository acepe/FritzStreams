package de.acepe.fritzstreams.backend;

import android.content.Context;
import android.content.res.Resources;

import de.acepe.fritzstreams.util.Utilities;

public class StreamDownload {

    public enum State {
        waiting, downloading, failed, finished, onlyWifi, cancelled
    }

    private final StreamInfo mStreamInfo;

    private int mDownloadedKB = 0;
    private int mCurrentProgress;
    private int size;
    private Context mContext;
    private State state;

    public StreamDownload(Context mContext, StreamInfo streamInfo) {
        this.mContext = mContext;
        this.state = State.waiting;
        mStreamInfo = streamInfo;
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

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public StreamInfo getStreamInfo() {
        return mStreamInfo;
    }

    public void setCurrentProgress(int currentProgress) {
        mCurrentProgress = currentProgress;
    }

    public void setDownloadedKB(int downloadedKB) {
        mDownloadedKB = downloadedKB;
    }

    public void setSize(int size) {
        this.size = size;
    }

}

package de.acepe.fritzstreams.backend;

import java.io.File;
import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import de.acepe.fritzstreams.App;

public class StreamDownload implements DownloadTask.Callback, ConvertTask.Callback {

    private String mDownloadedKB = "0";
    private int mCurrentProgress;
    private DownloadTask mDownloadTask;
    private ConvertTask mConvertTask;

    public enum State {
        waiting, downloading, converting, failed, finished, onlyWifi, cancelled
    }

    private final DownloadInformation mDownloadInformation;
    private Context context;
    private State state;

    public StreamDownload(Context context, Calendar cal, Stream stream) {
        this.context = context;
        this.state = State.waiting;
        mDownloadInformation = new DownloadInformation(context, cal, stream);
    }

    public void downloadAndConvert() {
        App.activeDownload = this;
        state = State.downloading;
        mDownloadTask = new DownloadTask(context, mDownloadInformation, this);
        mDownloadTask.execute();
    }

    public void cancel() {
        switch (state) {
            case downloading:
                mDownloadTask.stop();
                break;
            case converting:
                mConvertTask.stop();
                break;
            default:
                App.downloaders.remove(this);
        }
    }

    public int getCurrentProgress() {
        return mCurrentProgress;
    }

    public void setDownloadedKB(String mDownloadedKB) {
        this.mDownloadedKB = mDownloadedKB;
    }

    public String getTitle() {
        return mDownloadInformation.getDisplayStreamCategory()
               + mDownloadInformation.getDisplayStreamType()
               + " "
               + mDownloadInformation.getDisplayDate();
    }

    public String getSubtitle() {
        Resources res = context.getResources();
        String localizedState = res.getString(res.getIdentifier(state.name(), "string", context.getPackageName()));
        if (state == State.downloading)
            return localizedState + ": " + mDownloadedKB;

        return localizedState;
    }

    public Uri getOutFileUri() {
        File outFileMp3 = new File(mDownloadInformation.getOutFileMp3());
        return Uri.fromFile(outFileMp3);
    }

    public State getState() {
        return state;
    }

    @Override
    public void onDownloadFinished(TaskResult taskResult) {
        if (taskResult == TaskResult.successful) {
            state = State.converting;
            mConvertTask = new ConvertTask(context, mDownloadInformation, StreamDownload.this);
            mConvertTask.execute();
            return;
        }

        switch (taskResult) {
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
    public void onConvertFinished(TaskResult taskResult) {
        switch (taskResult) {
            case successful:
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
        this.mCurrentProgress = currentProgress;
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

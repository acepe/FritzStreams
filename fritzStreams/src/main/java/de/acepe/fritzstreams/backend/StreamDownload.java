package de.acepe.fritzstreams.backend;

import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import de.acepe.fritzstreams.App;

public class StreamDownload implements DownloadTask.Callback, ConvertTask.Callback {

    private String downloadedKB = "0";

    public enum State {
        waiting, downloading, converting, failed, finished
    }

    private final DownloadInformation downloadInformation;
    private Context context;
    private State state;

    public StreamDownload(Context context, Calendar cal, Stream stream) {
        this.context = context;
        this.state = State.waiting;
        downloadInformation = new DownloadInformation(context, cal, stream);
    }

    public void downloadAndConvert() {
        App.activeDownload = this;
        state = State.downloading;
        new DownloadTask(context, downloadInformation, this).execute();
    }

    public int getCurrentProgress() {
        // TODO: set and return progress when converting
        return 0;
    }

    public void setDownloadedKB(String downloadedKB) {
        this.downloadedKB = downloadedKB;
    }

    public String getTitle() {
        return downloadInformation.getDisplayStreamCategory()
               + downloadInformation.getDisplayStreamType()
               + " "
               + downloadInformation.getDisplayDate();
    }

    public String getSubtitle() {
        Resources res = context.getResources();
        String localizedState = res.getString(res.getIdentifier(state.name(), "string", context.getPackageName()));
        if (state == State.downloading)
            return localizedState + ": " + downloadedKB;

        return localizedState;
    }

    public State getState() {
        return state;
    }

    @Override
    public void onDownloadFinished(boolean succeeded) {
        if (!succeeded) {
            state = State.failed;
            App.activeDownload = null;
            startNext();
            return;
        }

        state = State.converting;
        new ConvertTask(context, downloadInformation, StreamDownload.this).execute();
    }

    @Override
    public void onConvertFinished(boolean succeeded) {
        state = State.finished;
        App.activeDownload = null;

        startNext();
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

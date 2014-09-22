package de.acepe.fritzstreams.backend;

import java.util.Calendar;

import android.content.Context;
import de.acepe.fritzstreams.App;

public class StreamDownloader implements DownloadTask.Callback, ConvertTask.Callback {

    private Context context;
    private final Calendar cal;
    private final Streams.Stream stream;

    private int mCurrentProgress;
    private String mTitle;
    private String mSubtitle;
    private int mId;

    public StreamDownloader(Context context, Calendar cal, Streams.Stream stream) {
        this.context = context;
        this.cal = cal;
        this.stream = stream;
    }

    public void downloadAndConvert() {
        // Get a new id
        mId = App.downloaders.size() + 1;
        App.downloaders.add(this);

        new DownloadTask(context, cal, stream, this).execute();
    }

    public int getCurrentProgress() {
        return mCurrentProgress;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    @Override
    public void onDownloadFinished(boolean succeeded) {
        new ConvertTask(context, cal, stream, StreamDownloader.this).execute();

    }

    @Override
    public void onConvertFinished(boolean succeeded) {
        // App.downloaders.remove(this);
    }

}

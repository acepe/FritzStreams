package de.acepe.fritzstreams.backend;

import java.util.Calendar;

import android.content.Context;

public class StreamDownload {

    private Context context;

    public StreamDownload(Context context) {
        this.context = context;
    }

    public void downloadAndConvert(final Calendar cal, final Streams.Stream stream) {
        new DownloadTask(context, cal, stream, new DownloadTask.Callback() {

            @Override
            public void onDownloadFinished(boolean succeeded) {
                new ConvertTask(context, cal, stream).execute();
            }

        }).execute();
    }
}

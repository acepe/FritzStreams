package de.acepe.fritzstreams.backend;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.Calendar;

public class OnDemandStream {

    private final Calendar day;
    private final StreamType streamType;

    private String title;
    private String subtitle;
    private String streamURL;
    private Bitmap image;
    private String filename;
    private boolean failed;

    public OnDemandStream(Calendar day, @NonNull StreamType streamType) {
        this.day = day;
        this.streamType = streamType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setStreamURL(String streamURL) {
        this.streamURL = streamURL;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getStreamURL() {
        return streamURL;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public StreamType getStream() {
        return streamType;
    }

    public boolean isInited() {
        return streamURL != null;
    }

    public Bitmap getImage() {
        return image;
    }

    public Calendar getDay() {
        return day;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "StreamInfo{" + "day=" + day + ", mStream=" + streamType + ", title='" + title + '\'' + '}';
    }


}

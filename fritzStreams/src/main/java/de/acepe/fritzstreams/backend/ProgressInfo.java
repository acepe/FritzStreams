package de.acepe.fritzstreams.backend;

import java.io.Serializable;

public class ProgressInfo implements Serializable {

    public enum State {
        waiting, downloading, failed, finished, cancelled
    }

    private State state = State.waiting;
    private String url;
    private int progressPercent;
    private int totalSize;
    private int downloadedSize;

    public ProgressInfo(String url, int progressPercent, int totalSize, int downloadedSize, State state) {
        this.url = url;
        this.progressPercent = progressPercent;
        this.totalSize = totalSize;
        this.downloadedSize = downloadedSize;
        this.state = state;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(int progressPercent) {
        this.progressPercent = progressPercent;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public int getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(int downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ProgressInfo{"
               + "state="
               + state
               + ", url='"
               + url
               + '\''
               + ", progressPercent="
               + progressPercent
               + ", totalSize="
               + totalSize
               + ", downloadedSize="
               + downloadedSize
               + '}';
    }
}

package de.acepe.fritzstreams.backend;

import java.io.Serializable;

public class DownloadInfo implements Serializable {

    public enum State {
        waiting, downloading, failed, finished, cancelled
    }
    private String mTitle;
    private String mSubtitle;
    private String mStreamURL;
    private String mFilename;
    private DownloadInfo.State state = DownloadInfo.State.waiting;
    private String url;
    private int progressPercent;
    private int totalSize;
    private int downloadedSize;

    public DownloadInfo(String title, String subtitle, String streamURL, String filename) {
        this.mTitle = title;
        this.mSubtitle = subtitle;
        this.mStreamURL = streamURL;
        this.mFilename = filename;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public void setSubtitle(String mSubtitle) {
        this.mSubtitle = mSubtitle;
    }

    public String getStreamURL() {
        return mStreamURL;
    }

    public void setStreamURL(String mStreamURL) {
        this.mStreamURL = mStreamURL;
    }

    public String getFilename() {
        return mFilename;
    }

    public void setFilename(String mFilename) {
        this.mFilename = mFilename;
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

    public DownloadInfo.State getState() {
        return state;
    }

    public void setState(DownloadInfo.State state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DownloadInfo that = (DownloadInfo) o;

        return mStreamURL.equals(that.mStreamURL);

    }

    @Override
    public int hashCode() {
        return mStreamURL.hashCode();
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "mTitle='" + mTitle + '\'' +
                ", mSubtitle='" + mSubtitle + '\'' +
                ", mStreamURL='" + mStreamURL + '\'' +
                ", mFilename='" + mFilename + '\'' +
                ", state=" + state +
                ", url='" + url + '\'' +
                ", progressPercent=" + progressPercent +
                ", totalSize=" + totalSize +
                ", downloadedSize=" + downloadedSize +
                '}';
    }


}

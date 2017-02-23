package de.acepe.fritzstreams.backend;

import java.io.Serializable;

public class DownloadInfo implements Serializable {

    private String mTitle;
    private String mSubtitle;
    private String mStreamURL;
    private String mFilename;

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
        return "DownloadInfo{"
               + "mTitle='"
               + mTitle
               + '\''
               + ", mSubtitle='"
               + mSubtitle
               + '\''
               + ", mStreamURL='"
               + mStreamURL
               + '\''
               + ", mFilename='"
               + mFilename
               + '\''
               + '}';
    }
}

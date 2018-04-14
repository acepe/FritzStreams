package de.acepe.fritzstreams.backend.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OnDemandStreamDescriptor {

    @SerializedName("_sortierArray")
    @Expose
    private List<Integer> sortierArray = null;
    @SerializedName("_defaultQuality")
    @Expose
    private List<String> defaultQuality = null;
    @SerializedName("rbbtitle")
    @Expose
    private String rbbtitle;
    @SerializedName("rbbhandle")
    @Expose
    private String rbbhandle;
    @SerializedName("_type")
    @Expose
    private String type;
    @SerializedName("_isLive")
    @Expose
    private Boolean isLive;
    @SerializedName("_dvrEnabled")
    @Expose
    private Boolean dvrEnabled;
    @SerializedName("_geoblocked")
    @Expose
    private Boolean geoblocked;
    @SerializedName("_duration")
    @Expose
    private Integer duration;
    @SerializedName("_audioImage")
    @Expose
    private String audioImage;
    @SerializedName("_mediaArray")
    @Expose
    private List<MediaArray> mediaArray = null;

    private List<Integer> getSortierArray() {
        return sortierArray;
    }

    public void setSortierArray(List<Integer> sortierArray) {
        this.sortierArray = sortierArray;
    }

    public List<String> getDefaultQuality() {
        return defaultQuality;
    }

    public void setDefaultQuality(List<String> defaultQuality) {
        this.defaultQuality = defaultQuality;
    }

    public String getRbbtitle() {
        return rbbtitle;
    }

    public void setRbbtitle(String rbbtitle) {
        this.rbbtitle = rbbtitle;
    }

    public String getRbbhandle() {
        return rbbhandle;
    }

    public void setRbbhandle(String rbbhandle) {
        this.rbbhandle = rbbhandle;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getLive() {
        return isLive;
    }

    public void setLive(Boolean live) {
        isLive = live;
    }

    public Boolean getDvrEnabled() {
        return dvrEnabled;
    }

    public void setDvrEnabled(Boolean dvrEnabled) {
        this.dvrEnabled = dvrEnabled;
    }

    public Boolean getGeoblocked() {
        return geoblocked;
    }

    public void setGeoblocked(Boolean geoblocked) {
        this.geoblocked = geoblocked;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getAudioImage() {
        return audioImage;
    }

    public void setAudioImage(String audioImage) {
        this.audioImage = audioImage;
    }

    public List<MediaArray> getMediaArray() {
        return mediaArray;
    }

    public void setMediaArray(List<MediaArray> mediaArray) {
        this.mediaArray = mediaArray;
    }
}
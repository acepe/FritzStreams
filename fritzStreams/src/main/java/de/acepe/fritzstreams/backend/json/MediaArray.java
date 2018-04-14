package de.acepe.fritzstreams.backend.json;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MediaArray {

    @SerializedName("_plugin")
    @Expose
    private Integer plugin;
    @SerializedName("_mediaStreamArray")
    @Expose
    private List<MediaStreamArray> mediaStreamArray = null;

    public Integer getPlugin() {
        return plugin;
    }

    public void setPlugin(Integer plugin) {
        this.plugin = plugin;
    }

    public List<MediaStreamArray> getMediaStreamArray() {
        return mediaStreamArray;
    }

    public void setMediaStreamArray(List<MediaStreamArray> mediaStreamArray) {
        this.mediaStreamArray = mediaStreamArray;
    }
}
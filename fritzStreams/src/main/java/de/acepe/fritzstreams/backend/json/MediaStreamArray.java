package de.acepe.fritzstreams.backend.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MediaStreamArray {

    @SerializedName("_stream")
    @Expose
    private String stream;
    @SerializedName("_termid")
    @Expose
    private String termid;
    @SerializedName("_server")
    @Expose
    private String server;

    @SerializedName("_quality")
    @Expose
    private Integer quality;

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    public String getTermid() {
        return termid;
    }

    public void setTermid(String termid) {
        this.termid = termid;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }
}
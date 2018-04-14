package de.acepe.fritzstreams.backend.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Analytics {

    private String rbbtitle;
    private String rbbhandle;
    private String aTIxtn2;
    private List<String> chapter = null;
    private Boolean isTrailer;
    private Integer duration;
    private List<String> termids = null;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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

    public String getaTIxtn2() {
        return aTIxtn2;
    }

    public void setaTIxtn2(String aTIxtn2) {
        this.aTIxtn2 = aTIxtn2;
    }

    public List<String> getChapter() {
        return chapter;
    }

    public void setChapter(List<String> chapter) {
        this.chapter = chapter;
    }

    public Boolean getTrailer() {
        return isTrailer;
    }

    public void setTrailer(Boolean trailer) {
        isTrailer = trailer;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public List<String> getTermids() {
        return termids;
    }

    public void setTermids(List<String> termids) {
        this.termids = termids;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}

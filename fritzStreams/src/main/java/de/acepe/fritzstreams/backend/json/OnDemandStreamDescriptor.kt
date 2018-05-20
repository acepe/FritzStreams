package de.acepe.fritzstreams.backend.json

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class OnDemandStreamDescriptor {

    @SerializedName("_sortierArray")
    @Expose
    private var sortierArray: List<Int>? = null
        set
    @SerializedName("_defaultQuality")
    @Expose
    var defaultQuality: List<String>? = null
    @SerializedName("rbbtitle")
    @Expose
    var rbbtitle: String? = null
    @SerializedName("rbbhandle")
    @Expose
    var rbbhandle: String? = null
    @SerializedName("_type")
    @Expose
    var type: String? = null
    @SerializedName("_isLive")
    @Expose
    var live: Boolean? = null
    @SerializedName("_dvrEnabled")
    @Expose
    var dvrEnabled: Boolean? = null
    @SerializedName("_geoblocked")
    @Expose
    var geoblocked: Boolean? = null
    @SerializedName("_duration")
    @Expose
    var duration: Int? = null
    @SerializedName("_audioImage")
    @Expose
    var audioImage: String? = null
    @SerializedName("_mediaArray")
    @Expose
    var mediaArray: List<MediaArray>? = null
}
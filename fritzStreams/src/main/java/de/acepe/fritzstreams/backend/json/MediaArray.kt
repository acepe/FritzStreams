package de.acepe.fritzstreams.backend.json

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MediaArray {

    @SerializedName("_plugin")
    @Expose
    var plugin: Int? = null
    @SerializedName("_mediaStreamArray")
    @Expose
    var mediaStreamArray: List<MediaStreamArray>? = null
}
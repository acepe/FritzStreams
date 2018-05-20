package de.acepe.fritzstreams.backend.json

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MediaStreamArray {

    @SerializedName("_stream")
    @Expose
    var stream: String? = null
    @SerializedName("_termid")
    @Expose
    var termid: String? = null
    @SerializedName("_server")
    @Expose
    var server: String? = null

    @SerializedName("_quality")
    @Expose
    var quality: Int? = null
}
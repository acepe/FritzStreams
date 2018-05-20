package de.acepe.fritzstreams.backend.json

import java.util.HashMap

class Analytics {

    var rbbtitle: String? = null
    var rbbhandle: String? = null
    private var aTIxtn2: String? = null
    var chapter: List<String>? = null
    var trailer: Boolean? = null
    var duration: Int? = null
    var termids: List<String>? = null
    var additionalProperties: Map<String, Any> = HashMap()

    fun getaTIxtn2(): String? {
        return aTIxtn2
    }

    fun setaTIxtn2(aTIxtn2: String) {
        this.aTIxtn2 = aTIxtn2
    }
}

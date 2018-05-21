package de.acepe.fritzstreams.backend

import java.util.*

interface StreamCache {

    var day: Calendar?

    fun getStream(stream: Stream, day: Calendar): OnDemandStream

    fun scheduleDownload(downloadInfo: DownloadInfo)
}
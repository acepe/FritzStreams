package de.acepe.fritzstreams.backend

import java.io.Serializable

data class DownloadInfo(val title: String, val subtitle: String, val streamURL: String, val filename: String) : Serializable {
    var state: DownloadState = DownloadState.WAITING
    var progressPercent: Int = 0
    var downloadedSize: Int = 0
    var totalSize: Int = 0

}

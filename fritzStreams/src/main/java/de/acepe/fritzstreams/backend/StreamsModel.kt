package de.acepe.fritzstreams.backend

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import java.util.*

class StreamsModel(application: Application) : AndroidViewModel(application) {

    val day = MutableLiveData<Calendar>()

    fun setDay(day: Calendar) {
        this.day.value = day
    }

    private val streamManager: StreamManager by lazy(::StreamManager)

    fun getStream(streamType: StreamType, day: Calendar): OnDemandStream {
        return streamManager.getOrCreateStream(streamType, day)
    }

    fun crawlStream(stream: OnDemandStream, onInitDone: (OnDemandStream) -> Unit) {
        StreamCrawler(stream, onInitDone).crawl()
    }

    val downloadServiceAdapter: DownloadServiceAdapter by lazy {
        DownloadServiceAdapter(application)
    }

    fun scheduleDownload(downloadInfo: DownloadInfo) {
        downloadServiceAdapter.addDownload(downloadInfo)
    }
}

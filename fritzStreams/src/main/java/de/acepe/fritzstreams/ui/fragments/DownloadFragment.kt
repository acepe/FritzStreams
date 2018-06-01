package de.acepe.fritzstreams.ui.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.acepe.fritzstreams.R
import de.acepe.fritzstreams.backend.DownloadInfo
import de.acepe.fritzstreams.backend.DownloadServiceAdapter
import de.acepe.fritzstreams.backend.DownloadState.*
import de.acepe.fritzstreams.backend.StreamsModel
import de.acepe.fritzstreams.ui.components.DownloadEntryView
import de.acepe.fritzstreams.util.Utilities.getFreeSpaceExternal
import de.acepe.fritzstreams.util.Utilities.humanReadableBytes
import kotlinx.android.synthetic.main.download_fragment.*
import java.util.*

private const val UPDATE_INTERVAL_IN_MS: Long = 3000

class DownloadFragment : Fragment(), DownloadServiceAdapter.ResultReceiver {

    private val model: StreamsModel by lazy {
        ViewModelProviders.of(activity!!).get(StreamsModel::class.java)
    }

    private val downloadViews = HashMap<DownloadInfo, DownloadEntryView>()

    private var lastUpdateFreeSpace: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.download_fragment, container, false)
    }

    override fun onPause() {
        model.downloadServiceAdapter.removeResultReceiver(this)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        model.downloadServiceAdapter.registerResultReceiver(this)
        model.downloadServiceAdapter.queryDownloadInfos()
        updateFreeSpace()
    }

    override fun downloadsInQueue(downloadInfoList: List<DownloadInfo>) {
        downloadsContainer.removeAllViews()
        for (download in downloadInfoList) {
            addDownload(download)
        }
    }

    override fun updateProgress(downloadInfo: DownloadInfo) {
        if (!downloadViews.containsKey(downloadInfo)) {
            addDownload(downloadInfo)
        }
        val view = downloadViews[downloadInfo]
        view!!.setDownload(downloadInfo)

        val now = Calendar.getInstance().timeInMillis
        if (lastUpdateFreeSpace == 0L || lastUpdateFreeSpace < now - UPDATE_INTERVAL_IN_MS) {
            updateFreeSpace()
            lastUpdateFreeSpace = now
        }
    }

    private fun updateFreeSpace() {
        downloadsFreespace.text = context!!.getString(R.string.download_freespace, humanReadableBytes(getFreeSpaceExternal(), false))
    }

    private fun addDownload(download: DownloadInfo) {
        val context = context ?: return

        val downloadView = DownloadEntryView(context)
        downloadView.setDownload(download)
        downloadsContainer.addView(downloadView)

        downloadViews[download] = downloadView
        downloadView.setButtonAction(this::execute)
    }

    private fun execute(downloadInfo: DownloadInfo) {
        if (!isAdded) return

        val downloader = model.downloadServiceAdapter
        with(downloader) {
            when (downloadInfo.state) {
                DOWNLOADING -> cancelDownload(downloadInfo)
                WAITING, FINISHED -> {
                    removeDownload(downloadInfo)
                    cancelDownload(downloadInfo)
                }
                FAILED, CANCELLED -> {
                    removeDownload(downloadInfo)
                    downloadInfo.state = WAITING
                    addDownload(downloadInfo)
                }
            }
        }
    }


}

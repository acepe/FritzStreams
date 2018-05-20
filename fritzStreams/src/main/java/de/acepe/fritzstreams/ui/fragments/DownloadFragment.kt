package de.acepe.fritzstreams.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.acepe.fritzstreams.R
import de.acepe.fritzstreams.backend.DownloadInfo
import de.acepe.fritzstreams.backend.DownloadServiceAdapter
import de.acepe.fritzstreams.backend.DownloadState.*
import de.acepe.fritzstreams.ui.components.DownloadEntryView
import de.acepe.fritzstreams.util.Utilities.getFreeSpaceExternal
import de.acepe.fritzstreams.util.Utilities.humanReadableBytes
import kotlinx.android.synthetic.main.download_fragment.*
import java.util.*

private const val UPDATE_INTERVAL_IN_MS: Long = 3000

class DownloadFragment : Fragment(), DownloadServiceAdapter.ResultReceiver {

    private val downloadViews = HashMap<DownloadInfo, DownloadEntryView>()

    private lateinit var mDownloaderSupplier: DownloadServiceAdapterSupplier
    private var lastUpdateFreeSpace: Long = 0

    interface DownloadServiceAdapterSupplier {
        val downloader: DownloadServiceAdapter
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mDownloaderSupplier = context as DownloadServiceAdapterSupplier
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.download_fragment, container, false)
    }

    override fun onPause() {
        mDownloaderSupplier.downloader.removeResultReceiver(this)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mDownloaderSupplier.downloader.registerResultReceiver(this)
        mDownloaderSupplier.downloader.queryDownloadInfos()
        updateFreeSpace()
    }

    override fun downloadsInQueue(downloads: List<DownloadInfo>) {
        downloadsContainer.removeAllViews()
        for (download in downloads) {
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

        val downloader = mDownloaderSupplier.downloader
        with(downloader) {
            when (downloadInfo.state) {
                WAITING, FINISHED -> {
                    removeDownload(downloadInfo)
                    cancelDownload(downloadInfo)
                }
                DOWNLOADING -> cancelDownload(downloadInfo)
                FAILED, CANCELLED -> {
                    removeDownload(downloadInfo)
                    downloadInfo.state = WAITING
                    addDownload(downloadInfo)
                }
            }
        }
    }


}

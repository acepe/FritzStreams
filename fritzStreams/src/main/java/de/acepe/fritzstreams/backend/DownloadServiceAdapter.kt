package de.acepe.fritzstreams.backend

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import android.widget.Toast
import de.acepe.fritzstreams.R
import de.acepe.fritzstreams.backend.Constants.*
import java.util.*

class DownloadServiceAdapter : BroadcastReceiver() {

    private val statusIntentFilter: IntentFilter = IntentFilter(RESPONSE_ACTION)
    private val resultReceivers = ArrayList<ResultReceiver>()

    private var mContext: Context? = null

    interface ResultReceiver {
        fun downloadsInQueue(downloadInfoList: List<DownloadInfo>)

        fun updateProgress(downloadInfo: DownloadInfo)
    }

    fun attach(context: Context) {
        mContext = context
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(this, statusIntentFilter)
    }

    fun detach() {
        LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(this)
        resultReceivers.clear()
        mContext = null
    }

    fun detachFromService() {
        val intent = Intent(mContext, DownloadService::class.java)
        intent.putExtra(SERVICE_REQUEST, REQUEST_PERMISSION_TO_DIE_ACTION)
        mContext!!.startService(intent)
    }

    fun registerResultReceiver(resultReceiver: ResultReceiver) {
        resultReceivers.add(resultReceiver)
    }

    fun removeResultReceiver(resultReceiver: ResultReceiver) {
        resultReceivers.remove(resultReceiver)
    }

    fun queryDownloadInfos() {
        val intent = Intent(mContext, DownloadService::class.java)
        intent.putExtra(SERVICE_REQUEST, REQUEST_QUERY_DOWNLOADS_ACTION)
        mContext!!.startService(intent)
    }

    fun addDownload(download: DownloadInfo) {
        val intent = Intent(mContext, DownloadService::class.java)
        intent.putExtra(SERVICE_REQUEST, REQUEST_ADD_DOWNLOAD_ACTION)
        intent.putExtra(DOWNLOAD_INFO, download)
        mContext!!.startService(intent)
    }

    fun removeDownload(download: DownloadInfo) {
        val intent = Intent(mContext, DownloadService::class.java)
        intent.putExtra(SERVICE_REQUEST, REQUEST_REMOVE_DOWNLOAD_ACTION)
        intent.putExtra(DOWNLOAD_INFO, download)
        mContext!!.startService(intent)
    }

    fun cancelDownload(download: DownloadInfo) {
        val intent = Intent(mContext, DownloadService::class.java)
        intent.putExtra(SERVICE_REQUEST, REQUEST_CANCEL_DOWNLOAD_ACTION)
        intent.putExtra(DOWNLOAD_INFO, download)
        mContext!!.startService(intent)
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras
        val progress: DownloadInfo? = extras.get(CURRENT_DOWNLOAD_PROGRESS_REPORT) as DownloadInfo?
        if (progress != null) {
            for (resultReceiver in resultReceivers) {
                resultReceiver.updateProgress(progress)
            }
        }
        val alreadyInQueue: DownloadInfo? = extras.get(ALREADY_IN_QUEUE) as DownloadInfo?
        if (alreadyInQueue != null) {
            Toast.makeText(mContext, R.string.already_in_queue, Toast.LENGTH_SHORT).show()
        }
        val downloadQueue: ArrayList<DownloadInfo>? = extras.get(QUERY_DOWNLOADS) as ArrayList<DownloadInfo>?
        if (downloadQueue != null) {
            for (resultReceiver in resultReceivers) {
                resultReceiver.downloadsInQueue(downloadQueue)
            }
        }
    }
}

package de.acepe.fritzstreams.backend

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import de.acepe.fritzstreams.backend.Constants.*
import de.acepe.fritzstreams.backend.DownloadState.CANCELLED
import de.acepe.fritzstreams.backend.DownloadState.FINISHED
import de.acepe.fritzstreams.util.Notifications
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DownloadService : Service() {

    private val mScheduledDownloads = ArrayList<DownloadInfo>()

    private var mWifiLock: WifiManager.WifiLock? = null
    private var mWakeLock: PowerManager.WakeLock? = null
    private var permissionToDie = false
    private var executor: ExecutorService? = null

    private val downloadCallback = Downloader.DownloadCallback { downloadInfo ->
        notifyProgress(downloadInfo)
    }

    override fun onCreate() {
        Log.i(TAG, "Service created")
        executor = Executors.newSingleThreadExecutor()
        acquireLocks()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "Connected to Service")

        val extras = intent.extras ?: return Service.START_STICKY

        val request = extras.getString(SERVICE_REQUEST)
        if (REQUEST_ADD_DOWNLOAD_ACTION == request) {
            val info = extras.get(DOWNLOAD_INFO) as DownloadInfo
            val existingDownload = findScheduledDownload(info)
            if (existingDownload == null) {
                permissionToDie = false
                mScheduledDownloads.add(info)

                executor!!.execute { downloadWorker(info) }
                reportQueue()
            } else {
                sendMessage(Intent(RESPONSE_ACTION).putExtra(ALREADY_IN_QUEUE,
                        existingDownload))
            }
        }
        if (REQUEST_QUERY_DOWNLOADS_ACTION == request) {
            reportQueue()
        }
        if (REQUEST_CANCEL_DOWNLOAD_ACTION == request) {
            val info = extras.get(DOWNLOAD_INFO) as DownloadInfo
            val download = findScheduledDownload(info)
            if (download != null) {
                download.state = CANCELLED
            }
            reportQueue()
        }
        if (REQUEST_REMOVE_DOWNLOAD_ACTION == request) {
            val info = extras.get(DOWNLOAD_INFO) as DownloadInfo
            val download = findScheduledDownload(info)
            if (download != null) {
                download.state = CANCELLED
                mScheduledDownloads.remove(info)
            }
            reportQueue()
        }
        if (REQUEST_PERMISSION_TO_DIE_ACTION == request) {
            permissionToDie = true
        }
        if (allComplete() && permissionToDie) {
            stopSelf(startId)
        }
        return Service.START_NOT_STICKY
    }

    private fun downloadWorker(download: DownloadInfo) {
        startForeground(SERVICE_ID, Notifications.createNotification(this, 0))

        Log.i("$TAG-Handler", "Starting Download $download")

        Downloader(download, downloadCallback).download()
        downloadCallback.reportProgress(download)

        if (download.state === FINISHED) {
            MediaScannerConnection.scanFile(baseContext, arrayOf(download.filename), null, null)
        }
        reportQueue()
    }

    private fun notifyProgress(downloadInfo: DownloadInfo) {
        sendMessage(Intent(RESPONSE_ACTION).putExtra(CURRENT_DOWNLOAD_PROGRESS_REPORT, downloadInfo))
        startForeground(SERVICE_ID, Notifications.createNotification(this@DownloadService, downloadInfo.progressPercent))
    }

    private fun sendMessage(localIntent: Intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent)
    }

    private fun allComplete(): Boolean {
        return mScheduledDownloads.none { download -> download.state.isPending() }
    }

    private fun findScheduledDownload(info: DownloadInfo): DownloadInfo? {
        val index = mScheduledDownloads.indexOf(info)
        return if (index == -1) null else mScheduledDownloads[index]
    }

    private fun reportQueue() {
        val progressList = ArrayList(mScheduledDownloads)
        val localIntent = Intent(RESPONSE_ACTION).putExtra(QUERY_DOWNLOADS, progressList)
        sendMessage(localIntent)
    }

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    override fun onDestroy() {
        Log.i(TAG, "Destroying Service")
        executor!!.shutdownNow()
        releaseLocks()
    }

    private fun acquireLocks() {
        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
        mWakeLock!!.acquire((30 * MINUTE_IN_MILLIS).toLong())

        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mWifiLock = wifiManager.createWifiLock(WIFI_MODE_FULL_HIGH_PERF, TAG)
        mWifiLock!!.acquire()
    }

    private fun releaseLocks() {
        if (mWifiLock != null && mWifiLock!!.isHeld)
            mWifiLock!!.release()

        if (mWakeLock != null && mWakeLock!!.isHeld)
            mWakeLock!!.release()
    }


    companion object {
        private const val TAG = "DownloadService"
        private const val MINUTE_IN_MILLIS = 60 * 1000
        const val SERVICE_ID = 2
    }

}
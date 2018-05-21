package de.acepe.fritzstreams.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.util.Log
import de.acepe.fritzstreams.backend.*
import java.util.*

private const val TAG = "CacheFragment"

class CacheFragment : Fragment(), StreamCache {

    override var day: Calendar? = null

    private val streamManager: StreamManager by lazy {
        StreamManager(context!!.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath())
    }
    val downloadServiceAdapter: DownloadServiceAdapter by lazy {
        DownloadServiceAdapter()
    }


    /*
     * This method will only be called once (but after onAttach) when the retained Fragment is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Cache Fragement created")

        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    /*
     * Hold a reference to the parent Activity so we can report the task's current progress and results. The Android
     * framework will pass us a reference to the newly created Activity after each configuration change.
     */
    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        downloadServiceAdapter.attach(activity)
    }

    /*
     * Set the callback to null so we don't accidentally leak the Activity instance.
     */
    override fun onDetach() {
        super.onDetach()
        downloadServiceAdapter.detach()
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadServiceAdapter.detachFromService()
    }

    override fun getStream(stream: Stream, day: Calendar): OnDemandStream {
        return streamManager.getOrCreateStream(stream, day)
    }

    override fun scheduleDownload(downloadInfo: DownloadInfo) {
        downloadServiceAdapter.addDownload(downloadInfo)
    }

}

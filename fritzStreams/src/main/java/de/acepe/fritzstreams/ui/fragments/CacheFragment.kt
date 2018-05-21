package de.acepe.fritzstreams.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import de.acepe.fritzstreams.backend.DownloadInfo
import de.acepe.fritzstreams.backend.DownloadServiceAdapter
import de.acepe.fritzstreams.backend.StreamInfo
import de.acepe.fritzstreams.backend.StreamInfo.Stream.NIGHTFLIGHT
import de.acepe.fritzstreams.backend.StreamInfo.Stream.SOUNDGARDEN
import java.util.*

private const val TAG = "CacheFragment"

class CacheFragment : Fragment() {
    var day: Calendar? = null

    val downloadServiceAdapter: DownloadServiceAdapter by lazy {
        DownloadServiceAdapter()
    }

    private val mSoundgardenStreamsForDay = HashMap<Calendar, StreamInfo>()
    private val mNightflightStreamsForDay = HashMap<Calendar, StreamInfo>()

    /*
     * This     method will only be called once (but after onAttach) when the retained Fragment is first created.
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

    fun addStream(streamInfo: StreamInfo) {
        when (streamInfo.stream) {
            SOUNDGARDEN -> mSoundgardenStreamsForDay[streamInfo.day] = streamInfo
            NIGHTFLIGHT -> mNightflightStreamsForDay[streamInfo.day] = streamInfo
        }
        Log.i(TAG, "Added $streamInfo")
    }

    fun getStream(stream: StreamInfo.Stream, day: Calendar): StreamInfo? {
        val streamsForDay = if (stream == NIGHTFLIGHT) mNightflightStreamsForDay else mSoundgardenStreamsForDay
        return streamsForDay[day]
    }

    fun scheduleDownload(downloadInfo: DownloadInfo) {
        downloadServiceAdapter.addDownload(downloadInfo)
    }

}

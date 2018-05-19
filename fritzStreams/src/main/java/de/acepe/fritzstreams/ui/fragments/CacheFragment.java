package de.acepe.fritzstreams.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import de.acepe.fritzstreams.backend.DownloadInfo;
import de.acepe.fritzstreams.backend.DownloadServiceAdapter;
import de.acepe.fritzstreams.backend.StreamInfo;

import java.util.Calendar;
import java.util.HashMap;

import static de.acepe.fritzstreams.backend.StreamInfo.Stream.NIGHTFLIGHT;
import static de.acepe.fritzstreams.backend.StreamInfo.Stream.SOUNDGARDEN;

/**
 * A simple {@link Fragment} subclass.
 */
public class CacheFragment extends Fragment {

    private static final String TAG = "CacheFragment";

    private HashMap<Calendar, StreamInfo> mSoundgardenStreamsForDay;
    private HashMap<Calendar, StreamInfo> mNightflightStreamsForDay;
    private Context mContext;
    private DownloadServiceAdapter downloadServiceAdapter;
    private Calendar mDay;

    /**
     * This method will only be called once when the retained Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Cache Fragement created");

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        mSoundgardenStreamsForDay = new HashMap<>();
        mNightflightStreamsForDay = new HashMap<>();

        downloadServiceAdapter = new DownloadServiceAdapter();
        downloadServiceAdapter.attach(mContext);
    }

    /**
     * Hold a reference to the parent Activity so we can report the task's current progress and results. The Android
     * framework will pass us a reference to the newly created Activity after each configuration change.
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        mContext = activity;
        if (downloadServiceAdapter != null) {
            downloadServiceAdapter.attach(mContext);
        }
    }

    /**
     * Set the callback to null so we don't accidentally leak the Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        downloadServiceAdapter.detach();
        mContext = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        downloadServiceAdapter.detachFromService();
    }

    public void setmDay(Calendar day) {
        this.mDay = day;
    }

    public Calendar getDay() {
        return mDay;
    }

    public void addStream(StreamInfo streamInfo) {
        if (streamInfo.getStream() == SOUNDGARDEN) {
            mSoundgardenStreamsForDay.put(streamInfo.getDay(), streamInfo);
            Log.i(TAG, "Added " + streamInfo);
        }
        if (streamInfo.getStream() == NIGHTFLIGHT) {
            mNightflightStreamsForDay.put(streamInfo.getDay(), streamInfo);
            Log.i(TAG, "Added " + streamInfo);
        }
    }

    public StreamInfo getStream(StreamInfo.Stream stream, Calendar day) {
        HashMap<Calendar, StreamInfo> streamsForDay = stream == NIGHTFLIGHT
                ? mNightflightStreamsForDay
                : mSoundgardenStreamsForDay;
        return streamsForDay.get(day);
    }

    public void scheduleDownload(DownloadInfo downloadInfo) {
        downloadServiceAdapter.addDownload(downloadInfo);
    }

    public DownloadServiceAdapter getDownloadServiceAdapter() {
        return downloadServiceAdapter;
    }

}

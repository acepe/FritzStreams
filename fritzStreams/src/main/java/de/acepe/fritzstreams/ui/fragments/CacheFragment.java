package de.acepe.fritzstreams.ui.fragments;

import static de.acepe.fritzstreams.backend.StreamInfo.Stream.NIGHTFLIGHT;
import static de.acepe.fritzstreams.backend.StreamInfo.Stream.SOUNDGARDEN;

import java.util.Calendar;
import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import de.acepe.fritzstreams.backend.StreamInfo;

/**
 * A simple {@link Fragment} subclass.
 */
public class CacheFragment extends Fragment {

    private static final String TAG = "CacheFragment";

    private HashMap<Calendar, StreamInfo> mSoundgardenStreamsForDay;
    private HashMap<Calendar, StreamInfo> mNightflightStreamsForDay;

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

        // Create and execute the background task.
        // mTask = new DummyTask();
        // mTask.execute();
    }

    /**
     * Hold a reference to the parent Activity so we can report the task's current progress and results. The Android
     * framework will pass us a reference to the newly created Activity after each configuration change.
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
    }

    /**
     * Set the callback to null so we don't accidentally leak the Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
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

}

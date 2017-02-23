package de.acepe.fritzstreams.ui.fragments;

import static de.acepe.fritzstreams.backend.StreamInfo.Stream.NIGHTFLIGHT;
import static de.acepe.fritzstreams.backend.StreamInfo.Stream.SOUNDGARDEN;
import static de.acepe.fritzstreams.util.Utilities.today;

import java.util.Calendar;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.DownloadInfo;
import de.acepe.fritzstreams.backend.StreamInfo;
import de.acepe.fritzstreams.ui.components.StreamView;
import de.acepe.fritzstreams.util.Utilities;

public class StreamsOverviewFragment extends Fragment {

    public interface StreamsCache {
        void addStream(StreamInfo streamInfo);

        StreamInfo getStream(StreamInfo.Stream stream, Calendar day);

        void scheduleDownload(DownloadInfo streamDownload);
    }

    private static final String TAG = "StreamOverviewFragment";

    private HashMap<View, Calendar> mDayButtons;
    private StreamsCache mStreamsCache;
    private StreamView mStreamViewSoundgarden;
    private StreamView mStreamViewNightflight;
    private StreamInfo mNightflightStreamInfo;
    private StreamInfo mSoundgardenStreamInfo;
    private RadioGroup mDaysToggleGroup;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mStreamsCache = (StreamsCache) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement StreamsCache");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mStreamsCache = null; // avoid leaking of context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streams_overview, container, false);

        mDayButtons = new HashMap<>();

        mDaysToggleGroup = ((RadioGroup) view.findViewById(R.id.daysToggleGroup));
        mDaysToggleGroup.setOnCheckedChangeListener(toggleListener);
        configureToggleButtons();

        mStreamViewNightflight = (StreamView) view.findViewById(R.id.ilbDownloadNightflight);
        mStreamViewNightflight.setOnClickListener(new DownloadOnclickListener(NIGHTFLIGHT));

        mStreamViewSoundgarden = (StreamView) view.findViewById(R.id.ilbDownloadSoundgarden);
        mStreamViewSoundgarden.setOnClickListener(new DownloadOnclickListener(SOUNDGARDEN));

        ToggleButton todaysToggle = (ToggleButton) mDaysToggleGroup.getChildAt(mDayButtons.size() - 1);
        todaysToggle.setChecked(true);
        onSelectedDayChange(today());

        return view;
    }

    private void configureToggleButtons() {
        for (int i = 0; i < mDaysToggleGroup.getChildCount(); i++) {
            ToggleButton view = (ToggleButton) mDaysToggleGroup.getChildAt(6 - i);
            view.setOnClickListener(oclDaySelected);

            Calendar date = today();
            date.add(Calendar.DAY_OF_YEAR, -i);

            String text = App.DAY_FORMAT.format(date.getTime());
            view.setTextOff(text);
            view.setTextOn(text);
            view.setText(text);
            mDayButtons.put(view, date);
        }
    }

    private void onSelectedDayChange(Calendar day) {
        mSoundgardenStreamInfo = init(SOUNDGARDEN, day);
        mNightflightStreamInfo = init(NIGHTFLIGHT, day);
    }

    private StreamInfo init(StreamInfo.Stream stream, Calendar day) {
        StreamView view = stream == NIGHTFLIGHT ? mStreamViewNightflight : mStreamViewSoundgarden;
        view.clearStream();
        StreamInfo streamInfo = mStreamsCache.getStream(stream, day);
        if (streamInfo == null) {
            streamInfo = new StreamInfo(getActivity(), day, stream);
            streamInfo.init(new InitStreamCallback(view));
        } else {
            setStreamView(view, streamInfo);
        }
        return streamInfo;
    }

    private class InitStreamCallback implements StreamInfo.Callback {
        private final StreamView view;

        InitStreamCallback(StreamView view) {
            this.view = view;
        }

        @Override
        public void initFinished(StreamInfo streamInfo) {
            setStreamView(view, streamInfo);
        }

    }

    private void setStreamView(StreamView view, StreamInfo streamInfo) {
        if (streamInfo == null) {
            view.failed();
        } else {
            view.setStreamInfo(streamInfo);
            mStreamsCache.addStream(streamInfo);
        }
    }

    static final RadioGroup.OnCheckedChangeListener toggleListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                ToggleButton view = (ToggleButton) radioGroup.getChildAt(i);
                boolean checked = view.getId() == checkedId;
                view.setChecked(checked);
            }
        }
    };

    private final View.OnClickListener oclDaySelected = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((RadioGroup) v.getParent()).clearCheck();
            ((RadioGroup) v.getParent()).check(v.getId());
            onSelectedDayChange(mDayButtons.get(v));
        }
    };

    private class DownloadOnclickListener implements View.OnClickListener {
        private final StreamInfo.Stream stream;

        DownloadOnclickListener(StreamInfo.Stream stream) {
            this.stream = stream;
        }

        @Override
        public void onClick(View v) {
            StreamInfo streamInfo = stream == NIGHTFLIGHT ? mNightflightStreamInfo : mSoundgardenStreamInfo;

            if (streamInfo.isInitFailed()) {
                init(stream, streamInfo.getDay());
            } else {
                download(streamInfo);
            }
        }
    }

    private void download(StreamInfo streamInfo) {
        if (!streamInfo.isInited()) {
            return;
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sharedPref.getBoolean(App.SP_WIFI_ONLY, false) && !Utilities.onWifi(getContext())) {
            Toast.makeText(getContext(), R.string.download_only_wifi_notification_title, Toast.LENGTH_SHORT).show();
            return;
        }

        mStreamsCache.scheduleDownload(createDownloadInfo(streamInfo));
    }

    @NonNull
    private DownloadInfo createDownloadInfo(StreamInfo streamInfo) {
        return new DownloadInfo(streamInfo.getTitle(),
                                streamInfo.getSubtitle(),
                                streamInfo.getStreamURL(),
                                streamInfo.getFilename());
    }

}

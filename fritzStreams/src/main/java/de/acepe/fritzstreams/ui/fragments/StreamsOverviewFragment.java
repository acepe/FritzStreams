package de.acepe.fritzstreams.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import java.util.Calendar;
import java.util.HashMap;

import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.StreamDownload;
import de.acepe.fritzstreams.backend.StreamInfo;
import de.acepe.fritzstreams.ui.components.StreamView;

import static de.acepe.fritzstreams.backend.StreamInfo.Stream.NIGHTFLIGHT;
import static de.acepe.fritzstreams.backend.StreamInfo.Stream.SOUNDGARDEN;

public class StreamsOverviewFragment extends Fragment {

    private static final String TAG = "StreamOverview";

    private HashMap<View, Calendar> mDayButtons;
    private HashMap<Calendar, StreamInfo> mSoundgardenStreamsForDay;
    private HashMap<Calendar, StreamInfo> mNightflightStreamsForDay;
    private StreamView mStreamViewSoundgarden;
    private StreamView mStreamViewNightflight;
    private StreamInfo mNightflightStreamInfo;
    private StreamInfo mSoundgardenStreamInfo;
    private RadioGroup mDaysToggleGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streams_overview, container, false);

        mDayButtons = new HashMap<>();
        mSoundgardenStreamsForDay = new HashMap<>();
        mNightflightStreamsForDay = new HashMap<>();

        mDaysToggleGroup = ((RadioGroup) view.findViewById(R.id.daysToggleGroup));
        mDaysToggleGroup.setOnCheckedChangeListener(toggleListener);
        configureToggleButtons();

        mStreamViewNightflight = (StreamView) view.findViewById(R.id.ilbDownloadNightflight);
        mStreamViewNightflight.setOnClickListener(oclDownloadNightflight);

        mStreamViewSoundgarden = (StreamView) view.findViewById(R.id.ilbDownloadSoundgarden);
        mStreamViewSoundgarden.setOnClickListener(oclDownloadSoundgarden);

        ToggleButton todaysToggle = (ToggleButton) mDaysToggleGroup.getChildAt(mDayButtons.size() - 1);
        todaysToggle.setChecked(true);
        onSelectedDayChange(Calendar.getInstance());

        return view;
    }

    private void configureToggleButtons() {
        for (int i = 0; i < mDaysToggleGroup.getChildCount(); i++) {
            ToggleButton view = (ToggleButton) mDaysToggleGroup.getChildAt(6 - i);
            view.setOnClickListener(oclDaySelected);

            Calendar date = Calendar.getInstance();
            date.add(Calendar.DAY_OF_YEAR, -i);

            String text = App.DAY_FORMAT.format(date.getTime());
            view.setTextOff(text);
            view.setTextOn(text);
            view.setText(text);
            mDayButtons.put(view, date);
        }
    }

    private void onSelectedDayChange(Calendar day) {
        mStreamViewSoundgarden.clearStream();

        StreamInfo streamInfoSoundgarden = mSoundgardenStreamsForDay.get(day);
        if (streamInfoSoundgarden == null) {
            mSoundgardenStreamInfo = new StreamInfo(getActivity(), day, SOUNDGARDEN);
            mSoundgardenStreamInfo.init(new InitStreamCallback(mStreamViewSoundgarden));
        } else {
            setStreamView(mStreamViewSoundgarden, streamInfoSoundgarden);
        }

        mStreamViewNightflight.clearStream();
        StreamInfo streamInfoNightflight = mNightflightStreamsForDay.get(day);
        if (streamInfoNightflight == null) {
            mNightflightStreamInfo = new StreamInfo(getActivity(), day, NIGHTFLIGHT);
            mNightflightStreamInfo.init(new InitStreamCallback(mStreamViewNightflight));
        } else {
            setStreamView(mStreamViewNightflight, streamInfoNightflight);
        }

    }

    private class InitStreamCallback implements StreamInfo.Callback {
        private StreamView view;

        public InitStreamCallback(StreamView view) {
            this.view = view;
        }

        @Override
        public void initFinished(StreamInfo streamInfo) {
            setStreamView(view, streamInfo);
        }

    }

    private void setStreamView(StreamView view, StreamInfo streamInfo) {
        view.setStreamInfo(streamInfo);
        if (streamInfo.getStream() == NIGHTFLIGHT) {
            mNightflightStreamsForDay.put(streamInfo.getDay(), streamInfo);
        }
        if (streamInfo.getStream() == SOUNDGARDEN) {
            mSoundgardenStreamsForDay.put(streamInfo.getDay(), streamInfo);
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

    private final View.OnClickListener oclDownloadNightflight = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            download(mNightflightStreamInfo);
        }
    };

    private final View.OnClickListener oclDownloadSoundgarden = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            download(mSoundgardenStreamInfo);
        }
    };

    private void download(StreamInfo streamInfo) {
        if (!streamInfo.isInited())
            return;

        App.downloader.scheduleDownload(new StreamDownload(getActivity(), streamInfo));
    }

}

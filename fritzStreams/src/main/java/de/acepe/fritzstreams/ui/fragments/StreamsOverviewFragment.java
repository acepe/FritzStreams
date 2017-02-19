package de.acepe.fritzstreams.ui.fragments;

import static de.acepe.fritzstreams.backend.StreamInfo.Stream.NIGHTFLIGHT;
import static de.acepe.fritzstreams.backend.StreamInfo.Stream.SOUNDGARDEN;
import static de.acepe.fritzstreams.util.Utilities.today;

import java.util.Calendar;
import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.ToggleButton;
import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.StreamDownload;
import de.acepe.fritzstreams.backend.StreamInfo;
import de.acepe.fritzstreams.ui.components.StreamView;

public class StreamsOverviewFragment extends Fragment {

    public interface StreamsCache {
        void addStream(StreamInfo streamInfo);

        StreamInfo getStream(StreamInfo.Stream stream, Calendar day);
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
        mStreamViewNightflight.setOnClickListener(oclDownloadNightflight);

        mStreamViewSoundgarden = (StreamView) view.findViewById(R.id.ilbDownloadSoundgarden);
        mStreamViewSoundgarden.setOnClickListener(oclDownloadSoundgarden);

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
        mStreamViewSoundgarden.clearStream();

        StreamInfo streamInfoSoundgarden = mStreamsCache.getStream(SOUNDGARDEN, day);
        if (streamInfoSoundgarden == null) {
            mSoundgardenStreamInfo = new StreamInfo(getActivity(), day, SOUNDGARDEN);
            mSoundgardenStreamInfo.init(new InitStreamCallback(mStreamViewSoundgarden));
        } else {
            setStreamView(mStreamViewSoundgarden, streamInfoSoundgarden);
        }

        mStreamViewNightflight.clearStream();
        StreamInfo streamInfoNightflight = mStreamsCache.getStream(NIGHTFLIGHT, day);
        if (streamInfoNightflight == null) {
            mNightflightStreamInfo = new StreamInfo(getActivity(), day, NIGHTFLIGHT);
            mNightflightStreamInfo.init(new InitStreamCallback(mStreamViewNightflight));
        } else {
            setStreamView(mStreamViewNightflight, streamInfoNightflight);
        }

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
        view.setStreamInfo(streamInfo);
        mStreamsCache.addStream(streamInfo);
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

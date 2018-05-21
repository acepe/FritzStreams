package de.acepe.fritzstreams.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.Constants;
import de.acepe.fritzstreams.backend.DownloadInfo;
import de.acepe.fritzstreams.backend.StreamInfo;
import de.acepe.fritzstreams.ui.components.StreamView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static de.acepe.fritzstreams.backend.StreamInfo.Stream.NIGHTFLIGHT;
import static de.acepe.fritzstreams.backend.StreamInfo.Stream.SOUNDGARDEN;
import static de.acepe.fritzstreams.util.Utilities.today;

public class StreamsOverviewFragment extends Fragment {

    public interface StreamsCache {
        void addStream(StreamInfo streamInfo);

        StreamInfo getStream(StreamInfo.Stream stream, Calendar day);

        void scheduleDownload(DownloadInfo streamDownload);

        void setDay(Calendar day);

        Calendar getDay();
    }

    private static final String TAG = "StreamOverviewFragment";

    private HashMap<ToggleButton, Calendar> mDayButtons;
    private StreamsCache mStreamsCache;
    private StreamView mStreamViewSoundgarden;
    private StreamView mStreamViewNightflight;
    private StreamInfo mNightflightStreamInfo;
    private StreamInfo mSoundgardenStreamInfo;
    private ViewGroup mDaysToggleGroup;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mStreamsCache = (StreamsCache) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mStreamsCache = null; // avoid leaking of context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_streams_overview, container, false);

        mDaysToggleGroup = view.findViewById(R.id.daysToggleGroup);

        configureToggleButtons();

        mStreamViewNightflight = view.findViewById(R.id.streamViewNightflight);
        mStreamViewNightflight.setOnClickListener(new DownloadOnclickListener(NIGHTFLIGHT));

        mStreamViewSoundgarden = view.findViewById(R.id.streamViewSoundgarden);
        mStreamViewSoundgarden.setOnClickListener(new DownloadOnclickListener(SOUNDGARDEN));

        Calendar dayFromCache = mStreamsCache.getDay();
        Calendar day = dayFromCache != null ? dayFromCache : today();
        onSelectedDayChange(day);

        ToggleButton daysToggle = findToggle(day);
        if (daysToggle != null) {
            daysToggle.setChecked(true);
        }

        return view;
    }

    private ToggleButton findToggle(Calendar day) {
        for (Map.Entry<ToggleButton, Calendar> entry : mDayButtons.entrySet()) {
            if (entry.getValue().equals(day))
                return entry.getKey();
        }
        return null;
    }

    private void configureToggleButtons() {
        mDayButtons = new HashMap<>();
        for (int i = 0; i < mDaysToggleGroup.getChildCount(); i++) {
            ToggleButton view = (ToggleButton) mDaysToggleGroup.getChildAt(6 - i);
            view.setOnClickListener(oclDaySelected);

            Calendar date = today();
            date.add(Calendar.DAY_OF_YEAR, -i);

            String text = Constants.DAY_FORMAT.format(date.getTime());
            view.setTextOff(text);
            view.setTextOn(text);
            view.setText(text);
            mDayButtons.put(view, date);
        }
    }

    private void onSelectedDayChange(Calendar day) {
        mStreamsCache.setDay(day);
        mSoundgardenStreamInfo = init(SOUNDGARDEN, day);
        mNightflightStreamInfo = init(NIGHTFLIGHT, day);
    }

    private StreamInfo init(StreamInfo.Stream stream, Calendar day) {
        StreamView view = stream == NIGHTFLIGHT ? mStreamViewNightflight : mStreamViewSoundgarden;
        view.clearStream();

        StreamInfo streamInfo = mStreamsCache.getStream(stream, day);
        if (streamInfo == null) {
            if (stream == SOUNDGARDEN && isTodayBeforeSoundgardenRelease(day)) {
                Calendar dayInLastWeek = (Calendar) day.clone();
                dayInLastWeek.add(Calendar.DAY_OF_YEAR, -7);
                streamInfo = new StreamInfo(getActivity(), dayInLastWeek, stream);
            } else {
                streamInfo = new StreamInfo(getActivity(), day, stream);
            }
            streamInfo.init(new InitStreamCallback(view));
        } else {
            setStreamView(view, streamInfo);
        }
        return streamInfo;
    }

    private boolean isTodayBeforeSoundgardenRelease(Calendar day) {
        Calendar todayAt2200 = today();
        todayAt2200.set(Calendar.HOUR_OF_DAY, 22);
        todayAt2200.set(Calendar.HOUR, 22);

        return today().get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
                && Calendar.getInstance().before(todayAt2200);
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
            if (mStreamsCache != null) {
                mStreamsCache.addStream(streamInfo);
            }
        }
    }

    private final View.OnClickListener oclDaySelected = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int childCount = mDaysToggleGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ToggleButton view = (ToggleButton) mDaysToggleGroup.getChildAt(i);
                boolean checked = view.getId() == v.getId();
                view.setChecked(checked);

                if (checked) {
                    onSelectedDayChange(mDayButtons.get(view));
                }
            }
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
        Toast.makeText(getContext(), R.string.download_started, Toast.LENGTH_SHORT).show();

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

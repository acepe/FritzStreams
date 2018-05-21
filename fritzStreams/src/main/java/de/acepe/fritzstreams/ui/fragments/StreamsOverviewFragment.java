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
import de.acepe.fritzstreams.backend.OnDemandStream;
import de.acepe.fritzstreams.backend.Stream;
import de.acepe.fritzstreams.ui.components.StreamView;

import java.util.Calendar;

import static de.acepe.fritzstreams.backend.Stream.NIGHTFLIGHT;
import static de.acepe.fritzstreams.backend.Stream.SOUNDGARDEN;
import static de.acepe.fritzstreams.util.Utilities.today;

public class StreamsOverviewFragment extends Fragment {

    public interface StreamsCache {

        OnDemandStream getStream(Stream stream, Calendar day);

        void scheduleDownload(DownloadInfo streamDownload);

        void setDay(Calendar day);

        Calendar getDay();
    }

    private StreamsCache mStreamsCache;

    private ViewGroup mDaysToggleGroup;
    private StreamView mStreamViewSoundgarden;
    private StreamView mStreamViewNightflight;

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
        for (int i = 0; i < mDaysToggleGroup.getChildCount(); i++) {
            ToggleButton view = (ToggleButton) mDaysToggleGroup.getChildAt(i);
            if (view.getTag().equals(day))
                return view;
        }
        return null;
    }

    private void configureToggleButtons() {
        for (int i = 0; i < mDaysToggleGroup.getChildCount(); i++) {
            ToggleButton view = (ToggleButton) mDaysToggleGroup.getChildAt(6 - i);

            Calendar day = today();
            day.add(Calendar.DAY_OF_YEAR, -i);

            String text = Constants.DAY_FORMAT.format(day.getTime());
            view.setTextOff(text);
            view.setTextOn(text);
            view.setText(text);
            view.setOnClickListener(new MyOnClickListener(day));
            view.setTag(day);
        }
    }

    private void onSelectedDayChange(Calendar day) {
        mStreamsCache.setDay(day);
        init(SOUNDGARDEN, day);
        init(NIGHTFLIGHT, day);
    }

    private void init(Stream stream, Calendar day) {
        StreamView view = stream == NIGHTFLIGHT ? mStreamViewNightflight : mStreamViewSoundgarden;
        view.clearStream();

        OnDemandStream onDemandStream = mStreamsCache.getStream(stream, day);
        onDemandStream.init(new InitStreamCallback(view));
    }

    private class InitStreamCallback implements OnDemandStream.Callback {
        private final StreamView view;

        InitStreamCallback(StreamView view) {
            this.view = view;
        }

        @Override
        public void initFinished(OnDemandStream onDemandStream) {
            setStreamView(view, onDemandStream);
        }
    }

    private void setStreamView(StreamView view, OnDemandStream onDemandStream) {
        if (onDemandStream.isInitFailed()) {
            view.failed();
            return;
        }
        view.setStreamInfo(onDemandStream);
    }


    private class DownloadOnclickListener implements View.OnClickListener {
        private final Stream stream;

        DownloadOnclickListener(Stream stream) {
            this.stream = stream;
        }

        @Override
        public void onClick(View v) {
            OnDemandStream onDemandStream = mStreamsCache.getStream(stream, mStreamsCache.getDay());

            if (onDemandStream.isInitFailed()) {
                init(stream, onDemandStream.getDay());
            } else {
                download(onDemandStream);
            }
        }
    }

    private void download(OnDemandStream onDemandStream) {
        if (!onDemandStream.isInited()) {
            return;
        }
        Toast.makeText(getContext(), R.string.download_started, Toast.LENGTH_SHORT).show();

        mStreamsCache.scheduleDownload(createDownloadInfo(onDemandStream));
    }

    @NonNull
    private DownloadInfo createDownloadInfo(OnDemandStream onDemandStream) {
        return new DownloadInfo(onDemandStream.getTitle(),
                onDemandStream.getSubtitle(),
                onDemandStream.getStreamURL(),
                onDemandStream.getFilename());
    }

    private final class MyOnClickListener implements View.OnClickListener {
        private final Calendar day;

        private MyOnClickListener(Calendar day) {
            this.day = day;
        }

        @Override
        public void onClick(View v) {
            int childCount = mDaysToggleGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ToggleButton toggle = (ToggleButton) mDaysToggleGroup.getChildAt(i);
                boolean checked = toggle.getId() == v.getId();
                toggle.setChecked(checked);

                if (checked) {
                    onSelectedDayChange(day);
                }
            }
        }
    }
}

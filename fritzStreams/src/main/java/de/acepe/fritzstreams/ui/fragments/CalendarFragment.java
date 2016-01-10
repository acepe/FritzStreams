package de.acepe.fritzstreams.ui.fragments;

import static de.acepe.fritzstreams.backend.StreamInfo.Stream.NIGHTFLIGHT;
import static de.acepe.fritzstreams.backend.StreamInfo.Stream.SOUNDGARDEN;

import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.StreamDownload;
import de.acepe.fritzstreams.backend.StreamInfo;
import de.acepe.fritzstreams.ui.components.IconLinkButton;

public class CalendarFragment extends Fragment {

    private CalendarView mCalendarView;

    private IconLinkButton mBtnDownloadSoundgarden;
    private IconLinkButton mBtnDownloadNightflight;
    private StreamInfo mNightflightStreamInfo;
    private StreamInfo mSoundgardenStreamInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_chooser, container, false);

        mCalendarView = (CalendarView) view.findViewById(R.id.calendarView);

        mBtnDownloadNightflight = (IconLinkButton) view.findViewById(R.id.ilbDownloadNightflight);
        mBtnDownloadNightflight.setOnClickListener(oclDownloadNightflight);

        mBtnDownloadSoundgarden = (IconLinkButton) view.findViewById(R.id.ilbDownloadSoundgarden);
        mBtnDownloadSoundgarden.setOnClickListener(oclDownloadSoundgarden);

        mCalendarView.setOnDateChangeListener(odclCalendar);

        updateLabels(Calendar.getInstance());

        return view;
    }

    private CalendarView.OnDateChangeListener odclCalendar = new CalendarView.OnDateChangeListener() {
        @Override
        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);

            updateLabels(cal);
        }
    };

    private void updateLabels(Calendar cal) {
        mNightflightStreamInfo = new StreamInfo(getActivity(), cal, NIGHTFLIGHT);
        mNightflightStreamInfo.init(new StreamInfo.Callback() {
            @Override
            public void initFinished() {
                mBtnDownloadNightflight.setCategoryText(mNightflightStreamInfo.getTitle());
                mBtnDownloadNightflight.setGenreText(mNightflightStreamInfo.getSubtitle());
            }
        });
        mSoundgardenStreamInfo = new StreamInfo(getActivity(), cal, SOUNDGARDEN);
        mSoundgardenStreamInfo.init(new StreamInfo.Callback() {
            @Override
            public void initFinished() {
                mBtnDownloadSoundgarden.setCategoryText(mSoundgardenStreamInfo.getTitle());
                mBtnDownloadSoundgarden.setGenreText(mSoundgardenStreamInfo.getSubtitle());
            }
        });
    }

    private View.OnClickListener oclDownloadNightflight = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            download(mNightflightStreamInfo);
        }
    };

    private View.OnClickListener oclDownloadSoundgarden = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            download(mSoundgardenStreamInfo);
        }
    };

    private void download(StreamInfo streamInfo) {
        if (!streamInfo.isInited())
            return;

        StreamDownload streamDownload = new StreamDownload(getActivity(), streamInfo);
        App.downloaders.add(streamDownload);
        if (!isDownloadInProgress()) {
            Toast.makeText(getActivity(), R.string.download_noti_started, Toast.LENGTH_SHORT).show();
            streamDownload.downloadAndConvert();
        }
    }

    private boolean isDownloadInProgress() {
        return App.activeDownload != null;
    }

}

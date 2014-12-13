package de.acepe.fritzstreams.ui.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.Stream;
import de.acepe.fritzstreams.backend.StreamDownload;
import de.acepe.fritzstreams.ui.components.IconLinkButton;

public class CalendarFragment extends Fragment {

    private CalendarView mCalendarView;
    private TextView mDayOfWeek;

    private IconLinkButton mBtnDownloadSoundgarden;
    private IconLinkButton mBtnDownloadNightflight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_chooser, container, false);

        mCalendarView = (CalendarView) view.findViewById(R.id.calendarView);
        mDayOfWeek = (TextView) view.findViewById(R.id.dayOfWeekLabel);

        mBtnDownloadNightflight = (IconLinkButton) view.findViewById(R.id.ilbDownloadNightflight);
        mBtnDownloadNightflight.setOnClickListener(oclDownloadNightflight);

        mBtnDownloadSoundgarden = (IconLinkButton) view.findViewById(R.id.ilbDownloadSoundgarden);
        mBtnDownloadSoundgarden.setOnClickListener(oclDownloadSoundgarden);

        mCalendarView.setOnDateChangeListener(odclCalendar);

        updateLabels(Calendar.getInstance());

        return view;
    }

    private void updateLabels(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat(App.DAY_OF_WEEK_FORMAT);
        String dayOfWeek = sdf.format(cal.getTime());

        mDayOfWeek.setText(dayOfWeek);
        mBtnDownloadNightflight.setGenreText(Stream.nightflight.getStreamType(cal));
        mBtnDownloadSoundgarden.setGenreText(Stream.soundgarden.getStreamType(cal));
    }

    private CalendarView.OnDateChangeListener odclCalendar = new CalendarView.OnDateChangeListener() {
        @Override
        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);

            updateLabels(cal);
        }
    };

    private View.OnClickListener oclDownloadNightflight = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            download(Stream.nightflight);
        }
    };

    private View.OnClickListener oclDownloadSoundgarden = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            download(Stream.soundgarden);
        }
    };

    private void download(Stream stream) {
        long selected = mCalendarView.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selected);

        StreamDownload streamDownload = new StreamDownload(getActivity(), cal, stream);
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

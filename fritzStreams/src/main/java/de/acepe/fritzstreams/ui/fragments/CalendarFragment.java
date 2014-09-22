package de.acepe.fritzstreams.ui.fragments;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.StreamDownloader;
import de.acepe.fritzstreams.backend.Streams;

public class CalendarFragment extends Fragment {

    private CalendarView mCalendarView;
    private TextView mDayOfWeek;
    private TextView mNightflight;
    private TextView mSoundgarden;
    private Button mBtnDownloadNightflight;
    private Button mBtnDownloadSoundgarden;
    private Streams mStreams;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_chooser, container, false);

        mCalendarView = (CalendarView) view.findViewById(R.id.calendarView);
        mDayOfWeek = (TextView) view.findViewById(R.id.dayOfWeekLabel);
        mNightflight = (TextView) view.findViewById(R.id.streamFirst);
        mSoundgarden = (TextView) view.findViewById(R.id.streamSecond);
        mBtnDownloadNightflight = (Button) view.findViewById(R.id.buttonDownloadNightflight);
        mBtnDownloadSoundgarden = (Button) view.findViewById(R.id.buttonDownloadSoundgarden);

        mBtnDownloadNightflight.setOnClickListener(oclDownload);
        mBtnDownloadSoundgarden.setOnClickListener(oclDownload);

        mCalendarView.setOnDateChangeListener(odclCalendar);

        mStreams = new Streams();

        updateLabels(Calendar.getInstance());

        return view;
    }

    private void updateLabels(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat(App.DAY_OF_WEEK_FORMAT);
        String dayOfWeek = sdf.format(cal.getTime());

        mDayOfWeek.setText(dayOfWeek);
        mNightflight.setText(mStreams.getStream(Streams.Stream.nightflight, cal));
        mSoundgarden.setText(mStreams.getStream(Streams.Stream.soundgarden, cal));
    }

    private CalendarView.OnDateChangeListener odclCalendar = new CalendarView.OnDateChangeListener() {
        @Override
        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);

            updateLabels(cal);
        }
    };

    private View.OnClickListener oclDownload = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonDownloadNightflight:
                    download(Streams.Stream.nightflight);
                    break;
                case R.id.buttonDownloadSoundgarden:
                    download(Streams.Stream.soundgarden);
                    break;
            }
        }
    };

    private void download(Streams.Stream stream) {
        long selected = mCalendarView.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selected);

        StreamDownloader streamDownloader = new StreamDownloader(getActivity(), cal, stream);
        streamDownloader.downloadAndConvert();
    }

}

package de.acepe.fritzstreams.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.acepe.fritzstreams.Config;
import de.acepe.fritzstreams.R;

public class CalendarFragment extends Fragment {

    private CalendarView mCalendarView;
    private TextView mDayOfWeekLabel;
    private TextView mStream1;
    private TextView mStream2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_chooser, container, false);

        mCalendarView = (CalendarView) view.findViewById(R.id.calendarView);
        mDayOfWeekLabel = (TextView) view.findViewById(R.id.dayOfWeekLabel);
        mStream1 = (TextView) view.findViewById(R.id.streamFirst);
        mStream2 = (TextView) view.findViewById(R.id.streamSecond);

        mCalendarView.setOnDateChangeListener(odclCalendar);
        updateLabels(Calendar.getInstance());

        return view;
    }

    private void updateLabels(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat(Config.DAY_OF_WEEK_FORMAT);
        String dayOfWeek = sdf.format(cal.getTime());

        mDayOfWeekLabel.setText(dayOfWeek);
        mStream1.setText(getFirstStream(cal));
        mStream2.setText(getSecondStream(cal));
    }

    private CalendarView.OnDateChangeListener odclCalendar = new CalendarView.OnDateChangeListener() {
        @Override
        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);

            updateLabels(cal);
        }
    };

    private static CharSequence getFirstStream(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Rock";
            case Calendar.TUESDAY:
                return "House";
            case Calendar.WEDNESDAY:
                return "Club/Rap";
            case Calendar.THURSDAY:
                return "Club";
            case Calendar.FRIDAY:
                return "Rock";
            case Calendar.SATURDAY:
                return "Club";
            case Calendar.SUNDAY:
                return "Remix/Club";
            default:
                throw new RuntimeException("Unknown day of week");
        }
    }

    private static CharSequence getSecondStream(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "International";
            case Calendar.TUESDAY:
                return "Club";
            case Calendar.WEDNESDAY:
                return "Rap";
            case Calendar.THURSDAY:
                return "Stahlwerk";
            case Calendar.FRIDAY:
                return "Urban";
            case Calendar.SATURDAY:
                return "Club";
            case Calendar.SUNDAY:
                return "Rock";
            default:
                throw new RuntimeException("Unknown day of week");
        }
    }
}
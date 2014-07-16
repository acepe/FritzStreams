package de.acepe.fritzstreams;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.acepe.fritzstreams.R;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.TextView;

public class DateChooser extends Activity {

    private static final Locale GERMANY = Locale.GERMANY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_chooser);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    public void downloadFirst(@SuppressWarnings("unused") View view) {

        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        long selected = calendarView.getDate();
        Calendar cal = Calendar.getInstance(GERMANY);
        cal.setTimeInMillis(selected);

        new DownloadTask(this, cal, Stream.nightflight).execute();
    }

    public void downloadSecond(@SuppressWarnings("unused") View view) {
        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView);
        long selected = calendarView.getDate();
        Calendar cal = Calendar.getInstance(GERMANY);
        cal.setTimeInMillis(selected);

        new DownloadTask(this, cal, Stream.soundgarden).execute();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_date_chooser, container, false);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            CalendarView calendarView = (CalendarView) getActivity().findViewById(R.id.calendarView);
            calendarView.setOnDateChangeListener(new OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                    Calendar cal = Calendar.getInstance(GERMANY);
                    cal.set(year, month, dayOfMonth);

                    updateLabels(cal);
                }
            });

            updateLabels(Calendar.getInstance());
        }

        private void updateLabels(Calendar cal) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE", GERMANY);

            String dayOfWeek = sdf.format(cal.getTime());

            TextView dayOfWeekLabel = (TextView) getActivity().findViewById(R.id.dayOfWeekLabel);
            dayOfWeekLabel.setText(dayOfWeek);

            TextView stream1 = (TextView) getActivity().findViewById(R.id.streamFirst);
            stream1.setText(getFirstStream(cal));

            TextView stream2 = (TextView) getActivity().findViewById(R.id.streamSecond);
            stream2.setText(getSecondStream(cal));
        }

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

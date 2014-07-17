package de.acepe.fritzstreams;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;

import java.util.Calendar;
import java.util.Locale;

import de.acepe.fritzstreams.ui.fragments.CalendarFragment;

public class MainActivity extends Activity {

    private static final Locale GERMANY = Locale.GERMANY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_chooser);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new CalendarFragment()).commit();
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


}

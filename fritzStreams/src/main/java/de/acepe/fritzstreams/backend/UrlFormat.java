package de.acepe.fritzstreams.backend;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.acepe.fritzstreams.App;

public class UrlFormat {

    public static String getUrl(Calendar cal, Stream stream) {
        SimpleDateFormat yearFormat = new SimpleDateFormat(App.URLYEAR_FORMAT, App.GERMANY);
        SimpleDateFormat dateFormat = new SimpleDateFormat(App.URL_DATE_FORMAT, App.GERMANY);
        String dayOfWeek = App.URL_DAY_OF_WEEK_FORMAT.format(cal.getTime()).toLowerCase(App.GERMANY);
        String year = yearFormat.format(cal.getTime());
        String date = dateFormat.format(cal.getTime());
        return String.format(getUrlTemplate(stream), dayOfWeek, year, dayOfWeek, date);
    }

    private static String getUrlTemplate(Stream stream) {
        return stream == Stream.nightflight ? App.URL_NIGHTFLIGHT_FORMAT : App.URL_SOUNDGARDEN_FORMAT;
    }
}

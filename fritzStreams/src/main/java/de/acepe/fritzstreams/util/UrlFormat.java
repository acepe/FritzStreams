package de.acepe.fritzstreams.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.acepe.fritzstreams.Config;
import de.acepe.fritzstreams.Stream;

public class UrlFormat {

    public static String getUrl(Calendar cal, Stream stream) {
        SimpleDateFormat yearFormat = new SimpleDateFormat(Config.URLYEAR_FORMAT, Config.GERMANY);
        SimpleDateFormat dateFormat = new SimpleDateFormat(Config.URL_DATE_FORMAT, Config.GERMANY);
        String dayOfWeek = Config.URL_DAY_OF_WEEK_FORMAT.format(cal.getTime()).toLowerCase(Config.GERMANY);
        String year = yearFormat.format(cal.getTime());
        String date = dateFormat.format(cal.getTime());
        return String.format(getUrlTemplate(stream), dayOfWeek, year, dayOfWeek, date);
    }

    private static String getUrlTemplate(Stream stream) {
        return stream == Stream.nightflight ? Config.URL_NIGHTFLIGHT_FORMAT : Config.URL_SOUNDGARDEN_FORMAT;
    }
}

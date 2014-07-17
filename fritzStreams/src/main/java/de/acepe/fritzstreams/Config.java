package de.acepe.fritzstreams;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Config {
    public static final Locale GERMANY = Locale.GERMANY;

    public static final SimpleDateFormat URL_DAY_OF_WEEK_FORMAT = new SimpleDateFormat("EEEE", GERMANY);
    public static final java.lang.String URL_DATE_FORMAT = "yyyyMMdd";
    public static final java.lang.String URLYEAR_FORMAT = "yyyy";

    public static final String URL_SOUNDGARDEN_FORMAT = "rtmp://ondemand.rbb-online.de/ondemand/frz/musikstreams/soundgarden_am_%s/%s/soundgarden_am_%s_%s.mp3";
    public static final String URL_NIGHTFLIGHT_FORMAT = "rtmp://ondemand.rbb-online.de/ondemand/frz/musikstreams/nightflight_am_%s/%s/nightflight_am_%s_%s.mp3";

    public static final java.lang.String FILE_DATE_FORMAT = "yyyy_MM_dd";

    public static final String FILE_SOUNDGARDEN_FORMAT = "soundgarden_am_%s_vom_%s";
    public static final String FILE_NIGHTFLIGHT_FORMAT = "nightflight_am_%s_vom_%s";

    public static final String FILE_EXTENSION_FLV = ".flv";
    public static final String FILE_EXTENSION_MP3 = ".mp3";

    public static final String RTMP_DUMP_FORMAT = "rtmpdump -r %s -o %s";

}

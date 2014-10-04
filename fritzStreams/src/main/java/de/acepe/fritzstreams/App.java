package de.acepe.fritzstreams;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Locale;

import android.app.Application;
import de.acepe.fritzstreams.backend.StreamDownload;

public class App {
    public static final Locale GERMANY = Locale.GERMANY;

    public static final String DAY_OF_WEEK_FORMAT = "EEEE";
    public static final SimpleDateFormat URL_DAY_OF_WEEK_FORMAT = new SimpleDateFormat(DAY_OF_WEEK_FORMAT, GERMANY);

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

    public static final String SP_DOWNLOAD_DIR = "pref_dl_dir";

    public static Application mApp;

    public static LinkedList<StreamDownload> downloaders = new LinkedList<>();

    public static StreamDownload activeDownload;

}

package de.acepe.fritzstreams.backend;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class Constants {

    public static final String SP_WIFI_ONLY = "pref_dl_wifi_only";
    public static final String SP_DOWNLOAD_DIR = "pref_dl_dir";
    public static final Locale GERMANY = Locale.GERMANY;
    public static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("E", GERMANY);
    public static final String FILE_EXTENSION_MP3 = ".mp3";
    public static final String FILE_DATE_FORMAT = "ddMM";

    static final String REQUEST_QUERY_DOWNLOADS_ACTION = "de.acepe.fritzstreams.REQUEST_QUERY_DOWNLOADS_ACTION";
    static final String REQUEST_ADD_DOWNLOAD_ACTION = "de.acepe.fritzstreams.REQUEST_ADD_DOWNLOAD_ACTION";
    static final String REQUEST_CANCEL_DOWNLOAD_ACTION = "de.acepe.fritzstreams.REQUEST_CANCEL_DOWNLOAD_ACTION";
    static final String REQUEST_REMOVE_DOWNLOAD_ACTION = "de.acepe.fritzstreams.REQUEST_REMOVE_DOWNLOAD_ACTION";
    static final String REQUEST_PERMISSION_TO_DIE_ACTION = "de.acepe.fritzstreams.REQUEST_PERMISSION_TO_DIE_ACTION";

    static final String RESPONSE_ACTION = "de.acepe.fritzstreams.RESPONSE_ACTION";
    static final String CURRENT_DOWNLOAD_PROGRESS_REPORT = "de.acepe.fritzstreams.RESPONSE_ACTION";
    static final String QUERY_DOWNLOADS = "de.acepe.fritzstreams.QUERY_DOWNLOADS";
    static final String ALREADY_IN_QUEUE = "de.acepe.fritzstreams.ALREADY_IN_QUEUE";

    static final java.lang.String SERVICE_REQUEST = "serviceRequest";
    static final String DOWNLOAD_INFO = "downloadInfo";

}
package de.acepe.fritzstreams;

import java.util.LinkedList;
import java.util.Locale;

import android.app.Application;

import de.acepe.fritzstreams.backend.StreamDownload;

public class App {
    public static final Locale GERMANY = Locale.GERMANY;

    public static final java.lang.String FILE_DATE_FORMAT = "ddMM";
    public static final String FILE_EXTENSION_MP3 = ".mp3";
    public static final String SP_WIFI_ONLY = "pref_dl_wifi_only";
    public static final String SP_DOWNLOAD_DIR = "pref_dl_dir";

    public static Application mApp;

    public static LinkedList<StreamDownload> downloaders = new LinkedList<>();

    public static StreamDownload activeDownload;

}

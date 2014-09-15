package de.acepe.fritzstreams.backend;

import android.util.Log;

public class CallBack {

    public static void rtmpMessage(String message) {
        Log.i("Test", message);
        DownloadTask.append(message);
    }
}

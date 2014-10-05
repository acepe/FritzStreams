package de.acepe.fritzstreams.backend;

import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.util.Utilities;

public class CallBack {

    public static void rtmpMessage(String message) {
        // RTMPDump output: 106775.074 kB / 4526.65 sec
        int divider = message.indexOf("/");
        if (divider == -1)
            return;

        String downloadedBytes = message.substring(0, divider - 3);
        downloadedBytes = downloadedBytes.trim().replace(".", "");
        long bytes = Long.parseLong(downloadedBytes);
        App.activeDownload.setmDownloadedKB(Utilities.humanReadableBytes(bytes, false));
    }
}

package de.acepe.fritzstreams.backend;

import static android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.schriek.rtmpdump.Rtmpdump;

import de.acepe.fritzstreams.Config;
import de.acepe.fritzstreams.MainActivity;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.util.DownloadFileUtil;
import de.acepe.fritzstreams.util.UrlFormat;

public class DownloadTask extends AsyncTask<Void, Void, Boolean> {

    public interface Callback {
        void onDownloadFinished(boolean succeeded);
    }

    private final static String TAG = "DownloadTask";
    private static final String TAG_RTMPDUMP = "RtmpDump";

    private final Context context;
    private Callback callback;
    private final String fileName;
    private final String outFileFLV;
    private final String url;

    private WifiManager.WifiLock mWifiLock;

    public DownloadTask(Context context, Calendar cal, Streams.Stream stream, Callback callback) {
        this.context = context;
        this.callback = callback;

        DownloadFileUtil downloadFileUtil = new DownloadFileUtil(context);
        fileName = downloadFileUtil.fileBaseName(cal, stream);
        outFileFLV = downloadFileUtil.pathForFLVFile(fileName);

        url = UrlFormat.getUrl(cal, stream);

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            mWifiLock = wifiManager.createWifiLock(WIFI_MODE_FULL_HIGH_PERF, TAG);
        }
    }

    @Override
    protected Boolean doInBackground(Void... commands) {
        String command = String.format(Config.RTMP_DUMP_FORMAT, url, outFileFLV);

        showNotification(R.string.downloading_notification_title, fileName);
        mWifiLock.acquire();

        Log.i(TAG_RTMPDUMP, "downloading: " + command);
        Rtmpdump dump = new Rtmpdump();
        dump.parseString(command);

        return true;
    }

    public static void append(String message) {
        Log.i(TAG_RTMPDUMP, "message");
    }

    @Override
    protected void onPostExecute(Boolean succeeded) {
        mWifiLock.release();
        showNotification(succeeded
                ? R.string.download_succeeded_notification_title
                : R.string.download_failed_notification_title, fileName);
        callback.onDownloadFinished(succeeded);
    }

    @Override
    protected void onCancelled() {
        mWifiLock.release();
        showNotification(R.string.failed_notification_title, fileName);
        super.onCancelled();
    }

    private void showNotification(int downloadingNotificationTitle, String text) {
        Notification notification = createNotification(context.getString(downloadingNotificationTitle), text);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    private Notification createNotification(String title, String text) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        return new NotificationCompat.Builder(context).setContentTitle(title)
                                                      .setContentText(text)
                                                      .setSmallIcon(R.drawable.ic_launcher)
                                                      .setContentIntent(pIntent)
                                                      .build();
    }

}

package de.acepe.fritzstreams.backend;

import static android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF;

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

import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.MainActivity;
import de.acepe.fritzstreams.R;

public class DownloadTask extends AsyncTask<Void, Void, Boolean> {

    public interface Callback {
        void onDownloadFinished(boolean succeeded);
    }

    private static final String TAG = "DownloadTask";
    private static final String TAG_RTMPDUMP = "RtmpDump";

    private final Context context;
    private final DownloadInformation downloadInformation;

    private Callback callback;
    private WifiManager.WifiLock mWifiLock;

    public DownloadTask(Context context, DownloadInformation downloadInformation, Callback callback) {
        this.context = context;
        this.downloadInformation = downloadInformation;
        this.callback = callback;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            mWifiLock = wifiManager.createWifiLock(WIFI_MODE_FULL_HIGH_PERF, TAG);
        }
    }

    @Override
    protected Boolean doInBackground(Void... commands) {
        String command = String.format(App.RTMP_DUMP_FORMAT,
                                       downloadInformation.getUrl(),
                                       downloadInformation.getOutFileFLV());

        showNotification(R.string.downloading_notification_title, downloadInformation.getFileBaseName());
        mWifiLock.acquire();

        Log.i(TAG_RTMPDUMP, "downloading: " + command);
        Rtmpdump dump = new Rtmpdump();
        int returnValue = dump.parseString(command);

        return returnValue == 0;
    }

    @Override
    protected void onPostExecute(Boolean succeeded) {
        mWifiLock.release();
        showNotification(succeeded
                ? R.string.download_succeeded_notification_title
                : R.string.download_failed_notification_title, downloadInformation.getFileBaseName());
        callback.onDownloadFinished(succeeded);
    }

    @Override
    protected void onCancelled() {
        mWifiLock.release();
        showNotification(R.string.failed_notification_title, downloadInformation.getFileBaseName());
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

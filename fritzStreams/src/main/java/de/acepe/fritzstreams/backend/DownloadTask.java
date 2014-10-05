package de.acepe.fritzstreams.backend;

import static android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.schriek.rtmpdump.Rtmpdump;

import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.MainActivity;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.util.Utilities;

public class DownloadTask extends AsyncTask<Void, Void, TaskResult> {

    public interface Callback {
        void onDownloadFinished(TaskResult succeeded);
    }

    private static final String TAG = "DownloadTask";
    private static final String TAG_RTMPDUMP = "RtmpDump";

    private final Context mContext;
    private final DownloadInformation mDownloadInformation;
    private final SharedPreferences mSharedPref;

    private boolean mCancelled;
    private Callback mCallback;
    private WifiManager.WifiLock mWifiLock;
    private Rtmpdump dump;

    public DownloadTask(Context context, DownloadInformation downloadInformation, Callback callback) {
        this.mContext = context;
        this.mDownloadInformation = downloadInformation;
        this.mCallback = callback;

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            mWifiLock = wifiManager.createWifiLock(WIFI_MODE_FULL_HIGH_PERF, TAG);
        }
    }

    @Override
    protected TaskResult doInBackground(Void... commands) {
        if (mSharedPref.getBoolean(App.SP_WIFI_ONLY, false) && !Utilities.onWifi(mContext)) {
            return TaskResult.onlyWifi;
        }

        String command = String.format(App.RTMP_DUMP_FORMAT,
                                       mDownloadInformation.getUrl(),
                                       mDownloadInformation.getOutFileFLV());

        showNotification(R.string.downloading_notification_title, mDownloadInformation.getFileBaseName());
        mWifiLock.acquire();

        Log.i(TAG_RTMPDUMP, "downloading: " + command);
        dump = new Rtmpdump();
        int returnValue = dump.parseString(command);

        return returnValue == 0 ? TaskResult.successful : TaskResult.failed;
    }

    @Override
    protected void onPostExecute(TaskResult result) {
        if (mWifiLock.isHeld())
            mWifiLock.release();

        if (mCancelled) {
            showNotification(R.string.download_cancelled_notification_title, mDownloadInformation.getFileBaseName());
            mCallback.onDownloadFinished(TaskResult.cancelled);
            deleteDownloadFile();
            return;
        }
        switch (result) {
            case successful:
                showNotification(R.string.download_succeeded_notification_title, mDownloadInformation.getFileBaseName());
                break;
            case failed:
                showNotification(R.string.download_failed_notification_title, mDownloadInformation.getFileBaseName());
                deleteDownloadFile();
                break;
            case onlyWifi:
                showNotification(R.string.download_only_wifi_notification_title, mDownloadInformation.getFileBaseName());
                break;
        }
        mCallback.onDownloadFinished(result);
    }

    private void deleteDownloadFile() {
        File downloadFile = new File(mDownloadInformation.getOutFileFLV());
        if (downloadFile.exists())
            downloadFile.delete();
    }

    @Override
    protected void onCancelled() {
        if (mWifiLock.isHeld())
            mWifiLock.release();

        deleteDownloadFile();
        showNotification(R.string.failed_notification_title, mDownloadInformation.getFileBaseName());
        super.onCancelled();
    }

    public void stop() {
        mCancelled = true;
        dump.stop();
    }

    private void showNotification(int downloadingNotificationTitle, String text) {
        Notification notification = createNotification(mContext.getString(downloadingNotificationTitle), text);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    private Notification createNotification(String title, String text) {
        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

        return new NotificationCompat.Builder(mContext).setContentTitle(title)
                                                       .setContentText(text)
                                                       .setSmallIcon(R.drawable.ic_launcher)
                                                       .setContentIntent(pIntent)
                                                       .build();
    }

}

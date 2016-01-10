package de.acepe.fritzstreams.backend;

import static android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.MainActivity;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.util.Utilities;

public class DownloadTask extends AsyncTask<Void, Void, TaskResult> {

    public interface Callback {
        void onDownloadFinished(TaskResult succeeded);

        void setCurrentProgress(int currentProgress);

        void setDownloadedKB(int downloadedKB);

        void setSize(int size);
    }

    private static final String TAG = "DownloadTask";

    private final Context mContext;
    private final StreamInfo mStreamInfo;
    private final Callback mCallback;
    private final SharedPreferences mSharedPref;

    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;

    public DownloadTask(Context context, StreamInfo mStreamInfo, Callback callback) {
        this.mContext = context;
        this.mStreamInfo = mStreamInfo;
        this.mCallback = callback;

        mSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    @Override
    protected TaskResult doInBackground(Void... commands) {
        if (mSharedPref.getBoolean(App.SP_WIFI_ONLY, false) && !Utilities.onWifi(mContext)) {
            return TaskResult.onlyWifi;
        }

        PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mWakeLock.acquire();
        }

        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            mWifiLock = wifiManager.createWifiLock(WIFI_MODE_FULL_HIGH_PERF, TAG);
            mWifiLock.acquire();
        }

        showNotification(R.string.downloading_notification_title, mStreamInfo.getTitle());

        return download();
    }

    public TaskResult download() {
        String pathname = mStreamInfo.getFilename();
        File file = new File(pathname);

        URLConnection connection = null;
        try {
            connection = new URL(mStreamInfo.getStreamURL()).openConnection();
        } catch (IOException e) {
            return TaskResult.failed;
        }
        try (InputStream is = connection.getInputStream();
                OutputStream outstream = new BufferedOutputStream(new FileOutputStream(file))) {
            int size = connection.getContentLength();
            Log.d(TAG, "Size is: " + size);
            mCallback.setCurrentProgress(0);
            mCallback.setSize(size);

            byte[] buffer = new byte[4096];
            int downloadedSum = 0;
            int len;
            while ((len = is.read(buffer)) > 0) {
                if (isCancelled()) {
                    break;
                }
                downloadedSum += len;
                mCallback.setCurrentProgress((int) (downloadedSum / (float) size * 100));
                mCallback.setDownloadedKB(downloadedSum);
                outstream.write(buffer, 0, len);
                Log.d(TAG, "Downloaded Bytes: " + downloadedSum + " / " + size);
            }
        } catch (IOException e) {
            return TaskResult.failed;
        }
        return TaskResult.successful;
    }

    @Override
    protected void onPostExecute(TaskResult result) {
        if (mWifiLock != null && mWifiLock.isHeld())
            mWifiLock.release();

        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();

        switch (result) {
            case successful:
                showNotification(R.string.download_succeeded_notification_title, mStreamInfo.getTitle());
                break;
            case failed:
                showNotification(R.string.download_failed_notification_title, mStreamInfo.getTitle());
                deleteDownloadFile();
                break;
            case onlyWifi:
                showNotification(R.string.download_only_wifi_notification_title, mStreamInfo.getTitle());
                break;
        }

        mCallback.onDownloadFinished(result);
    }

    @Override
    protected void onCancelled(TaskResult taskResult) {
        if (mWifiLock != null && mWifiLock.isHeld())
            mWifiLock.release();

        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();

        showNotification(R.string.download_cancelled_notification_title, mStreamInfo.getTitle());
        mCallback.onDownloadFinished(TaskResult.cancelled);
        deleteDownloadFile();
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

    private void deleteDownloadFile() {
        deleteFile(mStreamInfo.getFilename());
    }

    private void deleteFile(String outFileFLV) {
        File downloadFile = new File(outFileFLV);
        if (downloadFile.exists())
            if (!downloadFile.delete()) {
                downloadFile.deleteOnExit();
            }
    }

}

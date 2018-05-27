package de.acepe.fritzstreams.backend;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import de.acepe.fritzstreams.util.Notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF;
import static de.acepe.fritzstreams.backend.Constants.*;
import static de.acepe.fritzstreams.backend.DownloadState.CANCELLED;
import static de.acepe.fritzstreams.backend.DownloadState.FINISHED;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    private static final int MINUTE_IN_MILLIS = 60 * 1000;
    public static final int SERVICE_ID = 2;

    private final List<DownloadInfo> mScheduledDownloads = new ArrayList<>();

    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;
    private boolean permissionToDie = false;

    private ExecutorService executor;


    @Override
    public void onCreate() {
        Log.i(TAG, "Service created");
        executor = Executors.newSingleThreadExecutor();
        acquireLocks();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Connected to Service");

        Bundle extras = intent.getExtras();
        if (extras == null) {
            return START_STICKY;
        }

        String request = extras.getString(SERVICE_REQUEST);
        if (REQUEST_ADD_DOWNLOAD_ACTION.equals(request)) {
            final DownloadInfo info = (DownloadInfo) extras.get(DOWNLOAD_INFO);
            DownloadInfo existingDownload = findScheduledDownload(info);
            if (existingDownload == null) {
                permissionToDie = false;
                mScheduledDownloads.add(info);

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        downloadWorker(info);
                    }
                });
                reportQueue();
            } else {
                sendMessage(new Intent(RESPONSE_ACTION).putExtra(ALREADY_IN_QUEUE,
                        existingDownload));
            }
        }
        if (REQUEST_QUERY_DOWNLOADS_ACTION.equals(request)) {
            reportQueue();
        }
        if (REQUEST_CANCEL_DOWNLOAD_ACTION.equals(request)) {
            DownloadInfo info = (DownloadInfo) extras.get(DOWNLOAD_INFO);
            DownloadInfo download = findScheduledDownload(info);
            if (download != null) {
                download.setState(CANCELLED);
            }
            reportQueue();
        }
        if (REQUEST_REMOVE_DOWNLOAD_ACTION.equals(request)) {
            DownloadInfo info = (DownloadInfo) extras.get(DOWNLOAD_INFO);
            DownloadInfo download = findScheduledDownload(info);
            if (download != null) {
                download.setState(CANCELLED);
                mScheduledDownloads.remove(info);
            }
            reportQueue();
        }
        if (REQUEST_PERMISSION_TO_DIE_ACTION.equals(request)) {
            permissionToDie = true;
        }
        if (allComplete() && permissionToDie) {
            stopSelf(startId);
        }
        return START_NOT_STICKY;
    }

    private boolean allComplete() {
        for (DownloadInfo download : mScheduledDownloads) {
            DownloadState state = download.getState();
            if (state.isPending()) {
                return false;
            }
        }
        return true;
    }

    private DownloadInfo findScheduledDownload(DownloadInfo info) {
        int index = mScheduledDownloads.indexOf(info);
        return index == -1 ? null : mScheduledDownloads.get(index);
    }

    private void reportQueue() {
        ArrayList<DownloadInfo> progressList = new ArrayList<>(mScheduledDownloads);
        Intent localIntent = new Intent(RESPONSE_ACTION).putExtra(QUERY_DOWNLOADS, progressList);
        sendMessage(localIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroying Service");
        executor.shutdownNow();
        releaseLocks();
    }

    private void acquireLocks() {
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mWakeLock.acquire(30 * MINUTE_IN_MILLIS);
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            mWifiLock = wifiManager.createWifiLock(WIFI_MODE_FULL_HIGH_PERF, TAG);
            mWifiLock.acquire();
        }
    }

    private void releaseLocks() {
        if (mWifiLock != null && mWifiLock.isHeld())
            mWifiLock.release();

        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();
    }

    private void sendMessage(Intent localIntent) {
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(localIntent);
    }

    private final Downloader.DownloadCallback downloadCallback = new Downloader.DownloadCallback() {
        @Override
        public void reportProgress(DownloadInfo downloadInfo) {
            sendMessage(new Intent(RESPONSE_ACTION).putExtra(CURRENT_DOWNLOAD_PROGRESS_REPORT, downloadInfo));
            startForeground(SERVICE_ID, Notifications.Companion.createNotification(DownloadService.this, downloadInfo.getProgressPercent()));
        }
    };


    private void downloadWorker(DownloadInfo download) {
        startForeground(SERVICE_ID, Notifications.Companion.createNotification(DownloadService.this, 0));

        Log.i(TAG + "-Handler", "Starting Download " + download);

        new Downloader(download, downloadCallback).download();
        downloadCallback.reportProgress(download);

        if (download.getState() == FINISHED) {
            MediaScannerConnection.scanFile(getBaseContext(), new String[]{download.getFilename()},
                    null,
                    null);
        }
        reportQueue();
    }

}
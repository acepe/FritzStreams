package de.acepe.fritzstreams.backend;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.wifi.WifiManager;
import android.os.*;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF;
import static de.acepe.fritzstreams.backend.Constants.*;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    private static final int MINUTE_IN_MILLIS = 60 * 1000;

    private final List<DownloadInfo> mScheduledDownloads = new ArrayList<>();

    private ServiceHandler mServiceHandler;
    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;
    private boolean permissionToDie = false;


    @Override
    public void onCreate() {
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        acquireLocks();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceHandler = new ServiceHandler(thread.getLooper());
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
            DownloadInfo info = (DownloadInfo) extras.get(DOWNLOAD_INFO);
            DownloadInfo existingDownload = findScheduledDownload(info);
            if (existingDownload == null) {
                permissionToDie = false;
                mScheduledDownloads.add(info);
                // For each start request, send a message to start a job and deliver the
                // start ID so we know which request we're stopping when we finish the job
                Message msg = mServiceHandler.obtainMessage();
                msg.arg1 = startId;
                mServiceHandler.sendMessage(msg);
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
                download.setState(DownloadState.CANCELLED);
            }
            reportQueue();
        }
        if (REQUEST_REMOVE_DOWNLOAD_ACTION.equals(request)) {
            DownloadInfo info = (DownloadInfo) extras.get(DOWNLOAD_INFO);
            DownloadInfo download = findScheduledDownload(info);
            if (download != null) {
                download.setState(DownloadState.CANCELLED);
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
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private boolean allComplete() {
        for (DownloadInfo download : mScheduledDownloads) {
            DownloadState state = download.getState();
            if (state == DownloadState.WAITING || state == DownloadState.DOWNLOADING) {
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
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(localIntent);
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        private final Downloader.DownloadCallback downloadCallback = new Downloader.DownloadCallback() {
            @Override
            public void reportProgress(DownloadInfo downloadInfo) {
                DownloadService.this.reportProgress(downloadInfo);
            }
        };

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            DownloadInfo download;
            while ((download = findNext()) != null) {
                Log.i(TAG + "-Handler", "Starting Download " + download);

                new Downloader(download, downloadCallback).download();

                if (download.getState() == DownloadState.FINISHED) {
                    MediaScannerConnection.scanFile(getBaseContext(),
                            new String[]{download.getFilename()},
                            null,
                            null);
                }
                reportQueue();
            }

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job. After all messages are processed, service will be
            // destroyed
            if (permissionToDie) {
                stopSelf(msg.arg1);
            }
        }

        private DownloadInfo findNext() {
            for (DownloadInfo download : mScheduledDownloads) {
                if (download.getState() == DownloadState.WAITING) {
                    return download;
                }
            }
            return null;
        }
    }

    private void reportProgress(DownloadInfo downloadInfo) {
        Intent localIntent = new Intent(RESPONSE_ACTION).putExtra(CURRENT_DOWNLOAD_PROGRESS_REPORT, downloadInfo);
        sendMessage(localIntent);
    }
}
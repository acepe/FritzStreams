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

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    public static final int MINUTE_IN_MILLIS = 60 * 1000;

    private final List<DownloadInfo> mScheduledDownloads = new ArrayList<>();

    private ServiceHandler mServiceHandler;
    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;
    private boolean permissionToDie = false;

    private final Downloader.DownloadCallback callback = downloadInfo -> {
        Intent localIntent = new Intent(Constants.RESPONSE_ACTION).putExtra(Constants.CURRENT_DOWNLOAD_PROGRESS_REPORT,
                downloadInfo);
        sendMessage(localIntent);
    };

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            DownloadInfo download;
            while ((download = findNext()) != null) {
                Log.i(TAG + "-Handler", "Starting Download " + download);
                new Downloader(download, callback).download();
                if (download.getState() == DownloadInfo.State.finished) {
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
                if (download.getState() == DownloadInfo.State.waiting) {
                    return download;
                }
            }
            return null;
        }
    }

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
        if (extras != null) {

            String request = extras.getString(Constants.SERVICE_REQUEST);
            if (Constants.REQUEST_ADD_DOWNLOAD_ACTION.equals(request)) {
                DownloadInfo info = (DownloadInfo) extras.get(Constants.DOWNLOAD_INFO);
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
                    sendMessage(new Intent(Constants.RESPONSE_ACTION).putExtra(Constants.ALREADY_IN_QUEUE,
                            existingDownload));
                }
            }
            if (Constants.REQUEST_QUERY_DOWNLOADS_ACTION.equals(request)) {
                reportQueue();
            }
            if (Constants.REQUEST_CANCEL_DOWNLOAD_ACTION.equals(request)) {
                DownloadInfo info = (DownloadInfo) extras.get(Constants.DOWNLOAD_INFO);
                DownloadInfo download = findScheduledDownload(info);
                if (download != null) {
                    download.setState(DownloadInfo.State.cancelled);
                }
                reportQueue();
            }
            if (Constants.REQUEST_REMOVE_DOWNLOAD_ACTION.equals(request)) {
                DownloadInfo info = (DownloadInfo) extras.get(Constants.DOWNLOAD_INFO);
                DownloadInfo download = findScheduledDownload(info);
                if (download != null) {
                    download.setState(DownloadInfo.State.cancelled);
                    mScheduledDownloads.remove(info);
                }
                reportQueue();
            }
            if (Constants.REQUEST_PERMISSION_TO_DIE_ACTION.equals(request)) {
                permissionToDie = true;
            }
            if (allComplete() && permissionToDie) {
                stopSelf(startId);
            }
        }
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private boolean allComplete() {
        for (DownloadInfo download : mScheduledDownloads) {
            DownloadInfo.State state = download.getState();
            if (state == DownloadInfo.State.waiting || state == DownloadInfo.State.downloading) {
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
        Intent localIntent = new Intent(Constants.RESPONSE_ACTION).putExtra(Constants.QUERY_DOWNLOADS, progressList);
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
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mWakeLock.acquire(30 * MINUTE_IN_MILLIS);
        }

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
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
}
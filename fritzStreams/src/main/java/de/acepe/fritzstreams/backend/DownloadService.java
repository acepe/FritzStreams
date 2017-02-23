package de.acepe.fritzstreams.backend;

import static android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.*;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";

    private final List<DownloadInfo> downloadInfos = new ArrayList<>();

    private ServiceHandler mServiceHandler;
    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;
    private DownloadInfo currentDownload;
    private boolean cancelled;

    private final Downloader.DownloadCallback callback = new Downloader.DownloadCallback() {
        @Override
        public void reportProgress(ProgressInfo progressInfo) {
            // Creates a new Intent containing a Uri object BROADCAST_ACTION is a custom Intent action
            Intent localIntent = new Intent(Constants.RESPONSE_ACTION).putExtra(Constants.CURRENT_DOWNLOAD_PROGRESS_REPORT,
                                                                                progressInfo);
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(localIntent);
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }
    };

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i("Service-Handler", "Handler received Message: " + msg);
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep and count

            DownloadInfo downloadInfo = (DownloadInfo) msg.getData().getSerializable(Constants.DOWNLOAD_INFO);
            new Downloader(downloadInfo, callback).download();

            downloadInfos.remove(downloadInfo);
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job. After all messages are processed, service will be
            // destroyed
            stopSelf(msg.arg1);
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
        Toast.makeText(this, "service starting/connected", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job

        Bundle extras = intent.getExtras();
        if (extras != null) {
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;

            String request = extras.getString(Constants.SERVICE_REQUEST);
            if (Constants.REQUEST_ADD_DOWNLOAD_ACTION.equals(request)) {
                DownloadInfo info = (DownloadInfo) extras.get(Constants.DOWNLOAD_INFO);
                downloadInfos.add(info);
                msg.getData().putSerializable(Constants.DOWNLOAD_INFO, info);
                mServiceHandler.sendMessage(msg);
                reportQueue();
            }
            if (Constants.REQUEST_QUERY_DOWNLOADS_ACTION.equals(request)) {
                reportQueue();
            }
            if (Constants.REQUEST_CANCEL_DOWNLOAD_ACTION.equals(request)) {
                cancelled = true;
                currentDownload = null;
                reportQueue();
            }

            // TODO: remove from Queue message
        }

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private void reportQueue() {
        ArrayList<DownloadInfo> progressList = new ArrayList<>(downloadInfos);
        Intent localIntent = new Intent(Constants.RESPONSE_ACTION).putExtra(Constants.QUERY_DOWNLOADS, progressList);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(localIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        releaseLocks();
    }

    private void acquireLocks() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mWakeLock.acquire();
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
}
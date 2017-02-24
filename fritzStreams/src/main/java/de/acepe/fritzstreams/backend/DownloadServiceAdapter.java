package de.acepe.fritzstreams.backend;

import static de.acepe.fritzstreams.backend.Constants.RESPONSE_ACTION;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DownloadServiceAdapter {

    public interface ResultReceiver {
        void downloadsInQueue(List<DownloadInfo> downloadInfoList);

        void currentProgress(DownloadInfo downloadInfo);
    }

    private static final String TAG = "ServiceAdapter";

    private final DownloadStateReceiver mDownloadStateReceiver;
    private final IntentFilter statusIntentFilter;
    private final ArrayList<ResultReceiver> resultReceivers = new ArrayList<>();

    private Context mContext;

    public DownloadServiceAdapter() {
        mDownloadStateReceiver = new DownloadStateReceiver();
        statusIntentFilter = new IntentFilter(RESPONSE_ACTION);
    }

    public void attach(Context context) {
        mContext = context;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDownloadStateReceiver, statusIntentFilter);
    }

    public void detach() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDownloadStateReceiver);
        resultReceivers.clear();
        mContext = null;
    }

    public void detachFromService() {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.SERVICE_REQUEST, Constants.REQUEST_PERMISSION_TO_DIE_ACTION);
        mContext.startService(intent);
    }

    public void registerResultReceiver(ResultReceiver resultReceiver) {
        Log.i(TAG, "adding Result Receiver");
        resultReceivers.add(resultReceiver);
    }

    public void removeResultReceiver(ResultReceiver resultReceiver) {
        Log.i(TAG, "Removing Result Receiver");
        resultReceivers.remove(resultReceiver);
        Log.i(TAG, "now: " + resultReceivers.size());
    }

    public void queryDownloadInfos() {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.SERVICE_REQUEST, Constants.REQUEST_QUERY_DOWNLOADS_ACTION);
        mContext.startService(intent);
    }

    public void addDownload(DownloadInfo download) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.SERVICE_REQUEST, Constants.REQUEST_ADD_DOWNLOAD_ACTION);
        intent.putExtra(Constants.DOWNLOAD_INFO, download);
        mContext.startService(intent);
    }

    public void removeDownload(DownloadInfo download) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.SERVICE_REQUEST, Constants.REQUEST_REMOVE_DOWNLOAD_ACTION);
        intent.putExtra(Constants.DOWNLOAD_INFO, download);
        mContext.startService(intent);
    }

    public void cancelDownload(DownloadInfo download) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.SERVICE_REQUEST, Constants.REQUEST_CANCEL_DOWNLOAD_ACTION);
        intent.putExtra(Constants.DOWNLOAD_INFO, download);
        mContext.startService(intent);
    }

    // Broadcast receiver for receiving status updates from the DownloadService
    private final class DownloadStateReceiver extends BroadcastReceiver {

        private DownloadStateReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            DownloadInfo progress = (DownloadInfo) extras.get(Constants.CURRENT_DOWNLOAD_PROGRESS_REPORT);
            if (progress != null) {
                Log.i(TAG, "resultreceivers: " + resultReceivers.size());
                for (ResultReceiver resultReceiver : resultReceivers) {
                    resultReceiver.currentProgress(progress);
                }
            }
            @SuppressWarnings("unchecked")
            ArrayList<DownloadInfo> downloadQueue = (ArrayList<DownloadInfo>) extras.get(Constants.QUERY_DOWNLOADS);
            if (downloadQueue != null) {
                for (ResultReceiver resultReceiver : resultReceivers) {
                    resultReceiver.downloadsInQueue(downloadQueue);
                }
            }
        }
    }
}

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

        void currentProgress(ProgressInfo progressInfo);
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
        mContext = null;
    }

    public void registerResultReceiver(ResultReceiver resultReceiver) {
        resultReceivers.add(resultReceiver);
    }

    public void removeResultReceiver(ResultReceiver resultReceiver) {
        resultReceivers.remove(resultReceiver);
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

    public void cancelDownload(DownloadInfo download) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.SERVICE_REQUEST, Constants.REQUEST_CANCEL_DOWNLOAD_ACTION);
        intent.putExtra(Constants.DOWNLOAD_INFO, download);
        mContext.startService(intent);
    }

    // Broadcast receiver for receiving status updates from the DownloadService
    private final class DownloadStateReceiver extends BroadcastReceiver {

        // Prevents instantiation
        private DownloadStateReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            ProgressInfo progress = (ProgressInfo) extras.get(Constants.CURRENT_DOWNLOAD_PROGRESS_REPORT);
            if (progress != null) {
                for (ResultReceiver resultReceiver : resultReceivers) {
                    resultReceiver.currentProgress(progress);
                }
            }
            @SuppressWarnings("unchecked")
            ArrayList<DownloadInfo> downloadQueue = (ArrayList<DownloadInfo>) extras.get(Constants.QUERY_DOWNLOADS);
            if (downloadQueue != null) {
                for (DownloadInfo downloadInfo : downloadQueue) {
                    Log.i(TAG, downloadInfo.toString());
                }
                for (ResultReceiver resultReceiver : resultReceivers) {
                    resultReceiver.downloadsInQueue(downloadQueue);
                }
            }
        }
    }
}

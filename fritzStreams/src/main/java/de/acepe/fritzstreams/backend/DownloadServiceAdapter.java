package de.acepe.fritzstreams.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import de.acepe.fritzstreams.R;

import java.util.ArrayList;
import java.util.List;

import static de.acepe.fritzstreams.backend.Constants.*;

public class DownloadServiceAdapter extends BroadcastReceiver {

    public interface ResultReceiver {
        void downloadsInQueue(List<DownloadInfo> downloadInfoList);

        void updateProgress(DownloadInfo downloadInfo);
    }

    private final IntentFilter statusIntentFilter;
    private final ArrayList<ResultReceiver> resultReceivers = new ArrayList<>();

    private Context mContext;

    public DownloadServiceAdapter() {
        statusIntentFilter = new IntentFilter(RESPONSE_ACTION);
    }

    public void attach(Context context) {
        mContext = context;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this, statusIntentFilter);
    }

    public void detach() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        resultReceivers.clear();
        mContext = null;
    }

    public void detachFromService() {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(SERVICE_REQUEST, REQUEST_PERMISSION_TO_DIE_ACTION);
        mContext.startService(intent);
    }

    public void registerResultReceiver(ResultReceiver resultReceiver) {
        resultReceivers.add(resultReceiver);
    }

    public void removeResultReceiver(ResultReceiver resultReceiver) {
        resultReceivers.remove(resultReceiver);
    }

    public void queryDownloadInfos() {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(SERVICE_REQUEST, REQUEST_QUERY_DOWNLOADS_ACTION);
        mContext.startService(intent);
    }

    public void addDownload(DownloadInfo download) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(SERVICE_REQUEST, REQUEST_ADD_DOWNLOAD_ACTION);
        intent.putExtra(DOWNLOAD_INFO, download);
        mContext.startService(intent);
    }

    public void removeDownload(DownloadInfo download) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(SERVICE_REQUEST, REQUEST_REMOVE_DOWNLOAD_ACTION);
        intent.putExtra(DOWNLOAD_INFO, download);
        mContext.startService(intent);
    }

    public void cancelDownload(DownloadInfo download) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(SERVICE_REQUEST, REQUEST_CANCEL_DOWNLOAD_ACTION);
        intent.putExtra(DOWNLOAD_INFO, download);
        mContext.startService(intent);
    }

    // Called when the BroadcastReceiver gets an Intent it's registered to receive
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        DownloadInfo progress = (DownloadInfo) extras.get(CURRENT_DOWNLOAD_PROGRESS_REPORT);
        if (progress != null) {
            for (ResultReceiver resultReceiver : resultReceivers) {
                resultReceiver.updateProgress(progress);
            }
        }
        DownloadInfo alreadyInQueue = (DownloadInfo) extras.get(ALREADY_IN_QUEUE);
        if (alreadyInQueue != null) {
            Toast.makeText(mContext, R.string.already_in_queue, Toast.LENGTH_SHORT).show();
        }
        @SuppressWarnings("unchecked")
        ArrayList<DownloadInfo> downloadQueue = (ArrayList<DownloadInfo>) extras.get(QUERY_DOWNLOADS);
        if (downloadQueue != null) {
            for (ResultReceiver resultReceiver : resultReceivers) {
                resultReceiver.downloadsInQueue(downloadQueue);
            }
        }
    }
}

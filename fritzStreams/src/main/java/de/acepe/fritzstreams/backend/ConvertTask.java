package de.acepe.fritzstreams.backend;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

import java.io.File;

import de.acepe.fritzstreams.MainActivity;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.backend.flv.FLV;

public class ConvertTask extends AsyncTask<Void, Void, Boolean> {

    public interface Callback {
        void onConvertFinished(TaskResult succeeded);

        void setCurrentProgress(int currentProgress);
    }

    private final Context mContext;
    private final DownloadInformation mDownloadInformation;
    private final Callback mCallback;
    private boolean mCancelled;

    public ConvertTask(Context context, DownloadInformation downloadInformation, Callback callback) {
        this.mContext = context;
        this.mDownloadInformation = downloadInformation;
        this.mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... commands) {
        showNotification(R.string.converting_notification_title, mDownloadInformation.getFileBaseName());

        String outFileMp3 = mDownloadInformation.getOutFileMp3();
        FLV flv = new FLV(mDownloadInformation.getOutFileFLV(), outFileMp3);
        flv.convert();

        MediaScannerConnection.scanFile(mContext, new String[]{new File(outFileMp3).getAbsolutePath()}, null, null);
        return true;
    }

    public void stop() {
        mCancelled = true;
    }

    @Override
    protected void onPostExecute(Boolean succeeded) {
        onFinished(succeeded);
    }

    @Override
    protected void onCancelled(Boolean result) {
        onFinished(result);
    }

    private void onFinished(Boolean succeeded) {
        if (succeeded) {
            showNotification(R.string.finished_notification_title, mDownloadInformation.getFileBaseName());
            mCallback.onConvertFinished(TaskResult.successful);
            return;
        }

        if (mCancelled) {
            showNotification(R.string.download_cancelled_notification_title, mDownloadInformation.getFileBaseName());
            deleteDownloadFiles();
            mCallback.onConvertFinished(TaskResult.cancelled);
            return;
        }

        deleteDownloadFiles();
        showNotification(R.string.download_failed_notification_title, mDownloadInformation.getFileBaseName());
        mCallback.onConvertFinished(TaskResult.failed);
    }

    private void deleteDownloadFiles() {
        deleteFile(mDownloadInformation.getOutFileFLV());
        deleteFile(mDownloadInformation.getOutFileMp3());
    }

    private void deleteFile(String outFileFLV) {
        File downloadFile = new File(outFileFLV);
        if (downloadFile.exists())
            if (!downloadFile.delete()) {
                downloadFile.deleteOnExit();
            }
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

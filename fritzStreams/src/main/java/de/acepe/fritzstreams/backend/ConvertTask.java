package de.acepe.fritzstreams.backend;

import java.io.File;
import java.io.IOException;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import de.acepe.fritzstreams.MainActivity;
import de.acepe.fritzstreams.R;

public class ConvertTask extends AsyncTask<Void, Void, Boolean> {

    public interface Callback {
        void onConvertFinished(TaskResult succeeded);

        void setCurrentProgress(int currentProgress);
    }

    private static final String TAG_FFMPEG = "ffmpeg";

    private final Context mContext;
    private final DownloadInformation mDownloadInformation;
    private final Callback mCallback;
    private FfmpegController mFfmpegController;
    private boolean mCancelled;

    public ConvertTask(Context context, DownloadInformation downloadInformation, Callback callback) {
        this.mContext = context;
        this.mDownloadInformation = downloadInformation;
        this.mCallback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... commands) {
        showNotification(R.string.converting_notification_title, mDownloadInformation.getFileBaseName());

        final Clip inClip = new Clip(mDownloadInformation.getOutFileFLV());
        final File outFile = new File(mDownloadInformation.getOutFileMp3());
        final File inFile = new File(mDownloadInformation.getOutFileFLV());

        File fileTmp = mContext.getApplicationContext().getCacheDir();
        File fileAppRoot = new File(mContext.getApplicationContext().getApplicationInfo().dataDir);

        try {
            mFfmpegController = new FfmpegController(fileTmp, fileAppRoot);
            mFfmpegController.extractAudio(inClip, outFile, new FFMpegCallback(inFile, outFile));
            MediaScannerConnection.scanFile(mContext, new String[] { outFile.getAbsolutePath() }, null, null);
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stop() {
        mCancelled = true;

        if (mFfmpegController != null)
            mFfmpegController.stop();
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
        File downloadFile = new File(mDownloadInformation.getOutFileFLV());
        if (downloadFile.exists())
            downloadFile.delete();

        File convertFile = new File(mDownloadInformation.getOutFileMp3());
        if (convertFile.exists())
            convertFile.delete();
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

    private class FFMpegCallback implements ShellCallback {
        private File inFile;
        private final File outFile;
        private final float inSize;

        public FFMpegCallback(File inFile, File outFile) {
            this.inFile = inFile;
            this.outFile = outFile;
            inSize = inFile.length();
        }

        @Override
        public void shellOut(String shellLine) {
            Log.i(TAG_FFMPEG, shellLine);
            float outSize = outFile.length();
            if (outSize > 0) {
                int frac = (int) (outSize / inSize * 100);
                mCallback.setCurrentProgress(frac);
            }
        }

        @Override
        public void processComplete(int exitValue) {
            if (exitValue != 0) {
                Log.e(TAG_FFMPEG, "Extraction error. FFmpeg failed");
                cancel(true);
            } else {
                if (outFile.exists()) {
                    Log.d(TAG_FFMPEG, "Success file:" + outFile.getPath());
                }
            }
            // noinspection ResultOfMethodCallIgnored
            inFile.delete();
        }
    }
}

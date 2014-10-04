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
        void onConvertFinished(boolean succeeded);
    }

    private static final String TAG_FFMPEG = "ffmpeg";

    private final Context context;
    private final DownloadInformation downloadInformation;
    private final Callback callback;

    public ConvertTask(Context context, DownloadInformation downloadInformation, Callback callback) {
        this.context = context;
        this.downloadInformation = downloadInformation;
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... commands) {
        Notification notification = createNotification(context.getString(R.string.converting_notification_title),
                                                       downloadInformation.getFileBaseName(),
                                                       true);
        showNotification(notification);

        final Clip inClip = new Clip(downloadInformation.getOutFileFLV());
        final File outFile = new File(downloadInformation.getOutFileMp3());
        final File inFile = new File(downloadInformation.getOutFileFLV());

        File fileTmp = context.getApplicationContext().getCacheDir();
        File fileAppRoot = new File(context.getApplicationContext().getApplicationInfo().dataDir);

        try {
            FfmpegController fc = new FfmpegController(fileTmp, fileAppRoot);
            fc.extractAudio(inClip, outFile, new FFMpegCallback(inFile, outFile));
            MediaScannerConnection.scanFile(context, new String[] { outFile.getAbsolutePath() }, null, null);
            return true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Notification notification = createNotification(context.getString(R.string.finished_notification_title),
                                                           downloadInformation.getFileBaseName(),
                                                           false);
            showNotification(notification);
        }
        callback.onConvertFinished(result);
    }

    private Notification createNotification(String title, String text, boolean ongoing) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        return new NotificationCompat.Builder(context).setContentTitle(title)
                                                      .setContentText(text)
                                                      .setSmallIcon(R.drawable.ic_launcher)
                                                      .setContentIntent(pIntent)
                                                      .setOngoing(ongoing)
                                                      .build();
    }

    private void showNotification(Notification notification) {
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
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
                long frac = (long) (outSize / inSize * 100);
                Notification notification = createNotification(context.getString(R.string.converting_notification_title),
                                                               frac + "%",
                                                               true);
                showNotification(notification);
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

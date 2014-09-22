package de.acepe.fritzstreams.backend;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import de.acepe.fritzstreams.MainActivity;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.util.DownloadFileUtil;

public class ConvertTask extends AsyncTask<Void, Void, Void> {

    public interface Callback {
        void onConvertFinished(boolean succeeded);
    }

    private static final String TAG_FFMPEG = "ffmpeg";

    private final Context context;
    private final String fileName;
    private final String outFileFLV;
    private final String outFileMP3;
    private final Callback callback;

    public ConvertTask(Context context, Calendar cal, Streams.Stream stream, Callback callback) {
        this.context = context;
        this.callback = callback;

        DownloadFileUtil downloadFileUtil = new DownloadFileUtil(context);
        fileName = downloadFileUtil.fileBaseName(cal, stream);
        outFileFLV = downloadFileUtil.pathForFLVFile(fileName);
        outFileMP3 = downloadFileUtil.pathForMP3File(fileName);
    }

    @Override
    protected Void doInBackground(Void... commands) {
        Notification notification = createNotification(context.getString(R.string.converting_notification_title),
                                                       fileName,
                                                       true);
        showNotification(notification);

        final Clip inClip = new Clip(outFileFLV);
        final File outFile = new File(outFileMP3);
        final File inFile = new File(outFileFLV);
        final long inSize = inFile.length();

        File fileTmp = context.getApplicationContext().getCacheDir();
        File fileAppRoot = new File(context.getApplicationContext().getApplicationInfo().dataDir);

        try {
            FfmpegController fc = new FfmpegController(fileTmp, fileAppRoot);
            fc.extractAudio(inClip, outFile, new FFMpegCallback(outFile, inSize));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        Notification notification = createNotification(context.getString(R.string.finished_notification_title),
                                                       fileName,
                                                       false);
        showNotification(notification);
        callback.onConvertFinished(true);
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
        private final File outFile;
        private final long inSize;

        public FFMpegCallback(File outFile, long inSize) {
            this.outFile = outFile;
            this.inSize = inSize;
        }

        @Override
        public void shellOut(String shellLine) {
            Log.i(TAG_FFMPEG, shellLine);
            long outSize = outFile.length();
            if (outSize > 0) {
                long frac = (long) (((float) outSize / (float) inSize) * 100);
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
            new File(outFileFLV).delete();
        }
    }
}

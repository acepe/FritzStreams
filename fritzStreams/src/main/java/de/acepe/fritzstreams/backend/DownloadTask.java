package de.acepe.fritzstreams.backend;

import static android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF;

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
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.schriek.rtmpdump.Rtmpdump;

import de.acepe.fritzstreams.Config;
import de.acepe.fritzstreams.MainActivity;
import de.acepe.fritzstreams.R;
import de.acepe.fritzstreams.util.FileFormat;
import de.acepe.fritzstreams.util.UrlFormat;

public class DownloadTask extends AsyncTask<Void, Void, Void> {

    private final static String TAG = "DownloadTask";
    private static final String TAG_FFMPEG = "ffmpeg";
    private static final String TAG_RTMPDUMP = "RtmpDump";

    private final Context context;
    private final String fileName;
    private final String outFileFLV;
    private final String outFileMP3;
    private final String url;

    private WifiManager.WifiLock mWifiLock;

    public DownloadTask(Context context, Calendar cal, Streams.Stream stream) {
        this.context = context;

        fileName = FileFormat.getFileName(cal, stream);
        outFileFLV = FileFormat.getPathForFLVFile(fileName);
        outFileMP3 = FileFormat.getPathForMP3File(fileName);
        url = UrlFormat.getUrl(cal, stream);

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            mWifiLock = wifiManager.createWifiLock(WIFI_MODE_FULL_HIGH_PERF, TAG);
        }
    }

    @Override
    protected Void doInBackground(Void... commands) {
        String command = String.format(Config.RTMP_DUMP_FORMAT, url, outFileFLV);

        showNotification(R.string.downloading_notification_title, fileName);
        mWifiLock.acquire();

        Log.i(TAG_RTMPDUMP, "downloading: " + command);
        Rtmpdump dump = new Rtmpdump();
        dump.parseString(command);

        showNotification(R.string.converting_notification_title, fileName);
        FfmpegController fc;
        try {
            final Clip inClip = new Clip(outFileFLV);
            final File outFile = new File(outFileMP3);
            final File inFile = new File(outFileFLV);
            final long inSize = inFile.length();

            File fileTmp = context.getApplicationContext().getCacheDir();
            File fileAppRoot = new File(context.getApplicationContext().getApplicationInfo().dataDir);

            fc = new FfmpegController(fileTmp, fileAppRoot);
            fc.extractAudio(inClip, outFile, new ShellCallback() {
                @Override
                public void shellOut(String shellLine) {
                    Log.i(TAG_FFMPEG, shellLine);
                    long outSize = outFile.length();
                    if (outSize > 0) {
                        long frac = (long) (((float) outSize / (float) inSize) * 100);
                        showNotification(R.string.converting_notification_title, frac + "%");
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
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mWifiLock.release();
        showNotification(R.string.finished_notification_title, fileName);
    }

    @Override
    protected void onCancelled() {
        mWifiLock.release();
        showNotification(R.string.failed_notification_title, fileName);
        super.onCancelled();
    }

    private void showNotification(int downloadingNotificationTitle, String text) {
        Notification notification = createNotification(context.getString(downloadingNotificationTitle), text);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    private Notification createNotification(String title, String text) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        return new Notification.Builder(context).setContentTitle(title)
                                                .setContentText(text)
                                                .setSmallIcon(R.drawable.ic_launcher)
                                                .setContentIntent(pIntent)
                                                .build();
    }

}

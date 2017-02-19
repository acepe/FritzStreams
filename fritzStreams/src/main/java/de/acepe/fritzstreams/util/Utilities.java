package de.acepe.fritzstreams.util;

import java.util.Calendar;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import de.acepe.fritzstreams.App;

public final class Utilities {

    private Utilities() {
    }

    /**
     * Get the free space on the external storage device (like sd card)
     *
     * @return The free space in byte
     */
    public static double getFreeSpaceExternal() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        return (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
    }

    /**
     * Get the free space on the internal storage device
     *
     * @return The free space in byte
     */
    public static double getFreeSpaceInternal() {
        StatFs stat = new StatFs(App.mApp.getFilesDir().getPath());
        return (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
    }

    /**
     * Is the device connceted to a wifi network?
     *
     * @return true if connected to a wifi network
     */
    public static boolean onWifi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return mWifi.isConnected();
    }

    @NonNull
    public static Calendar today() {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.HOUR, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        return date;
    }

    /**
     * Convert bytes in a human readable format.
     *
     * @param bytes
     *            The byte count
     * @param iec
     *            false for KB, false for KiB
     * @return The human readable file size
     */
    public static String humanReadableBytes(long bytes, boolean iec) {
        // Are we using xB or xiB?
        int byteUnit = iec ? 1024 : 1000;
        float newBytes = bytes;
        int exp = 0;

        // Calculate the file size in the best readable way
        while (newBytes > byteUnit) {
            newBytes = newBytes / byteUnit;
            exp++;
        }

        // What prefix do we have to use?
        String prefix = "";
        if (exp > 0) {
            prefix = (iec ? " KMGTPE" : " kMGTPE").charAt(exp) + ((iec) ? "i" : "");
        }

        // Return a human readable String
        return String.format("%.2f %sB", newBytes, prefix);
    }

}

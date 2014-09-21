package de.acepe.fritzstreams.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import de.acepe.fritzstreams.Config;
import de.acepe.fritzstreams.backend.Streams;

public class DownloadFileUtil {

    private final Context context;

    public DownloadFileUtil(Context context) {
        this.context = context;
    }

    public String fileBaseName(Calendar cal, Streams.Stream stream) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Config.FILE_DATE_FORMAT, Config.GERMANY);
        String fileTemplate = getFileTemplate(stream);
        String dayOfWeek = Config.URL_DAY_OF_WEEK_FORMAT.format(cal.getTime()).toLowerCase(Config.GERMANY);
        String date = dateFormat.format(cal.getTime());
        return String.format(fileTemplate, dayOfWeek, date);
    }

    public String getFileTemplate(Streams.Stream stream) {
        return stream == Streams.Stream.nightflight ? Config.FILE_NIGHTFLIGHT_FORMAT : Config.FILE_SOUNDGARDEN_FORMAT;
    }

    public String pathForFLVFile(String fileName) {
        return getPathWithoutExtension(fileName) + Config.FILE_EXTENSION_FLV;
    }

    public String pathForMP3File(String fileName) {
        return getOutFileName(getPathWithoutExtension(fileName));
    }

    private String getPathWithoutExtension(String fileName) {
        String dir = getDownloadDir();

        return dir + File.separatorChar + fileName;
    }

    private String getDownloadDir() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultPath = Environment.getExternalStorageDirectory() + File.separator + Config.DEFAULT_DOWNLOAD_DIR;
        return sharedPreferences.getString(Config.SP_DOWNLOAD_DIR, defaultPath);
    }

    private String getOutFileName(String outFileWithoutExtension) {
        return getOutFileName(outFileWithoutExtension, 0);
    }

    private String getOutFileName(String outFileWithoutExtension, int count) {
        String outFileName = outFileWithoutExtension
                             + (count == 0 ? "" : "(" + count + ")")
                             + Config.FILE_EXTENSION_MP3;
        final File outFile = new File(outFileName);
        if (outFile.exists())
            return getOutFileName(outFileWithoutExtension, ++count);

        return outFileName;
    }

}

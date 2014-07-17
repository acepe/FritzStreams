package de.acepe.fritzstreams.util;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.acepe.fritzstreams.Config;
import de.acepe.fritzstreams.Stream;

public class FileFormat {

    public static String getFileName(Calendar cal, Stream stream) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Config.FILE_DATE_FORMAT, Config.GERMANY);
        String fileTemplate = getFileTemplate(stream);
        String dayOfWeek = Config.URL_DAY_OF_WEEK_FORMAT.format(cal.getTime()).toLowerCase(Config.GERMANY);
        String date = dateFormat.format(cal.getTime());
        return String.format(fileTemplate, dayOfWeek, date);
    }

    public static String getFileTemplate(Stream stream) {
        return stream == Stream.nightflight ? Config.FILE_NIGHTFLIGHT_FORMAT : Config.FILE_SOUNDGARDEN_FORMAT;
    }

    public static String getPathForFLVFile(String fileName) {
        return getPathWithoutExtension(fileName) + Config.FILE_EXTENSION_FLV;
    }

    public static String getPathForMP3File(String fileName) {
        return getOutFileName(getPathWithoutExtension(fileName));
    }


    private static String getPathWithoutExtension(String fileName) {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        return dir + File.separatorChar + fileName;
    }

    private static String getOutFileName(String outFileWithoutExtension) {
        return getOutFileName(outFileWithoutExtension, 0);
    }

    private static String getOutFileName(String outFileWithoutExtension, int count) {
        String outFileName = outFileWithoutExtension + (count == 0 ? "" : "(" + count + ")") + Config.FILE_EXTENSION_MP3;
        final File outFile = new File(outFileName);
        if (outFile.exists())
            return getOutFileName(outFileWithoutExtension, ++count);

        return outFileName;
    }

}

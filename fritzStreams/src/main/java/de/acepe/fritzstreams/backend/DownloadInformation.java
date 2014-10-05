package de.acepe.fritzstreams.backend;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import de.acepe.fritzstreams.App;

public class DownloadInformation {

    private final Context context;
    private final Calendar cal;
    private final Stream stream;
    private final String fileBaseName;
    private final String outFileFLV;

    private final String outFileMp3;
    private final String url;

    public DownloadInformation(Context context, Calendar cal, Stream stream) {
        this.context = context;
        this.cal = cal;
        this.stream = stream;

        fileBaseName = fileBaseName();
        outFileFLV = pathForFLVFile();
        outFileMp3 = pathForMP3File();

        url = UrlFormat.getUrl(cal, stream);
    }

    private String fileBaseName() {
        String fileTemplate = getFileTemplate(stream);
        String dayOfWeek = App.URL_DAY_OF_WEEK_FORMAT.format(cal.getTime()).toLowerCase(App.GERMANY);
        SimpleDateFormat dateFormat = new SimpleDateFormat(App.FILE_DATE_FORMAT, App.GERMANY);
        String date = dateFormat.format(cal.getTime());
        return String.format(fileTemplate, dayOfWeek, date);
    }

    private String getFileTemplate(Stream stream) {
        return stream == Stream.nightflight ? App.FILE_NIGHTFLIGHT_FORMAT : App.FILE_SOUNDGARDEN_FORMAT;
    }

    private String pathForFLVFile() {
        return getPathWithoutExtension(fileBaseName) + App.FILE_EXTENSION_FLV;
    }

    private String pathForMP3File() {
        return getOutFileName(getPathWithoutExtension(fileBaseName));
    }

    private String getPathWithoutExtension(String fileName) {
        String dir = getDownloadDir();

        return dir + File.separatorChar + fileName;
    }

    private String getDownloadDir() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String defaultPath = Environment.DIRECTORY_MUSIC;
        return sharedPreferences.getString(App.SP_DOWNLOAD_DIR, defaultPath);
    }

    private String getOutFileName(String outFileWithoutExtension) {
        return getOutFileName(outFileWithoutExtension, 0);
    }

    private String getOutFileName(String outFileWithoutExtension, int count) {
        String outFileName = outFileWithoutExtension + (count == 0 ? "" : "(" + count + ")") + App.FILE_EXTENSION_MP3;
        final File outFile = new File(outFileName);
        if (outFile.exists())
            return getOutFileName(outFileWithoutExtension, ++count);

        return outFileName;
    }

    public String getFileBaseName() {
        return fileBaseName;
    }

    public String getOutFileMp3() {
        return outFileMp3;
    }

    public String getOutFileFLV() {
        return outFileFLV;
    }

    public String getUrl() {
        return url;
    }

    public Stream getStream() {
        return stream;
    }

    public String getDisplayStreamType() {
        return context.getString(stream.getStreamType(cal));
    }

    public String getDisplayDate() {
        return DateFormat.getDateInstance().format(cal.getTime());
    }

    public String getDisplayStreamCategory() {
        return context.getString(stream.getStreamCategorie());
    }
}

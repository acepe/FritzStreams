package de.acepe.fritzstreams.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;

import de.acepe.fritzstreams.App;
import de.acepe.fritzstreams.R;

public class StreamInfo {
    public enum Stream {
        SOUNDGARDEN, NIGHTFLIGHT
    }

    public interface Callback {
        void initFinished();
    }

    static final String BASE_URL = "http://fritz.de%s";
    static final String NIGHTFLIGHT_URL = "/livestream/liveplayer_nightflight.htm/day=%s.html";
    static final String SOUNDGARDEN_URL = "/livestream/liveplayer_soundgarden.htm/day=%s.html";
    static final String TITLE_SELECTOR = "#main > article > div.teaserboxgroup.intermediate.count2.even.layoutstandard.layouthalf_2_4 > section > article.manualteaser.first.count1.odd.layoutlaufende_sendung.doctypesendeplatz > h3 > a > span";
    static final String SUBTITLE_SELECTOR = "#main > article > div.teaserboxgroup.intermediate.count2.even.layoutstandard.layouthalf_2_4 > section > article.manualteaser.first.count1.odd.layoutlaufende_sendung.doctypesendeplatz > div > p";
    static final String DOWNLOAD_SELECTOR = "#main > article > div.teaserboxgroup.first.count1.odd.layoutstandard.layouthalf_2_4 > section > article.manualteaser.last.count2.even.layoutmusikstream.layoutbeitrag_av_nur_av.doctypeteaser > div";
    static final String DOWNLOAD_DESCRIPTOR_ATTRIBUTE = "data-media-ref";

    private final Context mContext;
    private final Calendar mDate;
    private final Stream mStream;

    private Document mDoc;
    private String mTitle;
    private String mSubtitle;
    private String mStreamURL;
    private String mFilename;

    public StreamInfo(Context mContext, Calendar mDate, Stream mStream) {
        this.mContext = mContext;
        this.mDate = mDate;
        this.mStream = mStream;
    }

    public void init(final Callback callback) {
        AsyncTask<Void, Void, Void> initTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                init();
                return null;
            }

            protected void onPostExecute(Void result) {

                callback.initFinished();
            }
        };
        initTask.execute();
    }

    private void init() {
        String contentURL = buildURL();
        try {
            mDoc = Jsoup.connect(contentURL).data("query", "Java").userAgent("Mozilla").timeout(3000).get();
            mTitle = extractTitle(TITLE_SELECTOR);
            mSubtitle = extractTitle(SUBTITLE_SELECTOR);
            mStreamURL = extractDownloadURL();
            mFilename = pathForMP3File();
        } catch (IOException e) {
            mTitle = mContext.getString(R.string.error);
        }
    }

    private String extractTitle(String selector) {
        Elements info = mDoc.select(selector);
        return info.text();
    }

    private String buildURL() {
        String contentURL = url(mStream == Stream.NIGHTFLIGHT ? NIGHTFLIGHT_URL : SOUNDGARDEN_URL);
        String ddMM = new SimpleDateFormat(App.FILE_DATE_FORMAT, App.GERMANY).format(mDate.getTime());
        return String.format(contentURL, ddMM);
    }

    private String url(String subUrl) {
        return String.format(BASE_URL, subUrl);
    }

    private String extractDownloadURL() {
        String downloadDescriptorURL = extractDownloadDescriptorUrl();
        if (downloadDescriptorURL == null) {
            return null;
        }

        try (InputStream is = new URL(url(downloadDescriptorURL)).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);

            String streamToken = "_stream\":\"";
            int beginIndex = jsonText.indexOf(streamToken) + streamToken.length();

            String mp3Token = ".mp3";
            int endIndex = jsonText.indexOf(mp3Token) + mp3Token.length();

            return jsonText.substring(beginIndex, endIndex);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractDownloadDescriptorUrl() {
        Elements info = mDoc.select(DOWNLOAD_SELECTOR);
        return info.attr(DOWNLOAD_DESCRIPTOR_ATTRIBUTE);
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private String pathForMP3File() {
        return getDownloadDir() + File.separator + getOutFileName();
    }

    private String getDownloadDir() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String defaultPath = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        return sharedPreferences.getString(App.SP_DOWNLOAD_DIR, defaultPath);
    }

    private String getOutFileName() {
        return mTitle
               + "_"
               + new SimpleDateFormat("yyyy-MM-dd", App.GERMANY).format(mDate.getTime())
               + App.FILE_EXTENSION_MP3;
    }

    // TODO: remove?
    private String getOutFileName(String outFileWithoutExtension, int count) {
        String outFileName = outFileWithoutExtension + (count == 0 ? "" : "(" + count + ")") + App.FILE_EXTENSION_MP3;
        final File outFile = new File(outFileName);
        if (outFile.exists())
            return getOutFileName(outFileWithoutExtension, ++count);

        return outFileName;
    }

    public String getStreamURL() {
        return mStreamURL;
    }

    public Stream getStream() {
        return mStream;
    }

    public String getFilename() {
        return mFilename;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public boolean isInited() {
        return getFilename() != null;
    }

}

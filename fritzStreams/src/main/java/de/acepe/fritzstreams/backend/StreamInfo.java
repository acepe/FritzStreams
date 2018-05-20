package de.acepe.fritzstreams.backend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.gson.Gson;
import de.acepe.fritzstreams.backend.json.MediaStreamArray;
import de.acepe.fritzstreams.backend.json.OnDemandDownload;
import de.acepe.fritzstreams.backend.json.OnDemandStreamDescriptor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.os.AsyncTask.THREAD_POOL_EXECUTOR;
import static java.nio.charset.StandardCharsets.UTF_8;

public class StreamInfo {


    public enum Stream {
        SOUNDGARDEN, NIGHTFLIGHT
    }

    public interface Callback {
        void initFinished(StreamInfo streamInfo);
    }

    private static final String TAG = "StreamInfo";
    private static final String BASE_URL = "https://fritz.de%s";
    private static final String NIGHTFLIGHT_URL = "/livestream/liveplayer_nightflight.htm/day=%s.html";
    private static final String SOUNDGARDEN_URL = "/livestream/liveplayer_bestemusik.htm/day=%s.html";
    private static final String TITLE_SELECTOR = "#main > article > div.teaserboxgroup.intermediate.count2.even.layoutstandard.layouthalf_2_4 > section > article.manualteaser.first.count1.odd.layoutlaufende_sendung.doctypesendeplatz > h3 > a > span";
    private static final String SUBTITLE_SELECTOR = "#main > article > div.teaserboxgroup.intermediate.count2.even.layoutstandard.layouthalf_2_4 > section > article.manualteaser.first.count1.odd.layoutlaufende_sendung.doctypesendeplatz > div > p";
    private static final String DOWNLOAD_SELECTOR = "#main > article > div.count1.first.layouthalf_2_4.layoutstandard.odd.teaserboxgroup > section > article.count2.doctypeteaser.even.last.layoutbeitrag_av_nur_av.layoutmusikstream.manualteaser > div";
    private static final String DOWNLOAD_DESCRIPTOR_ATTRIBUTE = "data-jsb";
    private static final String IMAGE_SELECTOR = "#main > article > div.teaserboxgroup.intermediate.count2.even.layoutstandard.layouthalf_2_4 > section > article.manualteaser.last.count2.even.layoutstandard.doctypeteaser > aside > div > a > img";
    private static final String FILE_DATE_FORMAT = "yyyy-MM-dd";

    private final Context mContext;
    private final Calendar mDate;
    private final Stream mStream;
    private final Gson mGson;

    private Document mDoc;
    private String mTitle;
    private String mSubtitle;
    private String mStreamURL;
    private String mFilename;
    private Bitmap mImage;
    private boolean failed;

    public StreamInfo(Context mContext, Calendar mDate, @NonNull Stream mStream) {
        this.mContext = mContext;
        this.mDate = mDate;
        this.mStream = mStream;
        this.mGson = new Gson();
    }

    public void init(Callback callback) {
        AsyncTask<Void, Void, Void> initTask = new InitTask(callback);
        initTask.executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private void init() throws IOException {
        String contentURL = buildURL();
        mDoc = Jsoup.connect(contentURL).timeout(10000).userAgent("Mozilla").get();
        mTitle = extractTitle(TITLE_SELECTOR);
        mSubtitle = extractTitle(SUBTITLE_SELECTOR);
        downloadImage(extractImageUrl());

        mStreamURL = extractDownloadURL();
        mFilename = pathForMP3File();
    }

    private void downloadImage(String imageUrl) {
        try {
            InputStream in = new java.net.URL(imageUrl).openStream();
            mImage = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildURL() {
        String contentURL = url(mStream == Stream.NIGHTFLIGHT ? NIGHTFLIGHT_URL : SOUNDGARDEN_URL);
        String ddMM = new SimpleDateFormat(Constants.FILE_DATE_FORMAT, Constants.GERMANY).format(mDate.getTime());
        return String.format(contentURL, ddMM);
    }

    private String url(String subUrl) {
        return String.format(BASE_URL, subUrl);
    }

    private String extractTitle(String selector) {
        Elements info = mDoc.select(selector);
        return info.text();
    }

    private String extractDownloadURL() {
        String downloadDescriptorURL = extractDownloadDescriptorUrl();
        if (downloadDescriptorURL == null) {
            return null;
        }

        try (InputStream is = new URL(url(downloadDescriptorURL)).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName(UTF_8.name())));
            String jsonText = readAll(rd);

            OnDemandStreamDescriptor target = mGson.fromJson(jsonText, OnDemandStreamDescriptor.class);

            for (MediaStreamArray mediaStreamArray : target.getMediaArray().get(0).getMediaStreamArray()) {
                if (mediaStreamArray.getQuality() != null) {
                    return mediaStreamArray.getStream();
                }
            }
            return null;
        } catch (Throwable e) {
            Log.e(TAG, "Couldn 't extract download-URL from stream website", e);
            return null;
        }
    }

    private String extractDownloadDescriptorUrl() {
        Elements info = mDoc.select(DOWNLOAD_SELECTOR);
        String downloadJSON = info.attr(DOWNLOAD_DESCRIPTOR_ATTRIBUTE);
        OnDemandDownload download = mGson.fromJson(downloadJSON, OnDemandDownload.class);

        return download.getMedia();
    }

    private String extractImageUrl() {
        String imageUrl = mDoc.select(IMAGE_SELECTOR).attr("src");
        return String.format(BASE_URL, imageUrl);
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
        return getDownloadDir() + File.separator + getFileName();
    }

    private String getDownloadDir() {
        return mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
    }

    private String getFileName() {
        return mTitle
                + "_"
                + new SimpleDateFormat(FILE_DATE_FORMAT, Constants.GERMANY).format(mDate.getTime())
                + Constants.FILE_EXTENSION_MP3;
    }

    public String getStreamURL() {
        return mStreamURL;
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

    public Stream getStream() {
        return mStream;
    }

    public boolean isInited() {
        return getFilename() != null;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public Calendar getDay() {
        return mDate;
    }

    public boolean isInitFailed() {
        return failed;
    }

    @Override
    public String toString() {
        return "StreamInfo{" + "mDate=" + mDate + ", mStream=" + mStream + ", mTitle='" + mTitle + '\'' + '}';
    }


    private class InitTask extends AsyncTask<Void, Void, Void> {
        private final Callback callback;
        private Exception error;

        InitTask(Callback callback) {
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                init();
            } catch (Exception e) {
                error = e;
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            if (error == null) {
                callback.initFinished(StreamInfo.this);
                failed = false;
            } else {
                callback.initFinished(null);
                failed = true;
            }
        }
    }
}

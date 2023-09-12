package de.acepe.fritzstreams.backend

import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import de.acepe.fritzstreams.backend.json.OnDemandDownload
import de.acepe.fritzstreams.backend.json.OnDemandStreamDescriptor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import kotlin.text.Charsets.UTF_8

class StreamCrawler(private val stream: OnDemandStream, private val onInitDone: (OnDemandStream) -> Unit) : AsyncTask<Void, Void, OnDemandStream>() {

    private val gson: Gson = Gson()

    private var mDoc: Document? = null
    private var error: Exception? = null

    fun crawl() {
        executeOnExecutor(THREAD_POOL_EXECUTOR)
    }

    override fun doInBackground(vararg params: Void): OnDemandStream? {
        try {
            init()
        } catch (e: Exception) {
            error = e
            stream.isFailed = true
        }
        return stream
    }

    override fun onPostExecute(result: OnDemandStream) {
        onInitDone.invoke(result)
    }

    @Throws(IOException::class)
    private fun init() {
        val contentURL = buildURL()
        mDoc = Jsoup.connect(contentURL).timeout(10000).userAgent("Mozilla").get()
        stream.title = extractTitle(TITLE_SELECTOR)
        stream.subtitle = extractTitle(SUBTITLE_SELECTOR)
        downloadImage(extractImageUrl())

        stream.streamURL = extractDownloadURL()
        setFilename()
    }

    private fun downloadImage(imageUrl: String) {
        try {
            val `in` = java.net.URL(imageUrl).openStream()
            stream.image = BitmapFactory.decodeStream(`in`)
        } catch (e: Exception) {
            val message = e.message
            if (message != null) {
                Log.e("Error", message)
            }
            e.printStackTrace()
        }
    }

    private fun buildURL(): String {
        val contentURL = url(if (stream.stream === StreamType.NIGHTFLIGHT) NIGHTFLIGHT_URL else SOUNDGARDEN_URL)
        val ddMM = SimpleDateFormat(Constants.FILE_DATE_FORMAT, Constants.GERMANY).format(stream.day.time)
        return String.format(contentURL, ddMM)
    }

    private fun url(subUrl: String): String {
        return String.format(BASE_URL, subUrl)
    }

    private fun extractTitle(selector: String): String {
        val info = mDoc!!.select(selector)
        return info.text()
    }

    private fun extractDownloadURL(): String? {
        val downloadDescriptorURL = extractDownloadDescriptorUrl() ?: return null

        try {
            URL(url(downloadDescriptorURL)).openStream().use { `is` ->
                InputStreamReader(`is`, Charset.forName(UTF_8.name())).use { isr ->
                    BufferedReader(isr).use { reader ->
                        return findDownloadUrl(readAndUnmarshal(reader))
                    }
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Couldn 't extract download-URL from stream website", e)
            return null
        }
    }

    private fun extractDownloadDescriptorUrl(): String? {
        val info = mDoc!!.select(DOWNLOAD_SELECTOR)
        val downloadJSON = info.attr(DOWNLOAD_DESCRIPTOR_ATTRIBUTE)
        val download = gson.fromJson(downloadJSON, OnDemandDownload::class.java)
        return download.media
    }

    private fun readAndUnmarshal(rd: Reader): OnDemandStreamDescriptor? {
        val jsonText = readAll(rd)
        return gson.fromJson(jsonText, OnDemandStreamDescriptor::class.java)
    }

    private fun findDownloadUrl(streamDescriptor: OnDemandStreamDescriptor?): String? {
        for (mediaStreamArray in streamDescriptor!!.mediaArray!![0].mediaStreamArray!!) {
            if (mediaStreamArray.quality != null) {
                return mediaStreamArray.stream
            }
        }
        return null
    }

    private fun extractImageUrl(): String {
        val imageUrl = mDoc!!.select(IMAGE_SELECTOR).attr("src")
        return String.format(BASE_URL, imageUrl)
    }

    @Throws(IOException::class)
    private fun readAll(rd: Reader): String {
        val sb = StringBuilder()
        var cp: Int

        do {
            cp = rd.read()
            if (cp != -1) sb.append(cp.toChar())
        } while (cp != -1)

        return sb.toString()
    }

    private fun setFilename() {
        stream.filename = stream.title + "_" + SimpleDateFormat(FILE_DATE_FORMAT, Constants.GERMANY).format(stream.day.time) + Constants.FILE_EXTENSION_MP3
    }

    companion object {
        private const val TAG = "StreamInfo"
        private const val BASE_URL = "https://fritz.de%s"
        private const val NIGHTFLIGHT_URL = "/livestream/liveplayer_nightflight.htm/day=%s0000.html"
        private const val SOUNDGARDEN_URL = "/livestream/liveplayer_bestemusik.htm/day=%s2000.html"
        private const val TITLE_SELECTOR = "#main > article > div.count2.even.last.layouthalf_2_4.layoutstandard.teaserboxgroup > section > article.manualteaser.first.count1.odd.layoutlaufende_sendung.doctypesendeplatz > h3 > a > span"
        private const val SUBTITLE_SELECTOR = "#main > article > div.count2.even.last.layouthalf_2_4.layoutstandard.teaserboxgroup > section > article.manualteaser.first.count1.odd.layoutlaufende_sendung.doctypesendeplatz > div > p"
        private const val DOWNLOAD_SELECTOR = "#main > article > div.count1.first.layouthalf_2_4.layoutstandard.odd.teaserboxgroup > section > article > div"
        private const val DOWNLOAD_DESCRIPTOR_ATTRIBUTE = "data-jsb"
        private const val IMAGE_SELECTOR = "#main .layoutlivestream .layouthalf_2_4.count2 .layoutlivestream_info .manualteaser .manualteaserpicture img"
        private const val FILE_DATE_FORMAT = "yyyy-MM-dd"
    }

}

package de.acepe.fritzstreams.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.support.v4.content.FileProvider
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import de.acepe.fritzstreams.BuildConfig
import de.acepe.fritzstreams.R
import de.acepe.fritzstreams.backend.DownloadInfo
import de.acepe.fritzstreams.backend.DownloadState.*
import kotlinx.android.synthetic.main.download_entry_view.view.*
import java.io.File

private const val FONTAWESOME = "fontawesome-webfont.ttf"

class DownloadEntryView(context: Context) : LinearLayout(context) {

    private lateinit var mDownload: DownloadInfo

    init {
        val font = Typeface.createFromAsset(context.assets, FONTAWESOME)
        LayoutInflater.from(context).inflate(R.layout.download_entry_view, this)

        playIcon.typeface = font
        cancelDownload.typeface = font
    }

    fun setButtonAction(action: (DownloadInfo) -> Unit) {
        cancelDownload.setOnClickListener { action(mDownload) }
    }

    fun setDownload(download: DownloadInfo) {
        mDownload = download
        title.text = download.title
        subtitle.text = download.subtitle

        when (download.state) {
            WAITING -> updateView(R.string.waiting, R.string.icon_remove)
            DOWNLOADING -> downloadProgress.progress = download.progressPercent
            CANCELLED -> updateView(R.string.cancelled, R.string.icon_retry)
            FAILED -> updateView(R.string.failed, R.string.icon_retry)
            FINISHED -> updateView(R.string.finished, R.string.icon_accept)
        }
        downloadProgress.visibility = if (download.state == DOWNLOADING) View.VISIBLE else View.INVISIBLE
        stateDisplay.visibility = if (download.state == DOWNLOADING) View.INVISIBLE else View.VISIBLE
        playIcon.visibility = if (download.state == FINISHED) View.VISIBLE else View.INVISIBLE

        setOnClickListener { if (download.state == FINISHED) openWithMusicApp() }
    }

    private fun updateView(stateText: Int, cancelText: Int) {
        stateDisplay.setText(stateText)
        cancelDownload.setText(cancelText)
    }

    private fun openWithMusicApp() {
        val outFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", File(mDownload.filename))

        val mediaIntent = Intent()
        with(mediaIntent) {
            action = Intent.ACTION_VIEW
            setDataAndType(outFile, "audio/*")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (mediaIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mediaIntent)
        } else {
            Toast.makeText(context, R.string.app_not_available, Toast.LENGTH_LONG).show()
        }
    }

}
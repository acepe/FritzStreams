package de.acepe.fritzstreams.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import de.acepe.fritzstreams.R
import de.acepe.fritzstreams.backend.StreamInfo
import de.acepe.fritzstreams.util.Utilities
import kotlinx.android.synthetic.main.progress_overlay.view.*
import kotlinx.android.synthetic.main.stream_view.view.*

class StreamView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.stream_view, this)
        clearStream()
    }

    override fun setOnClickListener(listener: View.OnClickListener?) {
        downloadButton.setOnClickListener(listener)
    }

    fun setStreamInfo(streamInfo: StreamInfo) {
        title.text = streamInfo.title
        subtitle.text = streamInfo.subtitle
        image.setImageBitmap(streamInfo.image)
        downloadButton.isEnabled = true
        downloadButton.setText(R.string.download)

        // Hide progress view (with animation):
        Utilities.animateView(progressOverlay, View.GONE, 0f, 200)
    }

    fun failed() {
        image.setImageResource(R.drawable.sampleimage)
        title.setText(R.string.error)
        subtitle.text = ""
        downloadButton.isEnabled = true
        downloadButton.setText(R.string.try_again)

        Utilities.animateView(progressOverlay, View.GONE, 0f, 200)
    }

    fun clearStream() {
        image.setImageResource(R.drawable.sampleimage)
        title.text = ""
        subtitle.text = ""
        downloadButton.isEnabled = false

        Utilities.animateView(progressOverlay, View.VISIBLE, 0.4f, 200)
    }


}
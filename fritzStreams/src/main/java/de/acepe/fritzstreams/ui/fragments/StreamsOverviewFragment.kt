package de.acepe.fritzstreams.ui.fragments

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ToggleButton
import de.acepe.fritzstreams.R
import de.acepe.fritzstreams.backend.*
import de.acepe.fritzstreams.backend.Stream.NIGHTFLIGHT
import de.acepe.fritzstreams.backend.Stream.SOUNDGARDEN
import de.acepe.fritzstreams.ui.components.StreamView
import de.acepe.fritzstreams.util.Utilities.today
import kotlinx.android.synthetic.main.fragment_streams_overview.*
import java.util.*

class StreamsOverviewFragment : Fragment() {

    private val model: StreamsModel by lazy {
        ViewModelProviders.of(activity!!).get(StreamsModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_streams_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        streamViewNightflight.setOnClickListener({ onDownloadClicked(NIGHTFLIGHT) })
        streamViewSoundgarden.setOnClickListener({ onDownloadClicked(SOUNDGARDEN) })

        configureToggleButtons()
        restoreState()
    }

    private fun onDownloadClicked(stream: Stream) {
        val onDemandStream = model.getStream(stream, model.day!!)

        if (onDemandStream.isInitFailed) {
            init(stream, onDemandStream.day)
        } else {
            download(onDemandStream)
        }
    }

    private fun configureToggleButtons() {
        for (i in 0 until daysToggleGroup.childCount) {
            val view = daysToggleGroup.getChildAt(6 - i) as ToggleButton

            val day = today()
            day.add(Calendar.DAY_OF_YEAR, -i)

            val text = Constants.DAY_FORMAT.format(day.time)
            view.textOff = text
            view.textOn = text
            view.text = text
            view.setOnClickListener({ onToggleChecked(it, day) })
            view.tag = day
        }
    }

    private fun onToggleChecked(v: View, day: Calendar) {
        val childCount = daysToggleGroup.childCount
        for (i in 0 until childCount) {
            val toggle = daysToggleGroup.getChildAt(i) as ToggleButton
            val checked = toggle.id == v.id
            toggle.isChecked = checked

            if (checked) {
                onSelectedDayChange(day)
            }
        }
    }

    private fun onSelectedDayChange(day: Calendar) {
        model.day = day
        init(SOUNDGARDEN, day)
        init(NIGHTFLIGHT, day)
    }

    private fun init(stream: Stream, day: Calendar) {
        val view = if (stream === NIGHTFLIGHT) streamViewNightflight else streamViewSoundgarden
        val onDemandStream = model.getStream(stream, day)

        view.clearStream()
        onDemandStream.init({ setStreamView(view, onDemandStream) })
    }

    private fun setStreamView(view: StreamView, onDemandStream: OnDemandStream) {
        if (onDemandStream.isInitFailed) {
            view.failed()
            return
        }
        view.setStreamInfo(onDemandStream)
    }

    private fun restoreState() {
        val dayFromCache = model.day
        val day = dayFromCache ?: today()
        onSelectedDayChange(day)

        val daysToggle = findToggle(day)
        if (daysToggle != null) {
            daysToggle.isChecked = true
        }
    }

    private fun findToggle(day: Calendar): ToggleButton? {
        for (i in 0 until daysToggleGroup.childCount) {
            val view = daysToggleGroup.getChildAt(i) as ToggleButton
            if (view.tag == day)
                return view
        }
        return null
    }


    private fun download(onDemandStream: OnDemandStream) {
        if (!onDemandStream.isInited) {
            return
        }
        Toast.makeText(context, R.string.download_started, Toast.LENGTH_SHORT).show()

        model.scheduleDownload(DownloadInfo(onDemandStream))
    }

}

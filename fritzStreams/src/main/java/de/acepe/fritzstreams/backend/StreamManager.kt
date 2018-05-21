package de.acepe.fritzstreams.backend

import de.acepe.fritzstreams.util.Utilities.isTodayBeforeSoundgardenRelease
import java.util.*

class StreamManager(private val downloadPath: String) {

    private val mSoundgardenStreamsForDay = HashMap<Calendar, OnDemandStream>()
    private val mNightflightStreamsForDay = HashMap<Calendar, OnDemandStream>()

    fun getOrCreateStream(stream: Stream, day: Calendar): OnDemandStream {
        return mapForStream(stream).getOrPut(day) { createStream(stream, day) }
    }

    private fun mapForStream(stream: Stream): HashMap<Calendar, OnDemandStream> {
        return if (stream == Stream.NIGHTFLIGHT) mNightflightStreamsForDay else mSoundgardenStreamsForDay
    }

    private fun createStream(stream: Stream, day: Calendar): OnDemandStream {
        return OnDemandStream(downloadPath, computeDayOfStream(stream, day), stream)
    }

    private fun computeDayOfStream(stream: Stream, day: Calendar): Calendar {
        if (stream == Stream.SOUNDGARDEN && isTodayBeforeSoundgardenRelease(day)) {
            val dayInLastWeek = day.clone() as Calendar
            dayInLastWeek.add(Calendar.DAY_OF_YEAR, -7)
            return dayInLastWeek
        } else {
            return day
        }
    }


}

package de.acepe.fritzstreams.backend

import de.acepe.fritzstreams.util.Utilities.isTodayBeforeSoundgardenRelease
import java.util.*

class StreamManager {

    private val mSoundgardenStreamsForDay = HashMap<Calendar, OnDemandStream>()
    private val mNightflightStreamsForDay = HashMap<Calendar, OnDemandStream>()

    fun getOrCreateStream(streamType: StreamType, day: Calendar): OnDemandStream {
        return mapForStream(streamType).getOrPut(day) { createStream(streamType, day) }
    }

    private fun mapForStream(streamType: StreamType): HashMap<Calendar, OnDemandStream> {
        return if (streamType == StreamType.NIGHTFLIGHT) mNightflightStreamsForDay else mSoundgardenStreamsForDay
    }

    private fun createStream(streamType: StreamType, day: Calendar): OnDemandStream {
        return OnDemandStream(computeDayOfStream(streamType, day), streamType)
    }

    private fun computeDayOfStream(streamType: StreamType, day: Calendar): Calendar {
        return if (streamType == StreamType.SOUNDGARDEN && isTodayBeforeSoundgardenRelease(day)) {
            val dayInLastWeek = day.clone() as Calendar
            dayInLastWeek.add(Calendar.DAY_OF_YEAR, -7)
            dayInLastWeek
        } else {
            day
        }
    }

}

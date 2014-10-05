package de.acepe.fritzstreams.backend;

import java.util.Calendar;

import de.acepe.fritzstreams.R;

public enum Stream {

    soundgarden, nightflight;

    public int getStreamCategorie() {
        if (this == Stream.soundgarden)
            return R.string.stream_soundgarden;
        return R.string.stream_nightflight;
    }

    public int getStreamType(Calendar cal) {
        if (this == Stream.soundgarden)
            return getSoundgarden(cal);

        return getNightflight(cal);
    }

    private static int getSoundgarden(Calendar cal) {
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return R.string.soundgarden_monday;
            case Calendar.TUESDAY:
                return R.string.soundgarden_tuesday;
            case Calendar.WEDNESDAY:
                return R.string.soundgarden_wednesday;
            case Calendar.THURSDAY:
                return R.string.soundgarden_thursday;
            case Calendar.FRIDAY:
                return R.string.soundgarden_friday;
            case Calendar.SATURDAY:
                return R.string.soundgarden_saturday;
            case Calendar.SUNDAY:
                return R.string.soundgarden_sunday;
            default:
                throw new RuntimeException("Unknown day of week");
        }
    }

    private static int getNightflight(Calendar cal) {
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return R.string.nightflight_monday;
            case Calendar.TUESDAY:
                return R.string.nightflight_tuesday;
            case Calendar.WEDNESDAY:
                return R.string.nightflight_wednesday;
            case Calendar.THURSDAY:
                return R.string.nightflight_thursday;
            case Calendar.FRIDAY:
                return R.string.nightflight_friday;
            case Calendar.SATURDAY:
                return R.string.nightflight_saturday;
            case Calendar.SUNDAY:
                return R.string.nightflight_sunday;
            default:
                throw new RuntimeException("Unknown day of week");
        }
    }

}
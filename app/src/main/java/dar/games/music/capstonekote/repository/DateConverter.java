package dar.games.music.capstonekote.repository;

import androidx.room.TypeConverter;

import java.time.LocalDate;

/**
 * LocalDate converter for the Room database
 */
public class DateConverter {

    @TypeConverter
    public static LocalDate toLocalDate(Long epochDay) {
        return epochDay == null ? null : LocalDate.ofEpochDay(epochDay);
    }

    @TypeConverter
    public static Long toEpochDay(LocalDate date) {
        return date == null ? null : date.toEpochDay();
    }
}

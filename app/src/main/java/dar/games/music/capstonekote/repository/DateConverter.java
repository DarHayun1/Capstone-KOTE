package dar.games.music.capstonekote.repository;

import androidx.room.TypeConverter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * LocalDate converter for the Room database
 */
public class DateConverter {

    @TypeConverter
    public static LocalDateTime toLocalDateTime(Long epochSecond) {
        return epochSecond == null ? null : LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.UTC);
    }

    @TypeConverter
    public static Long toEpochSecond(LocalDateTime date) {
        return date == null ? null : date.toEpochSecond(ZoneOffset.UTC);
    }
}

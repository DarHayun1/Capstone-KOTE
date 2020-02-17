package dar.games.music.capstonekote.repository;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Date converter for the Room database
 */
public class DateConverter {

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}

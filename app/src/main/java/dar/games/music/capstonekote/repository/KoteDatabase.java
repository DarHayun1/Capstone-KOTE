package dar.games.music.capstonekote.repository;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {GameResultModel.class}, version = 1, exportSchema = false)
public abstract class KoteDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "kotedb";
    private static KoteDatabase sInstance;

    static KoteDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null)
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            KoteDatabase.class, KoteDatabase.DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
            }
        }
        return sInstance;
    }

    public abstract KoteDao dao();
}


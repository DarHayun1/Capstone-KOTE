package dar.games.music.capstonekote.repository;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.TypeConverters;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
@TypeConverters(DateConverter.class)
public interface KoteDao {

    @Insert(onConflict = REPLACE)
    void insertGame(GameResultModel gameResultModel);

    @Query("SELECT * FROM results ORDER BY time")
    LiveData<List<GameResultModel>> getAllGamesByTime();

    @Query("SELECT * FROM results ORDER BY time ")
    LiveData<List<GameResultModel>> getAllGames();

    @Query("SELECT time, score, difficulty FROM results WHERE difficulty = 0 ORDER BY time ")
    LiveData<List<GameResultModel>> getEasyScores();

    @Query("SELECT time, score, difficulty FROM results WHERE difficulty = 1 ORDER BY time ")
    LiveData<List<GameResultModel>> getHardScores();

    @Query("SELECT time, score, difficulty FROM results WHERE difficulty = 2 ORDER BY time ")
    LiveData<List<GameResultModel>> getExtremeScores();

    @Query("SELECT max(score) FROM results WHERE difficulty = 0")
    LiveData<Integer> getEasyHighScore();

    @Query("SELECT max(score) FROM results WHERE difficulty = 1")
    LiveData<Integer> getHardHighScore();

    @Query("SELECT max(score) FROM results WHERE difficulty = 2")
    LiveData<Integer> getExtremeHighScore();

    @Query("SELECT * FROM results LIMIT 1")
    GameResultModel getAnyScore();

}

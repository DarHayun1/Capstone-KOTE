package dar.games.music.capstonekote.repository;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

@Entity(tableName = "results")
public class GameResultModel {

    @TypeConverters(DateConverter.class)
    @PrimaryKey
    private Date time;
    private int score;
    private int difficulty;

    public GameResultModel(Date time, int score, int difficulty) {
        this.score = score;
        this.time = time;
        this.difficulty = difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public Date getTime() {
        return time;
    }

    public int getDifficulty() {
        return difficulty;
    }

}

package dar.games.music.capstonekote.repository;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalDateTime;

@Entity(tableName = "results")
public class GameResultModel {

    @TypeConverters(DateConverter.class)
    @PrimaryKey
    private LocalDateTime date;
    private int score;
    private int difficulty;

    public GameResultModel(LocalDateTime date, int score, int difficulty) {
        this.score = score;
        this.date = date;
        this.difficulty = difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public int getDifficulty() {
        return difficulty;
    }

    @NonNull
    @Override
    public String toString() {
        return "Date: " + getDate().toString() + "score: " + getScore() + "diff: " + getDifficulty();
    }
}

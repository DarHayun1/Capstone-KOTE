package dar.games.music.capstonekote.repository;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalDate;

@Entity(tableName = "results")
public class GameResultModel {

    @TypeConverters(DateConverter.class)
    @PrimaryKey
    private LocalDate date;
    private int score;
    private int difficulty;

    public GameResultModel(LocalDate date, int score, int difficulty) {
        this.score = score;
        this.date = date;
        this.difficulty = difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getDifficulty() {
        return difficulty;
    }

}

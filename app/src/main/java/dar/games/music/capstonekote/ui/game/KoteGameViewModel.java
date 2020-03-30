package dar.games.music.capstonekote.ui.game;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.gamelogic.KoteGame;
import dar.games.music.capstonekote.repository.GamesHistoryRepository;

public class KoteGameViewModel extends AndroidViewModel {

    // **********************************
    // Arrays holding the music files ids
    // **********************************

//    private static final int[] EASY_FILES_IDS = new int[]{R.raw.easy1, R.raw.easy2, R.raw.easy3,
//            R.raw.easy4, R.raw.easy5, R.raw.easy6, R.raw.easy7,
//            R.raw.easy8, R.raw.easy9, R.raw.easy10};
//
//    private static final int[] HARD_FILES_IDS = new int[]{R.raw.hard1, R.raw.hard2, R.raw.hard3,
//            R.raw.hard4, R.raw.hard5, R.raw.hard6, R.raw.hard7,
//            R.raw.hard8, R.raw.hard9, R.raw.hard10};
//
//    private static final int[] EXTREME_FILES_IDS = new int[]{R.raw.extreme1, R.raw.extreme2,
//            R.raw.extreme3, R.raw.extreme4, R.raw.extreme5,
//            R.raw.extreme6, R.raw.extreme7, R.raw.extreme8, R.raw.extreme9, R.raw.extreme10};


    private final GamesHistoryRepository mGamesHistoryRepo;
    private KoteGame currentGame;

    public KoteGameViewModel(@NonNull Application context) {
        super(context);
        mGamesHistoryRepo = GamesHistoryRepository.getInstance(context);
    }

    /**
     * Creating a new game if no game has started yet.
     *
     * @param diff - the Difficulty.
     */
    void initiateGame(int diff) {
        if (currentGame == null) {
            int stringArrId;
            switch (diff) {
                case KoteGame.HARD_DIFFICULTY:
                    stringArrId = R.array.hard_musical_parts;
                    break;

                case KoteGame.EXTREME_DIFFICULTY:
                    stringArrId = R.array.extreme_musical_parts;
                    break;

                default:
                    stringArrId = R.array.easy_musical_parts;
            }

            String[] stringArr = getApplication().getResources().getStringArray(stringArrId);
            currentGame = new KoteGame(diff, stringArr);
        }
    }

    KoteGame getGame() {
        if (currentGame == null)
            initiateGame(KoteGame.EASY_DIFFICULTY);
        return currentGame;
    }

    void playSample() {
        currentGame.addSamplePlay();
    }

    /**
     * Creating a new game with the same difficulty.
     */
    void restartGame() {
        if (currentGame != null) {
            int diff = currentGame.getDifficulty();
            currentGame = null;
            initiateGame(diff);
        } else
            initiateGame(KoteGame.EASY_DIFFICULTY);
    }

    MutableLiveData<Integer> getHighScore(int difficulty) {
        return mGamesHistoryRepo.getHighScore(difficulty);
    }

    void saveGameToDB() {
        mGamesHistoryRepo.saveResultToDB(currentGame.getGameResult());
    }

    void submitLeaderboardScore() {
        mGamesHistoryRepo.uploadLeaderboardScore(currentGame.getDifficulty(),
                currentGame.getTotalScore());
    }

    LiveData<Integer> getPlaysLeft() {
        return currentGame.samplePlayesLeft();
    }

}

package dar.games.music.capstonekote.ui.mainmenu;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.List;

import dar.games.music.capstonekote.repository.GameResultModel;
import dar.games.music.capstonekote.repository.GamesHistoryRepository;

public class MainMenuViewModel extends AndroidViewModel {

    private final GamesHistoryRepository mGamesHistoryRepository;

    public MainMenuViewModel(@NonNull Application application) {
        super(application);

        mGamesHistoryRepository = GamesHistoryRepository.getInstance(application);
    }

    LiveData<List<GameResultModel>> getLastResults() {
        return mGamesHistoryRepository.resultsHistory();
    }

    MutableLiveData<Integer> getHighScore(int difficulty) {
        return mGamesHistoryRepository.getHighScore(difficulty);
    }

    void updateDiff(int difficulty) {
        mGamesHistoryRepository.updateHighscoreForDifficulty(difficulty);
    }

    void setAccount(GoogleSignInAccount signInAccount) {
        mGamesHistoryRepository.setAccount(signInAccount);
    }

    GoogleSignInAccount getGoogleAccount() {
        return mGamesHistoryRepository.getGoogleAccount();
    }

    MutableLiveData<String> getPlayerName() {
        return mGamesHistoryRepository.getPlayerName();
    }
}

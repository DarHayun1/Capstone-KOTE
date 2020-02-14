package dar.games.music.capstonekote.repository;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.List;

import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.gamelogic.KoteGame;
import dar.games.music.capstonekote.utils.AppExecutors;

import static com.google.android.gms.games.leaderboard.LeaderboardVariant.COLLECTION_PUBLIC;
import static com.google.android.gms.games.leaderboard.LeaderboardVariant.TIME_SPAN_ALL_TIME;

public class GamesHistoryRepository {

    private static final String ACCOUNT_STATUS_KEY = "account_status_key";
    private static final String GAME_ENDED_EVENT = "game_ended";
    private static GamesHistoryRepository sInstance;

    private final KoteDao mDao;
    private final Context mAppContext;
    private LiveData<List<GameResultModel>> gameResults;
    private MutableLiveData<Integer> mEasyHighscore;
    private MutableLiveData<Integer> mHardHighscore;
    private MutableLiveData<Integer> mExtremeHighscore;
    private MutableLiveData<String> playerName;
    private GoogleSignInAccount mGoogleAccount;
    private boolean mAppStarted;

    private GamesHistoryRepository(Application application) {

        mAppContext = application.getApplicationContext();
        KoteDatabase db = KoteDatabase.getInstance(mAppContext);

        mEasyHighscore = new MutableLiveData<>();
        mEasyHighscore.setValue(0);
        mHardHighscore = new MutableLiveData<>();
        mHardHighscore.setValue(0);
        mExtremeHighscore = new MutableLiveData<>();
        mExtremeHighscore.setValue(0);

        mDao = db.dao();

        //Connecting the device highscore to the highscore from the leaderboard
        mDao.getEasyHighScore()
                .observeForever(score -> {
                    if (score != null && mEasyHighscore.getValue() != null) {
                        if (score > mEasyHighscore.getValue())
                            mEasyHighscore.postValue(score);
                    }

                });
        mDao.getHardHighScore()
                .observeForever(score -> {
                    if (score != null && mHardHighscore.getValue() != null) {
                        if (score > mHardHighscore.getValue())
                            mHardHighscore.postValue(score);
                    }

                });
        mDao.getExtremeHighScore()
                .observeForever(score -> {
                    if (score != null && mExtremeHighscore.getValue() != null) {
                        if (score > mExtremeHighscore.getValue())
                            mExtremeHighscore.postValue(score);
                    }
                });
        playerName = new MutableLiveData<>();
        playerName.setValue(mAppContext.getString(R.string.guest_name));
    }

    public static GamesHistoryRepository getInstance(Application application) {
        if (sInstance == null) {
            synchronized (GamesHistoryRepository.class) {
                if (sInstance == null)
                    sInstance = new GamesHistoryRepository(application);
            }
        }
        return sInstance;
    }

    private void updateGoogleGamesHighscore(int difficulty) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mAppContext);
        if (GoogleSignIn.hasPermissions(account, Games.SCOPE_GAMES_LITE)) {
            LeaderboardsClient leaderboardsClient =
                    Games.getLeaderboardsClient(mAppContext, account);
            leaderboardsClient.loadCurrentPlayerLeaderboardScore(getLeaderboardId(difficulty),
                    TIME_SPAN_ALL_TIME, COLLECTION_PUBLIC)
                    .addOnSuccessListener(leaderboardData -> {
                        MutableLiveData<Integer> highScoreObject = getHighScoreObject(difficulty);
                        if (leaderboardData.get() != null
                                && highScoreObject.getValue() < leaderboardData.get().getRawScore())

                            highScoreObject.postValue((int) leaderboardData.get()
                                    .getRawScore());
                    });
        }
    }

    private MutableLiveData<Integer> getHighScoreObject(int difficulty) {
        switch (difficulty) {
            case KoteGame.HARD_DIFFICULTY:
                return mHardHighscore;
            case KoteGame.EXTREME_DIFFICULTY:
                return mExtremeHighscore;
            default:
                return mEasyHighscore;
        }
    }

    public LiveData<List<GameResultModel>> resultsHistory() {
        if (gameResults == null)
            gameResults = mDao.getAllGames();
        return gameResults;
    }

    public MutableLiveData<Integer> getHighScore(int difficulty) {
        updateGoogleGamesHighscore(difficulty);
        return getHighScoreObject(difficulty);
    }

    public void saveResultToDB(GameResultModel gameResult) {
        AppExecutors.getInstance().diskIO().execute(() -> mDao.insertGame(gameResult));
    }

    public void uploadLeaderboardScore(int difficulty, int score) {
        String leaderboardId = getLeaderboardId(difficulty);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mAppContext);
        Bundle bundle = new Bundle();
        if (GoogleSignIn.hasPermissions(account, Games.SCOPE_GAMES_LITE)) {
            bundle.putString(ACCOUNT_STATUS_KEY, "Connected");
            AppExecutors.getInstance().networkIO().execute(() ->
                    Games.getLeaderboardsClient(mAppContext, account)
                            .submitScore(leaderboardId, score));
        } else
            bundle.putString(ACCOUNT_STATUS_KEY, "Disconnected");

        FirebaseAnalytics.getInstance(mAppContext).logEvent(GAME_ENDED_EVENT, bundle);
    }

    private String getLeaderboardId(int difficulty) {
        switch (difficulty) {
            case KoteGame.HARD_DIFFICULTY:
                return mAppContext.getString(R.string.leaderboard_hard);
            case KoteGame.EXTREME_DIFFICULTY:
                return mAppContext.getString(R.string.leaderboard_extreme);
            default:
                return mAppContext.getString(R.string.leaderboard_easy);
        }
    }

    public void updateHighscoreForDifficulty(int difficulty) {
        updateGoogleGamesHighscore(difficulty);
    }

    public void setAccount(@Nullable GoogleSignInAccount account) {
        mGoogleAccount = account;
        accountUpdated();

    }

    private void accountUpdated() {
        if (mGoogleAccount != null)
            playerName.postValue(mGoogleAccount.getDisplayName());
        else
            playerName.setValue(mAppContext.getString(R.string.guest_name));
    }

    public GoogleSignInAccount getGoogleAccount() {
        return mGoogleAccount;
    }

    public MutableLiveData<String> getPlayerName() {
        if (mGoogleAccount == null) {
            playerName.setValue(mAppContext.getString(R.string.guest_name));
            return playerName;
        } else {
            playerName.setValue(mGoogleAccount.getDisplayName());
            return playerName;
        }
    }

    public boolean appStarted() {
        if (mAppStarted) {
            return true;
        }
        mAppStarted = true;
        return false;
    }
}

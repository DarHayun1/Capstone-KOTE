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

/**
 * The app's repository, responsible of providing the correct synchronized data from the DB and
 * Google Play Games server.
 */
public class GamesHistoryRepository {

    private static final String ACCOUNT_STATUS_KEY = "account_status_key";
    private static final String GAME_ENDED_EVENT = "game_ended";

    //Holding the one and only instance of the repository.
    private static GamesHistoryRepository sInstance;

    // *****************
    // Member variables
    // *****************
    private final KoteDao mDao;
    private final Context mAppContext;
    private LiveData<List<GameResultModel>> gameResults;
    private MutableLiveData<Integer> mEasyHighscore;
    private MutableLiveData<Integer> mHardHighscore;
    private MutableLiveData<Integer> mExtremeHighscore;
    private MutableLiveData<String> playerName;
    private GoogleSignInAccount mGoogleAccount;
    private boolean mAppStarted;

    /**
     * A private constructor called by the newInstance method.
     * Creating all of the Class variables.
     *
     * @param application
     */
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

    /**
     * Creating an instance only once in an app lifetime. performed in a sync way.
     *
     * @param application = the App reference.
     * @return The repository singleton instance.
     */
    public static GamesHistoryRepository getInstance(Application application) {
        if (sInstance == null) {
            synchronized (GamesHistoryRepository.class) {
                if (sInstance == null)
                    sInstance = new GamesHistoryRepository(application);
            }
        }
        return sInstance;
    }

    /**
     * Updating the Google Play Games highscore
     *
     * @param difficulty
     */
    public void updateGoogleGamesHighscore(int difficulty) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mAppContext);
        //Checking for the needed permission
        if (GoogleSignIn.hasPermissions(account, Games.SCOPE_GAMES_LITE)) {
            LeaderboardsClient leaderboardsClient =
                    Games.getLeaderboardsClient(mAppContext, account);

            leaderboardsClient.loadCurrentPlayerLeaderboardScore(getLeaderboardId(difficulty),
                    TIME_SPAN_ALL_TIME, COLLECTION_PUBLIC)
                    .addOnSuccessListener(leaderboardData ->
                    {
                        MutableLiveData<Integer> highScoreObject = getHighScoreObject(difficulty);
                        if (leaderboardData.get() != null
                                && highScoreObject.getValue() < leaderboardData.get().getRawScore()) {
                            highScoreObject.postValue((int) leaderboardData.get()
                                    .getRawScore());
                        }
                    });
        }
    }

    /**
     * Return the Highscore object matching the specified difficulty.
     *
     * @param difficulty - the Highscore difficulty.
     * @return the Highscore object matching the specified difficulty
     */
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

    /**
     * Return the LiveData highscore object and refreshing the Google score.
     *
     * @param difficulty - The requested difficulty.
     * @return a LiveData highscore object.
     */
    public MutableLiveData<Integer> getHighScore(int difficulty) {
        updateGoogleGamesHighscore(difficulty);
        return getHighScoreObject(difficulty);
    }

    /**
     * Saving a new GameResult to the database on a different thread
     *
     * @param gameResult - The game data.
     */
    public void saveResultToDB(GameResultModel gameResult) {
        AppExecutors.getInstance().diskIO().execute(() -> mDao.insertGame(gameResult));
    }

    /**
     * Posting a new score the the google play games leaderboard (If not a highscore
     * the server will ignore the score).
     *
     * @param difficulty - Define the correct leaderboard for the score.
     * @param score      - The posted score.
     */
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

    public void setAccount(@Nullable GoogleSignInAccount account) {
        mGoogleAccount = account;
        accountUpdated();

    }

    /**
     * Updating the player's name.
     */
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
        accountUpdated();
        return playerName;
    }

    /**
     * Return true for the first time called only.
     *
     * @return true for the first time called only.
     */
    public boolean isAppStarted() {
        if (mAppStarted) {
            return true;
        }
        mAppStarted = true;
        return false;
    }
}

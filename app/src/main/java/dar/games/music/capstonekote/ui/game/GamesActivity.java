package dar.games.music.capstonekote.ui.game;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.gamelogic.KoteGame;
import dar.games.music.capstonekote.ui.mainmenu.MainActivity;

/**
 * The activity where the games are created and played.
 * Using the KoteGameViewModel managing games, changing the relevant fragments (Round, Result
 * and EndGame) corresponding to game events.
 */
public class GamesActivity extends AppCompatActivity implements OnGameFragInteractionListener {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean recordPermissionGranted = false;

    private KoteGameViewModel mViewModel;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            recordPermissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!recordPermissionGranted) finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.games_activity);

        mViewModel = new ViewModelProvider(this).get(KoteGameViewModel.class);
        mViewModel.initiateGame(getIntent().getIntExtra(MainActivity.DIFFICULTY_EXTRA,
                KoteGame.EASY_DIFFICULTY));

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new KoteRoundFragment())
                    .commitNow();
        }
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRoundFinished() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, KoteResultFragment.newInstance())
                .commitNow();
    }

    @Override
    public void onReadyClicked() {

        if (mViewModel.getGame().nextRound() == KoteGame.GAME_ENDED) {
            mViewModel.saveGameToDB();
            mViewModel.submitLeaderboardScore();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new EndGameFragment())
                    .commitNow();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, KoteRoundFragment.newInstance())
                    .commitNow();
        }
    }

    @Override
    public void onPlayAgain() {
        mViewModel.restartGame();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new KoteRoundFragment())
                .commitNow();
    }

    @Override
    public void onMainMenu() {
        finish();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getResources().getString(R.string.quit_game_dialog_title))
                .setMessage(getResources().getString(R.string.quit_game_dialog_message))
                .setPositiveButton(getResources().getString(R.string.quit_game_dialog_yes),
                        (dialog, which) -> finish())
                .setNegativeButton(getResources().getString(R.string.quit_game_dialog_not),
                        null)
                .show();
    }
}

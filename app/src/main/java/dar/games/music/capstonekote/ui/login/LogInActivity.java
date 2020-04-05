package dar.games.music.capstonekote.ui.login;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.ui.mainmenu.MainActivity;
import dar.games.music.capstonekote.utils.OnPbFinishedListener;
import dar.games.music.capstonekote.utils.ProgressBarAsyncTask;

/**
 * Launcher activity for logging in to google play games.
 */
public class LogInActivity extends AppCompatActivity implements OnPbFinishedListener {

    // ****************
    // Class constants
    // ****************

    public static final int RC_SIGN_IN = 303;
    public static final String LOGIN_RESULT_KEY = "login_result_key";
    public static final String LOG_IN_EVENT = "log_in_attempt";

    // *******
    // Views
    // *******

    @BindView(R.id.skip_log_in_btn)
    View skipButton;
    @BindView(R.id.sign_in_btn)
    View signInButton;
    @BindView(R.id.login_top_drawer)
    View topDrawer;
    @BindView(R.id.login_bottom_drawer)
    View bottomDrawer;
    @BindView(R.id.login_pb)
    ProgressBar loginProgressBar;

    // ****************
    // Member variables
    // ****************
    private GoogleSignInClient signInClient;
    private boolean silentAttempted = false;
    private Unbinder mUnbinder;
    private LogInViewModel logInViewModel;
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean shouldContinueToMainActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mUnbinder = ButterKnife.bind(this);
        logInViewModel = new ViewModelProvider(this).get(LogInViewModel.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Running a progressBar when the app runs for the first time.
        showLoader();
        continueToMainActivityIfNeeded();
    }

    private void continueToMainActivityIfNeeded() {
        if (shouldContinueToMainActivity) {
            shouldContinueToMainActivity = false;
            continueToMainActivity();
        }
    }

    private void showLoader() {
        if (!logInViewModel.appStarted()) {
            signInButton.setVisibility(View.INVISIBLE);
            skipButton.setVisibility(View.INVISIBLE);
            loginProgressBar.setVisibility(View.VISIBLE);
            new ProgressBarAsyncTask(this).execute();
        } else {
            trySilent();
        }
    }

    private void trySilent() {
        if (!silentAttempted) {

            GoogleSignInOptions signInOptions = new GoogleSignInOptions
                    .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(Games.SCOPE_GAMES_LITE)
                    .requestEmail()
                    .build();
            signInClient = GoogleSignIn.getClient(this, signInOptions);
            signInSilently();
            silentAttempted = true;
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    /**
     * Trying to sign-in without user interaction.
     */
    private void signInSilently() {
        GoogleSignInAccount mGoogleAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(mGoogleAccount, Games.SCOPE_GAMES_LITE)) {

            logInViewModel.setAccount(mGoogleAccount);
            continueToMainActivity();
        } else {

            Task<GoogleSignInAccount> silentSignInTask = signInClient.silentSignIn();
            silentSignInTask.addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {

                    logInViewModel.setAccount(task.getResult());
                    continueToMainActivity();
                } else {

                    googleSignIn();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Getting a result from the Google Sign In intent
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Bundle bundle = new Bundle();
            if (result.isSuccess()) {
                //Logging a FireBase Analytics successful connection event.
                bundle.putString(LOGIN_RESULT_KEY, getResources().getString(R.string.log_success));
                mFirebaseAnalytics.logEvent(LOG_IN_EVENT, bundle);
                logInViewModel.setAccount(result.getSignInAccount());
                shouldContinueToMainActivity = true;
            } else {
                //Logging a FireBase Analytics unsuccessful connection event.
                bundle.putString(LOGIN_RESULT_KEY, getResources().getString(R.string.log_failure));
                mFirebaseAnalytics.logEvent(LOG_IN_EVENT, bundle);
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {

                    message = getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(this).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }

    /**
     * Continue with the account set to null.
     */
    @OnClick(R.id.skip_log_in_btn)
    void skipSignIn() {
        logInViewModel.setAccount(null);
        continueToMainActivity();
    }

    private void continueToMainActivity() {
        setTheme(R.style.AppTheme);
        // Drawers shared elements transition
        final String bottomDrawerTrans = getString(R.string.bottom_drawer_transition_name);
        final String topDrawerTrans = getString(R.string.top_drawer_transition_name);
        final ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                this,
                Pair.create(bottomDrawer, bottomDrawerTrans),
                Pair.create(topDrawer, topDrawerTrans));
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent, options.toBundle());
        // Slide animation with the MainActivity
        overridePendingTransition(R.anim.fragment_fade_enter, R.anim.fragment_fade_exit);
    }

    @OnClick(R.id.sign_in_btn)
    void googleSignIn() {
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }

    /**
     * Progress bar finished
     */
    @Override
    public void onPbFinished() {
        if (loginProgressBar != null)
            loginProgressBar.setVisibility(View.INVISIBLE);
        if (signInButton != null)
            signInButton.setVisibility(View.VISIBLE);
        if (skipButton != null)
            skipButton.setVisibility(View.VISIBLE);
        trySilent();
    }
}

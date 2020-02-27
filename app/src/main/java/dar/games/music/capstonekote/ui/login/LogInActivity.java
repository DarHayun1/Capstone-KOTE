package dar.games.music.capstonekote.ui.login;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mUnbinder = ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        logInViewModel = new ViewModelProvider(this).get(LogInViewModel.class);
        // Running a progressBar when the app runs for the first time.
        if (!logInViewModel.appStarted()) {
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
                continueToMainActivity();
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
        startActivity(new Intent(this, MainActivity.class));
        // Slide animation with the MainActivity
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @OnClick(R.id.sign_in_btn)
    void googleSignIn() {
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
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

package dar.games.music.capstonekote.ui.mainmenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.WidgetUpdateService;
import dar.games.music.capstonekote.gamelogic.KoteGame;
import dar.games.music.capstonekote.ui.customviews.LabelAndDataView;
import dar.games.music.capstonekote.ui.game.GamesActivity;

import static dar.games.music.capstonekote.ui.login.LogInActivity.LOGIN_RESULT_KEY;
import static dar.games.music.capstonekote.ui.login.LogInActivity.LOG_IN_EVENT;
import static dar.games.music.capstonekote.ui.login.LogInActivity.RC_SIGN_IN;
import static dar.games.music.capstonekote.ui.mainmenu.MainActivity.DIFFICULTY_EXTRA;

/**
 * The main screen fragment
 */
public class MainMenuFragment extends Fragment {

    private static final int RC_LEADERBOARD_UI = 302;
    private static final int RC_ACCOUNT_DISCONNECTED = 10001;

    // *****************
    // Member variables
    // *****************
    private FirebaseAnalytics mFirebaseAnalytics;

    private MainMenuViewModel mViewModel;
    private int mDifficulty;
    private SharedPreferences mPreferences;
    private GameResultsAdapter mResultsAdapter;
    private Context mContext;
    private Unbinder mUnbinder;
    private int shortAnimationDuration;


    // *****************
    // Views
    // *****************
    @BindView(R.id.diff_name_tv)
    TextView diffNameTv;
    @BindView(R.id.instructions_view)
    View instructionsLayout;
    @BindView(R.id.last_games_view)
    View lastGamesLayout;
    @BindView(R.id.results_rv)
    RecyclerView lastGamesRv;
    @BindView(R.id.no_last_games_tv)
    TextView noLastGamesTv;
    @BindView(R.id.player_name_tv)
    TextView playerNameTv;
    @BindView(R.id.player_icon_iv)
    ImageView playerIconIv;
    @BindView(R.id.mainmenu_top_drawer)
    View topDrawer;
    @BindView(R.id.mainmenu_bottom_drawer)
    View bottomDrawer;
    @BindView(R.id.highscore_ld)
    LabelAndDataView highscoreLdV;
    @BindView(R.id.diff_vp)
    ViewPager2 difficultiesViewpager;
    private DiffVpAdapter vpAdapter;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;
    private Integer tempWidgetHighscore;

    public static MainMenuFragment newInstance() {
        return new MainMenuFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.main_menu_fragment, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupViews();
    }

    private void setupViews() {
        mPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (mPreferences.contains(getString(R.string.saved_difficulty_key))) {
            mDifficulty = mPreferences.getInt(getString(R.string.saved_difficulty_key), 0);
        } else
            mDifficulty = KoteGame.EASY_DIFFICULTY;
        vpAdapter = new DiffVpAdapter(mContext);
        difficultiesViewpager.setAdapter(vpAdapter);
        displayDifficulty();
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                difficultyChanged(position);
            }
        };
        difficultiesViewpager.registerOnPageChangeCallback(pageChangeCallback);

        mViewModel = new ViewModelProvider(this).get(MainMenuViewModel.class);

        mResultsAdapter = new GameResultsAdapter(mContext);

        mViewModel.getHighScore(mDifficulty).observe(getViewLifecycleOwner(), score ->
                highscoreLdV.setValue(score));

        mViewModel.getLastResults().observe(getViewLifecycleOwner(), gameResults -> {
            if (gameResults != null) {
                noLastGamesTv.setVisibility(View.INVISIBLE);
                mResultsAdapter.setResultsData(gameResults);
            } else
                noLastGamesTv.setVisibility(View.VISIBLE);
        });


        lastGamesRv.setAdapter(mResultsAdapter);
        LinearLayoutManager lastGamesLM = new LinearLayoutManager(mContext, RecyclerView.VERTICAL,
                false);
        lastGamesRv.setLayoutManager(lastGamesLM);
        lastGamesRv.addItemDecoration(
                new DividerItemDecoration(getActivity().getApplicationContext(),
                        DividerItemDecoration.VERTICAL));

        mViewModel.getPlayerName().observe(getViewLifecycleOwner(), playerName -> {
            String welcomeText = getResources().getString(R.string.welcome_base_text)
                    + " " + playerName;
            playerNameTv.setText(welcomeText);
        });

        mViewModel.getPlayerIcon().observe(getViewLifecycleOwner(), iconUrl ->
                Picasso.with(mContext).load(iconUrl).into(playerIconIv, new Callback() {
                    @Override
                    public void onSuccess() {
                        playerIconIv.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        playerIconIv.setVisibility(View.GONE);
                    }
                }));
    }

    @OnClick(R.id.start_game_btn)
    void startGameActivity() {
        WidgetUpdateService
                .startActionUpdateHighscoreWidget(mContext, mDifficulty, tempWidgetHighscore);
        Intent intent = new Intent(getActivity(), GamesActivity.class);
        intent.putExtra(DIFFICULTY_EXTRA, mDifficulty);
        startActivity(intent);
    }

    @OnClick(R.id.last_games_btn)
    void showLastGames() {
        fadeInViews(lastGamesLayout);
    }

    @OnClick(R.id.close_last_games_btn)
    void closeLastGames() {
        fadeOutViews(lastGamesLayout);
    }

    @OnClick(R.id.instruction_btn)
    void showInstructions() {
        fadeInViews(instructionsLayout);
    }

    @OnClick(R.id.close_instructions_btn)
    void closeInstructions() {
        fadeOutViews(instructionsLayout);
    }

    private void fadeInViews(View... views) {
        Arrays.stream(views).forEach(view -> {
            view.setAlpha(0);
            view.setVisibility(View.VISIBLE);
            view.animate().alpha(1f).setDuration(shortAnimationDuration).setListener(null);
        });
    }

    private void fadeOutViews(View... views) {
        Arrays.stream(views).forEach(view -> view.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.INVISIBLE);
                    }
                })
        );
    }

    @OnClick(R.id.leaderboard_btn)
    void showLeaderboard() {
        String leaderboardId;
        switch (mDifficulty) {
            case KoteGame.HARD_DIFFICULTY:
                leaderboardId = getString(R.string.leaderboard_hard);
                break;
            case KoteGame.EXTREME_DIFFICULTY:
                leaderboardId = getString(R.string.leaderboard_extreme);
                break;
            default:
                leaderboardId = getString(R.string.leaderboard_easy);
                break;
        }
        if (mViewModel.getGoogleAccount() != null) {
            Games.getLeaderboardsClient(mContext, mViewModel.getGoogleAccount())
                    .getLeaderboardIntent(leaderboardId)
                    .addOnSuccessListener(intent ->
                            startActivityForResult(intent, RC_LEADERBOARD_UI))
                    .addOnFailureListener(e -> Toast.makeText(mContext,
                            getString(R.string.gms_connection_problem),
                            Toast.LENGTH_SHORT).show());
        } else {
            new AlertDialog.Builder(mContext)
                    //Add google games icon
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(getString(R.string.not_connected_title))
                    .setMessage(getString(R.string.not_connected_message))
                    .setPositiveButton(getString(R.string.no_connection_dialog_yes), (dialogInterface, i) -> googleSignIn())
                    .setNegativeButton(getString(R.string.no_connection_dialog_no), null)
                    .show();
        }
    }

    /**
     * Starting a google sign-in intent
     */
    private void googleSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Games.SCOPE_GAMES_LITE)
                .requestEmail()
                .build();
        GoogleSignInClient signInClient = GoogleSignIn.getClient(mContext, signInOptions);
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Bundle bundle = new Bundle();
            if (result.isSuccess()) {
                //Logging a FireBase Analytics successful connection event.
                bundle.putString(LOGIN_RESULT_KEY, getResources().getString(R.string.log_success));
                mFirebaseAnalytics.logEvent(LOG_IN_EVENT, bundle);
                mViewModel.setAccount(result.getSignInAccount());
                mViewModel.updateDiff(mDifficulty);
                showLeaderboard();
            } else {
                //Logging a FireBase Analytics unsuccessful connection event.
                bundle.putString(LOGIN_RESULT_KEY, getResources().getString(R.string.log_failure));
                mFirebaseAnalytics.logEvent(LOG_IN_EVENT, bundle);
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }
                new AlertDialog.Builder(mContext).setMessage(message)
                        .setNeutralButton(android.R.string.ok, null).show();
                mViewModel.setAccount(null);
            }
        }
        //If the user disconnected from google play games inside the leaderboard screen.
        if (requestCode == RC_LEADERBOARD_UI) {
            if (resultCode == RC_ACCOUNT_DISCONNECTED) {
                mViewModel.setAccount(null);
            }
        }
    }

    @OnClick(R.id.prev_diff_ib)
    void prevDifficulty() {
        changeDifficulty(-1);
    }

    @OnClick(R.id.next_diff_ib)
    void nextDifficulty() {
        changeDifficulty(1);
    }


    private void changeDifficulty(int delta) {
        mDifficulty += delta;
        if (mDifficulty < 0)
            mDifficulty = 3 - (Math.abs(mDifficulty) % 3);
        if (mDifficulty > 2)
            mDifficulty = mDifficulty % 3;
        difficultiesViewpager.setCurrentItem(mDifficulty, true);

        mPreferences.edit()
                .putInt(getString(R.string.saved_difficulty_key), mDifficulty)
                .apply();
        mViewModel.updateDiff(mDifficulty);


    }

    private void difficultyChanged(int position) {
        mViewModel.getHighScore(mDifficulty).removeObservers(this);
        mDifficulty = position;
        mViewModel.getHighScore(mDifficulty).observe(getViewLifecycleOwner(), score ->
        {
            highscoreLdV.setValue(score);
            tempWidgetHighscore = score;
        });
        displayDifficulty();

    }


    private void displayDifficulty() {
        switch (mDifficulty) {
            case 1:
                diffNameTv.setText(getString(R.string.hard_diff));
                break;

            case 2:
                diffNameTv.setText(getString(R.string.extreme_diff));
                break;

            default:
                diffNameTv.setText(getString(R.string.easy_diff));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        difficultiesViewpager.unregisterOnPageChangeCallback(pageChangeCallback);
        mUnbinder.unbind();

    }
}

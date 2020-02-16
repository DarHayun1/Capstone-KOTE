package dar.games.music.capstonekote.ui.game;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dar.games.music.capstonekote.R;

/**
 * A fragment displaying the finished game summary.
 */
public class EndGameFragment extends Fragment {

    private KoteGameViewModel mViewModel;
    private Unbinder mUnbinder;

    @BindView(R.id.final_result_tv)
    TextView finalResultTv;
    @BindView(R.id.final_result_highscore_tv)
    TextView highScoreTv;
    private OnGameFragInteractionListener mCallback;

    public EndGameFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnGameFragInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnRoundFinishedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_end_game, container, false);

        mUnbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = new ViewModelProvider(getActivity()).get(KoteGameViewModel.class);

        mViewModel.getHighScore(mViewModel.getGame().getDifficulty())
                .observe(getViewLifecycleOwner(), score -> {
            if (score > mViewModel.getGame().getTotalScore()) {
                String highscoreText = getResources().getString(R.string.final_score_highscore)
                       + " " + score;
                highScoreTv.setText(highscoreText);
            }
        });
        finalResultTv.setText(String.valueOf(mViewModel.getGame().getTotalScore()));
    }

    @OnClick(R.id.play_again_btn)
    void onPlayAgainClicked(){
        mCallback.onPlayAgain();
    }

    @OnClick(R.id.main_menu_btn)
    void onMainMenuClicked(){
        mCallback.onMainMenu();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();

    }
}

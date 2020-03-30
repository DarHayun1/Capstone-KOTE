package dar.games.music.capstonekote.ui.game;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.ui.customviews.KoteButton;
import dar.games.music.capstonekote.ui.customviews.LabelAndDataView;

/**
 * A fragment displaying the finished round score.
 */
public class KoteResultFragment extends Fragment {

    private OnGameFragInteractionListener mCallback;
    private Unbinder mUnbinder;

    // ********
    // Views
    // ********
    @BindView(R.id.round_container)
    LabelAndDataView roundLad;
    @BindView(R.id.result_container)
    LabelAndDataView resultLad;
    @BindView(R.id.ready_button)
    KoteButton readyBtn;


    private KoteResultFragment() {
        // Required empty public constructor
    }

    static KoteResultFragment newInstance() {
//        Not necessary at the moment
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
        return new KoteResultFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        View rootView = inflater.inflate(R.layout.fragment_kote_result, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        KoteGameViewModel gameViewModel =
                new ViewModelProvider(getActivity()).get(KoteGameViewModel.class);

        int score = gameViewModel.getGame().getCurrentScore();
        String scoreText = score + "/100";
        resultLad.setStringData(scoreText);

        String rdyBtnText;
        if (gameViewModel.getGame().hasNextRound()) {
            rdyBtnText = getResources().getString(R.string.ready_btn_label_text) + " "
                    + (gameViewModel.getGame().getRound() + 1);
        } else {
            rdyBtnText = getResources().getString(R.string.finish);

        }
        readyBtn.setText(rdyBtnText);
    }

    @OnClick(R.id.ready_button)
    void onReadyClicked() {
        mCallback.onReadyClicked();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}

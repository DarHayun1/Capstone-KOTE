package dar.games.music.capstonekote.ui.game;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchProcessor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.gamelogic.KoteGame;
import dar.games.music.capstonekote.gamelogic.Note;
import dar.games.music.capstonekote.gamelogic.NoteArrayList;
import dar.games.music.capstonekote.ui.customviews.KoteButton;
import dar.games.music.capstonekote.ui.customviews.LabelAndDataView;
import dar.games.music.capstonekote.ui.customviews.PianoView;
import dar.games.music.capstonekote.utils.AppExecutors;

/**
 * A fragment displaying the a round of the game.
 * Playing the melodies, the first note and recording the player's attempt
 */
public class KoteRoundFragment extends Fragment {

    // *****************
    // Constants
    // *****************
    private static final String RECORD_INTERRUPTED_KEY = "record_interrupted_key";
    private static final String EXTRA_PLAYER_POS = "extra_player_pos";

    // *****************
    // Member variables
    // *****************
    private MediaPlayer mMediaPlayer;
    private NoteArrayList mPlayerAttemptArr;
    private AudioDispatcher mDispatcher;
    private int mPlayerPos = 0;

    private Context mContext;
    private OnGameFragInteractionListener mCallback;
    private int shortAnimationDuration;

    private KoteGameViewModel mGameViewModel;

    private boolean isRecording = false;
    private boolean isPlayingSample;
    private Handler mSeekbarUpdateHandler = new Handler();
    private Runnable mUpdateSeekbar = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null) {
                seekBar.setProgress(mMediaPlayer.getCurrentPosition());
                mSeekbarUpdateHandler.postDelayed(this, 50);
            } else {
                mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
            }

        }
    };
    private Timer recordTimer;
    private boolean recordInterrupted = false;
    private boolean playInterrupted = false;
    private int mPlaysLeft;

    private Unbinder mUnbinder;

    // *****************
    // Views
    // *****************
    @BindView(R.id.note_name_tv)
    TextView mNoteNameTv;
    @BindView(R.id.seek_bar)
    SeekBar seekBar;
    @BindView(R.id.play_sample_btn)
    View playSampleBtn;
    @BindView(R.id.record_button)
    KoteButton recordBtnKb;
    @BindView(R.id.round_container)
    LabelAndDataView roundView;
    @BindView(R.id.score_container)
    LabelAndDataView scoreView;
    @BindView(R.id.plays_left_icon)
    View playsLeftContainer;
    @BindView(R.id.plays_left_tv)
    TextView playsLeftTv;
    @BindView(R.id.game_piano)
    PianoView pianoView;
    @BindView(R.id.major_key_lad)
    LabelAndDataView majorKeyLad;
    @BindView(R.id.first_note_btn)
    View firstNoteButton;

    @BindView(R.id.close_scales_iv)
    View closeScalesBtn;
    @BindView(R.id.scale_layout)
    View scaleLayout;
    @BindView(R.id.scale_viewpager)
    ViewPager2 scaleVp;
    @BindView(R.id.note_num_1)
    TextView noteNameNum1;
    @BindView(R.id.note_num_2)
    TextView noteNameNum2;
    @BindView(R.id.note_num_3)
    TextView noteNameNum3;
    @BindView(R.id.note_num_4)
    TextView noteNameNum4;
    @BindView(R.id.note_num_5)
    TextView noteNameNum5;
    @BindView(R.id.note_num_6)
    TextView noteNameNum6;
    @BindView(R.id.note_num_7)
    TextView noteNameNum7;

    static KoteRoundFragment newInstance() {

//        Bundle args = new Bundle();
        KoteRoundFragment fragment = new KoteRoundFragment();
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        try {
            mCallback = (OnGameFragInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnRoundFinishedListener");
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_kote_round, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        isPlayingSample = false;
        shortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        seekBar.setOnTouchListener((v, event) -> true);
        mPlayerAttemptArr = new NoteArrayList(4000);

        /* If the app stopped while recording
         */
        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(RECORD_INTERRUPTED_KEY, false))
                Toast.makeText(mContext,
                        getResources().getString(R.string.record_interrupted_message),
                        Toast.LENGTH_LONG).show();
            mPlayerPos = savedInstanceState.getInt(EXTRA_PLAYER_POS);

        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (recordInterrupted)
            outState.putBoolean(RECORD_INTERRUPTED_KEY, true);
        if (playInterrupted)
            outState.putInt(EXTRA_PLAYER_POS, mPlayerPos);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGameViewModel = new ViewModelProvider(getActivity()).get(KoteGameViewModel.class);
        setupViews();
    }

    private void setupViews() {
        roundView.setData(getString(R.string.round), mGameViewModel.getGame().getRound());
        scoreView.setData(getString(R.string.score_label_text),
                mGameViewModel.getGame().getTotalScore());
        majorKeyLad.setStringData(mGameViewModel.getGame().getCurrentSample().get(0).getName());
        //Updating every time the game plays the melody.
        mGameViewModel.getPlaysLeft().observe(getViewLifecycleOwner(), numOfPlays -> {
            mPlaysLeft = numOfPlays;
            playsLeftTv.setText(String.valueOf(mPlaysLeft));
        });

        if (mPlayerPos != 0) {
            releaseMediaPlayer();
            playSample();
        }

        setupScales();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick(R.id.first_note_btn)
    void playFirstNote() {
        if (!isPlayingSample) {
            if (mGameViewModel.getGame().getCurrentSample() == null ||
                    mGameViewModel.getGame().getCurrentSample().size() < 1)
                return;

            Note firstNote = mGameViewModel.getGame().getCurrentSample().get(0);

            //Playing the first note with the MediaPlayer.
            releaseMediaPlayer();
            mMediaPlayer = MediaPlayer.create(mContext, firstNote.getSoundRes());
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                pianoView.clearHighlight();
            });
            pianoView.highlightNote(firstNote);
        }
    }

    /**
     * Start to dispatch sound from the microphone and record the data to the mPlayerAttemptArr.
     */
    private void startRecordPlay() {
        releaseMediaPlayer();
        releaseDispatcher();
        hideButtons();
        mDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(44100, 2048, 0);
        isRecording = true;


        mDispatcher.addAudioProcessor(
                new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
                        44100,
                        2048,
                        (pitchDetectionResult, audioEvent) ->
                        {
                            final double detectProb = pitchDetectionResult.getProbability();
                            final double pitchInHz = pitchDetectionResult.getPitch();
                            if (pitchInHz > 0 && isRecording) {
                                Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                                {
                                    Note note = Note.convertToNote(pitchInHz,
                                            audioEvent.getTimeStamp(), detectProb);

                                    mPlayerAttemptArr.add(note);
                                    if ((note.getAbsoluteNoteValue() - 24) < 36
                                            && note.getAbsoluteNoteValue() > 0
                                            && isRecording) {
                                        pianoView.clearHighlight();
                                        pianoView.highlightNote(note);
                                    }
                                });
                            }
                        }));
        AppExecutors.getInstance().diskIO().execute(mDispatcher);

        /* Stopping the record automatically after 2 times the length of the original
        melody + 3 seconds */
        recordTimer = new Timer();
        recordTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> stopRecording());
            }
        }, (long) (mGameViewModel.getGame().getSampleLength() * 2 + 3) * 1000);
    }

    /**
     * Hides the buttons
     */
    private void hideButtons() {
        fadeOutViews(playSampleBtn, playsLeftContainer, firstNoteButton);
    }

    /**
     * Shows the button again
     */
    private void revealButtons() {
        fadeInViews(playSampleBtn, playsLeftContainer, firstNoteButton);
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

    private void stopRecording() {
        recordTimer.cancel();
        releaseDispatcher();
        isRecording = false;
        //Sending the player attempt to the game to analyze
        mGameViewModel.getGame().analyzePlayerAttempt(mPlayerAttemptArr);
        if (mGameViewModel.getGame().getCurrentScore() != KoteGame.INVALID_SCORE) {
            mCallback.onRoundFinished();
        } else {
            invalidAttempt();
        }
    }

    /**
     * Cancel the recordings and prompt a message.
     */
    private void invalidAttempt() {
        recordBtnKb.setupButton(getResources().getString(R.string.record_btn_label),
                ContextCompat.getDrawable(mContext, R.drawable.record_circle));
        revealButtons();
        mPlayerAttemptArr.clear();
        Toast.makeText(mContext, getString(R.string.invalid_record_text),
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void onStop() {
        if (isRecording)
            recordInterrupted = true;
        if (isPlayingSample) {
            playInterrupted = true;
            mPlayerPos = mMediaPlayer.getCurrentPosition();
        }
        releaseDispatcher();
        releaseMediaPlayer();
        super.onStop();
    }

    private void releaseMediaPlayer() {
        pianoView.clearHighlight();
        mNoteNameTv.setVisibility(View.INVISIBLE);
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            } else
                mMediaPlayer.release();
            isPlayingSample = false;
            mMediaPlayer = null;
        }
    }

    private void releaseDispatcher() {
        if (mDispatcher != null) {
            if (!mDispatcher.isStopped()) {
                mDispatcher.stop();
            }
            isRecording = false;
            mDispatcher = null;
        }
    }

    @OnClick(R.id.record_button)
    void onRecordClick() {
        if (!isPlayingSample) {
            if (isRecording) {
                stopRecording();
            } else {
                if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                    mMediaPlayer.stop();
                startRecordPlay();
                recordBtnKb.setupButton(getResources().getString(R.string.stop_player_record),
                        ContextCompat.getDrawable(mContext, R.drawable.stop_icn));
            }
        }
    }

    @OnClick(R.id.play_sample_btn)
    void onPlaySampleClicked() {
        if (!isPlayingSample) {
            if (mPlaysLeft < 1) {
                Toast.makeText(mContext, getString(R.string.no_more_plays_text),
                        Toast.LENGTH_LONG).show();
            } else {
                mGameViewModel.playSample();
                releaseMediaPlayer();
                mPlayerPos = 0;
                playSample();
            }
        }
    }

    private void playSample() {
        if (mGameViewModel.getGame().hasAudioFile()) {
            String musicFileURI = "android.resource://" + mContext.getPackageName()
                    + "/raw/" + mGameViewModel.getGame().getFileName();
            mMediaPlayer = MediaPlayer.create(mContext, Uri.parse(musicFileURI));
            seekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(mPlayerPos);
            mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                isPlayingSample = false;
                mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
                seekBar.setProgress(0);
                releaseMediaPlayer();
            });
            mMediaPlayer.start();
            mSeekbarUpdateHandler.post(mUpdateSeekbar);
            isPlayingSample = true;
        }
    }

    private void setupScales() {
        Note firstNote = mGameViewModel.getGame().getCurrentSample()
                .get(0);
        ScaleVpAdapter scaleAdapter = new ScaleVpAdapter(mContext, firstNote.getformattedName());
        scaleVp.setAdapter(scaleAdapter);
        ArrayList<String> scaleNotes = firstNote.getScaleNotes();
        noteNameNum1.setText(scaleNotes.get(0));
        noteNameNum2.setText(scaleNotes.get(1));
        noteNameNum3.setText(scaleNotes.get(2));
        noteNameNum4.setText(scaleNotes.get(3));
        noteNameNum5.setText(scaleNotes.get(4));
        noteNameNum6.setText(scaleNotes.get(5));
        noteNameNum7.setText(scaleNotes.get(6));
    }

    @OnClick(R.id.scale_btn)
    void displayScale() {
        fadeInViews(scaleLayout);
    }

    @OnClick(R.id.close_scales_iv)
    void closeScalesWindow() {
        fadeOutViews(scaleLayout);
    }
    /*
    private void displayScale(){
        TODO: Waiting for graphic design
        String scaleName = "pian_scale_" + mGameViewModel.getGame()
                            .getCurrentSample().get(0).getformattedName();
        int scaleDrawId = mContext.getResources().getIdentifier(scaleName, "drawable",
                mContext.getPackageName());
        mSamplePianoIv.setImageDrawable(ContextCompat.getDrawable(mContext, scaleDrawId));
        String pianoContentDesc = scaleName + " " + getString(R.string.scale);
        mSamplePianoIv.setContentDescription(pianoContentDesc);

        String scaleName = mGameViewModel.getGame().getCurrentSample().get(0).getName();
        mSamplePianoIv.setImageDrawable(ContextCompat
                .getDrawable(mContext, R.drawable.pian_empty));
        mSamplePianoIv.setContentDescription(getString(R.string.piano_image_content_desc));

    }
    */
}
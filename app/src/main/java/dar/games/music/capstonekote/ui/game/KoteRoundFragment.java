package dar.games.music.capstonekote.ui.game;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

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
            } else
                mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        }
    };
    private Timer recordTimer;
    private boolean recordInterrupted = false;
    private boolean playInterrupted = false;
    private int mPlaysLeft;

    private ConstraintSet mConstraintSet;
    private Unbinder mUnbinder;

    // *****************
    // Views
    // *****************
    @BindView(R.id.sample_piano_iv)
    ImageView mSamplePianoIv;
    @BindView(R.id.note_name_tv)
    TextView mNoteNameTv;
    @BindView(R.id.seek_bar)
    SeekBar seekBar;
    @BindView(R.id.play_sample_btn)
    ImageView playSampleBtn;
    @BindView(R.id.record_button_image)
    ImageView recordBtnIv;
    @BindView(R.id.record_button_text)
    TextView recordBtnTv;
    @BindView(R.id.piano_board_player)
    ConstraintLayout pianoBoardConstraintPlayer;
    @BindView(R.id.piano_board_recording)
    View pianoBoardRecording;
    @BindView(R.id.round_tv)
    TextView roundTv;
    @BindView(R.id.total_score_round_tv)
    TextView totalScoreTv;
    @BindView(R.id.plays_left)
    TextView playsLeftTextView;


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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_kote_round, container, false);
        mUnbinder = ButterKnife.bind(this, rootView);
        isPlayingSample = false;
        mConstraintSet = new ConstraintSet();
        mConstraintSet.clone(pianoBoardConstraintPlayer);

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
        totalScoreTv.setText(String.valueOf(mGameViewModel.getGame().getTotalScore()));
        roundTv.setText(String.valueOf(mGameViewModel.getGame().getRound()));

        //Updating every time the game plays the melody.
        mGameViewModel.getPlaysLeft().observe(getViewLifecycleOwner(), numOfPlays -> {
            mPlaysLeft = numOfPlays;
            String tempText = getString(R.string.plays_left_base_text)
                    + " " + numOfPlays;
            playsLeftTextView.setText(tempText);
        });

        if (mPlayerPos != 0) {
            releaseMediaPlayer();
            playSample();
        }
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
                displayScale();
                mNoteNameTv.setVisibility(View.INVISIBLE);
            });
            showPianoNote(firstNote);
        }
    }

    /**
     * Displaying a piano image with the given note highlighted + the note's name and octave below
     * the highlighted note.
     *
     * @param note - The note to display.
     */
    private void showPianoNote(Note note) {
        mSamplePianoIv.setImageDrawable(ContextCompat.getDrawable(mContext, note.getImageRes()));
        String pianoContentDesc = note.getName()
                + getString(R.string.note) + getString(R.string.piano_image_content_desc);
        mSamplePianoIv.setContentDescription(pianoContentDesc);

        String noteText = note.getName() + "(" + note.getOctave() + ")";
        mNoteNameTv.setText(noteText);
        /*Setting the horizontal bias of the first note's text view based on the position of the
         note on the 3 octaves piano. */
        mConstraintSet.setHorizontalBias(mNoteNameTv.getId(),
                (float) (note.getAbsoluteNoteValue() - 24) / 36);
        mConstraintSet.applyTo(pianoBoardConstraintPlayer);
        mNoteNameTv.setVisibility(View.VISIBLE);
    }

    /**
     * Start to dispatch sound from the microphone and record the data to the mPlayerAttemptArr.
     */
    private void startRecordPlay() {
        releaseMediaPlayer();
        releaseDispatcher();
        displayRecording();
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
                                        showPianoNote(note);
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
     * Changes the display of the piano board.
     */
    private void displayRecording() {
        pianoBoardConstraintPlayer.setVisibility(View.GONE);
        pianoBoardRecording.setVisibility(View.VISIBLE);
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
        recordBtnIv.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.record_circle));
        recordBtnTv.setText(getResources().getString(R.string.record_btn_label));
        pianoBoardRecording.setVisibility(View.GONE);
        pianoBoardConstraintPlayer.setVisibility(View.VISIBLE);
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
        displayScale();
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
                recordBtnIv.setImageDrawable(
                        ContextCompat.getDrawable(mContext, R.drawable.stop_icn));
                recordBtnTv.setText(getResources().getString(R.string.stop_player_record));
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
            String musicFileURI = "android.resource://" + mContext.getPackageName() + "/raw/" + mGameViewModel.getGame().getFileName();
            mMediaPlayer = MediaPlayer.create(mContext, Uri.parse(musicFileURI));
            seekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(mPlayerPos);
            mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                isPlayingSample = false;
                mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
                releaseMediaPlayer();
            });
            mMediaPlayer.start();
            mSeekbarUpdateHandler.post(mUpdateSeekbar);
            isPlayingSample = true;
        }
    }

    private void displayScale(){
        /*TODO: Waiting for graphic design
        String scaleName = "pian_scale_" + mGameViewModel.getGame()
                            .getCurrentSample().get(0).getformattedName();
        int scaleDrawId = mContext.getResources().getIdentifier(scaleName, "drawable",
                mContext.getPackageName());
        mSamplePianoIv.setImageDrawable(ContextCompat.getDrawable(mContext, scaleDrawId));
        String pianoContentDesc = scaleName + " " + getString(R.string.scale);
        mSamplePianoIv.setContentDescription(pianoContentDesc);
         */
        mSamplePianoIv.setImageDrawable(ContextCompat
                .getDrawable(mContext, R.drawable.pian_empty));
        mSamplePianoIv.setContentDescription(getString(R.string.piano_image_content_desc));

    }
}
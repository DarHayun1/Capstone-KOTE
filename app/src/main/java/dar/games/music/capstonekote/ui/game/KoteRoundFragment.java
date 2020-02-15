package dar.games.music.capstonekote.ui.game;

import android.content.Context;
import android.media.MediaPlayer;
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
import dar.games.music.capstonekote.gamelogic.Note;
import dar.games.music.capstonekote.gamelogic.NoteArrayList;

public class KoteRoundFragment extends Fragment {

    private static final String TAG = KoteRoundFragment.class.getSimpleName();
    private static final String RECORD_INTERRUPTED_KEY = "record_interrupted_key";
    private static final String EXTRA_PLAYER_POS = "extra_player_pos";
    private MediaPlayer mMediaPlayer;
    private NoteArrayList mPlayerTryArr;
    private AudioDispatcher mDispatcher;
    private int mPlayerPos = 0;


    private Context mContext;
    private OnGameFragInteractionListener mCallback;

    private Unbinder mUnbinder;

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

    private ConstraintSet mConstraintSet;

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

    public static KoteRoundFragment newInstance() {

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

        mPlayerTryArr = new NoteArrayList(4000, NoteArrayList.PLAYER_ID);

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

    //gets only valid samples with a note in the 2-4 octave
    @OnClick(R.id.first_note_btn)
    void playFirstNote() {
        if (!isPlayingSample) {
            if (mGameViewModel.getGame().getCurrentSample() == null ||
                    mGameViewModel.getGame().getCurrentSample().size() < 1)
                return;

            Note firstNote = mGameViewModel.getGame().getCurrentSample().get(0);

            releaseMediaPlayer();
            mMediaPlayer = MediaPlayer.create(mContext, firstNote.getSoundRes());
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(mediaPlayer -> {
                mSamplePianoIv.setImageDrawable(ContextCompat
                        .getDrawable(mContext, R.drawable.pian_empty));
                mNoteNameTv.setVisibility(View.INVISIBLE);
            });
            showPianoNote(firstNote);
        }
    }

    private void showPianoNote(Note note) {
        mSamplePianoIv.setImageDrawable(ContextCompat.getDrawable(mContext, note.getImageRes()));
        String pianoContentDesc = note.getName()
                + getString(R.string.note) + getString(R.string.piano_image_content_desc);
        mSamplePianoIv.setContentDescription(pianoContentDesc);

        String noteText = note.getName() + "(" + note.getOctave() + ")";
        mNoteNameTv.setText(noteText);
        mConstraintSet.setHorizontalBias(mNoteNameTv.getId(),
                (float) (note.getAbsoluteNoteValue() - 24) / 36);
        mConstraintSet.applyTo(pianoBoardConstraintPlayer);
        mNoteNameTv.setVisibility(View.VISIBLE);
    }

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
                        (pitchDetectionResult, audioEvent) -> {
                            final double detectProb = pitchDetectionResult.getProbability();
                            final double pitchInHz = pitchDetectionResult.getPitch();
                            if (pitchInHz > 0 && isRecording) {
                                Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                                    Note note = Note.convertToNote(pitchInHz,
                                            audioEvent.getTimeStamp(), detectProb);
                                    mPlayerTryArr.add(note);
                                    if ((note.getAbsoluteNoteValue() - 24) < 36
                                            && note.getAbsoluteNoteValue() > 0
                                            && isRecording) {
                                        showPianoNote(note);
                                    }
                                });
                            }
                        }));
        new Thread(mDispatcher, "Audio Dispatcher").start();
        recordTimer = new Timer();
        recordTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() != null)
                    getActivity().runOnUiThread(() -> stopRecording());
            }
        }, (long) (mGameViewModel.getGame().getSampleLength() * 2 + 3) * 1000);
    }

    private void displayRecording() {
        pianoBoardConstraintPlayer.setVisibility(View.GONE);
        pianoBoardRecording.setVisibility(View.VISIBLE);
    }

    private void stopRecording() {
        recordTimer.cancel();
        releaseDispatcher();
        isRecording = false;
        mGameViewModel.getGame().analyzePlayerAttempt(mPlayerTryArr);
        if (mGameViewModel.getGame().getCurrentScore() != -1) {
            mCallback.onRoundFinished();
        } else {
            invalidAttempt();
        }
    }

    private void invalidAttempt() {
        recordBtnIv.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.record_circle));
        recordBtnTv.setText(getResources().getString(R.string.record_btn_label));
        pianoBoardRecording.setVisibility(View.GONE);
        pianoBoardConstraintPlayer.setVisibility(View.VISIBLE);
        mPlayerTryArr.clear();
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
        mSamplePianoIv.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.pian_empty));
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

//        String uri1 = "android.resource://" + mContext.getPackageName() + "/raw/" + mGameViewModel.getGame().ge
        mMediaPlayer = MediaPlayer.create(mContext, mGameViewModel.getGame()
                .getCurrentSample().getSampleId());
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
package dar.games.music.capstonekote.ui.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.gamelogic.Note;

public class PianoView extends LinearLayout {

    private int mNumOfOctaves;
    OctaveView[] octaves = new OctaveView[4];
    private HashSet<Integer> highlightedOctaves = new HashSet<>(4);

    public PianoView(Context context) {
        this(context, null);
    }

    public PianoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PianoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public PianoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.piano_view, this);
        bindViews();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PianoView,
                0, 0);

        try {
            mNumOfOctaves = ta.getInt(R.styleable.PianoView_numOfOctaves, 2);
        } catch (NullPointerException e){e.printStackTrace();}
        finally {
            ta.recycle();
        }

        displayOctaves();
    }

    private void bindViews() {
        octaves[2] = findViewById(R.id.octave3);
        octaves[1] = findViewById(R.id.octave2);
        octaves[3] = findViewById(R.id.octave4);
        octaves[0] = findViewById(R.id.octave1);
    }

    private void displayOctaves() {
        switch (mNumOfOctaves){
            case 2:
                octaves[2].setVisibility(VISIBLE);
                octaves[1].setVisibility(VISIBLE);
                octaves[3].setVisibility(GONE);
                octaves[0].setVisibility(GONE);
                break;
            case 3:
                octaves[2].setVisibility(VISIBLE);
                octaves[1].setVisibility(VISIBLE);
                octaves[3].setVisibility(VISIBLE);
                octaves[0].setVisibility(GONE);
                break;
            case 4:
                octaves[2].setVisibility(VISIBLE);
                octaves[1].setVisibility(VISIBLE);
                octaves[3].setVisibility(VISIBLE);
                octaves[0].setVisibility(VISIBLE);
                break;
            default:
                octaves[2].setVisibility(VISIBLE);
                octaves[1].setVisibility(GONE);
                octaves[3].setVisibility(GONE);
                octaves[0].setVisibility(GONE);
        }
    }

    public void highlightNote(Note note){
        int octave = note.getOctave();
        if (0 < octave && octave < octaves.length
            && octaves[octave-1].getVisibility()==VISIBLE){
            highlightedOctaves.add(octave);
            octaves[octave-1].highlightTile(note);
        }
        else {
            int closestOctave = findClosestOctave(octave);
            highlightedOctaves.add(closestOctave);
            octaves[closestOctave-1].highlightTile(note);
        }
    }

    /*
    Assuming 4 octaves
     */
    private int findClosestOctave(int octave) {
        if (octave == 4 || octave == 2)
            return 3;
        if (octaves[1].getVisibility()==VISIBLE) return 2;
        return 3;
    }

    public void clearHighlight() {
        for (int i = 0; i < 4; i++) {
            if (highlightedOctaves.remove(i))
                octaves[i-1].clearOctave();
        }
    }
}

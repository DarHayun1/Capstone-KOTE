package dar.games.music.capstonekote.ui.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Arrays;
import java.util.Set;

import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.gamelogic.Note;

public class OctaveView extends ConstraintLayout {

    private final static int[] MAJOR_SCALE_SEQUENCE = {2,2,1,2,2,2};
    private ImageView[] tiles = new ImageView[12];

    public OctaveView(Context context) {
        this(context, null, 0);
    }

    public OctaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OctaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.c_to_b_tiles, this);
        bindViews();

    }

    private void bindViews() {
        tiles[0] = findViewById(R.id.c_tile);
        tiles[1] = findViewById(R.id.db_tile);
        tiles[2] = findViewById(R.id.d_tile);
        tiles[3] = findViewById(R.id.eb_tile);
        tiles[4] = findViewById(R.id.e_tile);
        tiles[5] = findViewById(R.id.f_tile);
        tiles[6] = findViewById(R.id.gb_tile);
        tiles[7] = findViewById(R.id.g_tile);
        tiles[8] = findViewById(R.id.ab_tile);
        tiles[9] = findViewById(R.id.a_tile);
        tiles[10] = findViewById(R.id.bb_tile);
        tiles[11] = findViewById(R.id.b_tile);
    }

    public void highlightScale(String noteName){
        int noteValue = Note.getNoteValueByName(noteName);
        highlightTile(noteValue);
        for (int pitchDiff : MAJOR_SCALE_SEQUENCE){
            noteValue = (noteValue+pitchDiff)%11;
            highlightTile(noteValue);
        }
    }

    private void highlightTile(int noteValue) {
        tiles[noteValue].setImageTintList(
                ColorStateList.valueOf(getContext().getColor(R.color.colorPrimaryLight)));
    }

}

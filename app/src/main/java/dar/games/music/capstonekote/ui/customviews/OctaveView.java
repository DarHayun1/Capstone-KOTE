package dar.games.music.capstonekote.ui.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.gamelogic.Note;

public class OctaveView extends ConstraintLayout {

    private final static List<Integer> BLACK_TILES = Arrays.asList(1,3,6,8,10);

    private ImageView[] tiles = new ImageView[12];
    private TextView noteNameTv;
    private ConstraintSet mConstraintSet;

    public OctaveView(Context context) {
        this(context, null, 0);
    }

    public OctaveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OctaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.c_to_b_tiles, this);
        bindViews();

        mConstraintSet = new ConstraintSet();
        mConstraintSet.clone(this);

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
        noteNameTv = findViewById(R.id.note_name_tv);
    }

    void highlightTile(Note note) {
        int noteValue = note.getAbsoluteNoteValue() % 12;
        tiles[noteValue].setImageTintList(
                ColorStateList.valueOf(getContext().getColor(R.color.colorPrimaryLight)));
        mConstraintSet.setHorizontalBias(noteNameTv.getId(), (float) noteValue/12);
        mConstraintSet.applyTo(this);
        noteNameTv.setText(note.getName());
        noteNameTv.setVisibility(VISIBLE);

    }

    void clearOctave(){
        Drawable blackTile = ContextCompat.getDrawable(getContext(), R.drawable.black_tile);
        Log.d("AAAA", blackTile != null ? blackTile.toString() : "");
        for (int i=0;i<tiles.length;i++){
            int colorId;
            if (BLACK_TILES.contains(i))
                colorId = R.color.blackTileColor;
            else
                colorId = R.color.whiteTileColor;
            tiles[i].setImageTintList(
                    ColorStateList.valueOf(getContext().getColor(colorId)));
            noteNameTv.setVisibility(INVISIBLE);
        }
    }

}

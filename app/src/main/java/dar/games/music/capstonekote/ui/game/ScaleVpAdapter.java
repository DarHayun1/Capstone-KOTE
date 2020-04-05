package dar.games.music.capstonekote.ui.game;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import dar.games.music.capstonekote.R;

public class ScaleVpAdapter extends RecyclerView.Adapter<ScaleVpAdapter.ScaleVh> {
    private static final int NUM_OF_INSTRUMENTS = 2;

    private final Context mContext;
    private final String formattedScaleName;

    ScaleVpAdapter(Context context, String scaleName) {
        mContext = context;
        formattedScaleName = scaleName;
    }

    @NonNull
    @Override
    public ScaleVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.scales_item, parent, false);
        return new ScaleVh(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScaleVh holder, int position) {
        holder.imageView.setImageDrawable(findDrawable(position));
    }

    @Override
    public int getItemCount() {
        return NUM_OF_INSTRUMENTS;
    }

    private Drawable findDrawable(int position) {
        String instrumentFormattedName;
        switch (position) {
            case 0:
                instrumentFormattedName = mContext.getString(R.string.guitar_file_ext);
                break;
            default:
                instrumentFormattedName = mContext.getString(R.string.piano_file_ext);
                break;
        }
        String fileName = formattedScaleName + "major_scale_" + instrumentFormattedName;
        int scaleDrawId = mContext.getResources().getIdentifier(fileName, "drawable",
                mContext.getPackageName());
        return ContextCompat.getDrawable(mContext, scaleDrawId);
    }


    class ScaleVh extends RecyclerView.ViewHolder {

        @BindView(R.id.scale_img)
        ImageView imageView;

        ScaleVh(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}


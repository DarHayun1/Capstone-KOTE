package dar.games.music.capstonekote.ui.mainmenu;

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

public class DiffVpAdapter extends RecyclerView.Adapter<DiffVpAdapter.DiffVH> {
    private static final int NUM_OF_DIFFICULTIES = 3;
    private final Context mContext;

    DiffVpAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public DiffVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.fragment_diff_obj, parent, false);
        return new DiffVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiffVH holder, int position) {
        holder.imageView.setImageDrawable(findDrawable(position));
    }

    @Override
    public int getItemCount() {
        return NUM_OF_DIFFICULTIES;
    }

    private Drawable findDrawable(int position) {
        int drawId;
        switch (position) {
            case 2:
                drawId = R.drawable.extreme_diff_icon;
                break;
            case 1:
                drawId = R.drawable.hard_diff_icon;
                break;
            default:
                drawId = R.drawable.easy_diff_icon;
        }
        return ContextCompat.getDrawable(mContext, drawId);
    }

    public void changeDiff(int delta) {

    }

    class DiffVH extends RecyclerView.ViewHolder {

        @BindView(R.id.diff_image)
        ImageView imageView;

        DiffVH(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}


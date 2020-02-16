package dar.games.music.capstonekote.ui.mainmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import dar.games.music.capstonekote.R;
import dar.games.music.capstonekote.gamelogic.KoteGame;
import dar.games.music.capstonekote.repository.GameResultModel;

/**
 * Adapter to display the last games saved on the device
 */
public class GameResultsAdapter extends RecyclerView.Adapter<GameResultsAdapter.ResultsVH> {

    private static final String DATE_FORMAT = "MMM d";

    static final int SORT_BY_DATE = 120;
    static final int SORT_BY_SCORE = 121;

    private Context mContext;
    private List<GameResultModel> mResults;
    private int mSortBy;

    GameResultsAdapter(Context context) {
        mContext = context;
        mResults = new ArrayList<>();
        mSortBy = SORT_BY_DATE;
    }

    public GameResultsAdapter(Context context, List<GameResultModel> results, int sortBy) {
        mResults = results;
        mContext = context;
        mSortBy = sortBy;
        sortResults();
    }

    void setResultsData(List<GameResultModel> results) {
        mResults = results;
        sortResults();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ResultsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.game_result_item, parent, false);
        return new ResultsVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultsVH holder, int position) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        holder.dateView.setText(dateFormat.format(mResults.get(position).getTime()));

        holder.scoreView.setText(String.valueOf(mResults.get(position).getScore()));
        switch (mResults.get(position).getDifficulty()) {
            case KoteGame.EASY_DIFFICULTY:
                holder.diffView.setText(mContext.getResources().getString(R.string.easy_diff));
                break;
            case KoteGame.HARD_DIFFICULTY:
                holder.diffView.setText(mContext.getResources().getString(R.string.hard_diff));
                break;
            case KoteGame.EXTREME_DIFFICULTY:
                holder.diffView.setText(mContext.getResources().getString(R.string.extreme_diff));
                break;
        }

    }

    @Override
    public int getItemCount() {
        if (mResults == null)
            return 0;
        return mResults.size();
    }

    //For future implementation.
    public void setSortOrder(int sortBy) {
        if (mSortBy != sortBy) {
            mSortBy = sortBy;
            sortResults();
            notifyDataSetChanged();
        }
    }

    private void sortResults() {
        switch (mSortBy) {
            case SORT_BY_DATE: {
                mResults.sort(Comparator.comparing(GameResultModel::getScore).reversed());
            }
            case SORT_BY_SCORE: {
                mResults.sort(Comparator.comparing(GameResultModel::getTime).reversed());
            }
        }
    }

    class ResultsVH extends RecyclerView.ViewHolder {

        @BindView(R.id.item_diff_tv)
        TextView diffView;
        @BindView(R.id.item_score_tv)
        TextView scoreView;
        @BindView(R.id.item_date_tv)
        TextView dateView;

        ResultsVH(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

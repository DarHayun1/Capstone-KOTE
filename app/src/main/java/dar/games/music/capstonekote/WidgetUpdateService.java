package dar.games.music.capstonekote;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import dar.games.music.capstonekote.gamelogic.KoteGame;


/**
 * IntentService for updating the Widget.
 */
public class WidgetUpdateService extends JobIntentService {

    public static final String ACTION_UPDATE_HIGHSCORE = "dar.games.music.capstonekote.action.update_highscore";
    public static final String EXTRA_DIFF_WIDGET = "extra_difficulty_widget";
    public static final String EXTRA_SCORE_WIDGET = "extra_score_widget";

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_UPDATE_HIGHSCORE)) {
                int diff = intent.getIntExtra(EXTRA_DIFF_WIDGET, KoteGame.EASY_DIFFICULTY);
                int score = intent.getIntExtra(EXTRA_SCORE_WIDGET, 0);

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                        new ComponentName(this, KoteWidgetAppProvider.class));
                KoteWidgetAppProvider.updateWidgetHighscore(this, appWidgetManager,
                        diff, score, appWidgetIds);
            }
        }
    }

    public static void startActionUpdateHighscoreWidget(Context context, int diff, int score) {
        Intent serviceIntent = new Intent(context, WidgetUpdateService.class);
        serviceIntent.setAction(WidgetUpdateService.ACTION_UPDATE_HIGHSCORE);
        serviceIntent.putExtra(WidgetUpdateService.EXTRA_DIFF_WIDGET, diff);
        serviceIntent.putExtra(WidgetUpdateService.EXTRA_SCORE_WIDGET, score);
        context.startService(serviceIntent);
    }
}


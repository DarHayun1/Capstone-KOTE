package dar.games.music.capstonekote;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import dar.games.music.capstonekote.gamelogic.KoteGame;


public class WidgetUpdateService extends IntentService {

    public static final String ACTION_UPDATE_HIGHSCORE = "dar.games.music.miriamsbakingapp.action.update_highscore";
    public static final String EXTRA_DIFF_WIDGET = "extra_difficulty_widget";
    public static final String EXTRA_SCORE_WIDGET = "extra_score_widget";

    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    public static void startActionUpdateRecipeWidget(Context context, int diff, int score) {
        Intent serviceIntent = new Intent(context, WidgetUpdateService.class);
        serviceIntent.setAction(WidgetUpdateService.ACTION_UPDATE_HIGHSCORE);
        serviceIntent.putExtra(WidgetUpdateService.EXTRA_DIFF_WIDGET, diff);
        serviceIntent.putExtra(WidgetUpdateService.EXTRA_SCORE_WIDGET, score);

        context.startService(serviceIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
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
}


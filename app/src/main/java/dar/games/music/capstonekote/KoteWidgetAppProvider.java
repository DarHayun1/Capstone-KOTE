package dar.games.music.capstonekote;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import dar.games.music.capstonekote.ui.mainmenu.MainActivity;

public class KoteWidgetAppProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int difficulty, int score, int appWidgetId) {

        String diffText;
        switch (difficulty) {
            case 1:
                diffText = context.getString(R.string.hard_diff);
                break;

            case 2:
                diffText = context.getString(R.string.extreme_diff);
                break;

            default:
                diffText = context.getString(R.string.easy_diff);
        }

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_app_provider);

        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

        views.setTextViewText(R.id.diff_label_widget_tv, diffText);
        views.setTextViewText(R.id.highscore_widget_tv, String.valueOf(score));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_app_provider);
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    public static void updateWidgetHighscore(Context context, AppWidgetManager appWidgetManager,
                                             int diff, int highscore, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, diff, highscore, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

package de.nico.asura.tools;

/*
 * Author: Nico Alt
 * See the file "LICENSE" for the full license governing this code.
 */

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import de.nico.asura.Main;
import de.nico.asura.R;

public final class Widget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        for (final int appWidgetId : appWidgetIds) {
            final Intent intent = new Intent(context, Main.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.update, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
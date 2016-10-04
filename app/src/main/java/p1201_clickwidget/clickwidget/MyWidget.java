package p1201_clickwidget.clickwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.RemoteViews;

import java.util.Date;

/**
 * Created by user on 04.10.2016.
 */

public class MyWidget extends AppWidgetProvider {

    final static String ACTION_CHANGE = "p1201_clickwidget.clickwidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        for( int id : appWidgetIds) {
            updateWidget(context,appWidgetManager,id);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        SharedPreferences.Editor editor = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF,Context.MODE_PRIVATE).edit();

        for(int widgetID : appWidgetIds) {
            editor.remove(ConfigActivity.WIDGET_TIME_FORMAT + widgetID);
            editor.remove(ConfigActivity.WIDGET_COUNT + widgetID);
        }
        editor.commit();
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                             int widgetID) {
        SharedPreferences sp = context.getSharedPreferences(ConfigActivity.WIDGET_PREF,Context.MODE_PRIVATE);

        String timeFormat = sp.getString(ConfigActivity.WIDGET_TIME_FORMAT + widgetID,null);

        if(timeFormat == null)
            return;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(timeFormat);
        String currentTime = simpleDateFormat.format(new Date(System.currentTimeMillis()));

        String count = String.valueOf(sp.getInt(ConfigActivity.WIDGET_COUNT + widgetID,0));

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget);
        remoteViews.setTextViewText(R.id.tvTime,currentTime);
        remoteViews.setTextViewText(R.id.tvCount,count);

        Intent configIntent = new Intent(context,ConfigActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetID);
        PendingIntent pIntent = PendingIntent.getActivity(context,widgetID,configIntent,0);

        remoteViews.setOnClickPendingIntent(R.id.tvPressConfig,pIntent);

        Intent updateIntent = new Intent(context,MyWidget.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                new int[] { widgetID } );
        pIntent = PendingIntent.getBroadcast(context,widgetID,updateIntent,0);
        remoteViews.setOnClickPendingIntent(R.id.tvPressUpdate,pIntent);

        Intent countIntent = new Intent(context, MyWidget.class);
        countIntent.setAction(ACTION_CHANGE);
        countIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        pIntent = PendingIntent.getBroadcast(context, widgetID, countIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.tvPressCount, pIntent);

        appWidgetManager.updateAppWidget(widgetID,remoteViews);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction().equalsIgnoreCase(ACTION_CHANGE)) {

            // извлекаем ID экземпляра
            int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mAppWidgetId = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID);

            }
            if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                // Читаем значение счетчика, увеличиваем на 1 и записываем
                SharedPreferences sp = context.getSharedPreferences(
                        ConfigActivity.WIDGET_PREF, Context.MODE_PRIVATE);
                int cnt = sp.getInt(ConfigActivity.WIDGET_COUNT + mAppWidgetId,  0);
                sp.edit().putInt(ConfigActivity.WIDGET_COUNT + mAppWidgetId,
                        ++cnt).commit();

                // Обновляем виджет
                updateWidget(context, AppWidgetManager.getInstance(context),
                        mAppWidgetId);
            }
        }
    }
}

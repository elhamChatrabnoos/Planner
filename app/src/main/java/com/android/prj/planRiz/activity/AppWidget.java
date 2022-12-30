package com.android.prj.planRiz.activity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.android.prj.planRiz.DataBase;
import com.android.prj.planRiz.R;
import com.android.prj.planRiz.workRelatedClass.WorkDao;
import com.android.prj.planRiz.workRelatedClass.WorkModel;

import java.util.List;

import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;


/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    public static final String CLICK_REFRESH = "click_refresh";
    static RemoteViews views;
    static RemoteViews listLayout;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        defineViewsAndLists(context);
//Inform AppWidgetManager about the RemoteViews object//
        appWidgetManager.updateAppWidget(appWidgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        // when refresh button clicked
        if (intent.getAction().equals(CLICK_REFRESH)){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, AppWidget.class)));

        }
    }


    public static String getSystemDate(@NonNull RemoteViews views) {
        PersianDate persianDate = new PersianDate();
        String systemDate = PersianDateFormat.format(persianDate, "l j F Y", PersianDateFormat.PersianDateNumberCharacter.FARSI);
        views.setTextViewText(R.id.widDateTxt1, systemDate);
        return systemDate;
    }


    public static void addViewToMainLayout(Context context,RemoteViews views,
                                           RemoteViews listLayout, List<WorkModel> worksList){
        // to prevent duplicate work list
        views.removeAllViews(R.id.nested_layout);
        for (int i = 0; i < worksList.size(); i++) {
            // add view to main remoteView
            listLayout = new RemoteViews(context.getPackageName(), R.layout.list_widget_layout);
            listLayout.setTextViewText(R.id.txt_test, worksList.get(i).getWork_name());
            if(worksList.get(i).isDone()){
                listLayout.setImageViewResource(R.id.widget_done_img, R.drawable.done_img);
            }
            views.addView(R.id.nested_layout, listLayout);
        }


        // GO TO MAIN PAGE OF APP WHEN WIDGET WAS CLICKED
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        views.setOnClickPendingIntent(R.id.mainLayout, pendingIntent);

        // when clicked on refresh button
        Intent intent2 = new Intent(context, AppWidget.class);
        intent2.setAction(CLICK_REFRESH);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(context, 0, intent2, PendingIntent.FLAG_MUTABLE);
        views.setOnClickPendingIntent(R.id.refresh_button, pendingIntent2);
    }


    public static void defineViewsAndLists(@NonNull Context context){
        //Load the layout resource file into a RemoteViews object//
        views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        listLayout = new RemoteViews(context.getPackageName(), R.layout.list_widget_layout);
        getSystemDate(views);

        // search current date in database to show work list in that date
        WorkDao workDao = DataBase.getDataBase(context).getWorkDao();
        List<WorkModel> worksList = workDao.getWorkFromDate(getSystemDate(views));

        addViewToMainLayout(context, views, listLayout, worksList);
    }
}

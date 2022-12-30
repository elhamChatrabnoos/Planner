package com.android.prj.planRiz;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.prj.planRiz.activity.AlarmActivity;
import com.android.prj.planRiz.activity.MainActivity;
import com.android.prj.planRiz.workRelatedClass.WorkDao;
import com.android.prj.planRiz.workRelatedClass.WorkModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class AlarmWorkService extends Service {

    public static final String WORK_NAME = "workName";
    public static final String WORK_ALARM = "workAlarm";
    private String channelId = "Id_1";
    private String channelName = "AlarmWork";
    private int notificationId = 1;
    private NotificationManager manager;
    private TimeChangeReceiver receiver;
    private Ringtone ringtone;
    private boolean service = false;
    private PowerManager.WakeLock wakeLock;
    private Handler handler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotification();
        startNotification(null, null);
        handler = new Handler();

        keepAwakeService();

        Thread checkTimeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                getDateAndTime();
                searchTimeInWorksList();
                handler.postDelayed(this, 60000);
            }
        });
        checkTimeThread.start();

//        receiver = new TimeChangeReceiver();
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        filter.addAction(Intent.ACTION_TIME_TICK);
//        filter.addAction(Intent.ACTION_DATE_CHANGED);
//        registerReceiver(receiver, filter);

        return START_STICKY;
    }

    private void keepAwakeService() {
        // cpu work also when phone is on sleep mode
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if(Build.VERSION.SDK_INT >= 23) {
            wakeLock = powerManager.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, ":TAG");
        } else {
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, ":TAG");
        }
        wakeLock.acquire();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId, channelName,
                    NotificationManager.IMPORTANCE_LOW);
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void startNotification(String alarm, String name){
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Intent mainPage = new Intent(this, MainActivity.class);
            PendingIntent mainPagePending = PendingIntent.getActivity(this,
                   0 , mainPage, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Intent alarmPage = new Intent(this, AlarmActivity.class);
            alarmPage.putExtra(WORK_ALARM, alarm);
            alarmPage.putExtra(WORK_NAME, name);
            PendingIntent alarmPending = PendingIntent.getActivity(this,
                    0, alarmPage, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

           if (service){
               Notification notification = new NotificationCompat.Builder(this, channelId)
                       .setContentTitle(getDateAndTime())
                       .setSmallIcon(R.drawable.calendar2)
                       .setContentIntent(alarmPending)
                       .build();
               startForeground(notificationId, notification);
               service = false;
           }
           else {
               Notification notification = new NotificationCompat.Builder(this, channelId)
                       .setContentTitle(getDateAndTime())
                       .setSmallIcon(R.drawable.calendar2)
                       .setContentIntent(mainPagePending)
                       .build();
               startForeground(notificationId, notification);
           }
    }

    private void startAlarm(WorkModel workModel){
        service = true;
        startNotification(workModel.getAlarm(), workModel.getWork_name());

        // get system time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // set intent to start alarm
        Intent alarmIntent = new Intent(this, AlarmActivity.class);
        alarmIntent.putExtra(WORK_NAME, workModel.getWork_name());
        alarmIntent.putExtra(WORK_ALARM, workModel.getAlarm());
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // run alarm activity in android version 10 to high
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this,
                    0, alarmIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), pendingIntent );

        playRingtone();
        unregisterReceiver(receiver);
    }

    public void playRingtone(){
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(this, alarm);
        ringtone.play();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ringtone.stop();
        wakeLock.release();
        Log.d("3030", "onDestroy: ");
        handler.removeCallbacksAndMessages(null);

        // start alarm activity
        Intent mainActIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainActIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainActIntent);
    }

    private String systemTime;
    private String systemDate;

    public String getDateAndTime(){
        // get system date and time
        Date date = new Date();
        systemTime = String.format(Locale.ENGLISH, "%tR",date);

        PersianDate persianDate = new PersianDate();
        systemDate = PersianDateFormat.format(persianDate, "l j F Y", PersianDateFormat.PersianDateNumberCharacter.FARSI);
        return systemDate;
    }


    private void searchTimeInWorksList(){
        WorkDao workDao = DataBase.getDataBase(getApplicationContext()).getWorkDao();
        List<WorkModel> workModelList = workDao.getWorks();

        for(int i = 0; i < workModelList.size(); i ++){
            WorkModel workModel = workModelList.get(i);

            // try catch for android api level 24
            try{
                if(workModel.getAlarm().equals(systemTime)
                        && workModel.getWorkDate().equals(systemDate)
                        && workModel.isAlarmOn() ){

                    startAlarm(workModelList.get(i));
                    workModel.setAlarmOn(false);
                    workDao.updateWork(workModel);
                }
            }catch (Exception e){
                Log.d("3030", e.getMessage());
            }
        }
    }

    public  class TimeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            getDateAndTime();
//            searchTimeInWorksList();
        }

        private void searchTimeInWorksList(){
            WorkDao workDao = DataBase.getDataBase(getApplicationContext()).getWorkDao();
            List<WorkModel> workModelList = workDao.getWorks();

            for(int i = 0; i < workModelList.size(); i ++){
                WorkModel workModel = workModelList.get(i);

                // try catch for android api level 24
               try{
                   if(workModel.getAlarm().equals(systemTime)
                           && workModel.getWorkDate().equals(systemDate)
                           && workModel.isAlarmOn() ){

                       startAlarm(workModelList.get(i));
                       workModel.setAlarmOn(false);
                       workDao.updateWork(workModel);
                   }
               }catch (Exception e){
                   Log.d("3030", e.getMessage());
               }
            }
        }

    }


}

package com.android.prj.planRiz.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.airbnb.lottie.Lottie;
import com.android.prj.planRiz.AlarmWorkService;
import com.android.prj.planRiz.DataBase;
import com.android.prj.planRiz.R;
import com.android.prj.planRiz.workRelatedClass.WorkDao;
import com.android.prj.planRiz.workRelatedClass.WorkModel;

import java.util.List;

public class AlarmActivity extends AppCompatActivity {

//    private ActivityAlarmBinding binding;
    boolean alarmIsOn = false;
    TextView  offAllAlarm, workName, workTime;
    View  offAlarm;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = ActivityAlarmBinding.inflate(getLayoutInflater());
//        View view = binding.getRoot();
        setContentView(R.layout.activity_alarm);

        offAlarm = findViewById(R.id.off_alarm);
        offAllAlarm = findViewById(R.id.off_all_alarm);
        workName = findViewById(R.id.work_name);
        workTime = findViewById(R.id.work_time);

        alarmIsOn = true;

//        Log.d("2020", "alarm activity");
        getSupportActionBar().hide();
        setFlagsActivity();
        getWorkInfo();

        offAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService2();
                finish();
            }
        });

        offAllAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               turnOffAllAlarm();
               stopService2();
               finish();
            }
        });

    }

    public void turnOffAllAlarm(){
        // to get system date
        AlarmWorkService alarmWorkService = new AlarmWorkService();

        WorkDao workDao = DataBase.getDataBase(AlarmActivity.this).getWorkDao();
        List<WorkModel> workModelList = workDao.getWorkFromDate(alarmWorkService.getDateAndTime());

        for(int i = 0; i < workModelList.size(); i ++){
            if(workModelList.get(i).isAlarmOn()){
                workModelList.get(i).setAlarmOn(false);
                workDao.updateWork(workModelList.get(i));
                workModelList.get(i).setAlarm("");
            }
        }
    }

    void getWorkInfo(){
        String workNAme = getIntent().getStringExtra(AlarmWorkService.WORK_NAME);
        String workALarm = getIntent().getStringExtra(AlarmWorkService.WORK_ALARM);
        workName.setText( "ساعت انجام " + workNAme + " میباشد.");
        workTime.setText(workALarm);
    }


    public void setFlagsActivity(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1){
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        }
        else{
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

    }

    public void stopService2(){
        Intent serviceIntent = new Intent(AlarmActivity.this, AlarmWorkService.class);
        stopService(serviceIntent);
    }


    @Override
    public void onBackPressed() {
        if(!alarmIsOn){
            super.onBackPressed();
            finish();
        }
    }
}
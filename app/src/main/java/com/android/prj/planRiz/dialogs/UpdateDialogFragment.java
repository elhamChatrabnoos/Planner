package com.android.prj.planRiz.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.prj.planRiz.AlarmWorkService;
import com.android.prj.planRiz.R;
import com.android.prj.planRiz.workRelatedClass.WorkModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;


public class UpdateDialogFragment extends BottomSheetDialogFragment{

    private UpdateCall updateCall;
    EditText work_name ;
    EditText alarm_work;
    WorkModel workModel;
    boolean alarmSet = false;
    TextView txt_save;
    BottomSheetDialog addWorkDialog;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        updateCall = (UpdateCall) context;
        workModel = getArguments().getParcelable("work_info");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        addWorkDialog = new BottomSheetDialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.insert_work_dialog, null);
        txt_save = view.findViewById(R.id.txt_save);

        work_name = view.findViewById(R.id.workNameInput);
        alarm_work = view.findViewById(R.id.workClockInput);
        txt_save = view.findViewById(R.id.txt_save);

        work_name.setText(workModel.getWork_name());
        alarm_work.setText(workModel.getAlarm());

        txt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(work_name.length() > 0){
                    workModel.setWork_name(work_name.getText().toString());
                    workModel.setAlarm(alarm_work.getText().toString());
                    if(alarm_work.getText().length() > 0){
                        workModel.setAlarmOn(true);
                        alarmSet = true;
                    }
                    updateCall.updateWork(workModel, 0);
                    addWorkDialog.cancel();

                    if(alarmSet){
                        alarmSet = false;
                        startService();
                    }

                    Toast.makeText(getContext(), R.string.update_done_txt, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), R.string.empty_field_message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        clockDialog();
        addWorkDialog.setContentView(view);
        return addWorkDialog;
    }

    public void startService(){
        Intent intent = new Intent(getContext(), AlarmWorkService.class);
        ContextCompat.startForegroundService(getContext(), intent);
    }

    @SuppressLint("SetTextI18n")
    public void setTimeToTextView(int hour, int minute){
        if(hour < 10){
            alarm_work.setText("0" + hour + ":" + minute);
        }
        if ( minute < 10){
            alarm_work.setText( hour + ":" + "0" + minute);
        }
        if ( minute < 10 && hour < 10){
            alarm_work.setText("0" + hour + ":" + "0" + minute);
        }
        if (minute >= 10 && hour >= 10){
            alarm_work.setText(hour + ":" + minute);
        }
    }


    public void clockDialog(){

        // get current time of system
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        alarm_work.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        setTimeToTextView(i , i1);
                    }
                }, hour, minute, true);

                timePickerDialog.setButton(timePickerDialog.BUTTON_POSITIVE, getString(R.string.save), timePickerDialog);
                timePickerDialog.setButton(timePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), timePickerDialog);

                timePickerDialog.setCancelable(false);

                timePickerDialog.create();
                timePickerDialog.show();
            }
        });
    }


    public interface UpdateCall{
        void updateWork(WorkModel model, int workPosition);
    }


}

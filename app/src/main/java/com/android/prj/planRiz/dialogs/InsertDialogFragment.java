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
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.android.prj.planRiz.AlarmWorkService;
import com.android.prj.planRiz.R;
import com.android.prj.planRiz.databinding.InsertWorkDialogBinding;
import com.android.prj.planRiz.workRelatedClass.WorkModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;


public class InsertDialogFragment extends BottomSheetDialogFragment {

    private InsertCall insertCall;
     EditText work_name ;
     EditText alarm_work;
     String work_date;
    WorkModel workModel;
    boolean alarmSet = false;
    TextView txt_save;
    BottomSheetDialog addWorkDialog;
    TextInputLayout inputClockBox;
    InsertWorkDialogBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        insertCall = (InsertCall) context;
        work_date = getArguments().getString("workDate");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        addWorkDialog = new BottomSheetDialog(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.insert_work_dialog, null);

        work_name = view.findViewById(R.id.workNameInput);
        alarm_work = view.findViewById(R.id.workClockInput);
        txt_save = view.findViewById(R.id.txt_save);
        inputClockBox = view.findViewById(R.id.clock_input);

        txt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(work_name.length() > 0){
                     workModel = new WorkModel();
                    workModel.setWork_name(work_name.getText().toString());
                    workModel.setWorkDate(work_date);
                    if(alarm_work.getText().length() > 0){
                        workModel.setAlarm(alarm_work.getText().toString());
                        workModel.setAlarmOn(true);
                        alarmSet = true;
                    }
                    insertCall.insertWork(workModel, 0);
                    addWorkDialog.cancel();
                    if(alarmSet){
                        alarmSet = false;
                        startService();
                    }

                    Toast.makeText(getContext(),  R.string.insert_done_txt, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), R.string.empty_field_message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        addWorkDialog.setContentView(view);
        clockDialog();

        return addWorkDialog;
    }

    public void startService(){
        Intent intent = new Intent(getContext(), AlarmWorkService.class);
        ContextCompat.startForegroundService(getContext(), intent);
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
                        alarm_work.setText(String.format("%02d:%02d", i, i1));
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

    public interface InsertCall{
        void insertWork(WorkModel model, int workPosition);
    }


}

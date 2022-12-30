package com.android.prj.planRiz.activity;

import static com.android.prj.planRiz.R.drawable.datepicker_shape;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import com.android.prj.planRiz.AlarmWorkService;
import com.android.prj.planRiz.DataBase;
import com.android.prj.planRiz.databinding.ActivityMainBinding;
import com.android.prj.planRiz.dialogs.InsertDialogFragment;
import com.android.prj.planRiz.R;
import com.android.prj.planRiz.daysRelatedClass.DaysAdapter;
import com.android.prj.planRiz.daysRelatedClass.DaysModel;
import com.android.prj.planRiz.daysRelatedClass.DaysDao;
import com.android.prj.planRiz.dialogs.UpdateDialogFragment;
import com.android.prj.planRiz.workRelatedClass.WorkDao;
import com.android.prj.planRiz.workRelatedClass.WorkModel;
import com.android.prj.planRiz.workRelatedClass.WorksAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.sql.DataTruncation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import ir.hamsaa.persiandatepicker.Listener;
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.util.PersianCalendar;
import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class MainActivity extends AppCompatActivity implements InsertDialogFragment.InsertCall
        , UpdateDialogFragment.UpdateCall, WorksAdapter.CallOperation {

    private RecyclerView daysRecycler;
    private FloatingActionButton calendarBtn;
    private DaysDao daysDao;
    private List<DaysModel> daysModelList = new ArrayList<>();
    private DaysAdapter daysAdapter;
    private DaysModel daysModel;
    private WorksAdapter worksAdapter;
    private WorkDao workDao;
    private PersianDate persianDate;
    private PersianDate gregorianDate;
    private int dayNumber = 0;
    private int dayNumberCount;
    private List<DaysModel> searchedDate = new ArrayList<>();
    private String dateCurrent;
    private FirebaseAnalytics firebaseAnalytics;
//    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        View view = binding.getRoot();
        setContentView(R.layout.activity_main);
        firebaseSetting();
        defineAndClickedViews();
        insertDates(0, 0, 0);
        showChanges(dayNumber, 0);
        continueService();
    }

    private void firebaseSetting(){
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "myContent");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "image");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        Bundle bundle2 = new Bundle();
        bundle2.putString(FirebaseAnalytics.Param.SCREEN_NAME, "myScreen");
        bundle2.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity");
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle2);
    }

    void continueService(){
        // turn service on if there is another alarm for today
        List<WorkModel> workModelList = workDao.getWorks();
        for(int i = 0; i < workModelList.size(); i ++){
          if(workModelList.get(i).isAlarmOn()){
              startService();
              break;
          }
        }

    }

    public void startService(){
        Intent intent = new Intent(this, AlarmWorkService.class);
        ContextCompat.startForegroundService(this, intent);
    }

    public void insertDates(int year, int month, int day) {
        // return current date and day number in week
        persianDate = new PersianDate();
        gregorianDate = new PersianDate();

        if(year > 0){
            setInputDate(year, month, day);
        }
        searchDate();
        // add each date to list just once
        if (searchedDate.size() == 0) {
            for (int i = 0; i < 7; i++) {
                persianDate = new PersianDate();
                gregorianDate = new PersianDate();

                if(year > 0){
                    setInputDate(year, month, day);
                }
                // show current date or input date
                if (i == dayNumber) {
                    daysModel.setDate(getPersianDate(persianDate));
                    daysModel.setGrDate(getGregorianDate(gregorianDate));
                    dayNumberCount ++;
                }
                // show dates before current date or input date
                else if (i < dayNumber) {
                    PersianDate date = persianDate.subDays(dayNumberCount);
                    daysModel.setDate(getPersianDate(date));

                    PersianDate grDate = gregorianDate.subDays(dayNumberCount);
                    daysModel.setGrDate(getGregorianDate(grDate));
                    dayNumberCount--;
                }
                // show dates after current date or input date
                else {
                    PersianDate date = persianDate.subDays(-dayNumberCount);
                    daysModel.setDate(getPersianDate(date));

                    PersianDate grDate = gregorianDate.subDays(-dayNumberCount);
                    daysModel.setGrDate(getGregorianDate(grDate));
                    dayNumberCount ++;
                }
                daysDao.insertDate(daysModel);
            }
        }
        DaysModel daysModel2 = daysDao.getSpecialDate(dateCurrent);
        showInputDateWeek(daysModel2);
    }

    private String getPersianDate(PersianDate persianDate){
        return PersianDateFormat.format(persianDate, "l j F Y", PersianDateFormat.PersianDateNumberCharacter.FARSI);
    }


    public String getGregorianDate(PersianDate gregorianDate){
        Calendar calendar = Calendar.getInstance();
        calendar.set(gregorianDate.getGrgYear(),  gregorianDate.getGrgMonth()-1, gregorianDate.getGrgDay());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMMM, yyyy");
        String date = dateFormat.format(calendar.getTime());
        Log.d("4030", date);
        return date;
    }

    // saerch a date and add to searchedDate list
    public void searchDate(){
        dayNumber = persianDate.dayOfWeek();
        dayNumberCount = dayNumber;

        // search each date in database to prevent generating duplicate date
        dateCurrent = getPersianDate(persianDate);
        searchedDate = daysDao.searchDate(dateCurrent);
    }

    // change current date to intended date
    public void setInputDate(int year, int month, int day){
        persianDate.setShYear(year);
        persianDate.setShMonth(month);
        persianDate.setShDay(day);

        gregorianDate.setShYear(year);
        gregorianDate.setShMonth(month);
        gregorianDate.setShDay(day);
    }

    public void  showChanges(int dayPosition, int workPosition) {
        // define dates recyclerview and show its item
        daysModel = new DaysModel();
        StaggeredGridLayoutManager staggeredManager = new StaggeredGridLayoutManager(
                1, StaggeredGridLayoutManager.HORIZONTAL);
        daysRecycler.setLayoutManager(staggeredManager);
        staggeredManager.scrollToPosition(dayPosition);
        daysDao = DataBase.getDataBase(this).getDaysDao();
        daysAdapter = new DaysAdapter(this, workPosition);
        daysRecycler.setAdapter(daysAdapter);
        daysAdapter.showItem(daysModelList);

        worksAdapter = new WorksAdapter(this);
        workDao = DataBase.getDataBase(this).getWorkDao();
    }

    // method to find views and check click on them
    public void defineAndClickedViews() {

        daysRecycler = findViewById(R.id.daysRecycler);
        calendarBtn  = findViewById(R.id.calendar_btn);

        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                PersianDatePickerDialog datePickerDialog = new PersianDatePickerDialog(MainActivity.this);
                datePickerDialog.setListener(new Listener() {
                    @Override
                    public void onDateSelected(PersianCalendar persianCalendar) {
                        int inputYear = persianCalendar.getPersianYear();
                        int inputMonth = persianCalendar.getPersianMonth();
                        int inputDay = persianCalendar.getPersianDay();

                        insertDates(inputYear, inputMonth, inputDay);
                        searchDate();
                        showInputDateWeek(searchedDate.get(0));
                    }

                    @Override
                    public void onDismissed() {

                    }
                });

                datePickerDialog.setPickerBackgroundDrawable(datepicker_shape);
                datePickerDialog.show();
            }
        });
        showChanges(dayNumber, 0);

        firebaseAnalytics.logEvent("but", null);
    }

    public void showInputDateWeek(DaysModel inputDaysModel){
        daysAdapter.deleteAllItems();
        daysModelList.clear();
        dayNumberCount = dayNumber;

        DaysModel currentDaysModel = new DaysModel();
        long id = inputDaysModel.getId();

        for (int i = 0; i < 7; i++) {
            // show current date or input date
            if (i == dayNumber) {
                currentDaysModel = inputDaysModel;
                dayNumberCount ++;
            }
            // show dates before current date or input date
            else if (i < dayNumber) {
                currentDaysModel = daysDao.getDateBeforeOrAfter(id, dayNumberCount);
                dayNumberCount --;
            }
            // show dates after current date or input date
            else {
                currentDaysModel = daysDao.getDateBeforeOrAfter(id, -dayNumberCount);
                dayNumberCount ++;
            }
            daysAdapter.insertItem(currentDaysModel);
            daysModelList.add(currentDaysModel);
        }

    }

    // set feature for menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        // show result search
        MenuItem searchItem = menu.findItem(R.id.search_btn);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(700);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<DaysModel> daysModelList = daysDao.searchDate(s);
                daysAdapter.searchItems(daysModelList);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
    boolean confirmation = false;

    // menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_all) {
            confirmation = confirmDialog(getString(R.string.delete_all_activity_txt), true, false);
        }

        if (item.getItemId() == R.id.off_alarm_menu){
            confirmation = confirmDialog(getString(R.string.delete_all_alarm_txt), false, true);
        }

        return super.onOptionsItemSelected(item);
    }


    private void deleteAllActivities(){
        workDao.deleteAll();
        worksAdapter.deleteAllItems();

        daysDao.deleteAllDates();
        daysAdapter.deleteAllItems();

        daysDao.resetPrimaryKey();
        showChanges(dayNumber, 0);
    }

    private boolean confirmDialog(String confirmMessage, boolean deleteActivity, boolean deleteAlarm){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(confirmMessage)
                .setPositiveButton("بله", (dialogInterface, i) -> {
                    if(deleteActivity){
                        deleteAllActivities();
                    }

                    if(deleteAlarm){
                        turnOffAllAlarm();
                    }
                })
                .setNegativeButton("خیر", (dialogInterface, i) -> {
                    confirmation = false;
                } )
                .create()
                .show();

        return confirmation;
    }


    private void turnOffAllAlarm(){
        List<WorkModel> workModelList = workDao.getWorks();

        for(int i = 0; i < workModelList.size(); i ++){
            if (!workModelList.get(i).isAlarmOn()) {
                continue;
            }
            workModelList.get(i).setAlarmOn(false);
            workModelList.get(i).setAlarm("");
            workDao.updateWork(workModelList.get(i));
            showChanges(dayNumber, 0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void insertWork(WorkModel model, int workPosition) {
        workDao.insertWork(model);
        worksAdapter.addItems(model);
        checkDayNumber(model, workPosition);
    }

    @Override
    public void updateWork(WorkModel model, int workPosition) {
        workDao.updateWork(model);
        worksAdapter.updateItems(model);
        checkDayNumber(model, workPosition);
    }

    @Override
    public void deleteRecord(WorkModel workModel, int workPosition) {
        workDao.deleteWork(workModel);
        worksAdapter.deleteItems(workModel);
        checkDayNumber(workModel, workPosition);
    }

    @Override
    public void updateDoneUndone(WorkModel workModel, int workPosition) {
        workDao.updateWork(workModel);
        worksAdapter.updateItems(workModel);
        checkDayNumber(workModel, workPosition);
    }

    // to not jump current day
    public void checkDayNumber(WorkModel workModel, int workPosition){
        DaysModel daysModel = daysDao.getSpecialDate(workModel.getWorkDate());
        String dateString = daysModel.getDate();
        String daysOfWeek[] = {"شنبه", "یک", "دو", "سه", "چهار", "پنج", "جمعه"};
        for(int i = 0; i < daysOfWeek.length; i++){
            boolean b = dateString.startsWith(daysOfWeek[i]);
            if(b){
                showChanges(i, workPosition);
            }
        }
    }


}
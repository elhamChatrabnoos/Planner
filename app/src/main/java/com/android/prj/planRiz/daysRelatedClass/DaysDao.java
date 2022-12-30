package com.android.prj.planRiz.daysRelatedClass;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DaysDao {

    @Insert
    long insertDate(DaysModel model);

    @Query("SELECT * FROM dates_tbl")
    List<DaysModel> getdDtes();

    @Query("DELETE FROM sqlite_sequence")
    void resetPrimaryKey();

    @Query("DELETE FROM dates_tbl")
    void deleteAllDates();

    @Query("SELECT *  FROM dates_tbl WHERE date LIKE '%' || :dateInput || '%'")
    List<DaysModel> searchDate(String dateInput);

    @Query("SELECT * FROM DATES_TBL WHERE date == :inputDate")
    DaysModel getSpecialDate(String inputDate);

    @Query("SELECT * FROM DATES_TBL WHERE id == :inputId - :number")
    DaysModel getDateBeforeOrAfter(long inputId, int number);

}

package com.android.prj.planRiz.workRelatedClass;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao

public interface WorkDao {

    @Insert
    long insertWork(com.android.prj.planRiz.workRelatedClass.WorkModel model);

    @Delete
    int deleteWork(com.android.prj.planRiz.workRelatedClass.WorkModel model);

    @Update
    int updateWork(com.android.prj.planRiz.workRelatedClass.WorkModel model);

    @Query("SELECT * FROM works_tbl")
    List<com.android.prj.planRiz.workRelatedClass.WorkModel> getWorks();

    @Query("DELETE FROM works_tbl")
    void deleteAll();

    @Query("SELECT * FROM works_tbl WHERE workDate = :date_text")
    List<com.android.prj.planRiz.workRelatedClass.WorkModel> getWorkFromDate(String date_text);

    @Query("SELECT * FROM works_tbl WHERE work_name LIKE '%' || :inputText|| '%'")
    List<WorkModel> searchWork(String inputText);

}

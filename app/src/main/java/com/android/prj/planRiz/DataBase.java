package com.android.prj.planRiz;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.android.prj.planRiz.daysRelatedClass.DaysDao;
import com.android.prj.planRiz.daysRelatedClass.DaysModel;
import com.android.prj.planRiz.workRelatedClass.WorkDao;
import com.android.prj.planRiz.workRelatedClass.WorkModel;


@Database(version = 4, exportSchema = false,
        entities = {WorkModel.class , DaysModel.class})

public abstract class DataBase extends RoomDatabase {

    private static DataBase dataBase;

    public static DataBase getDataBase(Context context){
        if (dataBase == null){
            dataBase = Room.databaseBuilder(context.getApplicationContext(),
                    DataBase.class, "db_1").fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }
        return dataBase;
    }

    public abstract WorkDao getWorkDao();
    public abstract DaysDao getDaysDao();

}

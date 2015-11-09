package com.tobyrich.app.SmartPlane.dispatcher;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.api.model.DatabaseHelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by anon on 02.11.2015.
 */
public class PersistDataService {
    private SQLiteDatabase connection;
    @Inject
    private DatabaseHelper databasehelper;

    @Inject
    public PersistDataService(DatabaseHelper databasehelper){
        this.databasehelper = databasehelper;
    }

    public void saveRudderData(Map<Long, Short> map){
        connection = databasehelper.getWritableDatabase();
        SQLiteStatement preparedStatement = connection.compileStatement(databasehelper.getInsertStatementRudder());
        for (Map.Entry<Long, Short> entry : map.entrySet())
        {
            if (entry.getKey() != null && entry.getValue() != null) {
                preparedStatement.bindString(1, entry.getKey().toString());
                preparedStatement.bindString(2, entry.getValue().toString());
                preparedStatement.execute();
            }
        }
        preparedStatement.close();
        connection.close();
    }

    public void saveMotorData(Map<Long, Short> map){
        connection = databasehelper.getWritableDatabase();
        SQLiteStatement preparedStatement = connection.compileStatement(databasehelper.getInsertStatementMotor());
        for (Map.Entry<Long, Short> entry : map.entrySet())
        {
            if (entry.getKey() != null && entry.getValue() != null) {
                preparedStatement.bindString(1, entry.getKey().toString());
                preparedStatement.bindString(2, entry.getValue().toString());
                preparedStatement.execute();
            }
        }
        preparedStatement.close();
        connection.close();
    }


    public Map<Long, Short> getAllRudderData(){
        connection = databasehelper.getWritableDatabase();
        Map<Long, Short> resultMap = new LinkedHashMap<Long, Short>();
        Cursor cursor = connection.query(
                databasehelper.getTableNameRudder(),        //Table name
                databasehelper.getSelectRudderColumns(),    //Columns in result
                null,                                       //no furhter selection
                null,                                       //no further selection
                null,                                       //no group by
                null,                                       //no having
                null                                        //no order by
        );
        if (cursor.moveToFirst()){
            do {
                resultMap.put(cursor.getLong(0), cursor.getShort(1));
            } while (cursor.moveToNext());
        }

        //Delete data from table
        SQLiteStatement preparedStatement = connection.compileStatement(databasehelper.getDeleteStatementRudder());
        preparedStatement.execute();
        preparedStatement.close();

        connection.close();
        return resultMap;
    }

    public Map<Long, Short> getAllMotorData(){
        connection = databasehelper.getWritableDatabase();
        Map<Long, Short> resultMap = new LinkedHashMap<Long, Short>();
        Cursor cursor = connection.query(
                databasehelper.getTableNameMotor(),        //Table name
                databasehelper.getSelectMotorColumns(),    //Columns in result
                null,                                       //no furhter selection
                null,                                       //no further selection
                null,                                       //no group by
                null,                                       //no having
                null                                        //no order by
        );
        if (cursor.moveToFirst()){
            do {
                resultMap.put(cursor.getLong(0), cursor.getShort(1));
            } while (cursor.moveToNext());
        }

        //Delete data from table
        SQLiteStatement preparedStatement = connection.compileStatement(databasehelper.getDeleteStatementMotor());
        preparedStatement.execute();
        preparedStatement.close();

        connection.close();
        return resultMap;
    }


}

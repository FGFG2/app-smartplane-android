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

    public void saveData(ValueType type, Map<Long, Short> map) {
        connection = databasehelper.getWritableDatabase();
        String insertStatement = "";

        switch (type) {
            case MOTOR:
                insertStatement = databasehelper.getInsertStatementMotor();
                break;
            case RUDDER:
                insertStatement = databasehelper.getInsertStatementRudder();
                break;
            case CONNECTION_STATE:
                break;
        }

        SQLiteStatement preparedStatement = connection.compileStatement(insertStatement);
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


    public Map<Long, Short> getAllData(ValueType type){
        connection = databasehelper.getWritableDatabase();
        Map<Long, Short> resultMap = new LinkedHashMap<Long, Short>();
        String tablename = "";
        String[] tableColumns = {""};
        String deletestatement = "";

        //Save table and column names according to ValueType
        switch (type) {
            case MOTOR:
                tablename = databasehelper.getTableNameMotor();
                tableColumns = databasehelper.getSelectMotorColumns();
                deletestatement = databasehelper.getDeleteStatementMotor();
                break;
            case RUDDER:
                tablename = databasehelper.getTableNameRudder();
                tableColumns = databasehelper.getSelectRudderColumns();
                deletestatement = databasehelper.getDeleteStatementRudder();
                break;
            case CONNECTION_STATE:
                //TODO: machen
                break;
        }

        if (tablename.length() > 0 && tableColumns.length > 0 && deletestatement.length() > 0) {
            //Query database with select * from $tablename
            Cursor cursor = connection.query(
                    tablename,        //Table name
                    tableColumns,    //Columns in result
                    null,                                       //no furhter selection
                    null,                                       //no further selection
                    null,                                       //no group by
                    null,                                       //no having
                    null                                        //no order by
            );

            //Fill map with data
            if (cursor.moveToFirst()) {
                do {
                    resultMap.put(cursor.getLong(0), cursor.getShort(1));
                } while (cursor.moveToNext());
            }

            //Delete data from table
            SQLiteStatement preparedStatement = connection.compileStatement(deletestatement);
            preparedStatement.execute();
            preparedStatement.close();

            connection.close();
        }
        return resultMap;
    }

}

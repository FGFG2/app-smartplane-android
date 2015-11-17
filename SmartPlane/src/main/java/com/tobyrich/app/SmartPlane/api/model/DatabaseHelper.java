package com.tobyrich.app.SmartPlane.api.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.R;

/**
 * Created by anon on 02.11.2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private Context context;

    @Inject
    public DatabaseHelper(Context context){
        super(
                context,
                context.getResources().getString(R.string.dbname),
                null,
                Integer.parseInt(context.getResources().getString(R.string.version)));
        this.context=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for(String sql : context.getResources().getStringArray(R.array.create))
            db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public String getInsertStatementRudder(){
        return context.getResources().getString(R.string.insertRudderPrep);
    }

    public String getInsertStatementMotor(){
        return context.getResources().getString(R.string.insertMotorPrep);
    }

    public String getInsertStatementConnection(){
        return context.getResources().getString(R.string.insertConnectionPrep);
    }

    public String[] getSelectRudderColumns(){
        return context.getResources().getStringArray(R.array.selectRudderColumns);
    }

    public String[] getSelectMotorColumns(){
        return context.getResources().getStringArray(R.array.selectMotorColumns);
    }

    public String[] getSelectConnectionColumns(){
        return context.getResources().getStringArray(R.array.selectConnectionColumns);
    }

    public String getDeleteStatementRudder(){
        return context.getResources().getString(R.string.deleteRudderPrep);
    }

    public String getDeleteStatementMotor(){
        return context.getResources().getString(R.string.deleteMotorPrep);
    }

    public String getDeleteStatementConnections(){
        return context.getResources().getString(R.string.deleteConnectionPrep);
    }

    public String getTableNameRudder(){
        return context.getResources().getString(R.string.tableNameRudder);
    }

    public String getTableNameMotor(){
        return context.getResources().getString(R.string.tableNameMotor);
    }

    public String getTableNameConnection(){
        return context.getResources().getString(R.string.tableNameConnection);
    }
}
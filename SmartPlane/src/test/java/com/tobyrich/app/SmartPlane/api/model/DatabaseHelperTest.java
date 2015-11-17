package com.tobyrich.app.SmartPlane.api.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.dispatcher.PersistDataService;
import com.tobyrich.app.SmartPlane.dispatcher.ValueType;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DatabaseHelperTest {
    @Inject
    private DatabaseHelper classUnderTest;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);

        // Override injector and perform injection
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }


    @Test
    public void databaseSetupTest() throws Exception {
        //Given
        ArrayList<String> tables = new ArrayList<String>();
        SQLiteDatabase connection = classUnderTest.getWritableDatabase();

        //When
        Cursor c =  connection.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        if (c.moveToFirst())
        {
            while ( !c.isAfterLast() ){
                tables.add( c.getString( c.getColumnIndex("name")) );
                c.moveToNext();
            }
        }

        //Then
        Assert.assertTrue(tables.contains("rudder"));
        Assert.assertTrue(tables.contains("motor"));
        Assert.assertTrue(tables.contains("connection"));
    }


    @Test
     public void dataBaseSaveReadMotor() throws Exception {
        //Given
        ValueType vt = ValueType.MOTOR;
        SQLiteDatabase connection = classUnderTest.getWritableDatabase();
        PersistDataService pds = new PersistDataService(classUnderTest);
        Map<Long, Object> storeValues = new LinkedHashMap<>();
        storeValues.put(10L, Short.valueOf("42"));
        storeValues.put(11L, Short.valueOf("43"));

        //When
        pds.saveData(vt, storeValues);
        Map<Long, Object> savedValues = pds.getAllData(vt);

        //Then
        Assert.assertEquals(savedValues, storeValues);

        //When getting motor data again
        savedValues = pds.getAllData(vt);

        //Then database should be empty
        Assert.assertTrue(savedValues.isEmpty());
    }

    @Test
    public void dataBaseSaveReadRudder() throws Exception {
        //Given
        ValueType vt = ValueType.RUDDER;
        SQLiteDatabase connection = classUnderTest.getWritableDatabase();
        PersistDataService pds = new PersistDataService(classUnderTest);
        Map<Long, Object> storeValues = new LinkedHashMap<>();
        storeValues.put(10L, Short.valueOf("42"));
        storeValues.put(11L, Short.valueOf("43"));

        //When
        pds.saveData(vt, storeValues);
        Map<Long, Object> savedValues = pds.getAllData(vt);

        //Then
        Assert.assertEquals(savedValues, storeValues);

        //When getting rudder data again
        savedValues = pds.getAllData(vt);

        //Then database should be empty
        Assert.assertTrue(savedValues.isEmpty());
    }

    @Test
    public void dataBaseSaveReadConnection() throws Exception {
        //Given
        ValueType vt = ValueType.CONNECTION_STATE;
        SQLiteDatabase connection = classUnderTest.getWritableDatabase();
        PersistDataService pds = new PersistDataService(classUnderTest);
        Map<Long, Object> storeValues = new LinkedHashMap<>();
        storeValues.put(10L, true);
        storeValues.put(11L, false);

        //When
        pds.saveData(vt, storeValues);
        Map<Long, Object> savedValues = pds.getAllData(vt);

        //Then
        Assert.assertEquals(savedValues, storeValues);

        //When getting connection data again
        savedValues = pds.getAllData(vt);

        //Then database should be empty
        Assert.assertTrue(savedValues.isEmpty());
    }

    @Test
    public void dataBaseSaveEmpty() throws Exception{
        //Given
        ValueType vt = ValueType.MOTOR;
        SQLiteDatabase connection = classUnderTest.getWritableDatabase();
        PersistDataService pds = new PersistDataService(classUnderTest);
        Map<Long, Object> storeValues = new LinkedHashMap<>();
        //storeValues.put(10L, Short.valueOf("42"));
        //storeValues.put(11L, Short.valueOf("43"));

        //When
        pds.saveData(vt, storeValues);
        vt = ValueType.RUDDER;
        pds.saveData(vt, storeValues);
        Map<Long, Object> savedValues = pds.getAllData(vt);

        //Then
        Assert.assertTrue(savedValues.isEmpty());
        vt = ValueType.MOTOR;
        savedValues = pds.getAllData(vt);
        Assert.assertTrue(savedValues.isEmpty());
    }

    @Test
    public void dataBaseSaveNull(){
        //Given
        ValueType vt = ValueType.RUDDER;
        SQLiteDatabase connection = classUnderTest.getWritableDatabase();
        PersistDataService pds = new PersistDataService(classUnderTest);
        Map<Long, Object> storeValues = new LinkedHashMap<>();
        storeValues.put(10L, null);
        storeValues.put(null, Short.valueOf("43"));

        //When
        pds.saveData(vt, storeValues);
        vt = ValueType.MOTOR;
        pds.saveData(vt, storeValues);
        Map<Long, Object> savedValues = pds.getAllData(vt);

        //Then
        Assert.assertTrue(savedValues.isEmpty());
        vt = ValueType.RUDDER;
        savedValues = pds.getAllData(vt);
        Assert.assertTrue(savedValues.isEmpty());
    }


    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }
}

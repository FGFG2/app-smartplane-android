package com.tobyrich.app.SmartPlane.api.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.dispatcher.PersistDataService;

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

/**
 * Created by anon on 09.11.2015.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DatabaseHelperTest {
    @Inject
    private DatabaseHelper classUnderTest;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);

        // Set up Mockito behavior


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
    }


    @Test
     public void dataBaseSaveReadMotor() throws Exception {
        //Given
        SQLiteDatabase connection = classUnderTest.getWritableDatabase();
        PersistDataService pds = new PersistDataService(classUnderTest);
        Map<Long, Short> storeValues = new LinkedHashMap<Long, Short>();
        storeValues.put(10L, Short.valueOf("42"));
        storeValues.put(11L, Short.valueOf("43"));

        //When
        pds.saveMotorData(storeValues);
        Map<Long, Short> savedValues = pds.getAllMotorData();

        //Then
        Assert.assertEquals(savedValues, storeValues);

        //When getting motor data again
        savedValues = pds.getAllMotorData();

        //Then database should be empty
        Assert.assertTrue(savedValues.isEmpty());
    }

    @Test
    public void dataBaseSaveReadRudder() throws Exception {
        //Given
        SQLiteDatabase connection = classUnderTest.getWritableDatabase();
        PersistDataService pds = new PersistDataService(classUnderTest);
        Map<Long, Short> storeValues = new LinkedHashMap<Long, Short>();
        storeValues.put(10L, Short.valueOf("42"));
        storeValues.put(11L, Short.valueOf("43"));

        //When
        pds.saveRudderData(storeValues);
        Map<Long, Short> savedValues = pds.getAllRudderData();

        //Then
        Assert.assertEquals(savedValues, storeValues);

        //When getting motor data again
        savedValues = pds.getAllRudderData();

        //Then database should be empty
        Assert.assertTrue(savedValues.isEmpty());
    }

    @Test
    public void dataBaseSaveEmpty() throws Exception{
        //Given
        SQLiteDatabase connection = classUnderTest.getWritableDatabase();
        PersistDataService pds = new PersistDataService(classUnderTest);
        Map<Long, Short> storeValues = new LinkedHashMap<Long, Short>();
        //storeValues.put(10L, Short.valueOf("42"));
        //storeValues.put(11L, Short.valueOf("43"));

        //When
        pds.saveRudderData(storeValues);
        pds.saveMotorData(storeValues);
        Map<Long, Short> savedValues = pds.getAllRudderData();

        //Then
        Assert.assertTrue(savedValues.isEmpty());
        savedValues = pds.getAllMotorData();
        Assert.assertTrue(savedValues.isEmpty());
    }

    @Test
    public void dataBaseSaveNull(){
        //Given
        SQLiteDatabase connection = classUnderTest.getWritableDatabase();
        PersistDataService pds = new PersistDataService(classUnderTest);
        Map<Long, Short> storeValues = new LinkedHashMap<Long, Short>();
        storeValues.put(10L, null);
        storeValues.put(null, Short.valueOf("43"));

        //When
        pds.saveRudderData(storeValues);
        pds.saveMotorData(storeValues);
        Map<Long, Short> savedValues = pds.getAllRudderData();

        //Then
        Assert.assertTrue(savedValues.isEmpty());
        savedValues = pds.getAllMotorData();
        Assert.assertTrue(savedValues.isEmpty());
    }


    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            //bind(RetrofitServiceManager.class).toInstance(retrofitServiceManager);
        }
    }

}

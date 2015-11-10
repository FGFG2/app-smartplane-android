package com.tobyrich.app.SmartPlane.dispatcher;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.api.model.DatabaseHelper;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.LinkedHashMap;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

/**
 * Created by anon on 03.11.2015.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class PersistDataServiceTest extends TestCase {

    @Inject
    private PersistDataService classUnderTest;

    @Mock
    private DatabaseHelper databasehelper;

    @Mock
    private SQLiteDatabase connection;

    @Mock
    private SQLiteStatement preparedStatement;

    @Mock
    private Cursor cursor;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);
        // Set up Mockito behavior
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);
    }

    @After
    public void tearDown() throws Exception {
        RoboGuice.Util.reset();
    }

    @Test
    public void testSaveRudderData() throws Exception {
        // Given
        Mockito.when(databasehelper.getWritableDatabase()).thenReturn(connection);
        Mockito.when(databasehelper.getInsertStatementRudder()).thenReturn("insert into...");
        Mockito.when(connection.compileStatement("insert into...")).thenReturn(preparedStatement);
        // list with 2 rudder data
        Map<Long, Short> rudderData = new LinkedHashMap<Long, Short>();
        rudderData.put(5L, (short) 42);
        rudderData.put(6L, (short) 42);

        // When save rudder data
        classUnderTest.saveRudderData(rudderData);

        // Then execute query 2 times
        Mockito.verify(preparedStatement, Mockito.times(2)).execute();
    }

    @Test
    public void testSaveMotorData() throws Exception {
        // Given
        Mockito.when(databasehelper.getWritableDatabase()).thenReturn(connection);
        Mockito.when(databasehelper.getInsertStatementMotor()).thenReturn("insert into...");
        Mockito.when(connection.compileStatement("insert into...")).thenReturn(preparedStatement);
        // list with 3 motor data
        Map<Long, Short> motorData = new LinkedHashMap<Long, Short>();
        motorData.put(5L, (short) 42);
        motorData.put(6L, (short) 42);
        motorData.put(7L, (short) 42);

        // When save rudder data
        classUnderTest.saveMotorData(motorData);

        // Then execute query 3 times
        Mockito.verify(preparedStatement, Mockito.times(3)).execute();
    }

    @Test
    public void testGetAllRudderData() throws Exception {
        //Given
        String[] strings = {"42", "2"};
        Mockito.when(databasehelper.getWritableDatabase()).thenReturn(connection);
        Mockito.when(connection.query(Mockito.anyString(), Mockito.any(String[].class), Mockito.anyString(), Mockito.any(String[].class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(cursor);
        Mockito.when(databasehelper.getTableNameRudder()).thenReturn("rudder");
        Mockito.when(databasehelper.getSelectRudderColumns()).thenReturn(strings);
        Mockito.when(cursor.moveToFirst()).thenReturn(true);

        Mockito.when(cursor.getLong(0)).thenReturn(12l).thenReturn(13l);
        Mockito.when(cursor.getShort(1)).thenReturn(Short.valueOf("3")).thenReturn(Short.valueOf("42"));
        Mockito.when(cursor.moveToNext()).thenReturn(true).thenReturn(false);

        Mockito.when(databasehelper.getDeleteStatementRudder()).thenReturn("delete ...");
        Mockito.when(connection.compileStatement(Mockito.anyString())).thenReturn(preparedStatement);

        //When
        Map<Long, Short> resultMap = classUnderTest.getAllRudderData();

        //Then
        assertEquals(2, resultMap.size());
        Mockito.verify(preparedStatement, Mockito.times(1)).execute();
    }

    @Test
    public void testGetAllMotorData() throws Exception {
        //Given
        String[] strings = {"42", "2"};
        Mockito.when(databasehelper.getWritableDatabase()).thenReturn(connection);
        Mockito.when(connection.query(Mockito.anyString(), Mockito.any(String[].class), Mockito.anyString(), Mockito.any(String[].class), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(cursor);
        Mockito.when(databasehelper.getTableNameMotor()).thenReturn("motor");
        Mockito.when(databasehelper.getSelectMotorColumns()).thenReturn(strings);
        Mockito.when(cursor.moveToFirst()).thenReturn(true);

        Mockito.when(cursor.getLong(0)).thenReturn(12l).thenReturn(13l);
        Mockito.when(cursor.getShort(1)).thenReturn(Short.valueOf("3")).thenReturn(Short.valueOf("42"));
        Mockito.when(cursor.moveToNext()).thenReturn(true).thenReturn(false);

        Mockito.when(databasehelper.getDeleteStatementMotor()).thenReturn("delete ...");
        Mockito.when(connection.compileStatement(Mockito.anyString())).thenReturn(preparedStatement);

        //When
        Map<Long, Short> resultMap = classUnderTest.getAllMotorData();

        //Then
        assertEquals(2, resultMap.size());
        Mockito.verify(preparedStatement, Mockito.times(1)).execute();
    }
    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(DatabaseHelper.class).toInstance(databasehelper);
            bind(SQLiteDatabase.class).toInstance(connection);
            bind(Cursor.class).toInstance(cursor);
        }
    }

}
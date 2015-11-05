package com.tobyrich.app.SmartPlane.api.service;

import android.util.Log;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.api.ConnectionManager;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.model.Achievement;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.Response;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class AchievementServiceTest extends TestCase {

    @Inject
    private RetrofitServiceManager serviceManager;
    private AchievementService achievementService;

    @Before
    public void setUp() throws Exception {
        // Override injector and perform injection
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);

        // Set up service
        achievementService = serviceManager.getAchievmentService();
    }

    @Test
    public void integrationTestSendMotorData() throws Exception {
        // Given
        final Map<Long, Short> map = new LinkedHashMap<>();
        map.put(Calendar.getInstance().getTimeInMillis(), (short) 0);

        // When
        final Call<?> call = achievementService.setMotor(map);
        final Response<?> response = call.execute();

        // Then
        Log.i(this.getClass().getSimpleName(), "Got response with code: " + response.code());
        assertTrue(response.isSuccess());
    }

    @Test
    public void integrationTestSendRudderData() throws Exception {
        // Given
        final Map<Long, Short> map = new LinkedHashMap<>();
        map.put(Calendar.getInstance().getTimeInMillis(), (short) 0);

        // When
        final Call<?> call = achievementService.setRudder(map);
        final Response<?> response = call.execute();

        // Then
        Log.i(this.getClass().getSimpleName(), "Got response with code: " + response.code());
        assertTrue(response.isSuccess());
    }

    @Test
    public void integrationTestSendConnectionStatusData() throws Exception {
        // Given
        final Map<Long, Boolean> map = new LinkedHashMap<>();
        map.put(Calendar.getInstance().getTimeInMillis(), Boolean.TRUE);
        Thread.sleep(1);
        map.put(Calendar.getInstance().getTimeInMillis(), Boolean.FALSE);

        // When
        final Call<?> call = achievementService.setIsConnected(map);
        final Response<?> response = call.execute();

        // Then
        Log.i(this.getClass().getSimpleName(), "Got response with code: " + response.code());
        assertTrue(response.isSuccess());
    }

    @Test
    public void testGetAllAchievements() throws Exception {
        // When
        final Call<List<Achievement>> call = achievementService.getAllAchievements();
        final List<Achievement> achievementList = call.execute().body();

        // Then
        assertFalse(achievementList.isEmpty());
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(ConnectionManager.class).toInstance(new ConnectionManager());
        }
    }
}
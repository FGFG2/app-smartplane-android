package com.tobyrich.app.SmartPlane.dispatcher;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;

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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import retrofit.Call;
import retrofit.Response;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

import static org.junit.Assert.assertTrue;


@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class SendDataServiceTest {

    @Inject
    private SendDataService classUnderTest;
    @Mock
    private RetrofitServiceManager retrofitServiceManager;
    @Mock
    private AchievementService achievementService;
    @Mock
    private Call call;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);

        // Set up Mockito behavior
        Mockito.when(retrofitServiceManager.getAchievmentService()).thenReturn(achievementService);
        Mockito.when(achievementService.setMotor(Mockito.anyMap())).thenReturn(call);
        Mockito.when(achievementService.setRudder(Mockito.anyMap())).thenReturn(call);
        Mockito.when(achievementService.setIsConnected(Mockito.anyMap())).thenReturn(call);

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
    public void testSendMotorDataSuccess() throws Exception {
        // Given
        Map<Long, Short> map = getLongShortMap();
        Response response = Response.success(null);
        Mockito.when(call.execute()).thenReturn(response);

        // When
        Map<Long, Short> result = classUnderTest.sendMotorData(map);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSendMotorDataFail() throws Exception {
        // Given
        Map<Long, Short> map = getLongShortMap();
        Response response = Response.error(404, null);
        Mockito.when(call.execute()).thenReturn(response);

        // When
        Map<Long, Short> result = classUnderTest.sendMotorData(map);

        // Then
        assertTrue(result.size() == 1);
    }

    @Test
    public void testSendMotorDataException() throws Exception {
        // Given
        Map<Long, Short> map = getLongShortMap();
        Response response = Response.error(404, null);
        Mockito.when(call.execute()).thenThrow(new IOException("Test"));

        // When
        Map<Long, Short> result = classUnderTest.sendMotorData(map);

        // Then
        assertTrue(result.size() == 1);
    }

    @Test
    public void testSendMotorDataNull() throws Exception {
        // Given
        Map<Long, Short> map = getLongShortMap();
        Mockito.when(call.execute()).thenReturn(null);

        // When
        Map<Long, Short> result = classUnderTest.sendMotorData(map);

        // Then
        assertTrue(result.size() == 1);
    }

    @Test
    public void testSendRudderData() throws Exception {
        // Given
        Map<Long, Short> map = getLongShortMap();
        Response response = Response.success(null);
        Mockito.when(call.execute()).thenReturn(response);

        // When
        Map<Long, Short> result = classUnderTest.sendRudderData(map);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSendIsConnectedData() throws Exception {
        // Given
        Map<Long, Boolean> map = new LinkedHashMap<>();
        map.put(0L, Boolean.FALSE);
        Response response = Response.success(null);
        Mockito.when(call.execute()).thenReturn(response);

        // When
        Map<Long, Boolean> result = classUnderTest.sendIsConnectedData(map);

        // Then
        assertTrue(result.isEmpty());
    }

    private Map<Long, Short> getLongShortMap() {
        Map<Long, Short> map = new LinkedHashMap<>();
        map.put(0L, (short) 0);
        return map;
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(RetrofitServiceManager.class).toInstance(retrofitServiceManager);
        }
    }
}
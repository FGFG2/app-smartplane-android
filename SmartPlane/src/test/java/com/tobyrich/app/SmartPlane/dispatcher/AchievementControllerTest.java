package com.tobyrich.app.SmartPlane.dispatcher;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.dispatcher.event.ActivityStoppedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.connection.DataNotSendEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.connection.DataSendEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.valuechanged.ConnectionStatusChangedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.valuechanged.MotorChangedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.valuechanged.RudderChangedEvent;

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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class AchievementControllerTest extends TestCase {

    @Inject
    private AchievementController classUnderTest;

    @Mock
    private SendDataService sendDataService;

    @Mock
    private PersistDataService persistDataService;


    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);

        // Override injector and perform injection
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);

        // Init AchievmentController
        classUnderTest.startAchievementMonitoring();
    }

    @After
    public void teardown() {
        EventBus.getDefault().post(new ActivityStoppedEvent());
        RoboGuice.Util.reset();
    }

    @Test
    public void testOnEventBackgroundThreadOneValue() throws Exception {
        // When
        MotorChangedEvent event = new MotorChangedEvent(Optional.of((short) 0));
        classUnderTest.onEventBackgroundThread(event);

        // Then
        assertTrue(classUnderTest.getMotorMap().size() == 1);
        Mockito.verify(sendDataService, Mockito.never()).sendData(Mockito.anyMap(), Mockito.any(ValueType.class));
    }

    @Test
    public void testOnEventBackgroundThreadMotor() throws Exception {
        // Given
        Mockito.when(sendDataService.sendData(Mockito.anyMap(), Mockito.any(ValueType.class))).thenReturn(Collections.emptyMap());

        // When
        for (int i = 0; i < AchievementController.MOTOR_BUFFER_SIZE; i++) {
            // Wait to satisfy map precision --> one value per time in map
            Thread.sleep(AchievementController.PRECISION);
            MotorChangedEvent event = new MotorChangedEvent(Optional.of((short) i));
            classUnderTest.onEventBackgroundThread(event);
        }

        // Then
        assertEquals(Collections.emptyMap(), classUnderTest.getMotorMap());
        Mockito.verify(sendDataService).sendData(Mockito.anyMap(), Mockito.any(ValueType.class));
    }

    @Test
    public void testOnEventBackgroundThreadRudder() throws Exception {
        // Given
        Mockito.when(sendDataService.sendData(Mockito.anyMap(), Mockito.any(ValueType.class))).thenReturn(Collections.emptyMap());

        // When
        for (int i = 0; i < AchievementController.RUDDER_BUFFER_SIZE; i++) {
            // Wait to satisfy map precision --> one value per time in map
            Thread.sleep(AchievementController.PRECISION);
            RudderChangedEvent event = new RudderChangedEvent(Optional.of((short) i));
            classUnderTest.onEventBackgroundThread(event);
        }

        // Then
        assertEquals(Collections.emptyMap(), classUnderTest.getRudderMap());
        Mockito.verify(sendDataService).sendData(Mockito.anyMap(), Mockito.any(ValueType.class));
    }

    @Test
    public void testOnEventBackgroundThreadIsConnected() throws Exception {
        // Given
        Mockito.when(sendDataService.sendData(Mockito.anyMap(), Mockito.any(ValueType.class))).thenReturn(Collections.emptyMap());

        // When
        for (int i = 0; i < AchievementController.IS_CONNECTED_BUFFER_SIZE; i++) {
            // Wait to satisfy map precision --> one value per time in map
            Thread.sleep(AchievementController.PRECISION);
            ConnectionStatusChangedEvent event = new ConnectionStatusChangedEvent(Optional.of(Boolean.TRUE));
            classUnderTest.onEventBackgroundThread(event);
        }

        // Then
        assertEquals(Collections.emptyMap(), classUnderTest.getIsConnectedMap());
        Mockito.verify(sendDataService).sendData(Mockito.anyMap(), Mockito.any(ValueType.class));
    }

    @Test
    public void testOnEventBackgroundThreadNotSend() throws Exception {
        // Given
        Map<Long, Object> map = new LinkedHashMap<>();
        map.put(1L, (short) 1);
        DataNotSendEvent event = new DataNotSendEvent("", ValueType.MOTOR);

        // When
        classUnderTest.getMotorMap().putAll(map);
        classUnderTest.onEventBackgroundThread(event);

        // Then
        assertTrue(classUnderTest.getMotorMap().isEmpty());
        assertTrue(classUnderTest.isCouldNotSendPreviousData());
        Mockito.verify(persistDataService).saveData(ValueType.MOTOR, classUnderTest.getMotorMap());
    }

    @Test
    public void testOnEventBackgroundThreadSend() throws Exception {
        // Given
        Map<Long, Object> map = new LinkedHashMap<>();
        map.put(1L, (short) 1);
        Mockito.when(persistDataService.getAllData(Mockito.any(ValueType.class))).thenReturn(map);
        DataSendEvent event = new DataSendEvent(ValueType.MOTOR);

        // When
        classUnderTest.setCouldNotSendPreviousData(true);
        classUnderTest.onEventBackgroundThread(event);

        // Then
        assertEquals(map, classUnderTest.getMotorMap());
        assertEquals(map, classUnderTest.getRudderMap());
        assertEquals(map, classUnderTest.getIsConnectedMap());
        assertFalse(classUnderTest.isCouldNotSendPreviousData());
        Mockito.verify(sendDataService, Mockito.never()).sendData(Mockito.anyMap(), Mockito.any(ValueType.class));
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(SendDataService.class).toInstance(sendDataService);
            bind(PersistDataService.class).toInstance(persistDataService);
        }
    }
}
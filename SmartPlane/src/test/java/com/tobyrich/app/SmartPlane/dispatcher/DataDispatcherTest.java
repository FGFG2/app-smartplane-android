package com.tobyrich.app.SmartPlane.dispatcher;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.dispatcher.event.ConnectionStatusChangedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.MotorChangedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.RudderChangedEvent;

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

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class DataDispatcherTest extends TestCase {

    @Inject
    private DataDispatcher classUnderTest;

    @Mock
    private SendDataService sendDataService;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);

        // Override injector and perform injection
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);

        // Init DataDispatcher
        classUnderTest.startAchievementMonitoring();
    }

    @After
    public void teardown() {
        classUnderTest.stopAchievementMonitoring();
        RoboGuice.Util.reset();
    }

    @Test
    public void testOnEventBackgroundThreadOneValue() throws Exception {
        // When
        MotorChangedEvent event = new MotorChangedEvent(Optional.of((short) 0));
        classUnderTest.onEventBackgroundThread(event);

        // Then
        assertTrue(classUnderTest.getMotorMap().size() == 1);
        Mockito.verify(sendDataService, Mockito.never()).sendMotorData(Mockito.anyMap());
    }

    @Test
    public void testOnEventBackgroundThreadMotor() throws Exception {
        // Given
        Mockito.when(sendDataService.sendMotorData(Mockito.anyMap())).thenReturn(Collections.emptyMap());

        // When
        for (int i = 0; i < DataDispatcher.MOTOR_BUFFER_SIZE; i++) {
            // Wait to satisfy map precision --> one value per time in map
            Thread.sleep(DataDispatcher.PRECISION);
            MotorChangedEvent event = new MotorChangedEvent(Optional.of((short) i));
            classUnderTest.onEventBackgroundThread(event);
        }

        // Then
        assertEquals(Collections.emptyMap(), classUnderTest.getMotorMap());
        Mockito.verify(sendDataService).sendMotorData(Mockito.anyMap());
    }

    @Test
    public void testOnEventBackgroundThreadRudder() throws Exception {
        // Given
        Mockito.when(sendDataService.sendRudderData(Mockito.anyMap())).thenReturn(Collections.emptyMap());

        // When
        for (int i = 0; i < DataDispatcher.RUDDER_BUFFER_SIZE; i++) {
            // Wait to satisfy map precision --> one value per time in map
            Thread.sleep(DataDispatcher.PRECISION);
            RudderChangedEvent event = new RudderChangedEvent(Optional.of((short) i));
            classUnderTest.onEventBackgroundThread(event);
        }

        // Then
        assertEquals(Collections.emptyMap(), classUnderTest.getRudderMap());
        Mockito.verify(sendDataService).sendRudderData(Mockito.anyMap());
    }

    @Test
    public void testOnEventBackgroundThreadIsConnected() throws Exception {
        // Given
        Mockito.when(sendDataService.sendIsConnectedData(Mockito.anyMap())).thenReturn(Collections.emptyMap());

        // When
        for (int i = 0; i < DataDispatcher.IS_CONNECTED_BUFFER_SIZE; i++) {
            // Wait to satisfy map precision --> one value per time in map
            Thread.sleep(DataDispatcher.PRECISION);
            ConnectionStatusChangedEvent event = new ConnectionStatusChangedEvent(Optional.of(Boolean.TRUE));
            classUnderTest.onEventBackgroundThread(event);
        }

        // Then
        assertEquals(Collections.emptyMap(), classUnderTest.getIsConnectedMap());
        Mockito.verify(sendDataService).sendIsConnectedData(Mockito.anyMap());
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(SendDataService.class).toInstance(sendDataService);
        }
    }
}
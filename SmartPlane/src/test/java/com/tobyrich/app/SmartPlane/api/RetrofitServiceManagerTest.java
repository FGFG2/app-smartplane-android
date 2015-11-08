package com.tobyrich.app.SmartPlane.api;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class RetrofitServiceManagerTest extends TestCase {

    @Inject
    private RetrofitServiceManager classUnderTest;
    private ConnectionManager connectionManager;

    @Before
    public void setUp() throws Exception {
        // Spy on object to create stub for specific mocking
        // Reason for stubbing: cannot mock Retrofit object
        connectionManager = Mockito.spy(new ConnectionManager());

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
    public void testGetAchievmentService() throws Exception {
        // Given
        Mockito.when(connectionManager.isNetworkAvailable()).thenReturn(Boolean.TRUE);

        // When
        Optional<AchievementService> achievementServiceOptional = Optional.fromNullable(classUnderTest.getAchievmentService());

        // Then
        assertTrue(achievementServiceOptional.isPresent());
        Mockito.verify(connectionManager).isNetworkAvailable();
        Mockito.verify(connectionManager).getRetrofitConnection();
    }

    @Test
    public void testGetAchievmentServiceNoConnection() throws Exception {
        // Given
        Mockito.when(connectionManager.isNetworkAvailable()).thenReturn(Boolean.FALSE);
        Optional<AchievementService> achievementServiceOptional = Optional.absent();

        // When
        try {
            achievementServiceOptional = Optional.fromNullable(classUnderTest.getAchievmentService());
        } catch (IOException ignored) {
        }

        // Then
        assertFalse(achievementServiceOptional.isPresent());
        Mockito.verify(connectionManager).isNetworkAvailable();
        Mockito.verify(connectionManager, Mockito.never()).getRetrofitConnection();
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(ConnectionManager.class).toInstance(connectionManager);
        }
    }
}
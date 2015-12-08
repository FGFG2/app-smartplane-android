package com.tobyrich.app.SmartPlane.api;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.squareup.okhttp.Interceptor;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;
import com.tobyrich.app.SmartPlane.api.service.UserService;

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

import java.io.IOException;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class RetrofitServiceManagerTest extends TestCase {

    @Inject
    private RetrofitServiceManager classUnderTest;

    @Mock
    private AuthInterceptor authInterceptor;

    private ConnectionManager connectionManager;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);

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
        Optional<String> optional = Optional.of("123");
        Mockito.when(connectionManager.isNetworkAvailable()).thenReturn(Boolean.TRUE);
        Mockito.when(authInterceptor.getToken()).thenReturn(optional);

        // When
        Optional<AchievementService> achievementServiceOptional = Optional.fromNullable(classUnderTest.getAchievmentService());

        // Then
        assertTrue(achievementServiceOptional.isPresent());
        Mockito.verify(connectionManager).isNetworkAvailable();
        Mockito.when(authInterceptor.getToken()).thenReturn(optional);
        Mockito.verify(connectionManager).getRetrofitConnection(Optional.of(authInterceptor));
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
        Mockito.verify(connectionManager, Mockito.never()).getRetrofitConnection(Optional.<Interceptor>absent());
    }

    @Test
    public void testGetAchievmentServiceNoToken() throws Exception {
        // Given
        Optional<String> optional = Optional.absent();
        Mockito.when(connectionManager.isNetworkAvailable()).thenReturn(Boolean.TRUE);
        Mockito.when(authInterceptor.getToken()).thenReturn(optional);
        Optional<AchievementService> achievementServiceOptional = Optional.absent();

        // When
        try {
            achievementServiceOptional = Optional.fromNullable(classUnderTest.getAchievmentService());
        } catch (IOException ignored) {
        }

        // Then
        assertFalse(achievementServiceOptional.isPresent());
        Mockito.verify(connectionManager).isNetworkAvailable();
        Mockito.verify(authInterceptor).getToken();
        Mockito.verify(connectionManager, Mockito.never()).getRetrofitConnection(Optional.<Interceptor>absent());
    }

    @Test
    public void testUserService() throws Exception {
        // Given
        Mockito.when(connectionManager.isNetworkAvailable()).thenReturn(Boolean.TRUE);

        // When
        Optional<UserService> userServiceOptional = Optional.fromNullable(classUnderTest.getUserService());

        // Then
        assertTrue(userServiceOptional.isPresent());
        Mockito.verify(connectionManager).isNetworkAvailable();
        Mockito.verify(connectionManager).getRetrofitConnection(Optional.<Interceptor>absent());
    }

    @Test
    public void testGetUserServiceNoConnection() throws Exception {
        // Given
        Mockito.when(connectionManager.isNetworkAvailable()).thenReturn(Boolean.FALSE);
        Optional<UserService> userServiceOptional = Optional.absent();

        // When
        try {
            userServiceOptional = Optional.fromNullable(classUnderTest.getUserService());
        } catch (IOException ignored) {
        }

        // Then
        assertFalse(userServiceOptional.isPresent());
        Mockito.verify(connectionManager).isNetworkAvailable();
        Mockito.verify(connectionManager, Mockito.never()).getRetrofitConnection(Optional.<Interceptor>absent());
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(ConnectionManager.class).toInstance(connectionManager);
            bind(AuthInterceptor.class).toInstance(authInterceptor);
        }
    }
}
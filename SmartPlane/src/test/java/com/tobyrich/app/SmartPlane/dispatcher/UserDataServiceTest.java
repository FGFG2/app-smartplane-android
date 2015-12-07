package com.tobyrich.app.SmartPlane.dispatcher;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.model.Token;
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

import retrofit.Call;
import retrofit.Response;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class UserDataServiceTest extends TestCase {

    @Inject
    private UserDataService classUnderTest;

    @Mock
    private RetrofitServiceManager retrofitServiceManager;
    @Mock
    private UserService userService;
    @Mock
    private Call call;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);

        // Override injector and perform injection
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);

        Mockito.when(retrofitServiceManager.getUserService()).thenReturn(userService);
        Mockito.when(userService.login(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(call);
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void testLogin() throws Exception {
        // Given
        String tokenString = "123";
        Token token = new Token();
        token.setAccess_token(tokenString);
        Mockito.when(call.execute()).thenReturn(Response.success(token));

        // When
        Optional<String> result = classUnderTest.login("", "");

        // Then
        assertTrue(result.isPresent());
        assertEquals(tokenString, result.get());
    }

    @Test
    public void testLoginFail() throws Exception {
        // Given
        Mockito.when(call.execute()).thenReturn(Response.error(400, null));

        // When
        Optional<String> result = classUnderTest.login("", "");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    public void testLoginNoResponse() throws Exception {
        // Given
        Mockito.when(call.execute()).thenReturn(null);

        // When
        Optional<String> result = classUnderTest.login("", "");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    public void testLoginException() throws Exception {
        // Given
        Mockito.when(call.execute()).thenThrow(new IOException());

        // When
        Optional<String> result = classUnderTest.login("", "");

        // Then
        assertFalse(result.isPresent());
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(RetrofitServiceManager.class).toInstance(retrofitServiceManager);
        }
    }
}
package com.tobyrich.app.SmartPlane.api.service;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.dispatcher.UserDataService;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import retrofit.Call;
import retrofit.Response;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class UserServiceTest extends TestCase {

    @Inject
    private RetrofitServiceManager serviceManager;
    private UserService userService;

    @Before
    public void setUp() throws Exception {
        // Override injector and perform injection
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);

        // Set up service
        userService = serviceManager.getUserService();
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }

    @Test
    public void testLogin() throws Exception {
        // Given
        String mail = "msuckau@fgfg.de";
        String pw = "test123";

        // When
        final Call<?> call = userService.login(UserDataService.GRANT_TYPE, mail, pw);
        final Response<?> response = call.execute();

        // Then
        assertTrue(response.isSuccess());
    }

    @Test
    public void testLoginFail() throws Exception {
        // Given
        String mail = "";
        String pw = "";

        // When
        final Call<?> call = userService.login(UserDataService.GRANT_TYPE, mail, pw);
        final Response<?> response = call.execute();

        // Then
        assertFalse(response.isSuccess());
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }
}
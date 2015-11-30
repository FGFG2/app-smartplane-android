package com.tobyrich.app.SmartPlane.api;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Protocol;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.api.model.Token;

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

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class AuthInterceptorTest extends TestCase {

    @Inject
    private AuthInterceptor classUnderTest;

    @Mock
    private Interceptor.Chain chain;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);

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
    public void testSetToken() throws Exception {
        // Given
        String authTokenString = "123456789";
        Token testToken = new Token();
        testToken.setAccess_token(authTokenString);

        // When
        classUnderTest.setToken(testToken);

        // Then
        assertTrue(classUnderTest.getToken().isPresent());
        assertEquals(testToken, classUnderTest.getToken().get());
    }

    @Test
    public void testInterceptNoToken() throws Exception {
        // Given
        Request request = new Request.Builder().url("http://google.de").build();
        Response response = new Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).build();
        Mockito.when(chain.request()).thenReturn(request);
        Mockito.when(chain.proceed(request)).thenReturn(response);

        // When
        Response result = classUnderTest.intercept(chain);

        // Then
        assertEquals(response, result);
    }

    @Test
    public void testInterceptWithToken() throws Exception {
        // Given
        Token token = new Token();
        token.setAccess_token("123");
        classUnderTest.setToken(token);
        Request request = new Request.Builder().url("http://google.de").build();
        Response response = new Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).build();
        Mockito.when(chain.request()).thenReturn(request);
        Mockito.when(chain.proceed(request)).thenReturn(response);

        // When
        Response result = classUnderTest.intercept(chain);

        // Then
        assertTrue(result == null);
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
        }
    }
}
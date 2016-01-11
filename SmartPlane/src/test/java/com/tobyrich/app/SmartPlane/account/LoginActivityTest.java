package com.tobyrich.app.SmartPlane.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.google.inject.AbstractModule;
import com.tobyrich.app.SmartPlane.BuildConfig;

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
public class LoginActivityTest extends TestCase {

    private LoginActivity classUnterTest;

    @Mock
    private AccountManager accountManager;

    @Mock
    private Intent intent;

    @Mock
    private AutoCompleteTextView emailView;

    @Mock
    private EditText pwView;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);
        // Set up Mockito behavior
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);

        classUnterTest = new LoginActivity(emailView, pwView);
        injector.injectMembersWithoutViews(classUnterTest);
        classUnterTest = Mockito.spy(classUnterTest);
    }

    @After
    public void tearDown() throws Exception {
        RoboGuice.Util.reset();
    }

    @Test
    public void testIsEmailValid() throws Exception {
        // Given
        String mail = "test@test.rocks";

        // When
        boolean result = classUnterTest.isEmailValid(mail);

        // Then
        assertTrue(result);
    }

    @Test
    public void testIsEmailValidNull() throws Exception {
        // When
        boolean result = classUnterTest.isEmailValid(null);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsEmailValidFalse() throws Exception {
        // Given
        String mail = "test";

        // When
        boolean result = classUnterTest.isEmailValid(mail);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsPasswordValid() throws Exception {
        // Given
        String pw = "test123";

        // When
        boolean result = classUnterTest.isPasswordValid(pw);

        // Then
        assertTrue(result);
    }

    @Test
    public void testIsPasswordValidShort() throws Exception {
        // Given
        String pw = "te";

        // When
        boolean result = classUnterTest.isPasswordValid(pw);

        // Then
        assertFalse(result);
    }

    @Test
    public void testIsPasswordValidNull() throws Exception {
        // When
        boolean result = classUnterTest.isPasswordValid(null);

        // Then
        assertFalse(result);
    }

    @Test
    public void testFinishLogin() throws Exception {
        // Given
        String mail = "testMail";
        String pw = "testPW";
        String type = "testAccount";
        String token = "testToken";

        Account account = new Account(mail, type);
        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, mail);
        data.putString(AccountManager.KEY_PASSWORD, pw);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, type);
        data.putString(AccountManager.KEY_AUTHTOKEN, token);
        data.putBoolean(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtras(data);
        Mockito.when(classUnterTest.getIntent()).thenReturn(intent);

        // When
        classUnterTest.finishLogin(intent);

        // Then
        assertTrue(classUnterTest.isFinishing());
        Mockito.verify(accountManager).addAccountExplicitly(account, pw, null);
        Mockito.verify(accountManager).setAuthToken(account, null, token);
    }

    @Test
    public void testFinishLoginExists() throws Exception {
        // Given
        String mail = "testMail";
        String pw = "testPW";
        String type = "testAccount";

        Account account = new Account(mail, type);
        Intent intent = new Intent();
        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, mail);
        data.putString(AccountManager.KEY_PASSWORD, pw);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, type);
        data.putBoolean(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, false);
        intent.putExtras(data);
        Mockito.when(classUnterTest.getIntent()).thenReturn(intent);

        // When
        classUnterTest.finishLogin(intent);

        // Then
        assertTrue(classUnterTest.isFinishing());
        Mockito.verify(accountManager).setPassword(account, pw);
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(AccountManager.class).toInstance(accountManager);
        }
    }
}
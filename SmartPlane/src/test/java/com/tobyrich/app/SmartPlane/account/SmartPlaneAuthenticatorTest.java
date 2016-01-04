package com.tobyrich.app.SmartPlane.account;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;

import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.BuildConfig;
import com.tobyrich.app.SmartPlane.dispatcher.UserDataService;

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
public class SmartPlaneAuthenticatorTest extends TestCase {

    @Mock
    private AccountAuthenticatorResponse authenticatorResponse;

    @Mock
    private UserDataService userDataService;

    @Mock
    private AccountManager accountManager;

    @Inject
    private SmartPlaneAuthenticator classUnterTest;

    @Before
    public void setUp() throws Exception {
        // Create mock for private members of test
        MockitoAnnotations.initMocks(this);
        // Set up Mockito behavior
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());
        RoboInjector injector = RoboGuice.getInjector(RuntimeEnvironment.application);
        injector.injectMembersWithoutViews(this);

        classUnterTest = Mockito.spy(classUnterTest);
    }

    @After
    public void tearDown() throws Exception {
        RoboGuice.Util.reset();
    }

    @Test
    public void testAddAccount() throws Exception {
        // Given
        String accountType = "testAccountType";
        String authTokenType = "testAuthTokenType";

        // When
        Bundle result = classUnterTest.addAccount(authenticatorResponse, accountType, authTokenType, null, null);

        // Then
        Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
        assertNotNull(intent);

        assertEquals(accountType, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        assertEquals(authTokenType, intent.getStringExtra(AccountManager.KEY_AUTHENTICATOR_TYPES));
        assertEquals(true, intent.getBooleanExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, false));
        assertEquals(authenticatorResponse, intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE));
    }

    @Test
    public void testGetAuthTokenWrongType() throws Exception {
        // Given
        Account account = new Account("TestAccount", AccountConstants.ACCOUNT_TYPE);

        // When
        Bundle result = classUnterTest.getAuthToken(authenticatorResponse, account, "someInvalidType", null);

        // Then
        assertNotNull(result.getString(AccountManager.KEY_ERROR_MESSAGE));
        Mockito.verifyZeroInteractions(accountManager);
    }

    @Test
    public void testGetAuthTokenPeek() throws Exception {
        // Given
        Account account = new Account("TestAccount", AccountConstants.ACCOUNT_TYPE);
        String token = "testToken";
        Mockito.when(accountManager.peekAuthToken(account, AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS)).thenReturn(token);

        // When
        Bundle result = classUnterTest.getAuthToken(authenticatorResponse, account, AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS, null);

        // Then
        Mockito.verify(classUnterTest, Mockito.never()).reauthenticateAccount(Mockito.anyString(), Mockito.anyString());
        assertEquals(account.name, result.getString(AccountManager.KEY_ACCOUNT_NAME));
        assertEquals(account.type, result.getString(AccountManager.KEY_ACCOUNT_TYPE));
        assertEquals(token, result.getString(AccountManager.KEY_AUTHTOKEN));
    }

    @Test
    public void testGetAuthTokenReauth() throws Exception {
        // Given
        Account account = new Account("TestAccount", AccountConstants.ACCOUNT_TYPE);
        String token = "testToken";
        Mockito.when(accountManager.peekAuthToken(account, AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS)).thenReturn(null);
        Mockito.when(accountManager.getPassword(account)).thenReturn("test123");
        Mockito.doReturn(token).when(classUnterTest).reauthenticateAccount(Mockito.anyString(), Mockito.anyString());

        // When
        Bundle result = classUnterTest.getAuthToken(authenticatorResponse, account, AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS, null);

        // Then
        Mockito.verify(classUnterTest).reauthenticateAccount(Mockito.anyString(), Mockito.anyString());
        assertEquals(account.name, result.getString(AccountManager.KEY_ACCOUNT_NAME));
        assertEquals(account.type, result.getString(AccountManager.KEY_ACCOUNT_TYPE));
        assertEquals(token, result.getString(AccountManager.KEY_AUTHTOKEN));
    }

    @Test
    public void testGetAuthTokenReprompt() throws Exception {
        // Given
        Account account = new Account("TestAccount", AccountConstants.ACCOUNT_TYPE);
        String token = "testToken";
        Mockito.when(accountManager.peekAuthToken(account, AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS)).thenReturn(null);
        Mockito.when(accountManager.getPassword(account)).thenReturn("test123");
        Mockito.doReturn(null).when(classUnterTest).reauthenticateAccount(Mockito.anyString(), Mockito.anyString());

        // When
        Bundle result = classUnterTest.getAuthToken(authenticatorResponse, account, AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS, null);

        // Then
        Mockito.verify(classUnterTest).reauthenticateAccount(Mockito.anyString(), Mockito.anyString());
        Intent intent = result.getParcelable(AccountManager.KEY_INTENT);
        assertNotNull(intent);
        assertEquals(account.type, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        assertEquals(AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS, intent.getStringExtra(AccountManager.KEY_AUTHENTICATOR_TYPES));
        assertEquals(false, intent.getBooleanExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true));
        assertEquals(authenticatorResponse, intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE));
    }

    @Test
    public void testReauthenticateAccount() throws Exception {
        // Given
        String userName = "test";
        String pw = "test123";
        Optional<String> token = Optional.of("testtoken");
        Mockito.when(userDataService.login(userName, pw)).thenReturn(token);

        // When
        String tokenResultString = classUnterTest.reauthenticateAccount(userName, pw);

        // Then
        assertEquals(token.get(), tokenResultString);
    }

    @Test
    public void testReauthenticateAccountNoPw() throws Exception {
        // Given
        String userName = "test";
        String pw = null;

        // When
        String tokenResultString = classUnterTest.reauthenticateAccount(userName, pw);

        // Then
        assertEquals("", tokenResultString);
        Mockito.verifyNoMoreInteractions(userDataService);
    }

    @Test
    public void testReauthenticateAccountNoToken() throws Exception {
        // Given
        String userName = "test";
        String pw = "test123";
        Optional<String> token = Optional.absent();
        Mockito.when(userDataService.login(userName, pw)).thenReturn(token);

        // When
        String tokenResultString = classUnterTest.reauthenticateAccount(userName, pw);

        // Then
        assertEquals("", tokenResultString);
    }

    private class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            // Replace injected class with mock
            bind(UserDataService.class).toInstance(userDataService);
            bind(AccountManager.class).toInstance(accountManager);
        }
    }
}
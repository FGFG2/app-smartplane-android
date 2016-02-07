package com.tobyrich.app.SmartPlane.account;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.dispatcher.UserDataService;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class SmartPlaneAuthenticator extends AbstractAccountAuthenticator {

    private final Context mContext;
    private String TAG = this.getClass().getSimpleName();

    @Inject
    private UserDataService userDataService;

    @Inject
    private AccountManager accountManager;

    @Inject
    public SmartPlaneAuthenticator(Context context) {
        super(context);
        this.mContext = context;

        // Perform dependency injection
        final RoboInjector injector = RoboGuice.getInjector(context.getApplicationContext());
        injector.injectMembersWithoutViews(this);
    }

    /**
     * Creates intent for LoginActivity to allow login using user credentials
     * Called by android account manager
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "Adding new account for SmartPlane.");
        final Bundle result = new Bundle();
        final Intent promptLogin = getIntentForLogin(response, accountType, authTokenType, Boolean.TRUE);
        result.putParcelable(AccountManager.KEY_INTENT, promptLogin);
        return result;
    }

    /**
     * Gets O-Auth token for specific account:
     * 1: Tries to get cached token
     * 2: Tries to reuse cached user credentials to login user automatically
     * 3: Prompts LoginActivity if everything else failed
     * Called by android account manager
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "Getting token for SmartPlane account.");
        String authToken;
        final Bundle result = new Bundle();

        // Only supported authToken types allowed --> else return error
        if (!authTokenType.equals(AccountConstants.AUTHTOKEN_TYPE_READ_ONLY) && !authTokenType.equals(AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS)) {
            Log.w(TAG, "Token type not supported: " + authTokenType);
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
        } else {
            // Get cached auth token from account manager
            authToken = accountManager.peekAuthToken(account, authTokenType);
            Log.d(TAG, "Token peek returned: " + authToken);

            // If not token --> lets give another try to authenticate the user with cached user credentials
            if (TextUtils.isEmpty(authToken)) {
                authToken = reauthenticateAccount(account.name, accountManager.getPassword(account));
            }

            // On success return token
            if (!TextUtils.isEmpty(authToken)) {
                Log.d(TAG, "Got token from user service: " + authToken);
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            } else {
                // Re-prompt to LoginActivity if we still got no token --> re-enter credentials
                Log.d(TAG, "Still no token --> re-prompting to enter authentication information.");
                final Intent promptLogin = getIntentForLogin(response, account.type, authTokenType, Boolean.FALSE);
                result.putParcelable(AccountManager.KEY_INTENT, promptLogin);
            }
        }
       return result;
    }


    /**
     * Provides account label for android account manager (shown in account settings)
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else if (AccountConstants.AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AccountConstants.AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";
    }

    /**
     * Tells android account manager that account has not features (e.g. settings / properties)
     */
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    /**
     * Creates login intent
     */
    private Intent getIntentForLogin(AccountAuthenticatorResponse response, String accountType, String authTokenType, Boolean isNewAccount) {
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_AUTHENTICATOR_TYPES, authTokenType);
        intent.putExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, isNewAccount);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        return intent;
    }

    /**
     * Tries to reauthenticate user against server
     */
    /* package */String reauthenticateAccount(String accountName, String accountPassword) {
        String authToken = "";
        if (accountPassword != null) {
            try {
                Log.d(TAG, "Got no token from peak -->  re-authenticating with the existing password");
                Optional<String> tokenOptional = userDataService.login(accountName, accountPassword);
                if (tokenOptional.isPresent()) {
                    authToken = tokenOptional.get();
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not re-authenticate --> Re-prompt for user credentials", e);
            }
        }
        return authToken;
    }
}
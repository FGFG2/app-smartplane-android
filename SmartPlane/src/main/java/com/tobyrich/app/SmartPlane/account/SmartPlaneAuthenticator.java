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

    public SmartPlaneAuthenticator(Context context) {
        super(context);
        this.mContext = context;

        // Perform dependency injection
        final RoboInjector injector = RoboGuice.getInjector(context.getApplicationContext());
        injector.injectMembersWithoutViews(this);
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "Adding new account for SmartPlane.");

        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);
        intent.putExtra(AccountManager.KEY_AUTHENTICATOR_TYPES, authTokenType);
        intent.putExtra(LoginActivity.ARG_IS_ADDING_NEW_ACCOUNT, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        Log.d(TAG, "Getting token for SmartPlane account.");

        // Only supported authToken types allowed --> else return error
        if (!authTokenType.equals(AccountConstants.AUTHTOKEN_TYPE_READ_ONLY) && !authTokenType.equals(AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }

        final AccountManager am = AccountManager.get(mContext);

        // Get cached auth token from account manager
        String authToken = am.peekAuthToken(account, authTokenType);

        Log.d(TAG, "Token peek returned: " + authToken);

        // If not token --> lets give another try to authenticate the user with cached user credentials
        if (TextUtils.isEmpty(authToken)) {
            final String password = am.getPassword(account);
            if (password != null) {
                try {
                    Log.d(TAG, "Got no token from peak -->  re-authenticating with the existing password");
                    Optional<String> tokenOptional = userDataService.login(account.name, password);
                    if (tokenOptional.isPresent()) {
                        authToken = tokenOptional.get();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Could not re-authenticate --> Re-prompt for user credentials", e);
                }
            }
        }

        // On success return token
        if (!TextUtils.isEmpty(authToken)) {
            Log.d(TAG, "Got token from user service: " + authToken);
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // Re-prompt to LoginActivity if we still got no token --> re-enter credentials
        Log.d(TAG, "Still no token --> re-prompting to enter authentication information.");
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        intent.putExtra(AccountManager.KEY_AUTHENTICATOR_TYPES, authTokenType);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }


    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS.equals(authTokenType))
            return AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL;
        else if (AccountConstants.AUTHTOKEN_TYPE_READ_ONLY.equals(authTokenType))
            return AccountConstants.AUTHTOKEN_TYPE_READ_ONLY_LABEL;
        else
            return authTokenType + " (Label)";
    }

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

}

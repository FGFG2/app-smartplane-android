package com.tobyrich.app.SmartPlane.account;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.R;
import com.tobyrich.app.SmartPlane.dispatcher.UserDataService;

import java.io.IOException;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;


/**
 * Class provides login screen that offers login via email/password to server
 */
public class LoginActivity extends AccountAuthenticatorActivity {

    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
    private final String TAG = this.getClass().getSimpleName();

    /**
     * Keep track of the login task to ensure we can cancel it if requested
     */
    private UserLoginTask mAuthTask = null;

    private String authTokenType;
    private String accountType;

    @Inject
    private UserDataService userDataService;
    @Inject
    private AccountManager accountManager;


    // UI references
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mEmailSignInButton;
    private Button mCancelButton;

    @Inject
    public LoginActivity() {
    }

    /**
     * Constructor for test
     *
     * @param emailView    text field for email
     * @param passwordView text field for password
     */
    /* package */LoginActivity(AutoCompleteTextView emailView, EditText passwordView) {
        mEmailView = emailView;
        mPasswordView = passwordView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Perform dependency injection
        final RoboInjector injector = RoboGuice.getInjector(this.getApplicationContext());
        injector.injectMembersWithoutViews(this);

        // Get account and token type from intent
        accountType = getIntent().getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        authTokenType = getIntent().getStringExtra(AccountManager.KEY_AUTHENTICATOR_TYPES);
        if (authTokenType == null) {
            authTokenType = AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS;
        }
        initViews();
        initListeners();
    }

    /**
     * Initialize all views
     * --> IOC-Container can not perform view injection due to inheritance problem
     * (class needs to inherit AccountAuthenticatorActivity)
     */
    private void initViews() {
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mProgressView = findViewById(R.id.login_progress);
        mLoginFormView = findViewById(R.id.login_form);
        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
    }

    /**
     * Add listeners to buttons
     */
    private void initListeners() {
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        // create alert to inform user about cancel action
        mCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(LoginActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.quit)
                        .setMessage(R.string.really_quit)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LoginActivity.this.setResult(RESULT_CANCELED);
                                LoginActivity.this.finish();
                            }

                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        });
    }


    /**
     * Attempts to sign in the account specified by the login form
     *
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            // other login still in progress --> return
            return;
        }

        // Reset errors
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!isEmailValid(email)) {
            cancel = true;
            focusView = mEmailView;
        }

        if (!isPasswordValid(password)) {
            cancel = true;
            focusView = mPasswordView;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((String) null);
        }
    }

    /**
     * Check for a valid email address
     *
     * @param email Account email address
     * @return Valid?
     */
    /* package */boolean isEmailValid(String email) {
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            return false;
        } else if (!email.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check for a valid password
     *
     * @param password Account password
     * @return Valid?
     */
    /* package */boolean isPasswordValid(String password) {
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            return false;
        } else if (password.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            return false;
        } else {
            return true;
        }
    }

    /**
     * Shows the progress UI and hides the login form
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Register or update account after successful login and inform AccountManager
     *
     * @param intent Account information
     */
    /* package */void finishLogin(Intent intent) {
        Log.d(TAG, "Finishing login.");

        // Get account information from intent (from login task)
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
        String accountPassword = intent.getStringExtra(AccountManager.KEY_PASSWORD);
        final Account account = new Account(accountName, accountType);

        // Add new account if intended, otherwise change cached password
        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d(TAG, "Account will be added explicitly.");
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = authTokenType;

            // Creating the account on the device and setting the auth token
            accountManager.addAccountExplicitly(account, accountPassword, null);
            accountManager.setAuthToken(account, authtokenType, authtoken);
        } else {
            Log.d(TAG, "Account already present. Setting new password.");
            accountManager.setPassword(account, accountPassword);
        }

        // Set result in activity and inform AccountManager
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        Log.d(TAG, "Login successful.");
        finish();
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, Intent> {

        private final String mEmail;
        private final String mPassword;

        public UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        /**
         * Perform login via UserDataService to get valid auth token for passed credentials
         *
         * @param params Ignored
         * @return Intent with account information
         */
        @Override
        protected Intent doInBackground(String... params) {
            Log.d(TAG, "Starting authentication");

            Bundle data = new Bundle();
            try {
                Optional<String> tokenOptional = userDataService.login(mEmail, mPassword);
                if (tokenOptional.isPresent()) {
                    // Set account information
                    data.putString(AccountManager.KEY_ACCOUNT_NAME, mEmail);
                    data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                    data.putString(AccountManager.KEY_AUTHTOKEN, tokenOptional.get());
                    data.putString(AccountManager.KEY_PASSWORD, mPassword);
                } else {
                    throw new IOException("No token present.");
                }
            } catch (Exception e) {
                data.putString(AccountManager.KEY_ERROR_MESSAGE, e.getMessage());
            }

            final Intent res = new Intent();
            res.putExtras(data);
            return res;
        }

        /**
         * Read intent to evaluate login information
         *
         * @param intent Intent from login progress
         */
        @Override
        protected void onPostExecute(Intent intent) {
            mAuthTask = null;
            showProgress(false);

            if (intent.hasExtra(AccountManager.KEY_ERROR_MESSAGE)) {
                // Display error and request focus on error field
                Toast.makeText(getBaseContext(), intent.getStringExtra(AccountManager.KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            } else {
                // Login successful
                finishLogin(intent);
            }
        }

        /**
         * Cancel login and clean up
         */
        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
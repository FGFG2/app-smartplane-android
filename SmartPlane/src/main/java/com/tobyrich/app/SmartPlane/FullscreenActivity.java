/*

Copyright (c) 2014, TobyRich GmbH
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package com.tobyrich.app.SmartPlane;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.account.AccountConstants;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.model.Achievement;
import com.tobyrich.app.SmartPlane.dispatcher.AchievementController;
import com.tobyrich.app.SmartPlane.dispatcher.event.AchievementUnlockedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.ActivityStoppedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.connection.DataNotSendEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.connection.DataSendEvent;
import com.tobyrich.app.SmartPlane.util.Const;
import com.tobyrich.app.SmartPlane.util.MeteoTask;
import com.tobyrich.app.SmartPlane.util.Util;
import com.viewpagerindicator.CirclePageIndicator;

import de.greenrobot.event.EventBus;
import lib.smartlink.BluetoothDisabledException;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;

/**
 * Entry point for the Smartplane app
 * @author Samit Vaidya
 * @date 04 March 2014
 * @edit Radu Hambasan
 * @date 19 Jun 2014
 * @edit Radu Hambasan
 * @date 17 Jul 2014
 */

@ContentView(R.layout.activity_fullscreen)
public class FullscreenActivity extends RoboActivity {
    private static final String TAG = "FullscreenActivity";
    @SuppressWarnings("FieldCanBeLocal")
    private static final int NUM_SCREENS = 3;

    private boolean[] initializedScreen = {false, false, false};

    // sound played when the user presses the "Control Tower" button
    private MediaPlayer atcSound;
    private BluetoothDelegate bluetoothDelegate;  // bluetooth events
    private SensorHandler sensorHandler;  // accelerometer & magnetometer
    private GestureDetector gestureDetector;  // touch events
    private PlaneState planeState;  // singleton with variables used app-wide

    private AudioManager audioManager;
    private SharedPreferences buttonConfig;  // cached button configuration

    @Inject
    private AchievementController achievementController;
    @Inject
    private AccountManager accountManager;
    @Inject
    private RetrofitServiceManager serviceManager;

    @Override
    public void onResume() {
        super.onResume();

        // The resolution might have changed while the app was paused
        ViewTreeObserver viewTree = findViewById(R.id.controlPanel).getViewTreeObserver();
        viewTree.addOnGlobalLayoutListener(new GlobalLayoutListener(this));

        if (sensorHandler != null) {
            sensorHandler.registerListener();
        }

        if (bluetoothDelegate != null) {
            try {
                bluetoothDelegate.connect();
            } catch (BluetoothDisabledException ex) {
                Intent enableBtIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Util.BT_REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorHandler != null) {
            sensorHandler.unregisterListener();
        }
    }

    /**
     * There are 3 interesting result codes that we should handle
     * BT_REQUEST_CODE is sent when a BluetoothDelegate discovers that Bluetooth is disabled
     * PHOTO_REQUEST_CODE is sent when we requested the camera to take a picture for social sharing
     * SHARE_REQUEST_CODE is sent when we return from a social sharing activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Util.BT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    try {
                        bluetoothDelegate.connect();
                    } catch (BluetoothDisabledException ex) {
                        Log.wtf(TAG, "user enabled BT, but we still couldn't connect");
                    }
                } else {
                    Log.e(TAG, "Bluetooth enabling was canceled by user");
                }
                return;
            case Util.PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_CANCELED)
                    return;

                Uri photoUri = Util.photoUri;
                if (photoUri == null) {
                    Util.inform(FullscreenActivity.this,
                            getString(R.string.social_share_picture_problem));
                    return;
                }
                Util.socialShare(this, photoUri);
                return;
            case Util.SHARE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Util.inform(this, getString(R.string.social_share_success));
                } else {
                    Log.e(TAG, "Sharing not successful");
                }
                // noinspection UnnecessaryReturnStatement
                return;
        }  // end switch
    }

    /**
     * Initialize main screen dependencies, such as:
     * the event listeners, the BluetoothDelegate & SensorHandler
     */
    private void initializeMainScreen() {
        sensorHandler = new SensorHandler(this, bluetoothDelegate);
        sensorHandler.registerListener();
        gestureDetector = new GestureDetector(this,
                new GestureListener(this, bluetoothDelegate));
        planeState = (PlaneState) getApplicationContext();

         /* setting the trivial listeners */
        ImageView socialShare = (ImageView) findViewById(R.id.socialShare);
        socialShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.showSocialShareDialog(FullscreenActivity.this);
            }
        });

        ImageView horizonImage = (ImageView) findViewById(R.id.imageHorizon);
        horizonImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        final ImageView atcOffButton = (ImageView) findViewById(R.id.atcOff);
        final ImageView atcOnButton = (ImageView) findViewById(R.id.atcOn);

        atcSound = MediaPlayer.create(this, R.raw.atc_sounds1);
        atcOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atcOnButton.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
                if (atcSound != null) atcSound.start();
            }
        });

        atcOnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                atcOffButton.setVisibility(View.VISIBLE);
                v.setVisibility(View.GONE);
                if (atcSound != null) atcSound.pause();
            }
        });

        View controlPanel = findViewById(R.id.controlPanel);
        controlPanel.setOnTouchListener(new PanelTouchListener(this,
                bluetoothDelegate));

        final ImageView checklist_vw = (ImageView) findViewById(R.id.checklist);
        checklist_vw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog checklist =
                        new AlertDialog.Builder(FullscreenActivity.this).create();
                checklist.requestWindowFeature(Window.FEATURE_NO_TITLE);

                View content = getLayoutInflater().inflate(R.layout.checklist_layout, null);
                checklist.setView(content);
                checklist.setCancelable(true);
                checklist.setCanceledOnTouchOutside(true);

                // dismiss the dialog on touch
                content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checklist.dismiss();
                    }
                });

                checklist.show();
            }
        });

    }

    /**
     * Configure the button listeners
     * Set the buttons to their last known configuration
     * */
    public void initializeSettingsScreen() {
        final float FX_VOLUME = 10.0f;
        /* setting the version data at the bottom of the screen */
        final String UNKNOWN = getString(R.string.unknown);
        String appVersion = UNKNOWN;
        try {
            appVersion = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not locate package; needed to set appVersion.");
            e.printStackTrace();
        }

        ((TextView) findViewById(R.id.softwareInfoData))
                .setText(getString(R.string.info_softwareLabel) + appVersion);
        ((TextView) findViewById(R.id.hardwareInfoData))
                .setText(getString(R.string.info_hardwareLabel) + UNKNOWN);
        ((TextView) findViewById(R.id.serialInfoData))
                .setText(getString(R.string.info_serialLabel) + UNKNOWN);

        /* setting the switch listeners */
        final Switch rudderReverse = (Switch) findViewById(R.id.rudderSwitch);
        rudderReverse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, FX_VOLUME);
                planeState.rudderReversed = isChecked;

                buttonConfig.edit().putBoolean("rudderReversed", isChecked).apply();
            }
        });

        boolean isRudderReversed = buttonConfig.getBoolean("rudderReversed",
                Const.DEFAULT_RUDDER_REVERSE);
        rudderReverse.setChecked(isRudderReversed);

        final Switch flAssistSwitch = (Switch) findViewById(R.id.flAssistSwitch);
        flAssistSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, FX_VOLUME);
                planeState.enableFlightAssist(isChecked);

                buttonConfig.edit().putBoolean("flAssist", isChecked).apply();
            }
        });

        boolean enableFlAssist = buttonConfig.getBoolean("flAssist",
                Const.DEFAULT_FLIGHT_ASSIST);
        flAssistSwitch.setChecked(enableFlAssist);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothDelegate = new BluetoothDelegate(this);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        // Instantiate a ViewPager and a PagerAdapter
        ViewPager screenPager = (ViewPager) findViewById(R.id.screenPager);
        screenPager.setAdapter(new ScreenSlideAdapter());

        CirclePageIndicator screenIndicator =
                (CirclePageIndicator) findViewById(R.id.screenIndicator);
        screenIndicator.setViewPager(screenPager);

        screenPager.setCurrentItem(1);  // horizon screen
        screenPager.setOffscreenPageLimit(2);

        buttonConfig = this.getSharedPreferences("button_config", MODE_PRIVATE);

        // Authenticate user --> get token
        getTokenForAccountCreateIfNeeded(AccountConstants.ACCOUNT_TYPE, AccountConstants.AUTHTOKEN_TYPE_FULL_ACCESS);
    }

    // ############################################################################################
    // ################### New code related to achievement add-on for app #########################
    // ############################################################################################

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        bluetoothDelegate.disconnect();
        EventBus.getDefault().post(new ActivityStoppedEvent());
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() { //change functionality of back button
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.exitConfirmationMsg))
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        //This resets all cached data from the app and breaks the connection.
                        //The app itself is only minimized, but not closed.
                        FullscreenActivity.this.finish();
                    }
                }).create().show();
    }

    /**
     * Inform user that service could not send data
     *
     * @param event DataNotSendEvent
     */
    public void onEventMainThread(DataNotSendEvent event) {
        Toast.makeText(this, event.getReason(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Inform user that server successfully send data
     *
     * @param event DataSendEvent
     */
    public void onEventMainThread(DataSendEvent event) {
        Toast.makeText(this, event.getType().name() + " data successfully send to server.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Inform user about new unlocked achievement
     *
     * @param event AchievementUnlockedEvent
     */
    public void onEventMainThread(AchievementUnlockedEvent event){
        // Vibrate for haptic feedback
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);

        // Build string to inform user about which achievement was unlocked
        String achievementString = "";
        for (Achievement achievement : event.getNewAchievements()) {
            achievementString += achievement.getName() + ", ";
        }
        achievementString = achievementString.substring(0, achievementString.length() - 2);

        // Build notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("New achievement unlocked !")
                        .setContentText("You have unlocked: " + achievementString);

        // Get intent from package manager to start hangar app
        Intent notificationIntent = getPackageManager().getLaunchIntentForPackage("com.tobyrich.dev.com.tobyrich.dev.hangarapp.lib");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // Build new stack to provide proper navigation
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(notificationIntent.getComponent());
        stackBuilder.addNextIntent(notificationIntent);

        // Extract pending intent for activity change
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        // Show notification
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    /**
     * Get an auth token for the account.
     * If not exist - add it and then return its auth token.
     * If one exist - return its auth token.
     * If more than one exists - show a picker and return the select account's auth token.
     * If canceled - proceed with saving data to local db
     */
    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
        accountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Bundle bnd = future.getResult();
                            // Register user token
                            final String authtoken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                            serviceManager.registerSession(authtoken);
                            Log.d(TAG, "Got token --> Successfully authenticated and registered");

                            // Start monitoring
                            achievementController.startAchievementMonitoring();

                        } catch (OperationCanceledException e) {
                            Log.i(TAG, "User canceled login --> no token present for data sending");
                        } catch (Exception e) {
                            Log.e(TAG, "No authentication token present!", e);
                        }
                    }
                }
                , null);
    }

    /**
     * All three screens need to be alive at all times, so we don't try to update
     * inexistent views. This PageAdapter, makes sure to initialize each page as soon
     * as it is inflated.
     */
    private class ScreenSlideAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return NUM_SCREENS;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            LayoutInflater inflater = (LayoutInflater) collection.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int layout_id = 1;
            switch (position) {
                case 0:
                    layout_id = R.layout.plane_settings;
                    break;
                case 1:
                    layout_id = R.layout.horizon_screen;
                    break;
                case 2:
                    layout_id = R.layout.weather_center;
                    break;
            }
            @SuppressWarnings("ResourceType")
            View screen = inflater.inflate(layout_id, null);
            collection.addView(screen, 0);

            switch (position) {
                case 0:
                    if (!initializedScreen[2]) {
                        initializedScreen[2] = true;
                        initializeSettingsScreen();
                        Log.d(TAG, "initializing settings screen");
                    }
                    break;
                case 1:
                    if (!initializedScreen[1]) {
                        initializedScreen[1] = true;
                        initializeMainScreen();
                        Log.d(TAG, "initializing main screen");
                    }
                    break;
                case 2:
                    if (!initializedScreen[0]) {
                        initializedScreen[0] = true;
                        new MeteoTask(FullscreenActivity.this).execute();
                    }
                    break;
            }
            return screen;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object o) {
            View screen = (View) o;
            collection.removeView(screen);
            initializedScreen[position] = false;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}

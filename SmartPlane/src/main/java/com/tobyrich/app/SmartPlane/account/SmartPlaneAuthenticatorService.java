package com.tobyrich.app.SmartPlane.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Class provides binder for automatic management by android account manager
 */
public class SmartPlaneAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new SmartPlaneAuthenticator(this).getIBinder();
    }
}

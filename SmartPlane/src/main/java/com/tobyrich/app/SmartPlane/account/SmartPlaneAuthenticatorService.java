package com.tobyrich.app.SmartPlane.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SmartPlaneAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return new SmartPlaneAuthenticator(this).getIBinder();
    }
}

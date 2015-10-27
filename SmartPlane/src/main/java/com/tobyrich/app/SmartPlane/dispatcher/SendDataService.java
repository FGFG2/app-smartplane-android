package com.tobyrich.app.SmartPlane.dispatcher;

import android.util.Log;

import java.util.Map;

/**
 * Service to send statistic data to server
 */
public class SendDataService {

    public void sendMotorData(Map map) {
        Log.wtf(this.getClass().getSimpleName(), "Motor Update Event !");
        // TODO: Send motor data to rest service
    }

    public void sendRudderData(Map map) {
        Log.wtf(this.getClass().getSimpleName(), "Rudder Update Event !");
        // TODO: Send rudder data to rest service
    }
}

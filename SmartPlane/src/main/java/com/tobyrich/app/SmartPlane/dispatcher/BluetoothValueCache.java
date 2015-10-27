package com.tobyrich.app.SmartPlane.dispatcher;

/**
 * Simple class to cache previous motor and rudder values
 */
public class BluetoothValueCache {

    private short previousMotorValue;
    private short previousRudderValue;

    public boolean isMotorValueChange(short motorValue) {
        if (previousMotorValue != motorValue) {
            previousMotorValue = motorValue;
            return true;
        } else {
            return false;
        }
    }

    public boolean isRudderValueChange(short rudderValue) {
        if (previousRudderValue != rudderValue) {
            previousRudderValue = rudderValue;
            return true;
        } else {
            return false;
        }
    }
}

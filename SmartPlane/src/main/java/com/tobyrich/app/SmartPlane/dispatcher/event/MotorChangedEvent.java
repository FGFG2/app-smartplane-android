package com.tobyrich.app.SmartPlane.dispatcher.event;

public class MotorChangedEvent {

    private short value;

    public MotorChangedEvent(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}

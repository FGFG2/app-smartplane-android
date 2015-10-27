package com.tobyrich.app.SmartPlane.dispatcher.event;

public class RudderChangedEvent {

    private short value;

    public RudderChangedEvent(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }
}

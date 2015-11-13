package com.tobyrich.app.SmartPlane.dispatcher.event.connection;

import com.tobyrich.app.SmartPlane.dispatcher.ValueType;

public class DataSendEvent {

    private ValueType type;

    public DataSendEvent(ValueType type) {
        this.type = type;
    }

    public ValueType getType() {
        return type;
    }
}

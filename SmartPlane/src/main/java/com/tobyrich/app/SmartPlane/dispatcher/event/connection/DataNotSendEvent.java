package com.tobyrich.app.SmartPlane.dispatcher.event.connection;

import com.tobyrich.app.SmartPlane.dispatcher.ValueType;

public class DataNotSendEvent {
    private String reason;
    private ValueType type;

    public DataNotSendEvent(String reason, ValueType type) {
        this.reason = reason;
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public ValueType getType() {
        return type;
    }
}

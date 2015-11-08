package com.tobyrich.app.SmartPlane.dispatcher.event.valuechanged;

import com.google.common.base.Optional;
import com.tobyrich.app.SmartPlane.dispatcher.ValueType;

public class ConnectionStatusChangedEvent implements ValueChangedEvent<Boolean> {
    private Optional<Boolean> isConnected;

    public ConnectionStatusChangedEvent(Optional<Boolean> isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    public Optional<Boolean> getValue() {
        return isConnected;
    }

    @Override
    public ValueType getType() {
        return ValueType.CONNECTION_STATE;
    }
}

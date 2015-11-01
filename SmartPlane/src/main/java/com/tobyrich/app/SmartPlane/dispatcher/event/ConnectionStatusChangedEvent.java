package com.tobyrich.app.SmartPlane.dispatcher.event;

import com.google.common.base.Optional;

public class ConnectionStatusChangedEvent {
    private Optional<Boolean> isConnected;

    public ConnectionStatusChangedEvent(Optional<Boolean> isConnected) {
        this.isConnected = isConnected;
    }

    public Optional<Boolean> isConnected() {
        return isConnected;
    }
}

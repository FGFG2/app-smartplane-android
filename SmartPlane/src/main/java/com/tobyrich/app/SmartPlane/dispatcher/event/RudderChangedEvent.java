package com.tobyrich.app.SmartPlane.dispatcher.event;

import com.google.common.base.Optional;

public class RudderChangedEvent {

    private Optional<Short> value;

    public RudderChangedEvent(Optional<Short> value) {
        this.value = value;
    }

    public Optional<Short> getValue() {
        return value;
    }
}

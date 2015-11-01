package com.tobyrich.app.SmartPlane.dispatcher.event;

import com.google.common.base.Optional;

public class MotorChangedEvent {

    private Optional<Short> value;

    public MotorChangedEvent(Optional<Short> value) {
        this.value = value;
    }

    public Optional<Short> getValue() {
        return value;
    }
}

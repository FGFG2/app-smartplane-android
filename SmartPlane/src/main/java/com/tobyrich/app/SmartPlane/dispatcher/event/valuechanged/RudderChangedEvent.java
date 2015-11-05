package com.tobyrich.app.SmartPlane.dispatcher.event.valuechanged;

import com.google.common.base.Optional;
import com.tobyrich.app.SmartPlane.dispatcher.ValueType;

public class RudderChangedEvent implements ValueChangedEvent {

    private Optional<Short> value;

    public RudderChangedEvent(Optional<Short> value) {
        this.value = value;
    }

    @Override
    public Optional<Short> getValue() {
        return value;
    }

    @Override
    public ValueType getType() {
        return ValueType.RUDDER;
    }
}

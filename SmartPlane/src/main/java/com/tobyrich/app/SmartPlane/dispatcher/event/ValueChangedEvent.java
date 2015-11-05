package com.tobyrich.app.SmartPlane.dispatcher.event;

import com.google.common.base.Optional;
import com.tobyrich.app.SmartPlane.dispatcher.ValueType;

public interface ValueChangedEvent {
    Optional<?> getValue();

    ValueType getType();
}

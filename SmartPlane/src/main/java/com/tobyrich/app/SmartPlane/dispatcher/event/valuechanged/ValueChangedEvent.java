package com.tobyrich.app.SmartPlane.dispatcher.event.valuechanged;

import com.google.common.base.Optional;
import com.tobyrich.app.SmartPlane.dispatcher.ValueType;

public interface ValueChangedEvent<T> {
    Optional<T> getValue();

    ValueType getType();
}

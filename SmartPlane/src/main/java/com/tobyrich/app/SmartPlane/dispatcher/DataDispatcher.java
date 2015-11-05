package com.tobyrich.app.SmartPlane.dispatcher;

import android.os.AsyncTask;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.dispatcher.event.ValueChangedEvent;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class DataDispatcher {

    public static final int MOTOR_BUFFER_SIZE = 100;
    public static final int RUDDER_BUFFER_SIZE = 100;
    public static final int IS_CONNECTED_BUFFER_SIZE = 10;
    public static final int PRECISION = 1; // Precision of time in maps --> 1 = ms, 1000 = s, 60000 = min ...

    private Map<Long, Object> motorMap;
    private Map<Long, Object> rudderMap;
    private Map<Long, Object> isConnectedMap;

    @Inject
    private SendDataService sendDataService;

    /**
     * Starts listening on events and initializes value maps
     */
    public void startAchievementMonitoring() {
        EventBus.getDefault().register(this);
        motorMap = new LinkedHashMap<>();
        rudderMap = new LinkedHashMap<>();
        isConnectedMap = new LinkedHashMap<>();
    }

    /**
     * Sends remaining data and stops listening on events
     */
    public void stopAchievementMonitoring() {
        // Do in background thread --> main thread not allowed to perform network operations
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                sendDataIfBufferOverflow(true);
                return null;
            }
        }.execute();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Receives MotorChangedEvent to add value to map and send data if buffer is full
     * Called in background thread to avoid blocking in main thread
     *
     * @param event MotorChangedEvent
     */
    public void onEventBackgroundThread(ValueChangedEvent event) {
        if (event.getValue().isPresent()) {
            switch (event.getType()) {
                case MOTOR:
                    motorMap.put(getCurrentTime(), event.getValue().get());
                    break;
                case RUDDER:
                    rudderMap.put(getCurrentTime(), event.getValue().get());
                    break;
                case CONNECTION_STATE:
                    isConnectedMap.put(getCurrentTime(), event.getValue().get());
                    break;
            }
        }
        sendDataIfBufferOverflow(false);
    }

    private void sendDataIfBufferOverflow(boolean ignoreBuffer) {
        if (motorMap.size() >= MOTOR_BUFFER_SIZE || (ignoreBuffer && !motorMap.isEmpty())) {
            motorMap = sendDataService.sendData(motorMap, ValueType.MOTOR);
        }
        if (rudderMap.size() >= RUDDER_BUFFER_SIZE || (ignoreBuffer && !rudderMap.isEmpty())) {
            rudderMap = sendDataService.sendData(rudderMap, ValueType.RUDDER);
        }
        if (isConnectedMap.size() >= IS_CONNECTED_BUFFER_SIZE || (ignoreBuffer && !isConnectedMap.isEmpty())) {
            isConnectedMap = sendDataService.sendData(isConnectedMap, ValueType.CONNECTION_STATE);
        }
    }

    /**
     * Returns current time on android system with second precision
     *
     * @return current time
     */
    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis() / PRECISION;
    }

    /* package */Map<Long, Object> getMotorMap() {
        return motorMap;
    }

    /* package */Map<Long, Object> getRudderMap() {
        return rudderMap;
    }

    /* package */Map<Long, Object> getIsConnectedMap() {
        return isConnectedMap;
    }
}
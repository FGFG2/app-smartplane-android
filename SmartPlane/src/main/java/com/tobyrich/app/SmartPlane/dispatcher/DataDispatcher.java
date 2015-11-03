package com.tobyrich.app.SmartPlane.dispatcher;

import android.os.AsyncTask;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.dispatcher.event.ConnectionStatusChangedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.MotorChangedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.RudderChangedEvent;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class DataDispatcher {

    public static final int MOTOR_BUFFER_SIZE = 100;
    public static final int RUDDER_BUFFER_SIZE = 100;
    public static final int IS_CONNECTED_BUFFER_SIZE = 10;

    private Map<Long, Short> motorMap;
    private Map<Long, Short> rudderMap;
    private Map<Long, Boolean> isConnectedMap;

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
                if (!motorMap.isEmpty()) {
                    sendDataService.sendMotorData(motorMap);
                }
                if (!rudderMap.isEmpty()) {
                    sendDataService.sendRudderData(rudderMap);
                }
                if (!isConnectedMap.isEmpty()) {
                    sendDataService.sendIsConnectedData(isConnectedMap);
                }
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
    public void onEventBackgroundThread(MotorChangedEvent event) {
        if (event.getValue().isPresent()) {
            motorMap.put(getCurrentTime(), event.getValue().get());
        }
        if (motorMap.size() >= MOTOR_BUFFER_SIZE) {
            motorMap = sendDataService.sendMotorData(motorMap);
        }
    }

    /**
     * Receives RudderChangedEvents to add value to map and send data if buffer is full
     * Called in background thread to avoid blocking in main thread
     *
     * @param event RudderChangedEvent
     */
    public void onEventBackgroundThread(RudderChangedEvent event) {
        if (event.getValue().isPresent()) {
            rudderMap.put(getCurrentTime(), event.getValue().get());
        }
        if (rudderMap.size() >= RUDDER_BUFFER_SIZE) {
            rudderMap = sendDataService.sendRudderData(rudderMap);
        }
    }

    /**
     * Receives RudderChangedEvents to add value to map and send data if buffer is full
     * Called in background thread to avoid blocking in main thread
     *
     * @param event RudderChangedEvent
     */
    public void onEventBackgroundThread(ConnectionStatusChangedEvent event) {
        if (event.isConnected().isPresent()) {
            isConnectedMap.put(getCurrentTime(), event.isConnected().get());
        }
        if (isConnectedMap.size() >= IS_CONNECTED_BUFFER_SIZE) {
            isConnectedMap = sendDataService.sendIsConnectedData(isConnectedMap);
        }
    }

    /**
     * Returns current time on android system with second precision
     *
     * @return current time
     */
    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis() / 1000;
    }
}
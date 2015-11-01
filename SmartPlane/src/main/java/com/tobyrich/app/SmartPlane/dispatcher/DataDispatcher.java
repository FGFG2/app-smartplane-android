package com.tobyrich.app.SmartPlane.dispatcher;

import android.os.AsyncTask;
import android.util.Log;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.dispatcher.event.ConnectionStatusChangedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.MotorChangedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.RudderChangedEvent;

import java.io.IOException;
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
    public void startAchievmentMonitoring() {
        EventBus.getDefault().register(this);
        motorMap = new LinkedHashMap<Long, Short>();
        rudderMap = new LinkedHashMap<Long, Short>();
        isConnectedMap = new LinkedHashMap<Long, Boolean>();
    }

    /**
     * Sends remaining data and stops listening on events
     *
     * TODO: REFACTOR EXCEPTION HANDLING
     */
    public void stopAchievmentMonitoring() {
        // Do in background thread --> main thread not allowed to perform network operations
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                if (!motorMap.isEmpty()) {
                    try {
                        sendDataService.sendMotorData(motorMap);
                    } catch (IOException e) {
                        // TODO: Persist data in database
                        Log.wtf(this.getClass().getSimpleName(), e.getMessage(), e);
                    }
                }
                if (!rudderMap.isEmpty()) {
                    try {
                        sendDataService.sendRudderData(rudderMap);
                    } catch (IOException e) {
                        // TODO: Persist data in database
                        Log.wtf(this.getClass().getSimpleName(), e.getMessage(), e);
                    }
                }
                if (!isConnectedMap.isEmpty()) {
                    try {
                        sendDataService.sendIsConnectedData(isConnectedMap);
                    } catch (IOException e) {
                        // TODO: Persist data in database
                        Log.wtf(this.getClass().getSimpleName(), e.getMessage(), e);
                    }
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
            try {
                sendDataService.sendMotorData(motorMap);
            } catch (IOException e) {
                // TODO: Persist data in database
                Log.wtf(this.getClass().getSimpleName(), e.getMessage(), e);
            }
            motorMap.clear();
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
            try {
                sendDataService.sendRudderData(rudderMap);
            } catch (IOException e) {
                // TODO: Persist data in database
                Log.wtf(this.getClass().getSimpleName(), e.getMessage(), e);
            }
            rudderMap.clear();
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
            try {
                sendDataService.sendIsConnectedData(isConnectedMap);
            } catch (IOException e) {
                // TODO: Persist data in database
                Log.wtf(this.getClass().getSimpleName(), e.getMessage(), e);
            }
            isConnectedMap.clear();
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
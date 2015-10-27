package com.tobyrich.app.SmartPlane.dispatcher;

import android.text.format.Time;

import com.tobyrich.app.SmartPlane.dispatcher.event.MotorChangedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.RudderChangedEvent;

import java.util.LinkedHashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class DataDispatcher {

    public static final int MOTOR_BUFFER_SIZE = 100;
    public static final int RUDDER_BUFFER_SIZE = 100;

    private Map<Time, Short> motorMap;
    private Map<Time, Short> rudderMap;

    private SendDataService sendDataService;

    public DataDispatcher() {
        sendDataService = new SendDataService();
    }

    /**
     * Starts listening on events and initializes value maps
     */
    public void startAchievmentMonitoring() {
        EventBus.getDefault().register(this);
        motorMap = new LinkedHashMap<Time, Short>();
        rudderMap = new LinkedHashMap<Time, Short>();
    }

    /**
     * Sends remaining data and stops listening on events
     */
    public void stopAchievmentMonitoring() {
        if (!motorMap.isEmpty()) {
            sendDataService.sendMotorData(motorMap);
        }
        if (!rudderMap.isEmpty()) {
            sendDataService.sendRudderData(rudderMap);
        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * Receives MotorChangedEvent to add value to map and send data if buffer is full
     * Called in background thread to avoid blocking in main thread
     *
     * @param event MotorChangedEvent
     */
    public void onEventBackgroundThread(MotorChangedEvent event) {
        motorMap.put(getCurrentTime(), event.getValue());
        if (motorMap.size() > MOTOR_BUFFER_SIZE) {
            sendDataService.sendMotorData(motorMap);
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
        rudderMap.put(getCurrentTime(), event.getValue());
        if (rudderMap.size() > RUDDER_BUFFER_SIZE) {
            sendDataService.sendRudderData(rudderMap);
            rudderMap.clear();
        }
    }

    /**
     * Returns current time on android system with second precision
     *
     * @return current time
     */
    private Time getCurrentTime() {
        Time currentTime = new Time();
        currentTime.setToNow();
        return currentTime;
    }
}
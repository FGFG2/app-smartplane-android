package com.tobyrich.app.SmartPlane.dispatcher;

import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.dispatcher.event.ActivityStoppedEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.connection.DataNotSendEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.connection.DataSendEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.valuechanged.ValueChangedEvent;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class DataDispatcher {

    public static final int MOTOR_BUFFER_SIZE = 100;
    public static final int RUDDER_BUFFER_SIZE = 100;
    public static final int IS_CONNECTED_BUFFER_SIZE = 10;
    public static final int PRECISION = 100; // Precision of time in maps --> 1 = ms, 1000 = s, 60000 = min ...

    private Map<Long, Object> motorMap;
    private Map<Long, Object> rudderMap;
    private Map<Long, Object> isConnectedMap;

    private boolean couldNotSendPreviousData = false;

    @Inject
    private SendDataService sendDataService;

    @Inject
    private PersistDataService persistDataService;

    /**
     * Starts listening on events, initializes value maps and send remaining data
     */
    public void startAchievementMonitoring() {
        // wait if object is still registered --> shutting down atm
        while (EventBus.getDefault().isRegistered(this)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
        EventBus.getDefault().register(this);
        motorMap = new LinkedHashMap<>();
        rudderMap = new LinkedHashMap<>();
        isConnectedMap = new LinkedHashMap<>();
        addRemainingDataFromDatabase();
    }

    /**
     * Sends remaining data and stops listening on events
     */
    public void onEventBackgroundThread(ActivityStoppedEvent event) {
        sendDataIfBufferOverflow(true);
        couldNotSendPreviousData = false;
        EventBus.getDefault().unregister(this);
    }

    /**
     * Receives ValueChangedEvent to add value to map and send data if buffer is full
     * Called in background thread to avoid blocking in main thread
     *
     * @param event ValueChangedEvent
     */
    public void onEventBackgroundThread(ValueChangedEvent<?> event) {
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

    /**
     * Triggers data saving of given ValueType in event
     *
     * @param event DataNotSendEvent
     */
    public void onEventBackgroundThread(DataNotSendEvent event) {
        couldNotSendPreviousData = true;
        saveData(event.getType());
    }

    /**
     * Triggers data sending if some data could not be send previously
     *
     * @param event DataSendEvent
     */
    public void onEventBackgroundThread(DataSendEvent event) {
        if (couldNotSendPreviousData) {
            addRemainingDataFromDatabase();
            couldNotSendPreviousData = false;
        }
    }

    /**
     * Save data of given type in database by using provided service
     *
     * @param type ValueType
     */
    private void saveData(ValueType type) {
        switch (type) {
            case MOTOR:
                persistDataService.saveData(ValueType.MOTOR, motorMap);
                motorMap.clear();
                break;
            case RUDDER:
                persistDataService.saveData(ValueType.RUDDER, rudderMap);
                rudderMap.clear();
                break;
            case CONNECTION_STATE:
                persistDataService.saveData(ValueType.CONNECTION_STATE, isConnectedMap);
                isConnectedMap.clear();
                break;
        }
    }

    /**
     * Gets remaining (not send) data from local database and tries to send it
     */
    private void addRemainingDataFromDatabase() {
        motorMap.putAll(persistDataService.getAllData(ValueType.MOTOR));
        rudderMap.putAll(persistDataService.getAllData(ValueType.RUDDER));
        isConnectedMap.putAll(persistDataService.getAllData(ValueType.CONNECTION_STATE));
    }

    /**
     * Send data on buffer overflow by using provided service
     *
     * @param ignoreBuffer Should buffer sizes be ignored
     */
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
        return (Calendar.getInstance().getTimeInMillis() / PRECISION) * PRECISION;
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

    /* package */boolean isCouldNotSendPreviousData() {
        return couldNotSendPreviousData;
    }

    /* package */void setCouldNotSendPreviousData(boolean couldNotSendPreviousData) {
        this.couldNotSendPreviousData = couldNotSendPreviousData;
    }
}
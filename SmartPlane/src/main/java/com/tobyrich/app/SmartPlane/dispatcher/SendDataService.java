package com.tobyrich.app.SmartPlane.dispatcher;

import android.util.Log;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;
import com.tobyrich.app.SmartPlane.dispatcher.event.connection.DataNotSendEvent;
import com.tobyrich.app.SmartPlane.dispatcher.event.connection.DataSendEvent;

import java.io.IOException;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Response;

public class SendDataService {

    private static final String TYPE_PLACEHOLDER = "TYPE";
    private static final String DATA_SEND = "TYPE data successfully send to server.";
    private static final String DATA_NOT_SEND = "Service could not send TYPE data.";
    @Inject
    private RetrofitServiceManager retrofitServiceManager;

    public <S, T> Map<S, T> sendData(Map<S, T> map, ValueType type) {
        try {
            Call<String> call = dispatchData(map, type);
            final Optional<Response<String>> response = Optional.fromNullable(call.execute());
            return handleResponse(response, map, type);
        } catch (IOException e) {
            final String message = DATA_NOT_SEND.replace(TYPE_PLACEHOLDER, type.name());
            Log.w(this.getClass().getSimpleName(), message + " || " + e.getMessage(), e);
            EventBus.getDefault().post(new DataNotSendEvent(message, type));
            return map;
        }
    }

    private <S, T> Call<String> dispatchData(Map<S, T> map, ValueType type) throws IOException {
        final AchievementService achievementService = retrofitServiceManager.getAchievmentService();
        Call<String> call = null;
        switch (type) {
            case MOTOR:
                call = achievementService.setMotor(map);
                break;
            case RUDDER:
                call = achievementService.setRudder(map);
                break;
            case CONNECTION_STATE:
                call = achievementService.setIsConnected(map);
                break;
        }
        return call;
    }

    private <S, T, U> Map<S, T> handleResponse(Optional<Response<U>> response, Map<S, T> map, ValueType type) {
        if (response.isPresent() && response.get().isSuccess()) {
            map.clear();
            Log.i(this.getClass().getSimpleName(), DATA_SEND.replace(TYPE_PLACEHOLDER, type.name()));
            EventBus.getDefault().post(new DataSendEvent(type));
        } else {
            final String message = DATA_NOT_SEND.replace(TYPE_PLACEHOLDER, type.name());
            Log.w(this.getClass().getSimpleName(), message + " Received invalid status code: " + (response.isPresent() ? response.get().code() : "NOT PRESENT"));
            EventBus.getDefault().post(new DataNotSendEvent(message, type));
        }
        return map;
    }
}
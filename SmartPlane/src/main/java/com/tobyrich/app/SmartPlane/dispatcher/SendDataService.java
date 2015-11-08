package com.tobyrich.app.SmartPlane.dispatcher;

import android.util.Log;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;
import com.tobyrich.app.SmartPlane.dispatcher.event.connection.DataNotSendEvent;

import java.io.IOException;
import java.util.Map;

import de.greenrobot.event.EventBus;
import retrofit.Call;
import retrofit.Response;

public class SendDataService {

    @Inject
    private RetrofitServiceManager retrofitServiceManager;

    public <S, T> Map<S, T> sendData(Map<S, T> map, ValueType type) {
        try {
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
            final Optional<Response<String>> response = Optional.fromNullable(call.execute());
            return handleResponse(response, map);
        } catch (IOException e) {
            Log.i(this.getClass().getSimpleName(), "Could not send data!", e);
            EventBus.getDefault().post(new DataNotSendEvent(e.getMessage(), type));
            return map;
        }
    }

    private <S, T, U> Map<S, T> handleResponse(Optional<Response<U>> response, Map<S, T> map) {
        if (response.isPresent() && response.get().isSuccess()) {
            map.clear();
            Log.i(this.getClass().getSimpleName(), "Data successfully send to server.");
        } else {
            Log.w(this.getClass().getSimpleName(), "Service could not send data. Received invalid status code: " + (response.isPresent() ? response.get().code() : "NOT PRESENT"));
        }
        return map;
    }
}
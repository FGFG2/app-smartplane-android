package com.tobyrich.app.SmartPlane.dispatcher;

import android.util.Log;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;

import java.io.IOException;
import java.util.Map;

import retrofit.Call;
import retrofit.Response;

public class SendDataService {

    @Inject
    private RetrofitServiceManager retrofitServiceManager;

    public Map<Long, Object> sendData(Map<Long, Object> map, ValueType type) {
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
            Log.e(this.getClass().getSimpleName(), "Could not send data!", e);
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
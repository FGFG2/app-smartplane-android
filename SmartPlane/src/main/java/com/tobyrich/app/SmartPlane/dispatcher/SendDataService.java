package com.tobyrich.app.SmartPlane.dispatcher;

import android.util.Log;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;

import java.io.IOException;
import java.util.Map;

import retrofit.Call;
import retrofit.Response;

public class SendDataService {

    @Inject
    private RetrofitServiceManager retrofitServiceManager;

    public Map<Long, Short> sendMotorData(Map<Long, Short> map) {
        try {
            final Call<String> motorCall = retrofitServiceManager.getAchievmentService().setMotor(map);
            final Optional<Response<String>> response = Optional.fromNullable(motorCall.execute());
            return handleResponse(response, map);
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "Could not send MotorData!", e);
            return map;
        }
    }

    public Map<Long, Short> sendRudderData(Map<Long, Short> map) {
        try {
            final Call<String> rudderCall = retrofitServiceManager.getAchievmentService().setRudder(map);
            final Optional<Response<String>> response = Optional.fromNullable(rudderCall.execute());
            return handleResponse(response, map);
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "Could not send RudderData!", e);
            return map;
        }
    }

    public Map<Long, Boolean> sendIsConnectedData(Map<Long, Boolean> map) {
        try {
            final Call<String> isConnectedCall = retrofitServiceManager.getAchievmentService().setIsConnected(map);
            final Optional<Response<String>> response = Optional.fromNullable(isConnectedCall.execute());
            return handleResponse(response, map);
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "Could not send connection state!", e);
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
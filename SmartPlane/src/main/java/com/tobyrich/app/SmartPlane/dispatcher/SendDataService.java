package com.tobyrich.app.SmartPlane.dispatcher;

import android.util.Log;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.api.ConnectionManager;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;

import java.io.IOException;
import java.util.Map;

import retrofit.Call;
import retrofit.Response;

public class SendDataService {

    private ConnectionManager connectionManager;
    private AchievementService achievementService;

    @Inject
    public SendDataService(ConnectionManager connectionManager) {
        Preconditions.checkNotNull(connectionManager, "No ConnectionManager present!");
        this.connectionManager = connectionManager;
        achievementService = connectionManager.getRetrofitConnection().create(AchievementService.class);
    }

    public void sendMotorData(Map<Long, Short> map) throws IOException {
        Call<String> motorCall = achievementService.setMotor(map);
        handleResponse(motorCall.execute());
    }

    public void sendRudderData(Map<Long, Short> map) throws IOException {
        Call<String> rudderCall = achievementService.setRudder(map);
        handleResponse(rudderCall.execute());
    }

    public void sendIsConnectedData(Map<Long, Boolean> map) throws IOException {
        Call<String> isConnectedCall = achievementService.setIsConnected(map);
        handleResponse(isConnectedCall.execute());
    }

    private void handleResponse(Response response) throws IOException {
        if (response.isSuccess()) {
            Log.i(this.getClass().getSimpleName(), "Data successfully send to server.");
        } else {
            throw new IOException("Could not send RudderData!");
        }
    }
}
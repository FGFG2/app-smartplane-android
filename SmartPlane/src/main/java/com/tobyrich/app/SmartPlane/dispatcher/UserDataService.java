package com.tobyrich.app.SmartPlane.dispatcher;

import android.util.Log;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.tobyrich.app.SmartPlane.api.RetrofitServiceManager;
import com.tobyrich.app.SmartPlane.api.model.Token;
import com.tobyrich.app.SmartPlane.api.service.UserService;

import java.io.IOException;

import retrofit.Response;

public class UserDataService {

    public static final String GRANT_TYPE = "password";

    @Inject
    private RetrofitServiceManager serviceManager;

    public Boolean login(String email, String password) {
        try {
            UserService userService = serviceManager.getUserService();
            final Optional<Response<Token>> response = Optional
                    .fromNullable(userService.login(GRANT_TYPE, email, password).execute());
            if (response.isPresent() && response.get().isSuccess()) {
                Token token = response.get().body();
                serviceManager.registerSession(token);
                Log.i(this.getClass().getSimpleName(), "Login successful.");
                return true;
            }
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "Error while logging in user.", e);
        }
        return false;
    }
}
package com.tobyrich.app.SmartPlane.api;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.okhttp.Interceptor;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;
import com.tobyrich.app.SmartPlane.api.service.UserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class RetrofitServiceManager {

    private Optional<AchievementService> achievementServiceOptional = Optional.absent();
    private Optional<UserService> userServiceOptional = Optional.absent();

    private Map<Class, Object> serviceMap = new HashMap<>();

    @Inject
    private ConnectionManager connectionManager;

    @Inject
    private AuthInterceptor authInterceptor;

    public RetrofitServiceManager() {
    }

    public AchievementService getAchievmentService() throws IOException {
        if (connectionManager.isNetworkAvailable()) {
            if (!achievementServiceOptional.isPresent()) {
                final Optional<? extends Interceptor> interceptorOptional = Optional.fromNullable(authInterceptor);
                achievementServiceOptional = Optional.fromNullable(connectionManager
                        .getRetrofitConnection(interceptorOptional).create(AchievementService.class));
            }
            return achievementServiceOptional.get();
        } else {
            throw new IOException("No network connection present!");
        }
    }

    public UserService getUserService() throws IOException {
        if (connectionManager.isNetworkAvailable()) {
            if (!userServiceOptional.isPresent()) {
                userServiceOptional = Optional.fromNullable(connectionManager
                        .getRetrofitConnection(Optional.<Interceptor>absent()).create(UserService.class));
            }
            return userServiceOptional.get();
        } else {
            throw new IOException("No network connection present!");
        }
    }

    public void registerSession(String token) {
        authInterceptor.setToken(token);
    }
}

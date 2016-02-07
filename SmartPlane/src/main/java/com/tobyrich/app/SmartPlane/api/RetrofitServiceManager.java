package com.tobyrich.app.SmartPlane.api;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.squareup.okhttp.Interceptor;
import com.tobyrich.app.SmartPlane.api.service.AchievementService;
import com.tobyrich.app.SmartPlane.api.service.UserService;

import java.io.IOException;

/**
 * Manager to handle all retrofit services for communication with server
 */
@Singleton
public class RetrofitServiceManager {

    private Optional<AchievementService> achievementServiceOptional = Optional.absent();
    private Optional<UserService> userServiceOptional = Optional.absent();

    @Inject
    private ConnectionManager connectionManager;

    @Inject
    private AuthInterceptor authInterceptor;

    /**
     * Returns achievement related service for communication with server (lazy initialization)
     * Uses Retrofit connection of ConnectionManager
     * (Passes interceptor to ConnectionManager)
     */
    public AchievementService getAchievmentService() throws IOException {
        if (connectionManager.isNetworkAvailable() && isTokenPresent()) {
            if (!achievementServiceOptional.isPresent()) {
                final Optional<? extends Interceptor> interceptorOptional = Optional.fromNullable(authInterceptor);
                achievementServiceOptional = Optional.fromNullable(connectionManager
                        .getRetrofitConnection(interceptorOptional).create(AchievementService.class));
            }
            return achievementServiceOptional.get();
        } else {
            throw new IOException("No network connection or OAuth token present!");
        }
    }

    /**
     * Returns user related service for communication with server (lazy initialization)
     * Uses Retrofit connection of ConnectionManager
     * (no interceptor needed)
     */
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

    /**
     * Set O-Auth token for authentication
     */
    public void registerSession(String token) {
        authInterceptor.setToken(token);
    }

    public Boolean isTokenPresent() {
        return authInterceptor.getToken().isPresent();
    }
}

package com.tobyrich.app.SmartPlane.api;

import com.google.common.base.Optional;
import com.google.inject.Singleton;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Interceptor to add O-Auth token to request header for authentication against server
 */
@Singleton
public class AuthInterceptor implements Interceptor {
    private Optional<String> tokenOptional = Optional.absent();

    /* package */Optional<String> getToken() {
        return tokenOptional;
    }

    public void setToken(String token) {
        tokenOptional = Optional.fromNullable(token);
    }

    /**
     * Add Authorization header element to request
     */
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (tokenOptional.isPresent() && !tokenOptional.get().isEmpty()) {
            Request.Builder requestBuilder = request.newBuilder()
                    .header("Authorization", "Bearer " + tokenOptional.get())
                    .method(request.method(), request.body());

            request = requestBuilder.build();
        }
        return chain.proceed(request);
    }
}
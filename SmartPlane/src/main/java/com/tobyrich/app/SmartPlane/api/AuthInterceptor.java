package com.tobyrich.app.SmartPlane.api;

import com.google.common.base.Optional;
import com.google.inject.Singleton;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tobyrich.app.SmartPlane.api.model.Token;

import java.io.IOException;

@Singleton
public class AuthInterceptor implements Interceptor {
    private Optional<Token> tokenOptional = Optional.absent();

    /* package */Optional<Token> getToken() {
        return tokenOptional;
    }

    public void setToken(Token token) {
        tokenOptional = Optional.fromNullable(token);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (tokenOptional.isPresent() && !tokenOptional.get().getAccess_token().isEmpty()) {
            Request.Builder requestBuilder = request.newBuilder()
                    .header("Authorization", "Bearer " + tokenOptional.get().getAccess_token())
                    .method(request.method(), request.body());

            request = requestBuilder.build();
        }
        return chain.proceed(request);
    }
}

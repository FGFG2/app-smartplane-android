package com.tobyrich.app.SmartPlane.api;

import com.google.common.base.Optional;
import com.google.inject.Singleton;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

@Singleton
public class ConnectionManager {

    public static final String URL_ALL_ACHIEVEMENTS = "http://chaos-krauts.de/Achievement/";
    private Optional<Retrofit> retrofitOptional = Optional.absent();

    public ConnectionManager() {
    }

    public Retrofit getRetrofitConnection() {
        if (!retrofitOptional.isPresent()) {
            retrofitOptional = Optional.of(new Retrofit.Builder()
                    .baseUrl(URL_ALL_ACHIEVEMENTS)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build());
        }
        return retrofitOptional.get();
    }
}

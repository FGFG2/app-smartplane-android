package com.tobyrich.app.SmartPlane.api.service;

import com.tobyrich.app.SmartPlane.api.model.Achievement;

import java.util.List;
import java.util.Map;

import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;

public interface AchievementService {
    @GET("api/AllAchievements")
    Call<List<Achievement>> getAllAchievements();

    @GET("api/ObtainedAchievements")
    Call<List<Achievement>> getObtainedAchievements();

    @POST("api/SetMotor")
    <S, T> Call<String> setMotor(@Body Map<S, T> map);

    @POST("api/SetRuder")
    <S, T> Call<String> setRudder(@Body Map<S, T> map);

    @POST("api/SetIsConnected")
    <S, T> Call<String> setIsConnected(@Body Map<S, T> map);
}
